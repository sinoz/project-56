package controllers;

import com.google.common.collect.Lists;
import concurrent.DbExecContext;
import forms.GameAccountProductInfoForm;
import forms.RegistrationForm;
import models.GameCategory;
import models.Product;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.libs.concurrent.HttpExecution;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.MyInventoryService;
import services.ProductService;
import views.html.register.index;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.*;

/**
 * A {@link Controller} for the Favourites page.
 *
 * @author Maurice van Veen
 */
public final class MyInventoryController extends Controller{

    private final MyInventoryService myInventoryService;
    private final ProductService productService;

    private final FormFactory formFactory;
    /**
     * The execution context used to asynchronously perform database operations.
     */
    private final DbExecContext dbEc;
    /**
     * The execution context used to asynchronously perform operations.
     */
    private final HttpExecutionContext httpEc;

    @Inject
    public MyInventoryController(MyInventoryService myInventoryService, ProductService productService, FormFactory formFactory, DbExecContext dbEc, HttpExecutionContext httpEc){
        this.myInventoryService = myInventoryService;
        this.productService = productService;
        this.formFactory = formFactory;
        this.dbEc = dbEc;
        this.httpEc = httpEc;
    }

    public CompletionStage<Result> index(String activeSubMenuItem) {
        String loggedInAs = session().get("loggedInAs");
        if (loggedInAs == null || loggedInAs.length() == 0) {
            return completedFuture(redirect("/login"));
        } else {
            Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);
            return supplyAsync(() -> myInventoryService.getInventory(loggedInAs), dbExecutor)
                    .thenApplyAsync(ids -> myInventoryService.getProducts(ids, activeSubMenuItem), dbExecutor)
                    .thenApplyAsync(prods -> ok(views.html.inventory.index.render(Lists.partition(prods, 2), session(), activeSubMenuItem)), httpEc.current());
        }
    }

    public CompletionStage<Result> indexAll() {
        return index("all");
    }

    public CompletionStage<Result> indexSelling() {
        return index("selling");
    }

    public CompletionStage<Result> indexTrading() {
        return index("trading");
    }

    public CompletionStage<Result> indexGame() {
        String loggedInAs = session().get("loggedInAs");
        if (loggedInAs == null || loggedInAs.length() == 0) {
            return completedFuture(redirect("/login"));
        } else {
            Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);
            return supplyAsync(productService::fetchGameCategories, dbExecutor).thenApplyAsync(gameCategories -> ok(views.html.inventory.game.render(Lists.partition(filterGameCategories(loggedInAs, gameCategories), 4), session(), "pergame")), httpEc.current());
        }
    }

    private List<GameCategory> filterGameCategories(String username, List<GameCategory> gameCategories) {
        List<GameCategory> out = new ArrayList<>();
        out.addAll(gameCategories);

        List<Integer> productIds = myInventoryService.getInventory(username);
        List<Product> products = myInventoryService.getProducts(productIds, null);

        for (int i = 0; i < out.size(); i++) {
            GameCategory gameCategory = out.get(i);

            boolean contains = false;
            for (Product product : products) {
                if (product.getGameId() == gameCategory.getId()) {
                    contains = true;
                    break;
                }
            }

            if (!contains) {
                out.remove(i);
                i--;
            }
        }

        return out;
    }

    public CompletionStage<Result> indexGameId(String id) {
        String loggedInAs = session().get("loggedInAs");
        if (loggedInAs == null || loggedInAs.length() == 0) {
            return completedFuture(redirect("/login"));
        }
        try {
            int ID = Integer.valueOf(id);

            return completedFuture(ok(views.html.inventory.index.render(Lists.partition(getProductsPerGameCategory(loggedInAs, ID), 2), session(), "pergame")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return completedFuture(redirect("/404"));
    }

    private List<Product> getProductsPerGameCategory(String username, int gameID) {
        List<Integer> productIds = myInventoryService.getInventory(username);
        List<Product> products = myInventoryService.getProducts(productIds, null);

        List<Product> out = new ArrayList<>();
        out.addAll(products);

        for (int i = 0; i < out.size(); i++) {
            if (out.get(i).getGameId() != gameID) {
                out.remove(i);
                i--;
            }
        }

        return out;
    }

    public CompletionStage<Result> indexProductDetails(String id) {
        String loggedInAs = session().get("loggedInAs");
        if (loggedInAs == null || loggedInAs.length() == 0) {
            return completedFuture(redirect("/login"));
        }
        try {
            int ID = Integer.valueOf(id);

            Optional<Product> product = productService.fetchProduct(ID);
            if (product.isPresent())
                return completedFuture(ok(views.html.inventory.details.render(product.get(), formFactory.form(GameAccountProductInfoForm.class), session(), "")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return completedFuture(redirect("/404"));
    }

    /**
     * Attempts to register a user. Returns either a {@link Controller#badRequest()} indicating
     * a failure in registering the user or a {@link Controller#ok()} result, indicating a successful
     * registration.
     */
    public Result updateProductInfo(String id) {
        Form<GameAccountProductInfoForm> formBinding = formFactory.form(GameAccountProductInfoForm.class).bindFromRequest();
        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return redirect("/404");
        } else {
            GameAccountProductInfoForm form = formBinding.get();

            // TODO: update description here
            System.out.println(form.getDescription());
            return redirect("/myaccount/inventory/details/" + id);
        }
    }

//    /**
//     * The method that adds/deletes a game to a users favourites
//     */
//    public CompletionStage<Result> addFavourite() {
//        Form<FavouriteForm> formBinding = formFactory.form(FavouriteForm.class).bindFromRequest();
//
//        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
//            return completedFuture(badRequest());
//        } else {
//            Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);
//            FavouriteForm form = formBinding.get();
//            String prodId = form.getId();
//
//            return runAsync(() -> myInventoryService.add(prodId, session().get("loggedInAs")), dbExecutor).thenApplyAsync(i -> redirect("/products/selected/" + prodId), httpEc.current());
//        }
//    }

    public CompletionStage<Result> indexAddGameAccount() {
        String loggedInAs = session().get("loggedInAs");
        if (loggedInAs == null || loggedInAs.length() == 0) {
            return completedFuture(redirect("/login"));
        } else {
            Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);
            return supplyAsync(productService::fetchGameCategories, dbExecutor).thenApplyAsync(gameCategories -> ok(views.html.inventory.addgameaccount.render(Lists.partition(gameCategories, 4), session(), "addgameaccount")), httpEc.current());
        }
    }
}
