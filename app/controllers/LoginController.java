package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.login.index;

/**
 * A {@link Controller} for the contact page.
 *
 * @author Daryl Bakhuis
 * @author
 */
public final class LoginController extends Controller {
    public Result index() {
        return ok(index.render());
    }
}
