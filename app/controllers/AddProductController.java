package controllers;

import concurrent.DbExecContext;
import forms.ProductForm;
import models.Product;
import models.ViewableUser;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecution;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.MyInventoryService;
import services.ProductService;
import services.SessionService;
import services.UserViewService;
import views.html.addproduct.index;
import views.html.addproduct.update;

import javax.inject.Inject;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * A {@link Controller} for the Add Product page
 *
 * @author Johan van der Hoeven
 */
public class AddProductController extends Controller {
    /**
     * A {@link FormFactory} to use forms.
     */
    private final FormFactory formFactory;

    private final MyInventoryService myInventoryService;

    private final ProductService productService;

    private final UserViewService userViewService;
    /**
     * The execution context used to asynchronously perform database operations.
     */
    private final DbExecContext dbEc;
    /**
     * The execution context used to asynchronously perform operations.
     */
    private final HttpExecutionContext httpEc;

    @Inject
    public AddProductController(FormFactory formFactory, MyInventoryService myInventoryService, ProductService productService, UserViewService userViewService, DbExecContext dbEc, HttpExecutionContext httpEc){
        this.formFactory = formFactory;
        this.myInventoryService = myInventoryService;
        this.productService = productService;
        this.userViewService = userViewService;
        this.dbEc = dbEc;
        this.httpEc = httpEc;
    }

    public Result index(String gameid){
        if (SessionService.redirect(session())) {
            return redirect("/login");
        } else {
            return ok(index.render(formFactory.form(ProductForm.class), gameid, session(), "addgameaccount"));
        }
    }

    public Result indexUpdateProduct(String gameid){
        if (SessionService.redirect(session())) {
            return redirect("/login");
        } else {
            try {
                int id = Integer.valueOf(gameid);

                Form<ProductForm> form = formFactory.form(ProductForm.class);

                Optional<Product> product = productService.fetchProduct(id);
                if (product.isPresent()) {
                    return ok(update.render(form, product.get(), session(), "updategameaccount"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return redirect("/404");
        }
    }

    public CompletionStage<Result> addProduct(String gameid){
        Form<ProductForm> formBinding = formFactory.form(ProductForm.class).bindFromRequest();
        if(formBinding.hasGlobalErrors() || formBinding.hasErrors()){
            return completedFuture(badRequest(index.render(formBinding, gameid, session(), "addgameaccount")));
        } else {
            if (SessionService.redirect(session())) {
                return completedFuture(redirect("/login"));
            }

            String loggedInAs = SessionService.getLoggedInAs(session());
            Optional<ViewableUser> user = userViewService.fetchViewableUser(loggedInAs);
            if (!user.isPresent()) {
                return completedFuture(redirect("/login"));
            }

            Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);
            Product product = new Product();

            ProductForm productForm = formBinding.get();

            product.setUserId(user.get().getId());
            product.setGameId(Integer.valueOf(gameid));
            product.setVisible(true);
            product.setDisabled(false);
            product.setTitle(productForm.title);
            product.setDescription(productForm.description);
            product.setAddedSince(new Date());
            product.setCanBuy(productForm.canBuy);
            product.setBuyPrice(productForm.buyPrice);
            product.setCanTrade(productForm.canTrade);
            product.setMailLast(productForm.emailCurrent);
            product.setMailCurrent(productForm.emailCurrent);
            product.setPasswordCurrent(productForm.passwordCurrent);

            return runAsync(() -> myInventoryService.addProduct(product), dbExecutor).thenApplyAsync(i -> redirect("/myaccount/inventory"), httpEc.current());
        }
    }

    public CompletionStage<Result> updateProduct(String productid){
        Form<ProductForm> formBinding = formFactory.form(ProductForm.class).bindFromRequest();
        if(formBinding.hasGlobalErrors() || formBinding.hasErrors()){
            try {
                int id = Integer.valueOf(productid);
                Optional<Product> product = productService.fetchProduct(id);
                if (product.isPresent()) {
                    return completedFuture(badRequest(update.render(formBinding, product.get(), session(), "updategameaccount")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return completedFuture(redirect("/404"));
        } else {
            if (SessionService.redirect(session())) {
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

                return runAsync(() -> myInventoryService.updateProduct(product), dbExecutor).thenApplyAsync(i -> redirect("/myaccount/inventory"), httpEc.current());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return completedFuture(redirect("/404"));
        }
    }
}
