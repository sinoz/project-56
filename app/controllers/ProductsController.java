package controllers;

import com.google.common.collect.Lists;
import forms.SearchForm;
import models.GameCategory;
import models.Product;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import services.ProductService;
import services.SearchService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * A {@link Controller} that handles searches.
 *
 * @author Maurice van Veen
 * @author Johan van der Hoeven
 */
public class ProductsController extends Controller {

    /**
     * A {@link FormFactory} to use search forms.
     */
    private FormFactory formFactory;

    /**
     * The {@link services.ProductService} to obtain product data from.
     */
    private ProductService productService;

    /**
     * The {@link services.SearchService} to search products.
     */
    private SearchService searchService;

    @Inject
    public ProductsController(FormFactory formFactory, ProductService productService) {
        this.formFactory = formFactory;
        this.productService = productService;
        this.searchService = new SearchService(productService);
    }

    public Result redirect() {
        Form<SearchForm> formBinding = formFactory.form(SearchForm.class).bindFromRequest();
        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return badRequest();
        } else {
            SearchForm form = formBinding.get();
            String formInput = form.getInput();
            return redirect("/products/input=" + formInput);
        }
    }

    @Inject
    public static Form<SearchForm> getSearchForm(FormFactory formFactory) {
        return formFactory.form(SearchForm.class);
    }

    public Result index(String token) {
        String[] split = token.split("&");
        token = split[0];
        String filters = null;
        if (split.length > 1) {
            filters = split[split.length - 1];
            if (split.length > 2)
                return redirect(token + "&" + filters);
        }

        SearchService.FilterPrices prices = searchService.filterToken(filters);
        if (token.startsWith("game=")) {
            return indexGame(token, filters, prices);
        } else if (token.startsWith("input=")) {
            return indexInput(token, filters, prices);
        }
        return ok(views.html.selectedproduct.index.render(token, "", session()));
    }

    private Result indexGame(String token, String filters, SearchService.FilterPrices prices) {
        token = token.replaceFirst("game=", "").replace("_", " ");
        Optional<GameCategory> gameCategory = productService.fetchGameCategory(token);
        if (gameCategory.isPresent()) {
            List<Product> products = productService.fetchProducts(gameCategory.get());
            try {
                products = searchService.filterProducts(products, filters, prices);
            } catch (Exception e) {
                return redirect("/404");
            }

            String input = gameCategory.get().getName();

            if (products.size() > 0)
                return ok(views.html.products.game.render(gameCategory.get(), Lists.partition(products, 2), session(), input, null, prices.getMin(), prices.getMax()));
            else
                return ok(views.html.products.gameError.render(gameCategory.get(), input, null, session()));
        } else {
            return redirect("/404");
        }
    }

    private Result indexInput(String token, String filters, SearchService.FilterPrices prices) {
        token = token.replaceFirst("input=", "");
        SearchService.SearchResults searchResults = searchService.processInput(token, filters);

        String input = token;

        if (searchResults.getProducts().size() > 0)
            if (searchResults.getSelectedGameCategory() != null)
                return ok(views.html.products.game.render(searchResults.getSelectedGameCategory(), Lists.partition(searchResults.getProducts(), 2), session(), input, searchResults.getMessage(), prices.getMin(), prices.getMax()));
            else
                return ok(views.html.products.products.render(Lists.partition(searchResults.getProducts(), 2), session(), input, searchResults.getMessage(), prices.getMin(), prices.getMax()));
        else if (searchResults.getSelectedGameCategory() != null)
            return ok(views.html.products.gameError.render(searchResults.getSelectedGameCategory(), input, searchResults.getMessage(), session()));
        else
            return ok(views.html.selectedproduct.index.render(token, input, session()));
    }
}