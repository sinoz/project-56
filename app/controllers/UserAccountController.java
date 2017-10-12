package controllers;

import models.Product;
import models.Review;
import models.User;
import models.ViewableUser;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.useraccount.index;
import views.html.useraccount.empty;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * A {@link Controller} for showing user accounts.
 *
 * @author Johan van der Hoeven
 */
public final class UserAccountController extends Controller {
	/**
	 * The required {@link Database} dependency to fetch database connections.
	 */
	private Database database;

	@Inject
	public UserAccountController(Database database) {
		this.database = database;
	}

	/**
	 * Returns a {@link Result} combined with a user account page.
	 */
	public Result index(String username) {
		Optional<ViewableUser> user = getUser(username);
		Optional<List<List<Product>>> inventory;
		Optional<List<Review>> reviews;

		if (!user.isPresent()) return ok(empty.render(session()));
		inventory = getUserProducts(user.get().getId());
		reviews = getUserReviews(user.get().getId());

		return ok(index.render(user.get(), inventory, reviews, session()));
	}

	/**
	 * Attempts to find a {@link ViewableUser} that matches the given username.
	 */
	private Optional<ViewableUser> getUser(String userName) {
		return database.withConnection(connection -> {
			Optional<ViewableUser> viewableUser = Optional.empty();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username=?");
			stmt.setString(1, userName);

			ResultSet results = stmt.executeQuery();

			if (results.next()) {
				String id = results.getString("id");
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
	 * Attempts to get the {@link Product}s that belong to the given userId.
	 */
	private Optional<List<List<Product>>> getUserProducts(String userId) {
		return database.withConnection(connection -> {
			Optional<List<List<Product>>> products = Optional.empty();
			List<List<Product>> list = new ArrayList<>();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts WHERE userid=? AND visible=TRUE");
			stmt.setInt(1, Integer.parseInt(userId));

			ResultSet results = stmt.executeQuery();

			boolean empty = true;
			List<Product> row = new ArrayList<>();
			int l = 0;
			while (results.next()) {
				empty = false;
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

				row.add(p);
				l++;
				if(l > 1) {
					list.add(row);
					row = new ArrayList<>();
					l = 0;
				}
			}

			if(empty) {
				return products;
			} else {
				products = Optional.of(list);
				return products;
			}
		});
	}

	/**
	 * Attempts to get the {@link Review}s that belong to the given userId.
	 */
	private Optional<List<Review>> getUserReviews(String userId){
		return database.withConnection(connection -> {
			Optional<List<Review>> reviews = Optional.empty();
			List<Review> list = new ArrayList<>();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM reviews WHERE userreceiverid=?");
			stmt.setInt(1, Integer.parseInt(userId));

			ResultSet results = stmt.executeQuery();

			boolean empty = true;
			while(results.next()){
				empty = false;
				Review r = new Review();

				r.setId(results.getString("id"));
				r.setUserReceiverId(results.getInt("userreceiverid"));
				r.setUserSenderId(results.getInt("usersenderid"));
				r.setTitle(results.getString("title"));
				r.setDescription(results.getString("description"));
				r.setRating(results.getInt("rating"));

				list.add(r);
			}

			if(empty) {
				return reviews;
			} else {
				reviews = Optional.of(list);
				return reviews;
			}
		});
	}
}
