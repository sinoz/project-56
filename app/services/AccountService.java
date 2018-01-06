package services;

import forms.PersonalSettingsForm;
import forms.PersonalSettingsPasswordForm;
import forms.RegistrationForm;
import forms.VerifyChangePasswordForm;
import models.User;

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
			boolean a = stmt.executeQuery().next();

            stmt = connection.prepareStatement("SELECT * FROM users_verification WHERE username=?");
            stmt.setString(1, username.toLowerCase());
            boolean b = stmt.executeQuery().next();

			return a || b;
		});
	}

    /**
     * Checks if a user with the given mail exists in the database.
     */
    public boolean mailExists(String mail) {
        return database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE mail=?");
            stmt.setString(1, mail);
            boolean a = stmt.executeQuery().next();

            stmt = connection.prepareStatement("SELECT * FROM users_verification WHERE mail=?");
            stmt.setString(1, mail);
            boolean b = stmt.executeQuery().next();

            return a || b;
        });
    }

	/**
	 * Attempts to register a new user and returns whether it has successfully registered the user
	 * using the given {@link RegistrationForm}.
	 */
	public void registered(RegistrationForm form, String verify) {
		database.withConnection(connection -> {
			PreparedStatement stmt = connection.prepareStatement("INSERT INTO users_verification (verification, username, password, passwordsalt, mail, paymentmail, profilepicture, membersince) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
			String salt = SecurityService.createSalt();
			String pwd = SecurityService.encodePassword(form.password, salt);
			stmt.setString(1, SecurityService.secure(verify));
			stmt.setString(2, form.name.toLowerCase());
			stmt.setString(3, pwd);
			stmt.setString(4, salt);
			stmt.setString(5, form.email);
			stmt.setString(6, form.paymentmail);
			stmt.setString(7, "images/default_profile_pic.png");
			stmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
			stmt.execute();
		});
	}

    public void saveChangePassword(String verification, String username, String mail, int userid) {
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO users_change_password (verification, username, mail, userid) VALUES(?, ?, ?, ?)");
            stmt.setString(1, SecurityService.secure(verification));
            stmt.setString(2, username);
            stmt.setString(3, mail);
            stmt.setInt(4, userid);
            stmt.execute();
        });
    }

    public boolean verifyCanChangePassword(int userid) {
        return database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users_change_password WHERE userid=?");
            stmt.setInt(1, userid);
            return stmt.executeQuery().next();
        });
    }

    public boolean verifyChangePassword(String verification, int userid) {
	    return database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users_change_password WHERE verification=? AND userid=?");
            stmt.setString(1, SecurityService.secure(verification));
            stmt.setInt(2, userid);
            return stmt.executeQuery().next();
        });
    }

    public void removeChangePassword(int userid) {
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM users_change_password WHERE userid=?");
            stmt.setInt(1, userid);
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

	/**
	 * Attempts to update a exciting user and returns whether it has successfully updated the settings
	 * using the given {@link PersonalSettingsForm}.
	 */
	public void updatePassword(User user, PersonalSettingsPasswordForm form) {
		database.withConnection(connection -> {
			PreparedStatement stmt = connection.prepareStatement("UPDATE users SET password=? WHERE username=? AND id=?");
			String pwd = SecurityService.encodePassword(form.password, user.getSalt());
			stmt.setString(1, pwd);
			stmt.setString(2, user.getUsername());
			stmt.setInt(3, user.getId());
			stmt.execute();
		});
	}

    /**
     * Attempts to update a exciting user and returns whether it has successfully updated the settings
     * using the given {@link PersonalSettingsForm}.
     */
    public void updatePassword(User user, VerifyChangePasswordForm form) {
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("UPDATE users SET password=? WHERE username=? AND id=?");
            String pwd = SecurityService.encodePassword(form.password, user.getSalt());
            stmt.setString(1, pwd);
            stmt.setString(2, user.getUsername());
            stmt.setInt(3, user.getId());
            stmt.execute();
        });
    }
}