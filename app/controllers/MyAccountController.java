package controllers;

import play.mvc.Controller;
import play.mvc.Result;

import views.html.faq.index;

/**
 * A {@link Controller} for the FAQ page. test
 * @author Melle Nout
 * @author I.A
 */
public final class MyAccountController extends Controller {
    public Result index() {
        return ok(index.render());
    }
}
