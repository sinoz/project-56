package services;

import forms.PersonalSettingsForm;
import forms.RegistrationForm;

import javax.inject.Inject;
import java.sql.PreparedStatement;

/**
 * TODO
 *
 * @author I.A
 */
public final class AccountService {
	/**
	 * TODO
	 */
	private final play.db.Database database;

	/**
	 * Creates a new {@link AccountService}.
	 */
	@Inject
	public AccountService(play.db.Database database) {
		this.database = database;
	}

	/**
	 * Checks if a user with the given username exists in the database.
	 */
	public boolean userExists(String username) {
		return database.withConnection(connection -> {
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username=?");

			stmt.setString(1, username);

			return stmt.executeQuery().next();
		});
	}

	/**
	 * Attempts to register a new user and returns whether it has successfully registered the user
	 * using the given {@link RegistrationForm}.
	 */
	public void registered(RegistrationForm form) {
		database.withConnection(connection -> {
			PreparedStatement stmt = connection.prepareStatement("INSERT INTO users (username, password, mail) VALUES(?, ?, ?)");
			stmt.setString(1, form.name.toLowerCase());
			stmt.setString(2, form.password);
			stmt.setString(3, form.email);
			stmt.execute();
		});
	}

	/**
	 * TODO
	 */
	public void updateSettings(String loggedInAs, PersonalSettingsForm form) {
		database.withConnection(connection -> {
			PreparedStatement stmt = connection.prepareStatement("UPDATE users SET username = ? WHERE username = ?");
			stmt.setString(1, form.usernameToChangeTo);
			stmt.setString(2, loggedInAs);
			stmt.execute();
		});
	}
}
