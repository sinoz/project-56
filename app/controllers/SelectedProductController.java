package controllers;

import models.GameCategory;
import models.Product;
import models.Review;
import models.User;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A {@link Controller} that handles searches.
 *
 * @author: Joris Stander
 * @author: D.Bakhuis
 * @author: Maurice van Veen
 *
 */
public class SelectedProductController extends Controller {
    /**
     * A {@link FormFactory} to use search forms.
     */
    private FormFactory formFactory;

    /**
     * The required {@link Database} dependency to fetch database connections.
     */
    private Database database;

    @Inject
    public SelectedProductController(FormFactory formFactory, Database database){
        this.formFactory = formFactory;
        this.database = database;
    }

    public Result index(String token) {
        Optional<Product> product = fetchDetails(token);
        List<Review> reviewsproduct;
        if (product.isPresent()) {
            Optional<GameCategory> gameCategory = fetchGameCategory(product.get().getGameId());
            if (gameCategory.isPresent()){
                reviewsproduct = fetchUserReviews(product.get().getUserId());
                return ok(views.html.selectedproduct.details.render(gameCategory.get(), product.get(),reviewsproduct, session()));
        }}
        return ok(views.html.selectedproduct.index.render(token, session()));
    }

    /**
     * Attempts to find a {@link Product} that matches the given token.
     */
    private Optional<Product> fetchDetails(String token) {
        return database.withConnection(connection -> {
            Optional<Product> returned = Optional.empty();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts WHERE id=? AND visible=TRUE AND disabled=FALSE;");
            stmt.setInt(1, Integer.valueOf(token));

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

                Optional<User> user = fetchUser(product.getUserId());
                user.ifPresent(product::setUser);
                returned = Optional.of(product);

            }
            return returned;
        });
    }

    /**
     * Attempts to find a {@link GameCategory} that matches the given game name.
     */
    private Optional<GameCategory> fetchGameCategory(int gameId) {
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

    /**
     * Attempts to get the {@link Review}s that belong to the given userId.
     */
    private List<Review> fetchUserReviews(int userId){
        return database.withConnection(connection -> {
            List<Review> list = new ArrayList<>();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM reviews WHERE userreceiverid=?");
            stmt.setInt(1, userId);

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
                Optional<User> receiver = fetchUser(r.getUserReceiverId());
                receiver.ifPresent(r::setReceiver);

                list.add(r);
            }
            return list;
        });
    }

}
