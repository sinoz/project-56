package controllers;

import concurrent.DbExecContext;
import models.Order;
import models.ViewableUser;
import play.db.Database;
import play.libs.concurrent.HttpExecution;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.OrderService;
import services.SessionService;
import services.UserViewService;
import views.html.trackorder.index;
import views.html.trackorder.error;


import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A {@link Controller} for the track order page.
 *
 * @author Johan van der Hoeven
 * @author Melle Nout
 * @author I.A
 */
public final class TrackOrderController extends Controller {
    /**
     * The required {@link Database} dependency to fetch database connections.
     */
    private final play.db.Database database;

    /**
     * The {@link OrderService} used for accessing orders in de database
     */
    private final OrderService orderService;

    /**
     * The {@link UserViewService} used for getting user information
     */
    private final UserViewService userViewService;

    /**
     * The execution context used to asynchronously perform database operations.
     */
    private final DbExecContext dbEc;

    /**
     * The execution context used to asynchronously perform operations.
     */
    private final HttpExecutionContext httpEc;

    @Inject
    public TrackOrderController(play.db.Database database, OrderService orderService, UserViewService userViewService, DbExecContext dbEc, HttpExecutionContext httpEc){
        this.database = database;
        this.orderService = orderService;
        this.userViewService = userViewService;
        this.dbEc = dbEc;
        this.httpEc = httpEc;
    }

    public CompletionStage<Result> index(String trackingId) {
        // If the user is not logged in redirect to login page
        if(SessionService.redirect(session(), database)){
            return completedFuture(redirect("/login"));
        } else {
            Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);
            CompletableFuture<Optional<Order>> optionalOrder = supplyAsync(() -> orderService.getOrderById(Integer.valueOf(trackingId)), dbExecutor);
            CompletableFuture<Optional<ViewableUser>> optionalViewableUser = supplyAsync(() -> userViewService.fetchViewableUser(session().get("loggedInAs")), dbExecutor);

            // Otherwise check if order exists and belongs to loggedin user
            return optionalOrder.thenCombineAsync(optionalViewableUser, (order, user) -> {
                if(!order.isPresent() || order.get().getUserId() != user.get().getId()){
                    return ok(error.render(session()));
                } else {
                    return ok(index.render(order.get(), session()));
                }
            }, httpEc.current());
        }
    }
}