package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.products.index;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 *
 * @author Maurice van Veen
 */
public final class ProductsController extends Controller {
	public Result index(String token) {
		// TODO: connection stuff
		return ok(index.render(token, session()));
	}
}
