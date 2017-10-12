package controllers;

import models.Product;
import models.Review;
import models.User;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.useraccount.index;
import views.html.useraccount.empty;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A {@link Controller} for showing user accounts.
 *
 * @author Johan van der Hoeven
 * @author: Maurice van Veen
 */
public final class UserAccountController extends Controller {
	/**
	 * The required {@link Database} dependency to fetch database connections.
	 */
	private Database database;

	@Inject
	public UserAccountController(Database database) {
		this.database = database;
	}

	/**
	 * Returns a {@link Result} combined with a user account page.
	 */
	public Result index(String username) {
		Optional<User> user = getUser(username);

		if (!user.isPresent()) return ok(empty.render(session()));

		List<Product> inventory = getUserProducts(user.get().getId());
		List<Review> reviews = getUserReviews(user.get().getId());

		return ok(index.render(user.get(), inventory, reviews, session()));
	}

	/**
	 * Attempts to find a {@link User} that matches the given username.
	 */
	private Optional<User> getUser(String username) {
		return database.withConnection(connection -> {
			Optional<User> user = Optional.empty();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username=?");
			stmt.setString(1, username);

			ResultSet results = stmt.executeQuery();

			if (results.next()) {
				User u = new User();

                u.setId(results.getString("id"));
                u.setUsername(results.getString("username"));
                u.setProfilePicture(results.getString("profilepicture"));
                u.setInventory(((List<String>) results.getObject("inventory")));
                u.setMemberSince(results.getDate("membersince"));

				user = Optional.of(u);
			}

			return user;
		});
	}

	/**
	 * Attempts to get the {@link Product}s that belong to the given userId.
	 */
	private List<Product> getUserProducts(String userId) {
		return database.withConnection(connection -> {
			List<Product> list = new ArrayList<>();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts WHERE userid=? AND visible=TRUE AND disabled=FALSE");
			stmt.setInt(1, Integer.parseInt(userId));

			ResultSet results = stmt.executeQuery();

			while (results.next()) {
				Product p = new Product();

				p.setId(results.getInt("id"));
				p.setUserId(results.getInt("userid"));
				p.setGameId(results.getInt("gameid"));
				p.setTitle(results.getString("title"));
				p.setDescription(results.getString("description"));
				p.setAddedSince(results.getDate("addedsince"));
				p.setCanBuy(results.getBoolean("canbuy"));
				p.setBuyPrice(results.getDouble("buyprice"));
				p.setCanTrade(results.getBoolean("cantrade"));

				list.add(p);
			}

			return list;
		});
	}

	/**
	 * Attempts to get the {@link Review}s that belong to the given userId.
	 */
	private List<Review> getUserReviews(String userId){
		return database.withConnection(connection -> {
			List<Review> list = new ArrayList<>();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM reviews WHERE userreceiverid=?");
			stmt.setInt(1, Integer.parseInt(userId));

			ResultSet results = stmt.executeQuery();

			while(results.next()){
				Review r = new Review();

				r.setId(results.getString("id"));
				r.setUserReceiverId(results.getInt("userreceiverid"));
				r.setUserSenderId(results.getInt("usersenderid"));
				r.setTitle(results.getString("title"));
				r.setDescription(results.getString("description"));
				r.setRating(results.getInt("rating"));

				Optional<User> sender = fetchUser(r.getUserSenderId());
				sender.ifPresent(r::setSender);

				list.add(r);
			}

			return list;
		});
	}

	/**
	 * Attempts to find a {@link User} that matches the given username and password combination.
	 */
	private Optional<User> fetchUser(int id) {
		return database.withConnection(connection -> {
			Optional<User> user = Optional.empty();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE id=?");
			stmt.setInt(1, id);

			ResultSet results = stmt.executeQuery();

			if (results.next()) {
				User u = new User();

				u.setId(results.getString("id"));
				u.setUsername(results.getString("username"));
				u.setPassword(results.getString("password"));
				u.setMail(results.getString("mail"));
				u.setPaymentMail(results.getString("paymentmail"));
				u.setProfilePicture(results.getString("profilepicture"));
				u.setMemberSince(results.getDate("membersince"));

				user = Optional.of(u);
			}

			return user;
		});
	}
}