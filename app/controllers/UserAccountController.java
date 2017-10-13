package controllers;

import concurrent.DbExecContext;
import models.*;
import play.db.Database;
import play.libs.concurrent.HttpExecution;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.UserViewService;
import views.html.useraccount.empty;
import views.html.useraccount.index;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A {@link Controller} for showing user accounts.
 *
 * @author Johan van der Hoeven
 * @author: Maurice van Veen
 */
public final class UserAccountController extends Controller {
	/**
	 * The required {@link Database} dependency to fetch database connections.
	 */
	private Database database;

	/**
	 * The execution context used to asynchronously perform database operations.
	 */
	private final DbExecContext dbEc;
	/**
	 * The execution context used to asynchronously perform operations.
	 */
	private final HttpExecutionContext httpEc;
	/**
	 * The {@link services.UserViewService} to obtain product data from.
	 */
	private UserViewService userViewService;

	/**
	 * Creates a new {@link UserAccountController}.
	 */
	@Inject
	public UserAccountController(Database database, UserViewService userViewService, DbExecContext dbEc, HttpExecutionContext httpEc) {
		this.database = database;
		this.userViewService = userViewService;
		this.dbEc = dbEc;
		this.httpEc = httpEc;
	}

	/**
	 * Returns a {@link Result} combined with a user account page.
	 */
	public CompletionStage<Result> index(String username) {
		Optional<ViewableUser> user = getViewableUser(username);
		CompletableFuture<List<List<Product>>> productsFetch;
		CompletableFuture<List<List<Review>>> reviewFetch;

		// If the user does not exist render "User Not Found" page
		if (!user.isPresent()) return CompletableFuture.completedFuture(ok(empty.render(session())));

		// Otherwise get inventory and reviews for this user
		Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);

		productsFetch = supplyAsync(() -> userViewService.getUserProducts(user.get().getId()), dbExecutor);
		reviewFetch = supplyAsync(() -> userViewService.getUserReviews(user.get().getId()), dbExecutor);

		return productsFetch.thenCombineAsync(reviewFetch, (products, reviews) -> ok(index.render(user.get(), products, reviews, session())), httpEc.current());
	}

	/**
	 * Attempts to find a {@link ViewableUser} that matches the given username.
	 */
	private Optional<ViewableUser> getViewableUser(String userName) {
		return database.withConnection(connection -> {
			Optional<ViewableUser> viewableUser = Optional.empty();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username=?");
			stmt.setString(1, userName);

			ResultSet results = stmt.executeQuery();

			if (results.next()) {
				int id = results.getInt("id");
                String username = results.getString("username");
                String profilepicture = results.getString("profilepicture");
                List<String> inventory = ((List<String>) results.getObject("inventory"));
                Date membersince = results.getDate("membersince");

				viewableUser = Optional.of(new ViewableUser(id, username, profilepicture, inventory, membersince));
			}

			return viewableUser;
		});
	}

	/**
	 * Attempts to find a {@link User} that matches the given id.
	 */
	private Optional<User> getUser(int id) {
		return database.withConnection(connection -> {
			Optional<User> user = Optional.empty();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE id=?");
			stmt.setInt(1, id);

			ResultSet results = stmt.executeQuery();

			if (results.next()) {
				User u = new User();

				u.setId(results.getString("id"));
				u.setUsername(results.getString("username"));
				u.setProfilePicture(results.getString("profilepicture"));

				user = Optional.of(u);
			}

			return user;
		});
	}

	/**
	 * Attempts to get the {@link Product}s that belong to the given userId.
	 */
	private List<List<Product>> getUserProducts(int userId) {
		return database.withConnection(connection -> {
			Optional<User> user = getUser(userId);
			List<List<Product>> list = new ArrayList<>();

			if (user.isPresent()) {
				PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts WHERE userid=? AND visible=TRUE AND disabled=FALSE");
				stmt.setInt(1, userId);

				ResultSet results = stmt.executeQuery();

				List<Product> row = new ArrayList<>();
				int l = 0;
				while (results.next()) {
					Product p = new Product();

					p.setId(results.getInt("id"));
					p.setUserId(results.getInt("userid"));
					p.setGameId(results.getInt("gameid"));
					p.setTitle(results.getString("title"));
					p.setDescription(results.getString("description"));
					p.setAddedSince(results.getDate("addedsince"));
					p.setCanBuy(results.getBoolean("canbuy"));
					p.setBuyPrice(results.getDouble("buyprice"));
					p.setCanTrade(results.getBoolean("cantrade"));

					p.setUser(user.get());

					Optional<GameCategory> gameCategory = fetchGameCategory(p.getGameId());
					gameCategory.ifPresent(p::setGameCategory);

					row.add(p);
					l++;
					if (l > 1) {
						list.add(row);
						row = new ArrayList<>();
						l = 0;
					}
				}
				if (l > 0) list.add(row);
			}

			return list;
		});
	}

	/**
	 * Attempts to find a {@link GameCategory} that matches the given game id.
	 */
	private Optional<GameCategory> fetchGameCategory(int id) {
		return database.withConnection(connection -> {
			Optional<GameCategory> gameCategory = Optional.empty();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gamecategories WHERE id=?");
			stmt.setInt(1, id);

			ResultSet results = stmt.executeQuery();

			if (results.next()) {
				GameCategory gc = new GameCategory();

				gc.setId(results.getInt("id"));
				gc.setName(results.getString("name"));
				gc.setImage(results.getString("image"));
				gc.setDescription(results.getString("description"));

				gameCategory = Optional.of(gc);
			}

			return gameCategory;
		});
	}

	/**
	 * Attempts to get the {@link Review}s that belong to the given userId.
	 */
	private List<List<Review>> getUserReviews(int userId){
		return database.withConnection(connection -> {
			List<List<Review>> list = new ArrayList<>();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM reviews WHERE userreceiverid=?");
			stmt.setInt(1, userId);

			ResultSet results = stmt.executeQuery();

			List<Review> row = new ArrayList<>();
			int l = 0;
			while(results.next()){
				Review r = new Review();

				r.setId(results.getString("id"));
				r.setUserReceiverId(results.getInt("userreceiverid"));
				r.setUserSenderId(results.getInt("usersenderid"));
				r.setTitle(results.getString("title"));
				r.setDescription(results.getString("description"));
				r.setRating(results.getInt("rating"));
				int senderid = results.getInt("usersenderid");
				r.setSender(getUser(senderid).get());

				row.add(r);
				l++;
				if(l > 1) {
					list.add(row);
					row = new ArrayList<>();
					l = 0;
				}
			}
            if(l > 0) list.add(row);

			return list;
		});
	}
}