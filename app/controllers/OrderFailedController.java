package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.orderfailed.index;

/**
 * A {@link Controller} for the contact page.
 *
 * @author Melle Nout
 * @author I.A
 */
public final class OrderFailedController extends Controller {
    public Result index() {
        return ok(index.render(session()));
    }
}