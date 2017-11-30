package controllers;

import models.ViewableUser;
import play.mvc.Controller;
import play.mvc.Result;
import services.*;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

/**
 * A {@link Controller} for the contact page.
 *
 * @author Maurice van Veen
 */
public final class PlaceOrderController extends Controller {

    private final play.db.Database database;

    /**
     * The {@link OrderService} used for accessing orders in de database
     */
    private final OrderService orderService;

    private final UserViewService userViewService;

    private final MailerService mails;

    private final TaskService taskService;

    @Inject
    public PlaceOrderController(play.db.Database database, OrderService orderService, UserViewService userViewService, MailerService mailerService, TaskService taskService) {
        this.database = database;
        this.orderService = orderService;
        this.userViewService = userViewService;
        this.mails = mailerService;
        this.taskService = taskService;
    }

    public Result index(String verification, String token, String userId, String sessionToken, String p, String trackingId, String couponCode, String mail) {
        String verification2 = orderService.createVerification(token, userId, sessionToken, p, trackingId, couponCode, mail);
        if (verification.equals(verification2)) {
            try {
                int t = Integer.valueOf(token);
                double price = Double.valueOf(p);

                try {
                    int id = Integer.valueOf(userId);

                    Optional<ViewableUser> user = userViewService.fetchViewableUser(id);

                    if (user.isPresent()) {
                        boolean result = placeOrder(t, id, price, trackingId, couponCode, mail);
                        if (result)
                            return redirect("/orderconfirmed/" + trackingId + "/" + mail);
                        else
                            return redirect("/orderfailed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // user does not exist or an error happened with user information
                boolean result = placeOrder(t, -1, price, trackingId, couponCode, mail);
                if (result)
                    return redirect("/orderconfirmed/" + trackingId + "/" + mail);
                else
                    return redirect("/orderfailed");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return redirect("/orderfailed");
    }

    private boolean placeOrder(int token, int userId, double price, String trackingId, String couponCode, String mail) {
        if (orderExists(trackingId))
            return false;

        saveToDatabase(token, userId, price, trackingId, couponCode);

        sendEmail(mail, trackingId);


        // taskService.tell();

        return true;
    }

    private boolean orderExists(String trackingId) {
        return database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM orders WHERE trackid=?");
            stmt.setString(1, trackingId);

            ResultSet result = stmt.executeQuery();

            return result.next();
        });
    }

    private void saveToDatabase(int token, int userId, double price, String trackingId, String couponCode) {
        database.withConnection(connection -> {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO orders (trackid, hasuser, userid, productid, price, couponcode, status) VALUES (?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, trackingId);
            stmt.setBoolean(2, userId != -1);
            stmt.setInt(3, userId);
            stmt.setInt(4, token);
            stmt.setDouble(5, price);
            stmt.setString(6, couponCode);
            stmt.setInt(7, 0);
            stmt.execute();
        });
    }

    private void sendEmail(String mail, String trackingId) {
        if (mail == null || !mail.contains("@"))
            return;

        String title = "ReStart - Order Placed";
        mails.sendEmail(title, mail, "Your order has tracking ID: " + trackingId + "");
    }
}