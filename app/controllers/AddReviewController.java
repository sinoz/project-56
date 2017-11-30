package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.addreview.index;

/**
 * A {@link Controller} for the contact page.
 *
 * @author Joris Stander
 *
 */
public final class AddReviewController extends Controller {
    public Result index() {
        return ok(index.render(session()));
    }
}