package controllers;

import com.google.common.collect.Lists;
import forms.SearchForm;
import models.GameCategory;
import models.Product;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.products.index;

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
public class ProductsController extends Controller {
    /**
     * A {@link FormFactory} to use search forms.
     */
    private FormFactory formFactory;

    /**
     * The required {@link Database} dependency to fetch database connections.
     */
    private Database database;

    @Inject
    public ProductsController(FormFactory formFactory, Database database){
        this.formFactory = formFactory;
        this.database = database;
    }

    public Result searchGames(){
        Form<SearchForm> formBinding = formFactory.form(SearchForm.class).bindFromRequest();
        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return badRequest();
        } else {
            SearchForm form = formBinding.get();
            //TODO get search results

            String formInput = form.getInput();
            return index("input=" + formInput);
        }
    }

    @Inject
    public static Form<SearchForm> getSearchForm(FormFactory formFactory){
        return formFactory.form(SearchForm.class);
    }

    public Result index(String token) {
        System.out.println("TOKEN: " + token);
        // TODO: connection stuff
        if (token.startsWith("game=")) {
            token = token.replaceFirst("game=", "").replace("_", " ");
            // database stuff
            Optional<GameCategory> gameCategory = fetchGameCategory(token);
            if (gameCategory.isPresent()) {
                List<Product> products = fetchProducts(gameCategory.get());
                if (products.size() > 0)
                    return ok(views.html.products.game.render(gameCategory.get(), Lists.partition(products, 2), session()));
                else
                    return ok(views.html.products.gameError.render(gameCategory.get(), session()));
            } else {
                return redirect("/404");
            }
        }
        return ok(index.render(token, session()));
    }

    /**
     * Attempts to find a {@link GameCategory} that matches the given game name.
     */
    private Optional<GameCategory> fetchGameCategory(String gameName) {
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

                user = Optional.of(u);
            }

            return user;
        });
    }

    /**
     * Attempts to find a {@link Product} that matches the given game category.
     */
    private List<Product> fetchProducts(GameCategory gameCategory) {
        return database.withConnection(connection -> {
            List<Product> list = new ArrayList<>();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts WHERE gameid=?;");
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

                Optional<User> user = fetchUser(product.getUserId());
                user.ifPresent(product::setUser);
                list.add(product);
            }

            return list;
        });
    }
}
