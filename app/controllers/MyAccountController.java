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
		return ok(index.render());
	}
}
