package services.auth;

import database.JavaJdbcConnection;
import models.User;

import javax.inject.Inject;

/**
 * An {@link AuthenticationService} using Play authentication to authenticate
 * users attempting to login.
 * @author I.A
 */
public final class PlayAuthenticationService implements AuthenticationService {
    @Inject
    private JavaJdbcConnection connection;

    public boolean authenticateUser(String username, String password){
        User result = connection.getUser(username, password);
        return result != null;
    }
}
