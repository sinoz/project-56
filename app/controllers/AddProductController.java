package controllers;

import concurrent.DbExecContext;
import forms.ProductForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.ProductService;
import services.UserViewService;
import views.html.addproduct.index;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

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

    private final ProductService productService;

    private final UserViewService userViewService;

    private final DbExecContext dbEc;

    private final HttpExecutionContext httpEc;

    @Inject
    public AddProductController(FormFactory formFactory, ProductService productService, UserViewService userViewService, DbExecContext dbEc, HttpExecutionContext httpEc){
        this.formFactory = formFactory;
        this.productService = productService;
        this.userViewService = userViewService;
        this.dbEc = dbEc;
        this.httpEc = httpEc;
    }
    public Result index(){
        String loggedInAs = session().get("loggedInAs");
        if(loggedInAs == null){
            return redirect("/login");
        } else {
            return ok(index.render(formFactory.form(ProductForm.class), session()));
        }
    }

    public CompletionStage<Result> addProduct(){
        Form<ProductForm> formBinding = formFactory.form(ProductForm.class).bindFromRequest();
        if(formBinding.hasGlobalErrors() || formBinding.hasErrors()){
            return completedFuture(badRequest(index.render(formBinding, session())));
        } else {
            return completedFuture(ok());
        }
    }
}
