package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.orderconfirmed.index;

/**
 * A {@link Controller} for the contact page.
 *
 * @author Melle Nout
 * @author I.A
 */
public final class OrderConfirmedController extends Controller {

    public Result index(String trackingId, String mail) {
        return ok(index.render(session(), trackingId, mail));
    }
}