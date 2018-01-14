package services;

import forms.AdminModifyProductForm;
import forms.AdminModifyUserForm;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Johan van der Hoeven
 * @author Maurice van Veen
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

    /**
     * Attempts to update a product using the given {@link forms.AdminModifyProductForm}.
     */
    public void updateSettings(int productId, AdminModifyProductForm form) {
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("UPDATE gameaccounts SET userid=?, gameid=?, visible=?, disabled=?, title=?, description=?, canbuy=?, buyprice=?, cantrade=?, maillast=?, mailcurrent=?, passwordcurrent=? WHERE id=?");
            stmt.setInt(1, form.userid);
            stmt.setInt(2, form.gameid);
            stmt.setBoolean(3, form.visible);
            stmt.setBoolean(4, form.disabled);
            stmt.setString(5, form.title);
            stmt.setString(6, form.description);
            stmt.setBoolean(7, form.canbuy);
            stmt.setDouble(8, form.buyprice);
            stmt.setBoolean(9, form.cantrade);
            stmt.setString(10, form.maillast);
            stmt.setString(11, form.mailcurrent);
            stmt.setString(12, form.currentpassword);
            stmt.setInt(13, productId);
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

    public void deleteUser(int userId){
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM gameaccounts WHERE userid=?");
            stmt.setInt(1, userId);
            stmt.execute();

            stmt = connection.prepareStatement("DELETE FROM reviews WHERE usersenderid=? OR userreceiverid=?");
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.execute();

            stmt = connection.prepareStatement("DELETE FROM reviewtokens WHERE usersenderid=? OR userreceiverid=?");
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.execute();

            stmt = connection.prepareStatement("DELETE FROM users WHERE id=?");
            stmt.setInt(1, userId);
            stmt.execute();
        });
    }

    public void deleteProduct(int productId){
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM gameaccounts WHERE id=?");
            stmt.setInt(1, productId);
            stmt.execute();
        });
    }
}