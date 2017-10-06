package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.myaccount.index;

/**
 * A {@link Controller} for the MyAccount page.
 *
 * @author Melle Nout
 * @author I.A
 */
public final class MyAccountController extends Controller {
	public Result index() {
		String loggedInAs = session().get("loggedInAs");
		if (loggedInAs == null || loggedInAs.length() == 0) {
			return redirect("/");
		} else {
			return ok(index.render(session()));
		}
	}
}