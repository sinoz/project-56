package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.trackorder.index;

/**
 * A {@link Controller} for the contact page.
 *
 * @author Melle Nout
 * @author I.A
 */
public final class TrackOrderController extends Controller {
    public Result index() {
        return ok(index.render(session()));
    }
}