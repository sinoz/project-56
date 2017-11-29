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
        } else { // Otherwise check if order exists and belongs to loggedin user
            Optional<ViewableUser> optionalLoggedInUser = userViewService.fetchViewableUser(session().get("loggedInAs"));
            int loggedInUserId = optionalLoggedInUser.get().getId();
            Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);
            boolean x = false;

//            return supplyAsync(() -> orderService.getOrderById(Integer.valueOf(trackingId)), dbExecutor)
//                    .thenApplyAsync((optionalOrder) -> {
//                        if(!optionalOrder.isPresent() || optionalOrder.get().getUserId() != loggedInUserId){
//                            x = true;
//                        } else {
//                            return ok(index.render(session()));
//                        }
//                    }, dbExecutor)
//                    .thenApplyAsync(() -> x ? ok(error.render(session()) : index.render(session())), httpEc.current());
            //TODO

            return completedFuture(ok(index.render(session())));
        }
    }
}