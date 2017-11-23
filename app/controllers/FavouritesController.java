package controllers;

import com.google.common.collect.Lists;
import concurrent.DbExecContext;
import forms.FavouriteForm;
import play.data.Form;
import play.data.FormFactory;
import play.db.Database;
import play.libs.concurrent.HttpExecution;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.FavouritesService;
import services.SessionService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A {@link Controller} for the Favourites page.
 *
 * @author Johan van der Hoeven
 */
public final class FavouritesController extends Controller{
    /**
     * The required {@link Database} dependency to fetch database connections.
     */
    private final play.db.Database database;
    /**
     * A {@link FormFactory} to use forms.
     */
    private final FormFactory formFactory;

    private final FavouritesService favouritesService;
    /**
     * The execution context used to asynchronously perform database operations.
     */
    private final DbExecContext dbEc;
    /**
     * The execution context used to asynchronously perform operations.
     */
    private final HttpExecutionContext httpEc;

    @Inject
    public FavouritesController(play.db.Database database, FormFactory formFactory, FavouritesService favouritesService, DbExecContext dbEc, HttpExecutionContext httpEc){
        this.database = database;
        this.formFactory = formFactory;
        this.favouritesService = favouritesService;
        this.dbEc = dbEc;
        this.httpEc = httpEc;
    }

    public CompletionStage<Result> index() {
        if (SessionService.redirect(session(), database)) {
            return completedFuture(redirect("/login"));
        } else {
            String loggedInAs = SessionService.getLoggedInAs(session());
            Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);
            return supplyAsync(() -> favouritesService.getFavourites(loggedInAs), dbExecutor)
                    .thenApplyAsync(favouritesService::getProducts, dbExecutor)
                    .thenApplyAsync(prods -> ok(views.html.favourites.index.render(Lists.partition(prods, 2), session())), httpEc.current());
        }
    }

    /**
     * The method that adds/deletes a game to a users favourites
     */
    public CompletionStage<Result> addFavourite() {
        Form<FavouriteForm> formBinding = formFactory.form(FavouriteForm.class).bindFromRequest();

        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return completedFuture(badRequest());
        } else {
            Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);
            FavouriteForm form = formBinding.get();
            String prodId = form.getId();

            return runAsync(() -> favouritesService.add(prodId, SessionService.getLoggedInAs(session())), dbExecutor).thenApplyAsync(i -> redirect("/products/selected/" + prodId), httpEc.current());
        }
    }
}
