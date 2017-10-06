package controllers;

import play.mvc.Controller;
import play.mvc.Result;

/** A {@link Controller} to log the user out, if logged in.
 * @author I.A
 */
public final class LogoutController extends Controller {
	public Result index() {
		String loggedInAs = session().get("loggedInAs");
		if (loggedInAs != null) {
			session().clear();
		}

		return redirect("/");
	}
}
