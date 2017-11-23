package services;

import models.User;
import play.db.Database;
import play.mvc.Http;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

/**
 * @author Maurice van Veen
 */
public class SessionService {

    public static void initSession(Http.Session session, User user, play.db.Database database) {
        clearSession(session);

        String token = SecurityService.createSalt();

        session.put("loggedInAs", user.getUsername());
        session.put("profilePictureURL", Optional.ofNullable(user.getProfilePicture()).orElse("images/default_profile_pic.png"));
        session.put("usedMail", user.getMail());
        session.put("usedPaymentMail", Optional.ofNullable(user.getPaymentMail()).orElse(""));
        session.put("sessionToken", token);

        updateToken(database, user.getId(), SecurityService.secure(token));
    }

    public static void updateSession(Http.Session session, String usernameToChangeTo, String emailToChangeTo, String paymentMailToChangeTo) {
        session.put("loggedInAs", usernameToChangeTo);
        session.put("usedMail", emailToChangeTo);
        session.put("usedPaymentMail", paymentMailToChangeTo);
    }

    public static void logout(Http.Session session) {
        String loggedInAs = session.get("loggedInAs");
        if (loggedInAs != null) {
            clearSession(session);
        }
    }

    public static String getLoggedInAs(Http.Session session) {
        return session.get("loggedInAs");
    }

    public static String getSessionToken(Http.Session session) {
        return session.get("sessionToken");
    }

    public static boolean redirect(Http.Session session, play.db.Database database) {
        String loggedInAs = getLoggedInAs(session);
        String sessionToken = getSessionToken(session);
        String databaseToken = fetchToken(database, loggedInAs);
        boolean output = loggedInAs == null || loggedInAs.length() == 0 || !SecurityService.secure(sessionToken).equals(databaseToken);
        if (output)
            clearSession(session);
        return output;
    }


    private static void clearSession(Http.Session session) {
        session.clear();
    }

    private static void updateToken(play.db.Database database, int id, String token) {
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("UPDATE users SET sessionToken=? WHERE id=?");
            stmt.setString(1, token);
            stmt.setInt(2, id);
            stmt.execute();
        });
    }

    private static String fetchToken(play.db.Database database, String username) {
        return database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT sessionToken FROM users WHERE username=?");
            stmt.setString(1, username);

            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getString("sessionToken");
            }
            return null;
        });
    }
}