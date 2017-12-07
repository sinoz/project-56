package services;

import forms.AdminModifyUserForm;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Johan van der Hoeven
 */
@Singleton
public final class AdminService {
    private final play.db.Database database;

    /**
     * Creates a new {@link AdminService}.
     */
    @Inject
    public AdminService(play.db.Database database) {
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
     * Attempts to update a user using the given {@link forms.AdminModifyUserForm}.
     */
    public void updateSettings(int userId, AdminModifyUserForm form) {
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("UPDATE users SET username=?, mail=?, paymentmail=?, isadmin=? WHERE id=?");
            stmt.setString(1, form.username);
            stmt.setString(2, form.mail);
            stmt.setString(3, form.paymentMail);
            stmt.setBoolean(4, form.isAdmin);
            stmt.setInt(5, userId);
            stmt.execute();
        });
    }

    public boolean isAdmin(int userId){
        return database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT isadmin FROM users WHERE id=?");
            stmt.setInt(1, userId);

            ResultSet result = stmt.executeQuery();
            return result.next() && result.getBoolean("isadmin");
        });
    }
}
