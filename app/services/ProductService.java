package services;

import com.google.common.collect.ImmutableList;
import models.GameCategory;
import models.Product;
import models.User;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author I.A
 * @author Maurice van Veen
 */
@Singleton
public final class ProductService {
	private final play.db.Database database;

	/**
	 * The {@link services.UserViewService} to obtain data from.
	 */
	private UserViewService userViewService;

	/**
	 * Creates a new {@link ProductService}.
	 */
	@Inject
	public ProductService(play.db.Database database, UserViewService userViewService) {
		this.database = database;
		this.userViewService = userViewService;
	}

	/**
	 * Computes an {@link ImmutableList} of {@link GameCategory}s.
	 */
	public ImmutableList<GameCategory> fetchGameCategories() {
		return database.withConnection(connection -> {
			List<GameCategory> gameCategories = new ArrayList<>();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gamecategories ORDER BY id ASC");
			ResultSet results = stmt.executeQuery();

			while (results.next()) {
				gameCategories.add(ModelService.createGameCategory(results));
			}

			return ImmutableList.copyOf(gameCategories);
		});
	}

	/**
	 * Attempts to find a {@link GameCategory} that matches the given game id.
	 */
	public Optional<GameCategory> fetchGameCategory(int gameId) {
		return database.withConnection(connection -> {
			Optional<GameCategory> gameCategory = Optional.empty();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gamecategories WHERE id=?");
			stmt.setInt(1, gameId);

			ResultSet results = stmt.executeQuery();

			if (results.next()) {
				gameCategory = Optional.of(ModelService.createGameCategory(results));
			}

			return gameCategory;
		});
	}

	/**
	 * Attempts to find a {@link GameCategory} that matches the given game name.
	 */
	public Optional<GameCategory> fetchGameCategory(String gameName) {
		return database.withConnection(connection -> {
			Optional<GameCategory> gameCategory = Optional.empty();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gamecategories WHERE name=?");
			stmt.setString(1, gameName);

			ResultSet results = stmt.executeQuery();

			if (results.next()) {
				gameCategory = Optional.of(ModelService.createGameCategory(results));
			}

			return gameCategory;
		});
	}

	/**
	 * Attempts to update a {@link GameCategory} that matches the given game id.
	 */
	public void updateGameCategory(int gameId, String name, String description) {
		database.withConnection(connection -> {
			PreparedStatement stmt = connection.prepareStatement("UPDATE gamecategories SET name=?, description=? WHERE id=?");
			stmt.setString(1, name);
			stmt.setString(2, description);
			stmt.setInt(3, gameId);
			stmt.execute();
		});
	}

	/**
	 * Attempts to update a {@link GameCategory} that matches the given game id.
	 */
	public void updateGameCategorySearch(int gameId) {
		database.withConnection(connection -> {
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gamecategories WHERE id=?");
			stmt.setInt(1, gameId);

			ResultSet result = stmt.executeQuery();

			if (result.next()) {
				int start = result.getInt("search");
				int search = start + 1;

				stmt = connection.prepareStatement("UPDATE gamecategories SET search=? WHERE id=?");
				stmt.setInt(1, search);
				stmt.setInt(2, gameId);
				stmt.execute();
			}
		});
	}

//    public void deleteGameCategory(int gameId) {
//        database.withConnection(connection -> {
//            PreparedStatement stmt = connection.prepareStatement("DELETE FROM gameaccounts WHERE gameid=?");
//            stmt.setInt(1, gameId);
//            stmt.execute();
//
//            stmt = connection.prepareStatement("DELETE FROM gamecategories WHERE id=?");
//            stmt.setInt(1, gameId);
//            stmt.execute();
//        });
//    }

	/**
	 * Attempts to find all {@link Product}.
	 */
	public List<Product> fetchProducts() {
		return database.withConnection(connection -> {
			List<Product> list = new ArrayList<>();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts WHERE visible=TRUE AND disabled=FALSE;");

			ResultSet results = stmt.executeQuery();

			while (results.next()) {
				list.add(ModelService.createProduct(results, userViewService, this));
			}

			return list;
		});
	}

	public List<Product> fetchAllProducts() {
		return database.withConnection(connection -> {
			List<Product> list = new ArrayList<>();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts ORDER BY visible, disabled, id;");

			ResultSet results = stmt.executeQuery();

			while (results.next()) {
				list.add(ModelService.createProduct(results, userViewService, this));
			}

			return list;
		});
	}

	/**
	 * Attempts to find a {@link Product} that matches the given game category.
	 */
	public List<Product> fetchProducts(GameCategory gameCategory) {
		return database.withConnection(connection -> {
			List<Product> list = new ArrayList<>();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts WHERE gameid=? AND visible=TRUE AND disabled=FALSE;");
			stmt.setInt(1, gameCategory.getId());

			ResultSet results = stmt.executeQuery();

			while (results.next()) {
				list.add(ModelService.createProduct(results, userViewService, this));
			}

			return list;
		});
	}

	/**
	 * Attempts to find a {@link Product} by an id.
	 */
	public Optional<Product> fetchVisibleProduct(int id) {
		return database.withConnection(connection -> {
			Optional<Product> product = Optional.empty();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts WHERE id=? AND visible=TRUE AND disabled=FALSE;");
			stmt.setInt(1, id);

			ResultSet results = stmt.executeQuery();

			if (results.next()) {
				product = Optional.of(ModelService.createProduct(results, userViewService, this));
			}

			return product;
		});
	}

	/**
	 * Attempts to find a {@link Product} by an id.
	 */
	public Optional<Product> fetchProduct(int id) {
		return database.withConnection(connection -> {
			Optional<Product> product = Optional.empty();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts WHERE id=?;");
			stmt.setInt(1, id);

			ResultSet results = stmt.executeQuery();

			if (results.next()) {
				product = Optional.of(ModelService.createProduct(results, userViewService, this));
			}

			return product;
		});
	}

	/**
	 * Attempts to get the {@link Product}s that belong to the given userId.
	 */
	public List<List<Product>> fetchUserProducts(int userId) {
		return database.withConnection(connection -> {
			Optional<User> user = userViewService.fetchUser(userId);
			List<List<Product>> list = new ArrayList<>();

			if (user.isPresent()) {
				PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts WHERE userid=? AND visible=TRUE AND disabled=FALSE");
				stmt.setInt(1, userId);

				ResultSet results = stmt.executeQuery();

				List<Product> row = new ArrayList<>();
				int l = 0;
				while (results.next()) {
					row.add(ModelService.createProduct(results, userViewService, this));
					l++;
					if (l > 1) {
						list.add(row);
						row = new ArrayList<>();
						l = 0;
					}
				}
				if (l > 0) list.add(row);
			}

			return list;
		});
	}
}