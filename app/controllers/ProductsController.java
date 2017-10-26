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
import services.UserViewService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * A {@link Controller} that handles searches.
 *
 * @author: Maurice van Veen
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

    @Inject
    public ProductsController(FormFactory formFactory, ProductService productService) {
        this.formFactory = formFactory;
        this.productService = productService;
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

        if (token.startsWith("game=")) {
            token = token.replaceFirst("game=", "").replace("_", " ");
            Optional<GameCategory> gameCategory = productService.fetchGameCategory(token);
            if (gameCategory.isPresent()) {
                List<Product> products = productService.fetchProducts(gameCategory.get());
                try {
                    products = filterProducts(products, filters);
                } catch (Exception e) {
                    return redirect("/404");
                }

                if (products.size() > 0)
                    return ok(views.html.products.game.render(gameCategory.get(), Lists.partition(products, 2), session()));
                else
                    return ok(views.html.products.gameError.render(gameCategory.get(), session()));
            } else {
                return redirect("/404");
            }
        } else if (token.startsWith("input=")) {
            token = token.replaceFirst("input=", "");
            return processInput(token, filters);
        }
        return ok(views.html.selectedproduct.index.render(token, session()));
    }

    private List<Product> filterProducts(List<Product> products, String filters) throws Exception {
        List<Product> list = new ArrayList<>();

        // TODO: change when more filters needed
        if (filters != null) {
            filters = filters.replace("filter=maxprice:", "");
            if (filters.length() > 0) {
                double buyPrice = Double.valueOf(filters);

                for (Product product : products) {
                    if (product.getBuyPrice() <= buyPrice) {
                        list.add(product);
                    }
                }

                return list;
            }
        }

        return products;
    }

    private Result processInput(String token, String filters) {
        List<GameCategory> gameCategories = productService.fetchGameCategories();
        List<Product> products = productService.fetchProducts();
        GameCategory selectedGameCategory = null;

        /**
         * Search for game categories
         */
        if (gameCategories.size() > 0) {
            HashMap<GameCategory, Integer> scores = new HashMap<>();
            for (GameCategory gameCategory : gameCategories)
                scores.put(gameCategory, 0);

            String[] split = token.split(" ");
            for (String s : split) {
                StringBuilder search = new StringBuilder(s);
                double scorePenalty = 1;
                while (search.length() > 1) {
                    for (GameCategory gameCategory : gameCategories) {
                        int score = 0;

                        String name = gameCategory.getName();
                        if (search.toString().toLowerCase().equals(name.toLowerCase()))
                            score += 100 / scorePenalty;
                        if (name.toLowerCase().contains(search.toString().toLowerCase()))
                            score += 100 / scorePenalty;

                        scores.put(gameCategory, scores.get(gameCategory) + score);
                    }
                    scorePenalty++;
                    search.delete(search.length() - 1, search.length());
                }
            }

            HashMap<Product, Integer> productScores = processProductScores(token, products);
            for (GameCategory gameCategory : gameCategories) {
                int score = 0;
                int cnt = 0;
                for (Product product : products) {
                    if (product.getGameId() == gameCategory.getId()) {
                        if (productScores.get(product) > 0) {
                            score += productScores.get(product);
                            cnt++;
                        }
                    }
                }
                score = (int) (score * 10 / (double) cnt);
                scores.put(gameCategory, scores.get(gameCategory) + score);
            }

            int max = 0;
            for (GameCategory gameCategory : gameCategories) {
                int score = scores.get(gameCategory);
                if (max < score)
                    max = score;
            }

            List<GameCategory> sortedGameCategories = new ArrayList<>();
            List<Integer> sortedScores = new ArrayList<>();
            while (scores.size() > 0 && max > 0) {
                for (GameCategory gameCategory : gameCategories) {
                    if (scores.containsKey(gameCategory) && scores.get(gameCategory) == max) {
                        sortedGameCategories.add(gameCategory);
                        sortedScores.add(max);
                        scores.remove(gameCategory);
                    }
                }
                max--;
            }

            if (sortedGameCategories.size() > 0) {
                max = sortedScores.get(0);
                List<GameCategory> selectedGameCategories = new ArrayList<>();
                for (int i = 0; i < sortedGameCategories.size(); i++) {
                    GameCategory gameCategory = sortedGameCategories.get(i);
                    if (sortedScores.get(i) == max) {
                        selectedGameCategories.add(gameCategory);
                        continue;
                    }
                    break;
                }

                if (selectedGameCategories.size() == 1) {
                    selectedGameCategory = selectedGameCategories.get(0);
                    List<Product> selectedProducts = new ArrayList<>();
                    for (Product product : products) {
                        if (product.getGameId() == selectedGameCategory.getId()) {
                            selectedProducts.add(product);
                        }
                    }
                    products = selectedProducts;
                } else {
                    List<Product> selectedProducts = new ArrayList<>();
                    for (GameCategory gameCategory : selectedGameCategories) {
                        for (Product product : products) {
                            if (product.getGameId() == gameCategory.getId()) {
                                selectedProducts.add(product);
                            }
                        }
                    }
                    products = selectedProducts;
                }
            }
        }

        /**
         * Search for products
         */
        if (products.size() > 0) {
            HashMap<Product, Integer> scores = processProductScores(token, products);

            int max = 0;
            for (Product product : products) {
                int score = scores.get(product);
                if (max < score)
                    max = score;
            }

            List<Product> sortedProducts = new ArrayList<>();
            while (scores.size() > 0 && (selectedGameCategory != null ? max >= 0 : max >= 1)) {
                for (Product product : products) {
                    if (scores.containsKey(product) && scores.get(product) == max) {
                        sortedProducts.add(product);
                        scores.remove(product);
                    }
                }
                max--;
            }

            products = sortedProducts;
        }

        try {
            products = filterProducts(products, filters);
        } catch (Exception e) {
            return redirect("/404");
        }

        if (products.size() > 0)
            if (selectedGameCategory != null)
                return ok(views.html.products.game.render(selectedGameCategory, Lists.partition(products, 2), session()));
            else
                return ok(views.html.products.products.render(Lists.partition(products, 2), session()));
        else if (selectedGameCategory != null)
            return ok(views.html.products.gameError.render(selectedGameCategory, session()));
        else
            return ok(views.html.selectedproduct.index.render(token, session()));
    }

    private HashMap<Product, Integer> processProductScores(String token, List<Product> products) {
        HashMap<Product, Integer> scores = new HashMap<>();
        for (Product product : products)
            scores.put(product, 0);

        String[] split = token.split(" ");
        for (String s : split) {
            StringBuilder search = new StringBuilder(s);
            double scorePenalty = 1;
            while (search.length() > 1) {
                for (Product product : products) {
                    int score = 0;

                    String title = product.getTitle();
                    if (search.toString().toLowerCase().equals(title.toLowerCase()))
                        score += 100 / scorePenalty;
                    if (title.toLowerCase().contains(search.toString().toLowerCase()))
                        score += 100 / scorePenalty;

                    scores.put(product, scores.get(product) + score);
                }
                scorePenalty++;
                search.delete(search.length() - 1, search.length());
            }
        }
        return scores;
    }
}