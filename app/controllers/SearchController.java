package controllers;

import database.JavaJdbcConnection;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.search.index;

import javax.inject.Inject;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 *
 * @author Maurice van Veen
 */
public final class SearchController extends Controller {
	@Inject
	private JavaJdbcConnection connection;

	public Result index(String token) {
	    // TODO: connection stuff
		return ok(index.render(token));
	}
}
