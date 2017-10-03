package controllers;

import database.JavaJdbcConnection;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.home.*;

import javax.inject.Inject;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 *
 * @author I.A
 */
public final class HomeController extends Controller {
	@Inject
	private JavaJdbcConnection connection;

	public Result index() {
		connection.tryIt();

		return ok(index.render("Your new application is ready."));
	}
}
