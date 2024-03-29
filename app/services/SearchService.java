package services;

import models.GameCategory;
import models.Order;
import models.Product;
import models.User;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * @author Maurice van Veen
 */
@Singleton
public class SearchService {
    /**
     * The {@link services.ProductService} to obtain product data from.
     */
    private ProductService productService;

    private play.db.Database database;

    private UserViewService userViewService;
    private FavouritesService favouritesService;
    private OrderService orderService;

    @Inject
    public SearchService(ProductService productService, play.db.Database database, UserViewService userViewService, FavouritesService favouritesService, OrderService orderService) {
        this.productService = productService;
        this.database = database;
        this.userViewService = userViewService;
        this.favouritesService = favouritesService;
        this.orderService = orderService;
    }

    /**
     * Creates a {@link FilterPrices} object for storing minimum and maximum price values.
     */
    public FilterPrices filterToken(String token) {
        if (token != null && token.contains("filter=")) {
            token = token.replace("filter=", "");
            String[] split = token.split(";");

            int minPrice = 0;
            int maxPrice = 150;
            for (String s : split) {
                if (!s.contains(":"))
                    continue;
                String[] d = s.split(":");
                String a = d[0];
                String b = d[1];

                try {
                    switch (a) {
                        case "minprice":
                            minPrice = Integer.valueOf(b);
                            break;
                        case "maxprice":
                            maxPrice = Integer.valueOf(b);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return new FilterPrices(minPrice, maxPrice);
        }
        return new FilterPrices(0, 150);
    }

    /**
     * Filters products given {@link FilterPrices} with a minimum and maximum price.
     */
    public List<Product> filterProducts(List<Product> products, String filters, FilterPrices prices) throws Exception {
        List<Product> list = new ArrayList<>();

        if (filters != null) {
            for (Product product : products) {
                if (product.getBuyPrice() <= prices.getMax() && product.getBuyPrice() >= prices.getMin()) {
                    list.add(product);
                }
            }
            return list;
        }

        return products;
    }

    /**
     * Processes a given input with raw filter data.
     */
    public List<Product> fetchSuggestedProducts(Http.Session session, int n) {
        List<Product> suggestions = new ArrayList<>();

        if (!SessionService.redirect(session, database)) {
            String loggedInAs = SessionService.getLoggedInAs(session);
            Optional<User> u = userViewService.fetchUser(loggedInAs);
            if (u.isPresent()) {
                User user = u.get();

                // add all favourites and orders to a list
                List<Product> favourites = favouritesService.getProducts(user.getFavorites());
                List<Order> orders = orderService.getOrdersByUser(user.getId());

                List<Product> products = new ArrayList<>();
                for (Product product : favourites) {
                    if (!products.contains(product) && product != null) {
                        products.add(product);
                    }
                }

                for (Order order : orders) {
                    if (!products.contains(order.getProduct()) && order.getProduct() != null) {
                        products.add(order.getProduct());
                    }
                }

                List<Product> allProducts = productService.fetchProducts();

                // assign scores to all products
                HashMap<Integer, Integer> scores = new HashMap<>();
                for (Product product : allProducts)
                    scores.put(product.getId(), 0);

                for (Product p : products) {
                    String title = p.getTitle();
                    String game = p.getGameCategory().getName();

                    List<String> t = new ArrayList<>();
                    t.addAll(Arrays.asList(title.split(" ")));
                    t.addAll(Arrays.asList(game.split(" ")));

                    for (String token : t) {
                        HashMap<Integer, Integer> subScores = processProductScores(token, allProducts);
                        for (Product product : allProducts) {
                            scores.put(product.getId(), scores.get(product.getId()) + subScores.get(product.getId()));
                        }
                    }
                }

                // get top n*5 sorted products
                HashMap<Integer, Integer> scoresCopy = new HashMap<>();
                scoresCopy.putAll(scores);
                List<Product> sorted = sortProducts(scoresCopy, allProducts);
                int m = sorted.size();
                if (m > n * 5)
                    m = n * 5;
                for (int i = 0; i < m; i++) {
                    if (scores.get(sorted.get(i).getId()) == 0)
                        break;
                    suggestions.add(sorted.get(i));
                }
            }
        }

        return suggestions;
    }

    /**
     * Processes a given input with raw filter data.
     */
    public SearchResults processInput(String token, String filters) {
        List<GameCategory> gameCategories = productService.fetchGameCategories();
        List<Product> products = productService.fetchProducts();
        GameCategory selectedGameCategory = null;

        // Search for game categories
        if (gameCategories.size() > 0) {
            HashMap<Integer, Integer> scores = processGameCategoryScores(token, gameCategories, products);

            SelectedGameCategoryProducts gcp = getSelectedGameCategoryProducts(scores, gameCategories, products);
            products = gcp.getProducts();
            selectedGameCategory = gcp.getSelectedGameCategory();
        }

        String message = "Couldn't find results for: " + token;
        boolean change = !(token != null && token.length() > 0);

        // Search for products
        if (products.size() > 0) {
            HashMap<Integer, Integer> scores = processProductScores(token, products);

            for (Product p : products) {
                if (scores.get(p.getId()) > 0) {
                    change = true;
                    break;
                }
            }

            products = sortProducts(scores, products, selectedGameCategory);
        }

        if (selectedGameCategory != null)
            change = true;

        if (!change)
            products = productService.fetchProducts();

        FilterPrices prices = filterToken(filters);
        try {
            products = filterProducts(products, filters, prices);

            if (change || products.size() == 0)
                message = null;
        } catch (Exception e) {
            return new SearchResults(null, null, message, true);
        }

        return new SearchResults(products, selectedGameCategory, message);
    }

    /**
     * Returns a {@link SelectedGameCategoryProducts} which contains a selected game category and a list of products.
     */
    private SelectedGameCategoryProducts getSelectedGameCategoryProducts(HashMap<Integer, Integer> scores, List<GameCategory> gameCategories, List<Product> products) {
        GameCategoriesSorted gcs = sortGameCategories(scores, gameCategories);
        List<GameCategory> sortedGameCategories = gcs.getSortedGameCategories();
        List<Integer> sortedScores = gcs.getSortedScores();

        if (sortedGameCategories.size() > 0) {
            int max = sortedScores.get(0);
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
                return getSelectedGameCategoryProductsOne(selectedGameCategories.get(0), products);
            } else {
                return getSelectedGameCategoryProductsMultiple(selectedGameCategories, products);
            }
        }
        return new SelectedGameCategoryProducts(products, null);
    }

    /**
     * Returns a {@link SelectedGameCategoryProducts} which contains one selected game category and a list of products.
     */
    private SelectedGameCategoryProducts getSelectedGameCategoryProductsOne(GameCategory selectedGameCategory, List<Product> products) {
        List<Product> selectedProducts = new ArrayList<>();
        for (Product product : products) {
            if (product.getGameId() == selectedGameCategory.getId()) {
                selectedProducts.add(product);
            }
        }
        return new SelectedGameCategoryProducts(selectedProducts, selectedGameCategory);
    }

    /**
     * Returns a {@link SelectedGameCategoryProducts} which contains a list of products.
     */
    private SelectedGameCategoryProducts getSelectedGameCategoryProductsMultiple(List<GameCategory> selectedGameCategories, List<Product> products) {
        List<Product> selectedProducts = new ArrayList<>();
        for (GameCategory gameCategory : selectedGameCategories) {
            for (Product product : products) {
                if (product.getGameId() == gameCategory.getId()) {
                    selectedProducts.add(product);
                }
            }
        }
        return new SelectedGameCategoryProducts(selectedProducts, null);
    }

    /**
     * Sorts a {@link List<GameCategory>} with a given {@link HashMap} with scores.
     */
    private GameCategoriesSorted sortGameCategories(HashMap<Integer, Integer> scores, List<GameCategory> gameCategories) {
        int max = getMaximumScore(scores);

        List<GameCategory> sortedGameCategories = new ArrayList<>();
        List<Integer> sortedScores = new ArrayList<>();
        while (scores.size() > 0 && max > 0) {
            for (GameCategory gameCategory : gameCategories) {
                int id = gameCategory.getId();
                if (scores.containsKey(id) && scores.get(id) == max) {
                    sortedGameCategories.add(gameCategory);
                    sortedScores.add(max);
                    scores.remove(id);
                }
            }
            max--;
        }
        return new GameCategoriesSorted(sortedGameCategories, sortedScores);
    }

    /**
     * Sorts a {@link List<Product>} with a given {@link HashMap} with scores.
     */
    private List<Product> sortProducts(HashMap<Integer, Integer> scores, List<Product> products, GameCategory selectedGameCategory) {
        int max = getMaximumScore(scores);

        List<Product> sortedProducts = new ArrayList<>();
        while (scores.size() > 0 && (selectedGameCategory != null || max == 0 ? max >= 0 : max >= 1)) {
            for (Product product : products) {
                if (scores.containsKey(product.getId()) && scores.get(product.getId()) == max) {
                    sortedProducts.add(product);
                    scores.remove(product.getId());
                }
            }
            max--;
        }

        return sortedProducts;
    }

    private List<Product> sortProducts(HashMap<Integer, Integer> scores, List<Product> products) {
        int max = getMaximumScore(scores);

        List<Product> sortedProducts = new ArrayList<>();
        while (scores.size() > 0 && max >= 0) {
            for (Product product : products) {
                if (scores.containsKey(product.getId()) && scores.get(product.getId()) == max) {
                    sortedProducts.add(product);
                    scores.remove(product.getId());
                }
            }
            max--;
        }

        return sortedProducts;
    }

    /**
     * Returns the maximum score in a {@link HashMap} of scores.
     */
    private int getMaximumScore(HashMap<Integer, Integer> scores) {
        int max = 0;
        for (Map.Entry<Integer, Integer> o : scores.entrySet()) {
            int score = o.getValue();
            if (max < score)
                max = score;
        }
        return max;
    }

    /**
     * Processes {@link List<GameCategory>} and returns a {@link HashMap} of scores.
     *
     */
    private HashMap<Integer, Integer> processGameCategoryScores(String token, List<GameCategory> gameCategories, List<Product> products) {
        List<String> titles = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();

        for (GameCategory gameCategory : gameCategories) {
            titles.add(gameCategory.getName());
            ids.add(gameCategory.getId());
        }

        HashMap<Integer, Integer> scores = calculateScores(token, titles, ids);
        HashMap<Integer, Integer> productScores = processProductScores(token, products);

        for (GameCategory gameCategory : gameCategories) {
            int id = gameCategory.getId();
            int score = scores.get(id);
            int cnt = 1;
            for (Product product : products) {
                if (product.getGameId() == gameCategory.getId()) {
                    if (productScores.get(product.getId()) > 0) {
                        score += productScores.get(product.getId());
                        cnt++;
                    }
                }
            }
            score = (int) (score * 4 / (double) cnt);
            scores.put(id, score);
        }

        return scores;
    }

    /**
     * Processes {@link List<Product>} and returns a {@link HashMap} of scores.
     */
    private HashMap<Integer, Integer> processProductScores(String token, List<Product> products) {
        List<String> titles = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();

        for (Product product : products) {
            titles.add(product.getTitle());
            ids.add(product.getId());
        }

        return calculateScores(token, titles, ids);
    }

    /**
     * Calculates scores for titles matching the given token.
     */
    private HashMap<Integer, Integer> calculateScores(String token, List<String> titles, List<Integer> ids) {
        HashMap<Integer, Integer> scores = new HashMap<>();
        for (int id : ids)
            scores.put(id, 0);

        String[] split = token.split(" ");
        for (String s : split) {
            StringBuilder search = new StringBuilder(s);
            double scorePenalty = 1;
            while (search.length() > 2) {
                for (int i = 0; i < titles.size(); i++) {
                    String title = titles.get(i);
                    int id = ids.get(i);
                    int scoreStart = scores.get(id);
                    int score = getSearchScore(search.toString(), title, scorePenalty);
                    scores.put(id, scoreStart + score);
                }
                scorePenalty++;
                search.delete(search.length() - 1, search.length());
            }
        }

        return scores;
    }

    /**
     * Returns an int with the score of the search and name.
     * ScorePenalty takes into account the weight of the score.
     */
    private int getSearchScore(String search, String name, double scorePenalty) {
        int score = 0;
        if (search.toLowerCase().equals(name.toLowerCase()))
            score += 100 / scorePenalty;
        if (name.toLowerCase().contains(search.toLowerCase()))
            score += 100 / scorePenalty;
        return score;
    }

    private final class GameCategoriesSorted {

        private List<GameCategory> sortedGameCategories;
        private List<Integer> sortedScores;

        private GameCategoriesSorted(List<GameCategory> sortedGameCategories, List<Integer> sortedScores) {
            this.sortedGameCategories = sortedGameCategories;
            this.sortedScores = sortedScores;
        }

        private List<GameCategory> getSortedGameCategories() {
            return sortedGameCategories;
        }

        private List<Integer> getSortedScores() {
            return sortedScores;
        }
    }

    private final class SelectedGameCategoryProducts {

        private List<Product> products;
        private GameCategory selectedGameCategory;

        private SelectedGameCategoryProducts(List<Product> products, GameCategory selectedGameCategory) {
            this.products = products;
            this.selectedGameCategory = selectedGameCategory;
        }

        private List<Product> getProducts() {
            return products;
        }

        private GameCategory getSelectedGameCategory() {
            return selectedGameCategory;
        }
    }

    public final class FilterPrices {

        private final int min, max;

        private FilterPrices(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }
    }

    public final class SearchResults {

        private List<Product> products;
        private GameCategory selectedGameCategory;
        private String message;
        private boolean redirect;

        private SearchResults(List<Product> products, GameCategory selectedGameCategory, String message) {
            this.products = products;
            this.selectedGameCategory = selectedGameCategory;
            this.message = message;
        }

        private SearchResults(List<Product> products, GameCategory selectedGameCategory, String message, boolean redirect) {
            this(products, selectedGameCategory, message);
            this.redirect = redirect;
        }

        public List<Product> getProducts() {
            return products;
        }

        public GameCategory getSelectedGameCategory() {
            return selectedGameCategory;
        }

        public String getMessage() {
            return message;
        }

        public boolean redirect() {
            return redirect;
        }
    }
}