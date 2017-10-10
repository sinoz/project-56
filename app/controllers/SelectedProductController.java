package controllers;

import com.google.common.collect.Lists;
import models.GameCategory;
import models.Product;
import models.User;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

/**
 * A {@link Controller} that handles searches.
 *
 * @author: Joris Stander
 * @author: D.Bakhuis
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
        if (product.isPresent()) {
            Optional<GameCategory> gameCategory = fetchGameCategory(product.get().getGameId());
            if (gameCategory.isPresent()){
                return ok(views.html.selectedproduct.details.render(gameCategory.get(), product.get() , session()));
        }}
        return ok(views.html.selectedproduct.index.render(token, session()));
    }

    /**
     * Attempts to find a {@link Product} that matches the given token.
     */
    private Optional<Product> fetchDetails(String token) {
        return database.withConnection(connection -> {
            Optional<Product> returned = Optional.empty();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts WHERE id=?;");
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

}
