package services;

import com.google.common.collect.ImmutableList;
import models.GameCategory;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author I.A
 */
public final class ProductService {
	/**
	 * TODO
	 */
	private final play.db.Database database;

	/**
	 * Creates a new {@link ProductService}.
	 */
	@Inject
	public ProductService(play.db.Database database) {
		this.database = database;
	}

	/**
	 * Computes an {@link ImmutableList} of {@link GameCategory}s.
	 */
	public ImmutableList<GameCategory> getGameCategories() {
		return database.withConnection(connection -> {
			List<GameCategory> gameCategories = new ArrayList<>();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gamecategories ORDER BY id ASC");
			ResultSet results = stmt.executeQuery();

			while (results.next()) {
				GameCategory gameCategory = new GameCategory();

				gameCategory.setId(results.getInt("id"));
				gameCategory.setName(results.getString("name"));
				gameCategory.setImage(results.getString("image"));
				gameCategory.setDescription(results.getString("description"));

				gameCategories.add(gameCategory);
			}

			return ImmutableList.copyOf(gameCategories);
		});
	}
}
