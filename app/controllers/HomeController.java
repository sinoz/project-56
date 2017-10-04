package controllers;

import database.JavaJdbcConnection;
import models.GameCategory;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.home.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

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
		/**
		 * All game categories get divided into lists of 4.
		 * This is used for generating the rows.
		 */
		List<GameCategory> gameCategories = connection.getGameCategories();
		List<List<GameCategory>> list = new ArrayList<>();
		int lIn = 0;
		int cIn = 0;
		for (int i = 0; i < gameCategories.size(); i++) {
			if (list.size() <= lIn)
				list.add(new ArrayList<>());

			List<GameCategory> list2 = list.get(lIn);
			list2.add(gameCategories.get(i));
			list.set(lIn, list2);

			cIn++;
			if (cIn % 4 == 0) {
				cIn = 0;
				lIn++;
			}
		}

		return ok(index.render("Your new application is ready.", list));
	}
}
