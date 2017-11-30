package controllers;

import concurrent.DbExecContext;
import forms.SearchForm;
import models.Order;
import models.ViewableUser;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecution;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.OrderService;
import services.SessionService;
import services.UserViewService;
import views.html.trackorder.*;


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

    private final play.db.Database database;

    /**
     * A {@link FormFactory} to use search forms.
     */
    private FormFactory formFactory;

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
    public TrackOrderController(play.db.Database database, FormFactory formFactory, OrderService orderService, UserViewService userViewService, DbExecContext dbEc, HttpExecutionContext httpEc){
        this.database = database;
        this.formFactory = formFactory;
        this.orderService = orderService;
        this.userViewService = userViewService;
        this.dbEc = dbEc;
        this.httpEc = httpEc;
    }

    public CompletionStage<Result> index() {
        return completedFuture(ok(index.render(session(), "")));
    }

    public CompletionStage<Result> indexOrder(String trackingId) {
        Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);
        CompletableFuture<Optional<Order>> optionalOrder = supplyAsync(() -> orderService.getOrderByTrackId(trackingId), dbExecutor);
        CompletableFuture<Optional<ViewableUser>> optionalViewableUser = supplyAsync(() -> userViewService.fetchViewableUser(SessionService.getLoggedInAs(session())), dbExecutor);

        return optionalOrder.thenCombineAsync(optionalViewableUser, (ord, user) -> {
            if (!ord.isPresent())
                return ok(error.render(session(), trackingId));

            Order o = ord.get();

            if (o.hasUser()) {
                boolean loggedIn = false;
                String loggedInAs = SessionService.getLoggedInAs(session());
                if (loggedInAs != null) {
                    String sessionToken = SessionService.getSessionToken(session());
                    if (SessionService.checkSessionToken(database, loggedInAs, sessionToken))
                        loggedIn = true;
                }

                if (!user.isPresent() || o.getUserId() != user.get().getId() || !loggedIn) {
                    return ok(error.render(session(), trackingId));
                } else {
                    return ok(order.render(o, session(), trackingId));
                }
            } else {
                return ok(order.render(o, session(), trackingId));
            }
        }, httpEc.current());
    }

    public Result redirect() {
        Form<SearchForm> formBinding = formFactory.form(SearchForm.class).bindFromRequest();
        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return badRequest();
        } else {
            SearchForm form = formBinding.get();
            String formInput = form.getInput();
            return redirect("/trackorder/" + formInput);
        }
    }
}