package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html._error_.index;

/**
 * A {@link Controller} to present the error page.
 * @author Maurice van Veen
 */
public final class ErrorController extends Controller {
	public Result index(String token) {
		return ok(index.render(token, session()));
	}
}
