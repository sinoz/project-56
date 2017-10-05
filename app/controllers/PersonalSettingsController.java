package controllers;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * A {@link Controller} for the PersonalSettings page.
 *
 * @author Melle Nout
 * @author I.A
 */
public final class PersonalSettingsController extends Controller {
	public Result index() {
		String loggedInAs = session().get("loggedInAs");
		if (loggedInAs == null || loggedInAs.length() == 0) {
			return redirect("/");
		} else {
			return ok(views.html.personalsettings.index.render(session()));
		}
	}
}