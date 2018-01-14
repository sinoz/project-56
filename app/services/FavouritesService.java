package services;

import models.Product;
import play.db.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The UserViewService that retrieves Favourites
 *
 * @author Maurice van Veen
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
                    list.addAll(Arrays.asList((Integer[]) a.getArray()));
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
                    list.addAll(Arrays.asList((Integer[]) a.getArray()));
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
                Product product = ModelService.createProduct(results, userViewService, productService);
                list.add(product);
            }

            return list;
        });
    }
}