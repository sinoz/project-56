package services;

import models.User;
import play.mvc.Http;

import java.util.Optional;

public class SessionService {

    public static void initSession(Http.Session session, User user) {
        clearSession(session);

        String token = SecurityService.createSalt();

        session.put("loggedInAs", user.getUsername());
        session.put("profilePictureURL", Optional.ofNullable(user.getProfilePicture()).orElse("images/default_profile_pic.png"));
        session.put("usedMail", user.getMail());
        session.put("usedPaymentMail", Optional.ofNullable(user.getPaymentMail()).orElse(""));
        session.put("token", token);
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

    private static void clearSession(Http.Session session) {
        session.clear();
    }

    public static String getLoggedInAs(Http.Session session) {
        return session.get("loggedInAs");
    }

    public static boolean redirect(Http.Session session) {
        String loggedInAs = getLoggedInAs(session);
        return loggedInAs == null || loggedInAs.length() == 0;
    }
}