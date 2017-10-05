package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html._error_.index;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 *
 * @author Maurice van Veen
 */
public final class ErrorControler extends Controller {

	public Result index(String token) {
		return ok(index.render(token));
	}
}
