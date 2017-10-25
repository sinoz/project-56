package services;

import models.User;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

/**
 * A service to authenticate users attempting to login.
 *
 * @author I.A
 */
public final class AuthenticationService {
	private final play.db.Database database;

	/**
	 * Creates a new {@link AuthenticationService}.
	 */
	@Inject
	public AuthenticationService(play.db.Database database) {
		this.database = database;
	}

	/**
	 * Attempts to find a {@link User} that matches the given username and password combination.
	 */
	public Optional<User> fetchUser(String username, String password) {
		return database.withConnection(connection -> {
			Optional<User> user = Optional.empty();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
			stmt.setString(1, username);
			stmt.setString(2, password);

			ResultSet results = stmt.executeQuery();

			if (results.next()) {
				User u = new User();

				u.setId(results.getString("id"));
				u.setUsername(results.getString("username"));
				u.setPassword(results.getString("password"));
				u.setMail(results.getString("mail"));
				u.setPaymentMail(results.getString("paymentmail"));
				u.setProfilePicture(results.getString("profilepicture"));
				u.setMemberSince(results.getDate("membersince"));

				user = Optional.of(u);
			}

			return user;
		});
	}
}
