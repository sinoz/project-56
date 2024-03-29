package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import services.SessionService;

/**
 * A {@link Controller} to log the user out, if logged in.
 *
 * @author I.A
 */
public final class LogoutController extends Controller {
	public Result index() {
		SessionService.logout(session());
		return redirect("/");
	}
}
