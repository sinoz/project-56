package controllers;

import akka.actor.ActorSystem;
import models.Product;
import models.ViewableUser;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import services.*;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Maurice van Veen
 */
public final class PlaceOrderController extends Controller {

    private final play.db.Database database;

    /**
     * The {@link OrderService} used for accessing orders in de database
     */
    private final OrderService orderService;

    private final ProductService productService;
    private final UserViewService userViewService;

    private final MailerService mails;

    private final ActorSystem actorSystem;
    private final ExecutionContext context;

    @Inject
    public PlaceOrderController(play.db.Database database, OrderService orderService, ProductService productService, UserViewService userViewService, MailerService mailerService, ActorSystem actorSystem, ExecutionContext context) {
        this.database = database;
        this.orderService = orderService;
        this.productService = productService;
        this.userViewService = userViewService;
        this.mails = mailerService;
        this.actorSystem = actorSystem;
        this.context = context;
    }

    public Result index(String verification, String token, String userId, String sessionToken, String p, String trackingId, String couponCode, String mail) {
        String verification2 = orderService.createVerification(token, userId, sessionToken, p, trackingId, couponCode, mail);
        if (verification.equals(verification2)) {
            try {
                int t = Integer.valueOf(token);
                Optional<Product> prod = productService.fetchProduct(t);
                if (prod.isPresent()) {
                    Product product = prod.get();

                    double price = Double.valueOf(p);

                    try {
                        int id = Integer.valueOf(userId);

                        Optional<ViewableUser> user = userViewService.fetchViewableUser(id);

                        if (user.isPresent()) {
                            boolean result = placeOrder(t, product, id, price, trackingId, couponCode, mail);
                            if (result)
                                return redirect("/orderconfirmed/" + trackingId + "/" + mail);
                            else
                                return redirect("/orderfailed");
                        }
                    } catch (Exception e) {
                    }

                    // user does not exist or an error happened with user information
                    boolean result = placeOrder(t, product, -1, price, trackingId, couponCode, mail);
                    if (result)
                        return redirect("/orderconfirmed/" + trackingId + "/" + mail);
                    else
                        return redirect("/orderfailed");
                } else {
                    return redirect("/orderfailed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return redirect("/orderfailed");
    }

    private boolean placeOrder(int token, Product product, int userId, double price, String trackingId, String couponCode, String mail) {
        if (orderExists(trackingId))
            return false;

        // TODO: check if product already is bought

        updateProduct(product);
        saveToDatabase(token, userId, price, trackingId, couponCode);

        sendOrderPlacedMail(mail, userId, trackingId, product);

        scheduleTask(trackingId, userId, mail, product);

        return true;
    }

    private void scheduleTask(String trackingId, int userId, String mail, Product product) {
        int status = 1;
        schedule(trackingId, userId, mail, status, product);
    }

    private void schedule(String trackingId, int userId, String mail, int status, Product product) {
        FiniteDuration d = Duration.create(30 + new Random().nextInt(15), TimeUnit.SECONDS);
        actorSystem.scheduler().scheduleOnce(d, () ->
        {
            updateDatabase(trackingId, status);
            if (status < 4) {
                schedule(trackingId, userId, mail, status + 1, product);
            } else {
                finishOrder(trackingId, userId, mail, product);
            }
        }, context);
    }

    private void finishOrder(String trackingId, int userId, String mail, Product product) {
        if (userId != -1)
            saveReviewToken(trackingId, userId, product);
        sendOrderFinishedMail(mail, userId, trackingId, product);
    }

    private boolean orderExists(String trackingId) {
        return database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM orders WHERE trackid=?");
            stmt.setString(1, trackingId);

            ResultSet result = stmt.executeQuery();

            return result.next();
        });
    }

    private void updateProduct(Product product) {
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("UPDATE gameaccounts SET disabled = TRUE WHERE id=?");
            stmt.setInt(1, product.getId());
            stmt.execute();
        });
    }

    private void saveToDatabase(int token, int userId, double price, String trackingId, String couponCode) {
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO orders (trackid, hasuser, userid, productid, price, couponcode, status, ordertype) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, trackingId);
            stmt.setBoolean(2, userId != -1);
            stmt.setInt(3, userId);
            stmt.setInt(4, token);
            stmt.setDouble(5, price);
            stmt.setString(6, couponCode);
            stmt.setInt(7, 0);
            stmt.setInt(8, 0);
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

    private void saveReviewToken(String trackingId, int userId, Product product) {
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO reviewtokens (reviewid, userreceiverid, usersenderid, productid) VALUES (?, ?, ?, ?)");
            stmt.setString(1, trackingId);
            stmt.setInt(2, product.getUserId());
            stmt.setInt(3, userId);
            stmt.setInt(4, product.getId());
            stmt.execute();
        });
    }

    private void sendOrderPlacedMail(String mail, int userId, String trackingId, Product product) {
        if (!checkMail(mail))
            return;

        String title = "ReStart - Order Placed - " + trackingId;
        mails.sendEmail(title, mail, "Your order has tracking ID: " + trackingId);
    }

    private void sendOrderFinishedMail(String mail, int userId, String trackingId, Product product) {
        if (!checkMail(mail))
            return;

        String title = "ReStart - Product Delivered - " + trackingId;
        String review = " You can write a review by going to: restart-webshop.herokuapp.com/review/" + trackingId;
        if (userId == -1)
            review = "";
        mails.sendEmail(title, mail, "Your order with tracking ID: " + trackingId + " has been delivered." + review + " You can use these details to log in: ...");
    }

    private boolean checkMail(String mail) {
        return mail != null && mail.contains("@");
    }
}