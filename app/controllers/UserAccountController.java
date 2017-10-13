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
 * @author Maurice van Veen
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
}