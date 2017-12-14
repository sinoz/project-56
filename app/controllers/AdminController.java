package controllers;

import com.google.common.collect.Lists;
import concurrent.DbExecContext;
import forms.ProductForm;

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

import play.libs.concurrent.HttpExecution;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.*;
import views.html.admin.*;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;

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

    private final AuthenticationService authService;
    private final AdminService adminService;

    private final MyInventoryService myInventoryService;

    /**
     * The execution context used to asynchronously perform database operations.
     */
    private final DbExecContext dbEc;

    /**
     * The execution context used to asynchronously perform operations.
     */
    private final HttpExecutionContext httpEc;

	@Inject
    public AdminController(play.db.Database database, UserViewService userViewService, ProductService productService, FormFactory formFactory, AuthenticationService authService, AdminService adminService, MyInventoryService myInventoryService, DbExecContext dbEc, HttpExecutionContext httpEc)
    {
            this.database = database;
            this.userViewService = userViewService;
            this.productService = productService;
            this.formFactory = formFactory;
            this.authService = authService;
            this.adminService = adminService;
            this.myInventoryService = myInventoryService;
            this.dbEc = dbEc;
            this.httpEc = httpEc;
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
     * The method that renders the modify user page
     */
    public Result indexModifyUser(String userid) {
        Optional<User> user = userViewService.fetchUser(Integer.valueOf(userid));
        boolean isAdmin = false;
        if (user.isPresent() && adminService.isAdmin(user.get().getId())) {
            isAdmin = true;
        }

        return ok(modifyUser.render(formFactory.form(AdminModifyUserForm.class), user, isAdmin, session()));
    }

    /**
     * The method that modifies the user
     */
    public Result modifyUser(String userid) {
        Form<AdminModifyUserForm> formBinding = formFactory.form(AdminModifyUserForm.class).bindFromRequest();
        Optional<User> user = userViewService.fetchUser(Integer.valueOf(userid));
        boolean isAdmin = false;
        if (user.isPresent() && adminService.isAdmin(user.get().getId())) {
            isAdmin = true;
        }

        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return badRequest(views.html.admin.modifyUser.render(formBinding, user, isAdmin, session()));
        } else {
            AdminModifyUserForm form = formBinding.get();

            if (adminService.userExists(form.username.toLowerCase()) && !form.username.equals(user.get().getUsername())) {
                formBinding = formBinding.withError(new ValidationError("username", "This username already exists."));
                return badRequest(views.html.admin.modifyUser.render(formBinding, user, isAdmin, session()));
            } else {
                Optional<User> admin = authService.fetchUser(session().get("loggedInAs"), form.getAdminPassword());
                if (!admin.isPresent()) {
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
     * The method that renders the delete user page
     */
    public Result indexDeleteUser(String userid) {
        Optional<User> user = userViewService.fetchUser(Integer.valueOf(userid));
        return ok(deleteUser.render(formFactory.form(AdminDeleteUserForm.class), user, session()));
    }

    /**
     * The method that deletes the user using the adminService
     */
    public Result deleteUser(String userid) {
        Form<AdminDeleteUserForm> formBinding = formFactory.form(AdminDeleteUserForm.class).bindFromRequest();
        Optional<User> user = userViewService.fetchUser(Integer.valueOf(userid));

        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return badRequest(views.html.admin.deleteUser.render(formBinding, user, session()));
        } else {
            AdminDeleteUserForm form = formBinding.get();
            Optional<User> admin = authService.fetchUser(session().get("loggedInAs"), form.getAdminPassword());
            if (!admin.isPresent()) {
                formBinding = formBinding.withError(new ValidationError("adminPassword", "Incorrect password."));
                return badRequest(views.html.admin.deleteUser.render(formBinding, user, session()));
            } else if (admin.get().getId() == Integer.valueOf(userid)) {
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
    public Result viewUser(String userid) {
        Optional<User> user = userViewService.fetchUser(Integer.valueOf(userid));
        return ok(viewUser.render(user, session()));
    }

    /**
     * The method that renders the modify product page
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

    public CompletionStage<Result> updateModifyProduct(String productid){
        Form<ProductForm> formBinding = formFactory.form(ProductForm.class).bindFromRequest();
        if(formBinding.hasGlobalErrors() || formBinding.hasErrors()){
            try {
                int id = Integer.valueOf(productid);
                Optional<Product> product = productService.fetchVisibleProduct(id);
                if (product.isPresent()) {
                    return completedFuture(badRequest(views.html.admin.productsSelected.render(formBinding, product.get(), session(), "updategameaccount")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return completedFuture(redirect("/404"));
        } else {
            if (SessionService.redirect(session(), database)) {
                return completedFuture(redirect("/login"));
            }

            String loggedInAs = SessionService.getLoggedInAs(session());
            Optional<ViewableUser> user = userViewService.fetchViewableUser(loggedInAs);
            if (!user.isPresent()) {
                return completedFuture(redirect("/login"));
            }

            try {
                int id = Integer.valueOf(productid);

                Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);
                Product product = new Product();

                ProductForm form = formBinding.get();

                product.setId(id);
                product.setTitle(form.title);
                product.setDescription(form.description);
                product.setAddedSince(new Date());
                product.setCanBuy(form.canBuy);
                product.setBuyPrice(form.buyPrice);
                product.setCanTrade(form.canTrade);
                product.setMailLast(form.emailCurrent);
                product.setMailCurrent(form.emailCurrent);
                product.setPasswordCurrent(form.passwordCurrent);

                return runAsync(() -> myInventoryService.updateProduct(product), dbExecutor)
                        .thenApplyAsync(i -> redirect("/admin/products"), httpEc.current());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return completedFuture(redirect("/404"));
        }
    }
}