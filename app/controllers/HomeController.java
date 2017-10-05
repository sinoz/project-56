package controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import models.GameCategory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.home.index;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 *
 * @author I.A
 * @author Maurice van Veen
 */
public final class HomeController extends Controller {
	/**
	 * The amount of columns of game categories to present per row in the view.
	 */
	private static final int COLS_PER_ROW = 4;

	/**
	 * The required {@link Database} dependency to fetch database connections.
	 */
	@Inject
	private Database database;

	/**
	 * Returns a {@link Result} combined with the home view.
	 */
	public Result index() {
		return ok(index.render(Lists.partition(getGameCategories(), COLS_PER_ROW), session()));
	}

	/**
	 * Computes an {@link ImmutableList} of {@link GameCategory}s.
	 */
	private ImmutableList<GameCategory> getGameCategories() {
		return database.withConnection(connection -> {
			ArrayList<GameCategory> gameCategories = new ArrayList<>();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gamecategories ORDER BY id ASC");
			ResultSet results = stmt.executeQuery();

			while (results.next()) {
				GameCategory gameCategory = new GameCategory();

				gameCategory.setId(results.getString("id"));
				gameCategory.setName(results.getString("name"));
				gameCategory.setImage(results.getString("image"));
				gameCategory.setDescription(results.getString("description"));

				gameCategories.add(gameCategory);
			}

			return ImmutableList.copyOf(gameCategories);
		});
	}
}
