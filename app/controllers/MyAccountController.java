package controllers;

import models.Order;
import models.Product;
import models.ViewableUser;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;
import services.OrderService;
import services.ProductService;
import services.SessionService;
import services.UserViewService;

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

	@Inject
    public MyAccountController(FormFactory formFactory, play.db.Database database, OrderService orderService, UserViewService userViewService)
	{
	    this.database = database;
	    this.formFactory = formFactory;
		this.userViewService = userViewService;
	    this.orderService = orderService;

    }

	public Result index() {
		if (SessionService.redirect(session(), database)) {
			return redirect("/login");
		} else {
			String loggedInAs = SessionService.getLoggedInAs(session());
			Optional<ViewableUser> user = userViewService.fetchViewableUser(loggedInAs);
			if (user.isPresent())
			{
				List<Order> orders = orderService.getOrdersByUser(user.get().getId());
				for (int i = orders.size() - 1; i >= 0; i--) {
					if (orders.get(i).getOrderType() != 0)
						orders.remove(i);
				}
				return ok(views.html.myaccount.index.render(session(), user.get(), orders));
			}else {
				return redirect("/404");
			}
		}
	}
}