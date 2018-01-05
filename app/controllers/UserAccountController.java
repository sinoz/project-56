package controllers;

import concurrent.DbExecContext;
import models.Product;
import models.Review;
import models.ViewableUser;
import play.libs.concurrent.HttpExecution;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.ProductService;
import services.UserViewService;
import views.html.useraccount.empty;
import views.html.useraccount.index;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A {@link Controller} for showing user accounts.
 *
 * @author Johan van der Hoeven
 * @author Maurice van Veen
 */
public final class UserAccountController extends Controller {

	/**
	 * The execution context used to asynchronously perform database operations.
	 */
	private final DbExecContext dbEc;
	/**
	 * The execution context used to asynchronously perform operations.
	 */
	private final HttpExecutionContext httpEc;

	/**
	 * The {@link services.UserViewService} to obtain data from.
	 */
	private UserViewService userViewService;

	/**
	 * The {@link services.ProductService} to obtain product data from.
	 */
	private ProductService productService;

	/**
	 * Creates a new {@link UserAccountController}.
	 */
	@Inject
	public UserAccountController(UserViewService userViewService, ProductService productService, DbExecContext dbEc, HttpExecutionContext httpEc) {
		this.userViewService = userViewService;
		this.productService = productService;
		this.dbEc = dbEc;
		this.httpEc = httpEc;
	}

	/**
	 * Returns a {@link Result} combined with a user account page.
	 */
	public CompletionStage<Result> index(String username) {
		Optional<ViewableUser> user = userViewService.fetchViewableUser(username);
		CompletableFuture<List<List<Product>>> productsFetch;
		CompletableFuture<List<List<Review>>> reviewFetch;

		// If the user does not exist render "User Not Found" page
		if (!user.isPresent()) return CompletableFuture.completedFuture(ok(empty.render(session())));

		// Otherwise get inventory and reviews for this user
		Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);

		productsFetch = supplyAsync(() -> productService.fetchUserProducts(user.get().getId()), dbExecutor);
		reviewFetch = supplyAsync(() -> userViewService.getUserReviews(user.get().getId()), dbExecutor);

		return productsFetch.thenCombineAsync(reviewFetch, (products, reviews) -> ok(index.render(user.get(), products, reviews, session())), httpEc.current());
	}
}