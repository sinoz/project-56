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
 * @author: Maurice van Veen
 * @author Johan van der Hoeven
 */
public class ProductCheckoutBuyController extends Controller {
    /**
     * A {@link FormFactory} to use search forms.
     */
    private FormFactory formFactory;

    /**
     * The required {@link Database} dependency to fetch database connections.
     */
    private Database database;

    @Inject
    public ProductCheckoutBuyController(FormFactory formFactory, Database database){
        this.formFactory = formFactory;
        this.database = database;
    }

    public Result index(String token) {
        try {
            int id = Integer.valueOf(token);
            Optional<Product> p = fetchProduct(id);
            if (p.isPresent()) {
                Product product = p.get();
                Optional<User> user = fetchUser(product.getUserId());

                List<Review> reviewsproduct = fetchUserReviews(product.getUserId());

                int totalRating = 0;
                for (Review review : reviewsproduct)
                    totalRating += review.getRating();
                int rating = (int) (totalRating / (double) reviewsproduct.size());

                return ok(views.html.checkout.buy.render(product, user, rating, session()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return redirect("/404");
    }

    /**
     * Attempts to find a {@link GameCategory} that matches the given game id.
     */
    private Optional<GameCategory> fetchGameCategory(int id) {
        return database.withConnection(connection -> {
            Optional<GameCategory> gameCategory = Optional.empty();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gamecategories WHERE id=?");
            stmt.setInt(1, id);

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
     * Attempts to find a {@link Product} by an id.
     */
    private Optional<Product> fetchProduct(int id) {
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

                Optional<User> user = fetchUser(p.getUserId());
                user.ifPresent(p::setUser);

                Optional<GameCategory> gameCategory = fetchGameCategory(p.getGameId());
                gameCategory.ifPresent(p::setGameCategory);

                product = Optional.of(p);
            }

            return product;
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