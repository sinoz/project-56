package controllers;

import com.google.common.collect.Lists;
import concurrent.DbExecContext;
import models.GameCategory;
import models.ViewableUser;
import play.libs.concurrent.HttpExecution;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.ProductService;
import services.SessionService;
import services.UserViewService;
import services.VisitTimeService;
import views.html.home.FPS;
import views.html.home.index;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 *
 * @author I.A
 * @author Maurice van Veen
 */
public final class HomeController extends Controller {
	/**
	 * The amount of columns of game categories to present per row in the view.
	 */
	private static final int COLS_PER_ROW = 4;

	/**
	 * The execution context used to asynchronously perform database operations.
	 */
	private final DbExecContext dbEc;

	/**
	 * The execution context used to asynchronously perform operations.
	 */
	private final HttpExecutionContext httpEc;
	private final play.db.Database database;

	/**
	 * The {@link ProductService} to obtain product data from.
	 */
	private final ProductService products;

	private final UserViewService userViewService;
	private final VisitTimeService visitTimeService;

	/**
	 * Creates a new {@link HomeController}.
	 */
	@Inject
	public HomeController(ProductService products, UserViewService userViewService, VisitTimeService visitTimeService, DbExecContext dbEc, play.db.Database database, HttpExecutionContext httpEc) {
		this.products = products;
		this.userViewService = userViewService;
		this.visitTimeService = visitTimeService;
		this.dbEc = dbEc;
		this.database = database;
		this.httpEc = httpEc;
	}

	/**
	 * Returns a {@link Result} combined with the home view.
	 */
	public CompletionStage<Result> index() {
		updateVisit();

		Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);

		return supplyAsync(products::fetchGameCategories, dbExecutor).thenApplyAsync(i -> ok(index.render("ALL",Lists.partition(i, COLS_PER_ROW), session())), httpEc.current());
	}

	/**
	 * Returns a {@link Result} combined with the home view.
	 */
	public CompletionStage<Result> indexFPS() {
		updateVisit();

        List<GameCategory> gameCategories = new ArrayList<>();
        gameCategories.addAll(products.fetchGameCategories());
		for (int i = gameCategories.size() - 1; i >= 0; i--) {
		    if (!gameCategories.get(i).getGenre().equals("FPS")) {
		        gameCategories.remove(i);
            }
        }

		return completedFuture(ok(FPS.render("FPS",Lists.partition(gameCategories, COLS_PER_ROW), session())));
	}
	/**
	 * Returns a {@link Result} combined with the home view.
	 */
	public CompletionStage<Result> indexMMORPG() {
		updateVisit();

        List<GameCategory> gameCategories = new ArrayList<>();
        gameCategories.addAll(products.fetchGameCategories());
        for (int i = gameCategories.size() - 1; i >= 0; i--) {
            if (!gameCategories.get(i).getGenre().equals("MMORPG")) {
                gameCategories.remove(i);
            }
        }

        return completedFuture(ok(FPS.render("MMORPG",Lists.partition(gameCategories, COLS_PER_ROW), session())));
	}

	/**
	 * Returns a {@link Result} combined with the home(MOBA) view.
	 */
	public CompletionStage<Result> indexMOBA() {
		updateVisit();

        List<GameCategory> gameCategories = new ArrayList<>();
        gameCategories.addAll(products.fetchGameCategories());
        for (int i = gameCategories.size() - 1; i >= 0; i--) {
            if (!gameCategories.get(i).getGenre().equals("MOBA")) {
                gameCategories.remove(i);
            }
        }

        return completedFuture(ok(FPS.render("MOBA",Lists.partition(gameCategories, COLS_PER_ROW), session())));
	}

	/**
	 * Returns a {@link Result} combined with the home(sandbox) view.
	 */
	public CompletionStage<Result> indexsandbox() {
		updateVisit();

        List<GameCategory> gameCategories = new ArrayList<>();
        gameCategories.addAll(products.fetchGameCategories());
        for (int i = gameCategories.size() - 1; i >= 0; i--) {
            if (!gameCategories.get(i).getGenre().equals("sandbox")) {
                gameCategories.remove(i);
            }
        }

        return completedFuture(ok(FPS.render("sandbox",Lists.partition(gameCategories, COLS_PER_ROW), session())));
	}

	/**
	 * Returns a {@link Result} combined with the home(garbage) view.
	 */
	public CompletionStage<Result> indexother() {
		updateVisit();

        List<GameCategory> gameCategories = new ArrayList<>();
        gameCategories.addAll(products.fetchGameCategories());
        for (int i = gameCategories.size() - 1; i >= 0; i--) {
            if (!gameCategories.get(i).getGenre().equals("other")) {
                gameCategories.remove(i);
            }
        }

        return completedFuture(ok(FPS.render("other",Lists.partition(gameCategories, COLS_PER_ROW), session())));
	}

	private void updateVisit() {
		if (!SessionService.redirect(session(), database)) {
			String loggedInAs = SessionService.getLoggedInAs(session());
			Optional<ViewableUser> user = userViewService.fetchViewableUser(loggedInAs);
			if (user.isPresent()) {
				visitTimeService.addVisitTime(user.get().getId());
			} else {
				visitTimeService.addVisitTime(-1);
			}
		} else {
			visitTimeService.addVisitTime(-1);
		}
	}
}
