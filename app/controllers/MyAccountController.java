package controllers;

import database.JavaJdbcConnection;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.myaccount.index;
import javax.inject.Inject;

/**
 * A {@link Controller} for the MyAccount page.
 *
 * @author Melle Nout
 * @author I.A
 */
public final class MyAccountController extends Controller {
	@Inject
	private JavaJdbcConnection connection;

    public Result index() {
        return ok(index.render(connection.getUser()));
    }
}