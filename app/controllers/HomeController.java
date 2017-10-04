package controllers;

import com.google.common.collect.Lists;
import database.JavaJdbcConnection;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.home.index;

import javax.inject.Inject;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 *
 * @author I.A, Maurice van Veen
 */
public final class HomeController extends Controller {
	@Inject
	private JavaJdbcConnection connection;

	public Result index() {
		return ok(index.render(Lists.partition(connection.getGameCategories(), 4)));
	}
}
