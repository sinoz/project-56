package services;

import models.GameCategory;
import models.Product;
import models.User;
import play.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The UserViewService that retrieves Favourites
 *
 * @author Johan van der Hoeven
 */
@Singleton
public final class FavouritesService {
    /**
     * The required {@link Database} dependency to fetch database connections.
     */
    private final play.db.Database database;

    private final ProductService productService;

    private final UserViewService userViewService;

    @Inject
    public FavouritesService(play.db.Database database, ProductService productService, UserViewService userViewService){
        this.database = database;
        this.productService = productService;
        this.userViewService = userViewService;
    }

    /**
     * The method that adds/deletes a game to a users favourites in the database
     */
    public void add(String prodId, String username){
        Integer productId = Integer.valueOf(prodId);

        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT favorites FROM users WHERE username=?");
            stmt.setString(1, username);

            ResultSet results = stmt.executeQuery();

            if(results.next()){
                List<Integer> list = new ArrayList<>();

                Array a = results.getArray("favorites");
                if(a != null){
                    for(Integer i : (Integer[]) a.getArray()){
                        list.add(i);
                    }
                }

                if(list.contains(productId)){
                    list.remove(productId);
                } else {
                    list.add(productId);
                }
                Array sqlArray = connection.createArrayOf("INTEGER", list.toArray());

                stmt = connection.prepareStatement("UPDATE users SET favorites=? WHERE username=?");
                stmt.setArray(1, sqlArray);
                stmt.setString(2, username);

                stmt.execute();
            }
        });
    }

    /**
     * Attempts to find the favourites for a given username.
     */
    public List<Integer> getFavourites(String username) {
        return database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT favorites FROM users WHERE username=?");
            stmt.setString(1, username);

            ResultSet results = stmt.executeQuery();
            List<Integer> list = new ArrayList<>();

            if(results.next()) {
                Array a = results.getArray("favorites");
                if (a != null) {
                    for (Integer i : (Integer[]) a.getArray()) {
                        list.add(i);
                    }
                }
            }
            return list;
        });
    }

    /**
     * Attempts to find the {@link Product}s that match the given ids.
     */
    public List<Product> getProducts(List<Integer> ids) {
        return database.withConnection(connection -> {
            List<Product> list = new ArrayList<>();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts WHERE visible=TRUE AND disabled=FALSE;");

            ResultSet results = stmt.executeQuery();

            while (results.next()) {
                if(!ids.contains(results.getInt("id"))){
                    continue;
                }
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

                Optional<GameCategory> gameCategory = productService.fetchGameCategory(product.getGameId());
                gameCategory.ifPresent(product::setGameCategory);

                list.add(product);
            }

            return list;
        });
    }
}
