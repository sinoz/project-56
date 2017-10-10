package controllers;

import com.google.common.collect.Lists;
import concurrent.DbExecContext;
import play.libs.concurrent.HttpExecution;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.ProductService;
import views.html.home.index;

import javax.inject.Inject;
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
	 * The {@link ProductService} to obtain product data from.
	 */
	private ProductService products;

	/**
	 * The execution context used to asynchronously perform database operations.
	 */
	private final DbExecContext dbEc;

	/**
	 * TODO
	 */
	private final HttpExecutionContext httpEc;

	/**
	 * Creates a new {@link HomeController}.
	 */
	@Inject
	public HomeController(ProductService products, DbExecContext dbEc, HttpExecutionContext httpEc) {
		this.products = products;
		this.dbEc = dbEc;
		this.httpEc = httpEc;
	}

	/**
	 * Returns a {@link Result} combined with the home view.
	 */
	public CompletionStage<Result> index() {
		Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);

		return supplyAsync(() -> products.getGameCategories(), dbExecutor)
				.thenApplyAsync(i -> ok(index.render(Lists.partition(i, COLS_PER_ROW), session())), httpEc.current());
	}
}
