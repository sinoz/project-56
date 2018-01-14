package services;

import models.User;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

/**
 * A service to authenticate users attempting to login.
 *
 * @author Maurice van Veen
 * @author I.A
 */
@Singleton
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

			PreparedStatement stmt = connection.prepareStatement("SELECT passwordsalt FROM users WHERE username=?");
			stmt.setString(1, username);
			ResultSet results = stmt.executeQuery();

			String salt;
			if (results.next()) {
				salt = results.getString("passwordsalt");
			} else {
				return user;
			}
			String pwd = SecurityService.encodePassword(password, salt);

			stmt = connection.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
			stmt.setString(1, username);
			stmt.setString(2, pwd);

			results = stmt.executeQuery();

			if (results.next()) {
				user = Optional.of(ModelService.createUser(results));
			}

			return user;
		});
	}

	public boolean verifyUser(String verify, String username) {
		return database.withConnection(connection -> {
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users_verification WHERE verification=? AND username=?");
			stmt.setString(1, SecurityService.secure(verify));
			stmt.setString(2, username);

			ResultSet results = stmt.executeQuery();
			return results.next();
		});
	}

	public void createUser(String verify, String username) {
		database.withConnection(connection -> {
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users_verification WHERE verification=? AND username=?");
			stmt.setString(1, SecurityService.secure(verify));
			stmt.setString(2, username);

			ResultSet results = stmt.executeQuery();

			if (results.next()) {
				stmt = connection.prepareStatement("INSERT INTO users (username, password, passwordsalt, mail, paymentmail, profilepicture, membersince) VALUES(?, ?, ?, ?, ?, ?, ?)");
				stmt.setString(1, results.getString("username"));
				stmt.setString(2, results.getString("password"));
				stmt.setString(3, results.getString("passwordsalt"));
				stmt.setString(4, results.getString("mail"));
				stmt.setString(5, results.getString("paymentmail"));
				stmt.setString(6, results.getString("profilepicture"));
				stmt.setTimestamp(7, results.getTimestamp("membersince"));
				stmt.execute();

                stmt = connection.prepareStatement("DELETE FROM users_verification WHERE verification=? AND username=?");
                stmt.setString(1, SecurityService.secure(verify));
                stmt.setString(2, username);
                stmt.execute();
			}
		});
	}
}
