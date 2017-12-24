package services;

import forms.PersonalSettingsForm;
import forms.RegistrationForm;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

/**
 * @author I.A
 * @author Maurice van Veen
 */
@Singleton
public final class AccountService {
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

			stmt.setString(1, username.toLowerCase());

			return stmt.executeQuery().next();
		});
	}

	/**
	 * Attempts to register a new user and returns whether it has successfully registered the user
	 * using the given {@link RegistrationForm}.
	 */
	public void registered(RegistrationForm form) {
		database.withConnection(connection -> {
			PreparedStatement stmt = connection.prepareStatement("INSERT INTO users (username, password, passwordsalt, mail, paymentmail, profilepicture, membersince) VALUES(?, ?, ?, ?, ?, ?, ?)");
			String salt = SecurityService.createSalt();
			String pwd = SecurityService.encodePassword(form.password, salt);
			stmt.setString(1, form.name.toLowerCase());
			stmt.setString(2, pwd);
			stmt.setString(3, salt);
			stmt.setString(4, form.email);
			stmt.setString(5, form.paymentmail);
			stmt.setString(6, "images/default_profile_pic.png");
			stmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
			stmt.execute();
		});
	}

	/**
	 * Attempts to update a exciting user and returns whether it has successfully updated the settings
	 * using the given {@link PersonalSettingsForm}.
	 */
	public void updateSettings(String loggedInAs, PersonalSettingsForm form) {
		database.withConnection(connection -> {
			PreparedStatement stmt = connection.prepareStatement("UPDATE users SET username=?, mail=?, paymentmail=? WHERE username=?");
			stmt.setString(1, form.usernameToChangeTo);
			stmt.setString(2, form.emailToChangeTo);
			stmt.setString(3, form.paymentMailToChangeTo);
			stmt.setString(4, loggedInAs);
			stmt.execute();
		});
	}
}