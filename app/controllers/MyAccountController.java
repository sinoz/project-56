package controllers;

import com.google.common.collect.Lists;
import models.Order;
import models.Product;
import models.User;
import models.ViewableUser;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;
import services.OrderService;
import services.ProductService;
import services.SessionService;
import services.UserViewService;
import views.html.myaccount.index;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * A {@link Controller} for the MyAccount page.
 *
 * @author Melle Nout
 * @author I.A
 * @author Joris Stander
 */
public final class MyAccountController extends Controller {

	/**
	 * A {@link FormFactory} to use search forms.
	 */
	private FormFactory formFactory;
	/**
	 * The {@link services.ProductService} to obtain product data from.
	 */
	private OrderService orderService;
	/**
	 * The required {@link Database} dependency to fetch database connections.
	 */
	private final play.db.Database database;

	/**
	 * The {@link UserViewService} to obtain data from.
	 */
	private UserViewService userViewService;

	private final ProductService productService;

	@Inject
    public MyAccountController(FormFactory formFactory, play.db.Database database, OrderService orderService, UserViewService userViewService, ProductService productService)
	{
	    this.database = database;
	    this.formFactory = formFactory;
		this.userViewService = userViewService;
	    this.orderService = orderService;
	    this.productService = productService;

    }

	public Result index() {
		if (SessionService.redirect(session(), database)) {
			return redirect("/login");
		} else {
			String loggedInAs = SessionService.getLoggedInAs(session());
			Optional<ViewableUser> user = userViewService.fetchViewableUser(loggedInAs);
			if (user.isPresent())
			{
				List<Order> order = orderService.getOrderByUser(user.get().getId());
				List<Product> product = productService.fetchProducts();
				return ok(views.html.myaccount.index.render(session(), user.get(), order, product));
			}else {
				return redirect("/404");
			}
		}
	}
}