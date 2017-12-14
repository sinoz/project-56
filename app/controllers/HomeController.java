package controllers;

import com.google.common.collect.Lists;
import concurrent.DbExecContext;
import models.ViewableUser;
import models.VisitTime;
import play.libs.concurrent.HttpExecution;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.ProductService;
import services.SessionService;
import services.UserViewService;
import services.VisitTimeService;
import views.html.home.index;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

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

		Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);

		return supplyAsync(products::fetchGameCategories, dbExecutor).thenApplyAsync(i -> ok(index.render(Lists.partition(i, COLS_PER_ROW), session())), httpEc.current());
	}
}
