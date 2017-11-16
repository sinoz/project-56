package services;

import models.GameCategory;
import models.Product;
import models.User;
import play.db.Database;

import javax.inject.Inject;
import java.sql.*;
import java.util.*;
import java.util.Date;

import static java.lang.System.currentTimeMillis;

/**
 * The UserViewService that retrieves Favourites
 *
 * @author Maurice van Veen
 */
public final class MyInventoryService {
    /**
     * The required {@link Database} dependency to fetch database connections.
     */
    private final Database database;

    private final ProductService productService;

    private final UserViewService userViewService;

    @Inject
    public MyInventoryService(Database database, ProductService productService, UserViewService userViewService){
        this.database = database;
        this.productService = productService;
        this.userViewService = userViewService;
    }

    /**
     * The method that adds/deletes a game to a users favourites in the database
     */
    public void init(int ID) {
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT id FROM gameaccounts WHERE userid=?");
            stmt.setInt(1, ID);

            ResultSet results = stmt.executeQuery();

            List<Integer> list = new ArrayList<>();
            while (results.next()) {
                list.add(results.getInt("id"));
            }

            Array sqlArray = connection.createArrayOf("INTEGER", list.toArray());

            stmt = connection.prepareStatement("UPDATE users SET inventory=? WHERE id=?");
            stmt.setArray(1, sqlArray);
            stmt.setInt(2, ID);

            stmt.execute();
        });
    }

    /**
     * Attempts to find the favourites for a given username.
     */
    public List<Integer> getInventory(String username) {
        return database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT id FROM users WHERE username=?");
            stmt.setString(1, username);

            ResultSet results = stmt.executeQuery();

            List<Integer> list = new ArrayList<>();

            int ID;
            if(results.next())
                ID = results.getInt("id");
            else
                return list;

            init(ID);

            stmt = connection.prepareStatement("SELECT inventory FROM users WHERE id=?");
            stmt.setInt(1, ID);

            results = stmt.executeQuery();

            if(results.next()) {
                Array a = results.getArray("inventory");
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
    public List<Product> getProducts(List<Integer> ids, String activeSubMenuItem) {
        return database.withConnection(connection -> {
            List<Product> list = new ArrayList<>();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts WHERE visible=TRUE AND disabled=FALSE;");

            ResultSet results = stmt.executeQuery();

            boolean filterOutSelling = false;
            boolean filterOutTrading = false;

            if (activeSubMenuItem != null)
                if (activeSubMenuItem.equals("selling")) {
                    filterOutTrading = true;
                } else if (activeSubMenuItem.equals("trading")) {
                    filterOutSelling = true;
                }

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

                if (filterOutSelling && product.isCanBuy() && !product.isCanTrade()) {
                    continue;
                } else if (filterOutTrading && product.isCanTrade() && !product.isCanBuy()) {
                    continue;
                }

                Optional<User> user = userViewService.fetchUser(product.getUserId());
                user.ifPresent(product::setUser);

                Optional<GameCategory> gameCategory = productService.fetchGameCategory(product.getGameId());
                gameCategory.ifPresent(product::setGameCategory);

                list.add(product);
            }

            return list;
        });
    }

    /**
     * Adds the product to the database
     */
    public void addProduct(Product product){
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO gameaccounts VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            stmt.setInt(1, product.getId());
            stmt.setInt(2, product.getUserId());
            stmt.setInt(3, product.getGameId());
            stmt.setBoolean(4, product.isVisible());
            stmt.setBoolean(5, product.isDisabled());
            stmt.setString(6, product.getTitle());
            stmt.setString(7, product.getDescription());
            stmt.setTimestamp(8, new Timestamp(product.getAddedSince().getTime()));
            stmt.setBoolean(9, product.isCanBuy());
            stmt.setDouble(10, product.getBuyPrice());
            stmt.setBoolean(11, product.isCanTrade());
            stmt.setString(12, product.getMailLast());
            stmt.setString(13, product.getMailCurrent());
            stmt.setString(14, product.getPasswordCurrent());

            stmt.execute();
        });
    }
}
