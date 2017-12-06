package services;

import akka.actor.ActorSystem;
import models.GameCategory;
import models.Product;
import models.User;
import play.db.Database;
import play.mvc.Result;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;

/**
 * The UserViewService that retrieves Favourites
 *
 * @author Maurice van Veen
 */
@Singleton
public final class MyInventoryService {
    /**
     * The required {@link Database} dependency to fetch database connections.
     */
    private final play.db.Database database;

    private final ProductService productService;

    private final UserViewService userViewService;

    private final MailerService mails;

    private final ActorSystem actorSystem;
    private final ExecutionContext context;

    @Inject
    public MyInventoryService(play.db.Database database, ProductService productService, UserViewService userViewService, MailerService mails, ActorSystem actorSystem, ExecutionContext context) {
        this.database = database;
        this.productService = productService;
        this.userViewService = userViewService;
        this.mails = mails;
        this.actorSystem = actorSystem;
        this.context = context;
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

    private boolean orderExists(String trackingId) {
        return database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM orders WHERE trackid=?");
            stmt.setString(1, trackingId);

            ResultSet result = stmt.executeQuery();

            return result.next();
        });
    }

    public boolean placeOrder(int token, Product product, int userId, String trackingId, String mail) {
        if (orderExists(trackingId))
            return false;

        saveToDatabase(token, userId, trackingId);

        sendOrderPlacedMail(mail, trackingId, product);

        scheduleTask(trackingId, mail, product);

        return true;
    }

    private void scheduleTask(String trackingId, String mail, Product product) {
        int status = 1;
        schedule(trackingId, mail, status, product);
    }

    private void schedule(String trackingId, String mail, int status, Product product) {
        FiniteDuration d = Duration.create(30 + new Random().nextInt(15), TimeUnit.SECONDS);
        actorSystem.scheduler().scheduleOnce(d, () ->
        {
            updateDatabase(trackingId, status);
            if (status < 5) {
                schedule(trackingId, mail, status + 1, product);
            } else {
                addProduct(product);
                sendOrderFinishedMail(mail, trackingId, product);
            }
        }, context);
    }

    private void saveToDatabase(int token, int userId, String trackingId) {
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO orders (trackid, hasuser, userid, productid, ordertype, status) VALUES (?, ?, ?, ?, ?, ?)");
            stmt.setString(1, trackingId);
            stmt.setBoolean(2, userId != -1);
            stmt.setInt(3, userId);
            stmt.setInt(4, token);
            stmt.setInt(5, 1);
            stmt.setInt(6, 0);
            stmt.execute();
        });
    }

    private void updateDatabase(String trackingId, int status) {
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("UPDATE orders SET status=? WHERE trackid=?");
            stmt.setInt(1, status);
            stmt.setString(2, trackingId);
            stmt.execute();
        });
    }

    private void sendOrderPlacedMail(String mail, String trackingId, Product product) {
        if (!checkMail(mail))
            return;

        String title = "ReStart - Add Game Account - " + trackingId;
        mails.sendEmail(title, mail, "Your order has tracking ID: " + trackingId);
    }

    private void sendOrderFinishedMail(String mail, String trackingId, Product product) {
        if (!checkMail(mail))
            return;

        String title = "ReStart - Game Account Added - " + trackingId;
        mails.sendEmail(title, mail, "Your order with tracking ID: " + trackingId + ". Has been added. You can login and view the game account in your inventory.");
    }

    private boolean checkMail(String mail) {
        return mail != null && mail.contains("@");
    }

    /**
     * Adds the product to the database
     */
    private void addProduct(Product product){
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO gameaccounts(userid, gameid, visible, disabled, title, description, addedsince, canbuy, buyprice, cantrade, maillast, mailcurrent, passwordcurrent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            stmt.setInt(1, product.getUserId());
            stmt.setInt(2, product.getGameId());
            stmt.setBoolean(3, product.isVisible());
            stmt.setBoolean(4, product.isDisabled());
            stmt.setString(5, product.getTitle());
            stmt.setString(6, product.getDescription());
            stmt.setTimestamp(7, new Timestamp(product.getAddedSince().getTime()));
            stmt.setBoolean(8, product.isCanBuy());
            stmt.setDouble(9, product.getBuyPrice());
            stmt.setBoolean(10, product.isCanTrade());
            stmt.setString(11, product.getMailLast());
            stmt.setString(12, product.getMailCurrent());
            stmt.setString(13, product.getPasswordCurrent());

            stmt.execute();
        });
    }

    /**
     * Updates the product in the database
     */
    public void updateProduct(Product product){
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("UPDATE gameaccounts SET title=?,description=?,canbuy=?,buyprice=?,cantrade=?,maillast=?,mailcurrent=?,passwordcurrent=? WHERE id=?;");

            stmt.setString(1, product.getTitle());
            stmt.setString(2, product.getDescription());
            stmt.setBoolean(3, product.isCanBuy());
            stmt.setDouble(4, product.getBuyPrice());
            stmt.setBoolean(5, product.isCanTrade());
            stmt.setString(6, product.getMailLast());
            stmt.setString(7, product.getMailCurrent());
            stmt.setString(8, product.getPasswordCurrent());
            stmt.setInt(9, product.getId());

            stmt.execute();
        });
    }
}
