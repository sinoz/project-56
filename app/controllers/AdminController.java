package controllers;

import com.google.common.collect.Lists;
import forms.ProductForm;
import models.GameCategory;
import models.Product;
import models.User;
import models.ViewableUser;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import services.ProductService;
import services.SessionService;
import services.UserViewService;
import views.html.admin.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * A {@link Controller} for the Admin page.
 *
 * @author Johan van der Hoeven
 * @author Maurice van Veen
 * @author Melle Nout
 */
public final class AdminController extends Controller {

	/**
	 * The required {@link Database} dependency to fetch database connections.
	 */
	private final play.db.Database database;

    /**
     * The {@link services.UserViewService} to obtain data from.
     */
    private final UserViewService userViewService;

    private final ProductService productService;

    private final FormFactory formFactory;


    @Inject
    public AdminController(play.db.Database database, UserViewService userViewService, ProductService productService, FormFactory formFactory) {
	    this.database = database;
	    this.userViewService = userViewService;
	    this.productService = productService;
	    this.formFactory = formFactory;
    }

	public Result index() {
	    return redirect(ok(index.render(session())));
	}

    public Result indexUsers() {
	    List<ViewableUser> u = userViewService.fetchViewableUsers();
        return redirect(ok(users.render(session(), u)));
    }

    public Result indexGameCategories() {
        List<GameCategory> gc = productService.fetchGameCategories();
        return redirect(ok(gamecategories.render(session(), Lists.partition(gc, 4))));
    }

    public Result indexProducts() {
        List<Product> p = productService.fetchProducts();
        return redirect(ok(products.render(session(), p)));
    }

    public Result indexStatistics() {
        return redirect(ok(statistics.render(session())));
    }

    private Result redirect(Result result) {
	    return SessionService.redirectAdmin(session(), database) ? redirect("/") : result;
    }

    /**
     *  The method that renders the modify user page
     */
    public Result indexModifyUser(String userid){
        Optional<User> user = userViewService.fetchUser(Integer.valueOf(userid));
        return ok(modifyUser.render(session(), user));
    }

    /**
     *  The method that renders the modify product page
     */
    public Result indexModifyProduct(String productid) {
        if (SessionService.redirect(session(), database)) {
            return redirect("/login");
        } else {
            try {
                int id = Integer.valueOf(productid);

                Form<ProductForm> form = formFactory.form(ProductForm.class);

                String loggedInAs = SessionService.getLoggedInAs(session());
                Optional<ViewableUser> user = userViewService.fetchViewableUser(loggedInAs);
                Optional<Product> product = productService.fetchVisibleProduct(id);

                if (product.isPresent() && user.isPresent() && product.get().getUserId() == user.get().getId()) {
                    return ok(views.html.admin.productsSelected.render(form, product.get(), session(), "updategameaccount"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return redirect("/404");
        }
    }
}