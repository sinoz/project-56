package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.home.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 *
 * @author I.A
 */
public final class HomeController extends Controller {
	public Result index() {
		return ok(index.render("Your new application is ready."));
	}
}
