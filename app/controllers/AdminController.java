package controllers;

import com.google.common.collect.Lists;
import forms.AdminDeleteUserForm;
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
	    return adminRedirect(ok(index.render(session())));
	}

    public Result indexUsers() {
	    List<ViewableUser> u = userViewService.fetchViewableUsers();
        return adminRedirect(ok(users.render(session(), u)));
    }

    public Result indexGameCategories() {
        List<GameCategory> gc = productService.fetchGameCategories();
        return adminRedirect(ok(gamecategories.render(session(), Lists.partition(gc, 4))));
    }

    public Result indexGameCategorySelected(String id) {
	    try {
	        int gameId = Integer.valueOf(id);
            Optional<GameCategory> gc = productService.fetchGameCategory(gameId);
            if (gc.isPresent())
                return adminRedirect(ok(gamecategorySelected.render(formFactory.form(GameCategoryForm.class), session(), gc.get())));
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
                    return adminRedirect(badRequest(gamecategorySelected.render(formBinding, session(), gc.get())));
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
//        return adminRedirect("/admin/gamecategories");
//    }

    public Result indexProducts() {
        List<Product> p = productService.fetchProducts();
        return adminRedirect(ok(products.render(session(), p)));
    }

    public Result indexStatistics() {
        return adminRedirect(ok(statistics.render(session())));
    }

    private Result adminRedirect(Result result) {
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

        return adminRedirect(ok(modifyUser.render(formFactory.form(AdminModifyUserForm.class), user, isAdmin, session())));
    }

    /**
     * The method that modifies the user
     */
    public Result modifyUser(String userid){
        //Check if userid is a valid user id
        int id;
        try {
            id = Integer.valueOf(userid);
        } catch (Exception e) {
            return adminRedirect(redirect("/404"));
        }

        Form<AdminModifyUserForm> formBinding = formFactory.form(AdminModifyUserForm.class).bindFromRequest();
        Optional<User> user = userViewService.fetchUser(id);

        //Check if user could be loaded from database
        if (!user.isPresent())
            return adminRedirect(redirect("/404"));

        boolean isAdmin = adminService.isAdmin(user.get().getId());

        //Check for form errors
        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return adminRedirect(badRequest(views.html.admin.modifyUser.render(formBinding, user, isAdmin, session())));
        } else {
            AdminModifyUserForm form = formBinding.get();
            String loggedInAs = SessionService.getLoggedInAs(session());

            // Check if user filled in new username and whether new username already exists
            if(!user.get().getUsername().equalsIgnoreCase(form.username) && adminService.userExists(form.username.toLowerCase())){
                formBinding = formBinding.withError(new ValidationError("username", "This username already exists."));
                return adminRedirect(badRequest(views.html.admin.modifyUser.render(formBinding, user, isAdmin, session())));
                //Check if admin is removing admin rights from his own account
            } else if(user.get().getUsername().toLowerCase().equals(loggedInAs) && !form.isAdmin){
                formBinding = formBinding.withError(new ValidationError("isAdmin", "You can not remove admin rights from your own account."));
                return adminRedirect(badRequest(views.html.admin.modifyUser.render(formBinding, user, isAdmin, session())));
            } else {
                Optional<User> admin = authService.fetchUser(loggedInAs, form.getAdminPassword());
                //Check if admin password is correct
                if(!admin.isPresent()) {
                    formBinding = formBinding.withError(new ValidationError("adminPassword", "Incorrect password."));
                    return adminRedirect(badRequest(views.html.admin.modifyUser.render(formBinding, user, isAdmin, session())));
                    //Modify user
                } else {
                    adminService.updateSettings(id, form);
                    return adminRedirect(redirect("/admin/users"));
                }
            }
        }
    }

    /**
     * The method that renders the delete user page
     */
    public Result indexDeleteUser(String userid){
        Optional<User> user = userViewService.fetchUser(Integer.valueOf(userid));
        return ok(deleteUser.render(formFactory.form(AdminDeleteUserForm.class), user, session()));
    }

    /**
     * The method that deletes the user using the adminService
     */
    public Result deleteUser(String userid){
        Form<AdminDeleteUserForm> formBinding = formFactory.form(AdminDeleteUserForm.class).bindFromRequest();
        Optional<User> user = userViewService.fetchUser(Integer.valueOf(userid));

        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return badRequest(views.html.admin.deleteUser.render(formBinding, user, session()));
        } else {
            AdminDeleteUserForm form = formBinding.get();
            Optional<User> admin = authService.fetchUser(session().get("loggedInAs"), form.getAdminPassword());
            if(!admin.isPresent()) {
                formBinding = formBinding.withError(new ValidationError("adminPassword", "Incorrect password."));
                return badRequest(views.html.admin.deleteUser.render(formBinding, user, session()));
            } else if(admin.get().getId() == Integer.valueOf(userid)) {
                formBinding = formBinding.withError(new ValidationError("adminPassword", "You can not delete your own account."));
                return badRequest(views.html.admin.deleteUser.render(formBinding, user, session()));
            } else {
                adminService.deleteUser(Integer.valueOf(userid));
                return redirect("/admin/users");
            }
        }
    }

    /**
     * The method that renders the view user page
     */
    public Result viewUser(String userid){
        int id;
        try {
            id = Integer.valueOf(userid);
        } catch (Exception e) {
            return adminRedirect(redirect("/404"));
        }

        Optional<User> user = userViewService.fetchUser(id);
        return adminRedirect(ok(viewUser.render(user, session())));
    }
}