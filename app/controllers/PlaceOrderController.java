package controllers;

import models.ViewableUser;
import play.mvc.Controller;
import play.mvc.Result;
import services.OrderService;
import services.SessionService;
import services.TaskService;
import services.UserViewService;

import javax.inject.Inject;
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

    private final TaskService taskService;

    @Inject
    public PlaceOrderController(play.db.Database database, OrderService orderService, UserViewService userViewService, TaskService taskService) {
        this.database = database;
        this.orderService = orderService;
        this.userViewService = userViewService;
        this.taskService = taskService;
    }

    public Result index(String verification, String token, String userId, String sessionToken, String trackingId, String mail) {
        String verification2 = orderService.createVerification(token, userId, sessionToken, trackingId, mail);
        if (verification.equals(verification2)) {
            try {
                int t = Integer.valueOf(token);

                try {
                    int id = Integer.valueOf(userId);

                    Optional<ViewableUser> user = userViewService.fetchViewableUser(userId);

                    if (user.isPresent()) {
                        if (SessionService.checkSessionToken(database, user.get().getUsername(), sessionToken)) {
                            placeOrder(t, id, sessionToken, trackingId, mail);
                            return redirect("/orderconfirmed/" + trackingId + "/" + mail);
                        } else {
                            return redirect("/orderfailed");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // user does not exist or an error happened with user information
                placeOrder(t, 0, null, trackingId, mail);

                return redirect("/orderconfirmed/" + trackingId + "/" + mail);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return redirect("/orderfailed");
    }

    private void placeOrder(int token, int userId, String sessionToken, String trackingId, String mail) {
        // PLACE ORDER HERE


        // taskService.tell();
    }


}