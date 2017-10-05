package controllers;

import database.JavaJdbcConnection;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.personalsettings.index;
import javax.inject.Inject;

/**
 * A {@link Controller} for the PersonalSettings page.
 *
 * @author Melle Nout
 * @author I.A
 */
public final class PersonalSettingsController extends Controller {
    @Inject
    private JavaJdbcConnection connection;

    public Result index() {
        return ok(index.render(connection.getUser()));
    }
}