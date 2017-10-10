package controllers;

import models.User;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.useraccount.empty;
import views.html.useraccount.index;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
		Optional<User> user = getUser(username);

		if (user.isPresent()) {
			return ok(index.render(user.get(), session()));
		} else {
			return ok(empty.render(session()));
		}
	}

	/**
	 * Attempts to find a {@link User} that matches the given username.
	 */
	private Optional<User> getUser(String username) {
		return database.withConnection(connection -> {
			Optional<User> user = Optional.empty();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username=?");
			stmt.setString(1, username);

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
}
