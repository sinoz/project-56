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
import services.UserViewService;
import views.html.addproduct.index;

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

    private final UserViewService userViewService;

    private final DbExecContext dbEc;

    private final HttpExecutionContext httpEc;

    @Inject
    public AddProductController(FormFactory formFactory, MyInventoryService myInventoryService, UserViewService userViewService, DbExecContext dbEc, HttpExecutionContext httpEc){
        this.formFactory = formFactory;
        this.myInventoryService = myInventoryService;
        this.userViewService = userViewService;
        this.dbEc = dbEc;
        this.httpEc = httpEc;
    }
    public Result index(String gameid){
        String loggedInAs = session().get("loggedInAs");
        if(loggedInAs == null){
            return redirect("/login");
        } else {
            return ok(index.render(formFactory.form(ProductForm.class), gameid, session(), "addgameaccount"));
        }
    }

    public CompletionStage<Result> addProduct(String gameid){
        Form<ProductForm> formBinding = formFactory.form(ProductForm.class).bindFromRequest();
        if(formBinding.hasGlobalErrors() || formBinding.hasErrors()){
            return completedFuture(badRequest(index.render(formBinding, gameid, session(), "addgameaccount")));
        } else {
            String loggedInAs = session().get("loggedInAs");
            if (loggedInAs == null || loggedInAs.length() == 0) {
                return completedFuture(redirect("/login"));
            }

            Optional<ViewableUser> user = userViewService.fetchViewableUser(loggedInAs);
            if (!user.isPresent()) {
                return completedFuture(redirect("/login"));
            }

            Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);
            Product product = new Product();

            product.setUserId(user.get().getId());
            product.setGameId(Integer.valueOf(gameid));
            product.setVisible(true);
            product.setDisabled(false);
            product.setTitle(formBinding.get().title);
            product.setDescription(formBinding.get().description);
            product.setAddedSince(new Date());
            product.setCanBuy(formBinding.get().canBuy);
            product.setBuyPrice(formBinding.get().buyPrice);
            product.setCanTrade(formBinding.get().canTrade);
            product.setMailLast(formBinding.get().emailCurrent);
            product.setMailCurrent(formBinding.get().emailCurrent);
            product.setPasswordCurrent(formBinding.get().passwordCurrent);

            return runAsync(() -> myInventoryService.addProduct(product), dbExecutor).thenApplyAsync(i -> redirect("/myaccount/inventory"), httpEc.current());
        }
    }
}
