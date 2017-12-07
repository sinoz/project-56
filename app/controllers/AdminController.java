package controllers;

import com.google.common.collect.Lists;
import forms.AdminModifyUserForm;
import forms.GameCategoryForm;
import models.GameCategory;
import models.Product;
import models.User;
import models.ViewableUser;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;
import services.*;
import views.html.admin.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * A {@link Controller} for the Admin page.
 *
 * @author Johan van der Hoeven
 * @author Maurice van Veen
 */
public final class AdminController extends Controller {

	/**
	 * The required {@link Database} dependency to fetch database connections.
	 */
	private final Database database;

    /**
     * The {@link services.UserViewService} to obtain data from.
     */
    private final UserViewService userViewService;

    private final ProductService productService;

    /**
     * The required {@link FormFactory} to produce forms for modifying a user's personal settings.
     */
    private final FormFactory formFactory;

    private final AuthenticationService authService;

    private final AdminService adminService;

	@Inject
    public AdminController(Database database, UserViewService userViewService, ProductService productService, FormFactory formFactory, AuthenticationService authService, AdminService adminService) {
	    this.database = database;
	    this.userViewService = userViewService;
	    this.productService = productService;
	    this.formFactory = formFactory;
	    this.authService = authService;
	    this.adminService = adminService;
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

    public Result indexGameCategorySelected(String id) {
	    try {
	        int gameId = Integer.valueOf(id);
            Optional<GameCategory> gc = productService.fetchGameCategory(gameId);
            if (gc.isPresent())
                return ok(gamecategorySelected.render(formFactory.form(GameCategoryForm.class), session(), gc.get()));
        } catch (Exception e) {
	        e.printStackTrace();
        }
        return indexGameCategories();
    }

    public Result updateGameCategorySelected(String id) {
        Form<GameCategoryForm> formBinding = formFactory.form(GameCategoryForm.class).bindFromRequest();
        try {
            int gameId = Integer.valueOf(id);
            Optional<GameCategory> gc = productService.fetchGameCategory(gameId);
            if (gc.isPresent()) {
                if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
                    return badRequest(gamecategorySelected.render(formBinding, session(), gc.get()));
                } else {
                    GameCategoryForm form = formBinding.get();
                    String title = form.getName();
                    String description = form.getDescription();
                    productService.updateGameCategory(gameId, title, description);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	    return redirect("/admin/gamecategories");
    }

//    public Result deleteGameCategorySelected(String id) {
//        try {
//            int gameId = Integer.valueOf(id);
//            Optional<GameCategory> gc = productService.fetchGameCategory(gameId);
//            if (gc.isPresent()) {
//                productService.deleteGameCategory(gameId);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return redirect("/admin/gamecategories");
//    }

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
        boolean isAdmin = false;
        if(user.isPresent() && adminService.isAdmin(user.get().getId())){
            isAdmin = true;
        }

        return ok(modifyUser.render(formFactory.form(AdminModifyUserForm.class), user, isAdmin, session()));
    }

    /**
     * The method that modifies the user
     */
    public Result modifyUser(String userid){
        Form<AdminModifyUserForm> formBinding = formFactory.form(AdminModifyUserForm.class).bindFromRequest();
        Optional<User> user = userViewService.fetchUser(Integer.valueOf(userid));
        boolean isAdmin = false;
        if(user.isPresent() && adminService.isAdmin(user.get().getId())){
            isAdmin = true;
        }

        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return badRequest(views.html.admin.modifyUser.render(formBinding, user, isAdmin, session()));
        } else {
            AdminModifyUserForm form = formBinding.get();

            if(adminService.userExists(form.username.toLowerCase())){
                formBinding = formBinding.withError(new ValidationError("username", "This username already exists."));
                return badRequest(views.html.admin.modifyUser.render(formBinding, user, isAdmin, session()));
            } else {
                Optional<User> admin = authService.fetchUser(session().get("loggedInAs"), form.getAdminPassword());
                if(!admin.isPresent()) {
                    formBinding = formBinding.withError(new ValidationError("adminPassword", "Incorrect password."));
                    return badRequest(views.html.admin.modifyUser.render(formBinding, user, isAdmin, session()));
                } else {
                    adminService.updateSettings(Integer.valueOf(userid), form);
                    return redirect("/admin/users/modify/" + userid);
                }
            }
        }
    }

    /**
     * The method that renders the view user page
     */
    public Result viewUser(String userid){
        Optional<User> user = userViewService.fetchUser(Integer.valueOf(userid));
        return ok(viewUser.render(user, session()));
    }
}