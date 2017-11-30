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
 * TODO
 *
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

	/**
	 * Attempts to find a {@link GameCategory} that matches the given game name.
	 */
	public Optional<GameCategory> fetchGameCategory(int gameId) {
		return database.withConnection(connection -> {
			Optional<GameCategory> gameCategory = Optional.empty();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gamecategories WHERE id=?");
			stmt.setInt(1, gameId);

			ResultSet results = stmt.executeQuery();

			if (results.next()) {
				GameCategory gc = new GameCategory();

				gc.setId(results.getInt("id"));
				gc.setName(results.getString("name"));
				gc.setImage(results.getString("image"));
				gc.setDescription(results.getString("description"));

				gameCategory = Optional.of(gc);
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
				GameCategory gc = new GameCategory();

				gc.setId(results.getInt("id"));
				gc.setName(results.getString("name"));
				gc.setImage(results.getString("image"));
				gc.setDescription(results.getString("description"));

				gameCategory = Optional.of(gc);
			}

			return gameCategory;
		});
	}

	/**
	 * Attempts to find all {@link Product}.
	 */
	public List<Product> fetchProducts() {
		return database.withConnection(connection -> {
			List<Product> list = new ArrayList<>();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts WHERE visible=TRUE AND disabled=FALSE;");

			ResultSet results = stmt.executeQuery();

			while (results.next()) {
				Product product = new Product();

				product.setId(results.getInt("id"));
				product.setUserId(results.getInt("userid"));
				product.setGameId(results.getInt("gameid"));
				product.setVisible(results.getBoolean("visible"));
				product.setDisabled(results.getBoolean("disabled"));
				product.setTitle(results.getString("title"));
				product.setDescription(results.getString("description"));
				product.setAddedSince(results.getDate("addedsince"));
				product.setCanBuy(results.getBoolean("canbuy"));
				product.setBuyPrice(results.getDouble("buyprice"));
				product.setCanTrade(results.getBoolean("cantrade"));
				product.setMailLast(results.getString("maillast"));
				product.setMailCurrent(results.getString("mailcurrent"));
				product.setPasswordCurrent(results.getString("passwordcurrent"));

				Optional<User> user = userViewService.fetchUser(product.getUserId());
				user.ifPresent(product::setUser);

				Optional<GameCategory> gameCategory = fetchGameCategory(product.getGameId());
				gameCategory.ifPresent(product::setGameCategory);

				list.add(product);
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
				Product product = new Product();

				product.setId(results.getInt("id"));
				product.setUserId(results.getInt("userid"));
				product.setGameId(results.getInt("gameid"));
				product.setVisible(results.getBoolean("visible"));
				product.setDisabled(results.getBoolean("disabled"));
				product.setTitle(results.getString("title"));
				product.setDescription(results.getString("description"));
				product.setAddedSince(results.getDate("addedsince"));
				product.setCanBuy(results.getBoolean("canbuy"));
				product.setBuyPrice(results.getDouble("buyprice"));
				product.setCanTrade(results.getBoolean("cantrade"));
				product.setMailLast(results.getString("maillast"));
				product.setMailCurrent(results.getString("mailcurrent"));
				product.setPasswordCurrent(results.getString("passwordcurrent"));

				Optional<User> user = userViewService.fetchUser(product.getUserId());
				user.ifPresent(product::setUser);

				product.setGameCategory(gameCategory);

				list.add(product);
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
				Product p = new Product();

				p.setId(results.getInt("id"));
				p.setUserId(results.getInt("userid"));
				p.setGameId(results.getInt("gameid"));
				p.setVisible(results.getBoolean("visible"));
				p.setDisabled(results.getBoolean("disabled"));
				p.setTitle(results.getString("title"));
				p.setDescription(results.getString("description"));
				p.setAddedSince(results.getDate("addedsince"));
				p.setCanBuy(results.getBoolean("canbuy"));
				p.setBuyPrice(results.getDouble("buyprice"));
				p.setCanTrade(results.getBoolean("cantrade"));
				p.setMailLast(results.getString("maillast"));
				p.setMailCurrent(results.getString("mailcurrent"));
				p.setPasswordCurrent(results.getString("passwordcurrent"));

				Optional<User> user = userViewService.fetchUser(p.getUserId());
				user.ifPresent(p::setUser);

				Optional<GameCategory> gameCategory = fetchGameCategory(p.getGameId());
				gameCategory.ifPresent(p::setGameCategory);

				product = Optional.of(p);
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
				Product p = new Product();

				p.setId(results.getInt("id"));
				p.setUserId(results.getInt("userid"));
				p.setGameId(results.getInt("gameid"));
				p.setVisible(results.getBoolean("visible"));
				p.setDisabled(results.getBoolean("disabled"));
				p.setTitle(results.getString("title"));
				p.setDescription(results.getString("description"));
				p.setAddedSince(results.getDate("addedsince"));
				p.setCanBuy(results.getBoolean("canbuy"));
				p.setBuyPrice(results.getDouble("buyprice"));
				p.setCanTrade(results.getBoolean("cantrade"));
				p.setMailLast(results.getString("maillast"));
				p.setMailCurrent(results.getString("mailcurrent"));
				p.setPasswordCurrent(results.getString("passwordcurrent"));

				Optional<User> user = userViewService.fetchUser(p.getUserId());
				user.ifPresent(p::setUser);

				Optional<GameCategory> gameCategory = fetchGameCategory(p.getGameId());
				gameCategory.ifPresent(p::setGameCategory);

				product = Optional.of(p);
			}

			return product;
		});
	}
}