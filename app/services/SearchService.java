package services;

import models.GameCategory;
import models.Product;

import javax.inject.Inject;
import java.util.*;

/**
 * @author Maurice van Veen
 */
public class SearchService {

    /**
     * The {@link services.ProductService} to obtain product data from.
     */
    private ProductService productService;

    @Inject
    public SearchService(ProductService productService) {
        this.productService = productService;
    }

    public FilterPrices filterToken(String token) {
        if (token != null) {
            token = token.replace("filter=", "");
            String[] split = token.split(";");

            int minPrice = 0;
            int maxPrice = 150;
            for (String s : split) {
                String[] d = s.split(":");
                String a = d[0];
                String b = d[1];

                switch (a) {
                    case "minprice":
                        minPrice = Integer.valueOf(b);
                        break;
                    case "maxprice":
                        maxPrice = Integer.valueOf(b);
                        break;
                }
            }
            return new FilterPrices(minPrice, maxPrice);
        }
        return new FilterPrices(0, 150);
    }

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

        // Search for products
        if (products.size() > 0) {
            HashMap<Integer, Integer> scores = processProductScores(token, products);
            products = sortProducts(scores, products, selectedGameCategory);
        }

        FilterPrices prices = filterToken(filters);
        try {
            products = filterProducts(products, filters, prices);
        } catch (Exception e) {
            return new SearchResults(null, null, true);
        }

        return new SearchResults(products, selectedGameCategory);
    }

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

    private SelectedGameCategoryProducts getSelectedGameCategoryProductsOne(GameCategory selectedGameCategory, List<Product> products) {
        List<Product> selectedProducts = new ArrayList<>();
        for (Product product : products) {
            if (product.getGameId() == selectedGameCategory.getId()) {
                selectedProducts.add(product);
            }
        }
        return new SelectedGameCategoryProducts(selectedProducts, selectedGameCategory);
    }

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

    private List<Product> sortProducts(HashMap<Integer, Integer> scores, List<Product> products, GameCategory selectedGameCategory) {
        int max = getMaximumScore(scores);

        List<Product> sortedProducts = new ArrayList<>();
        while (scores.size() > 0 && (selectedGameCategory != null ? max >= 0 : max >= 1)) {
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

    private int getMaximumScore(HashMap<Integer, Integer> scores) {
        int max = 0;
        for (Map.Entry<Integer, Integer> o : scores.entrySet()) {
            int score = o.getValue();
            if (max < score)
                max = score;
        }
        return max;
    }

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
            int score = 0;
            int cnt = 0;
            for (Product product : products) {
                if (product.getGameId() == gameCategory.getId()) {
                    if (productScores.get(product.getId()) > 0) {
                        score += productScores.get(product.getId());
                        cnt++;
                    }
                }
            }
            score = (int) (score * 10 / (double) cnt);
            scores.put(gameCategory.getId(), scores.get(gameCategory.getId()) + score);
        }

        return scores;
    }

    private HashMap<Integer, Integer> processProductScores(String token, List<Product> products) {
        List<String> titles = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();

        for (Product product : products) {
            titles.add(product.getTitle());
            ids.add(product.getId());
        }

        return calculateScores(token, titles, ids);
    }

    private HashMap<Integer, Integer> calculateScores(String token, List<String> titles, List<Integer> ids) {
        HashMap<Integer, Integer> scores = new HashMap<>();
        for (int id : ids)
            scores.put(id, 0);

        String[] split = token.split(" ");
        for (String s : split) {
            StringBuilder search = new StringBuilder(s);
            double scorePenalty = 1;
            while (search.length() > 1) {
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

        public GameCategoriesSorted(List<GameCategory> sortedGameCategories, List<Integer> sortedScores) {
            this.sortedGameCategories = sortedGameCategories;
            this.sortedScores = sortedScores;
        }

        public List<GameCategory> getSortedGameCategories() {
            return sortedGameCategories;
        }

        public List<Integer> getSortedScores() {
            return sortedScores;
        }
    }

    private final class SelectedGameCategoryProducts {

        private List<Product> products;
        private GameCategory selectedGameCategory;

        public SelectedGameCategoryProducts(List<Product> products, GameCategory selectedGameCategory) {
            this.products = products;
            this.selectedGameCategory = selectedGameCategory;
        }

        public List<Product> getProducts() {
            return products;
        }

        public GameCategory getSelectedGameCategory() {
            return selectedGameCategory;
        }
    }

    public final class FilterPrices {

        private final int min, max;

        public FilterPrices(int min, int max) {
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
        private boolean redirect;

        public SearchResults(List<Product> products, GameCategory selectedGameCategory) {
            this.products = products;
            this.selectedGameCategory = selectedGameCategory;
        }

        public SearchResults(List<Product> products, GameCategory selectedGameCategory, boolean redirect) {
            this(products, selectedGameCategory);
            this.redirect = redirect;
        }

        public List<Product> getProducts() {
            return products;
        }

        public GameCategory getSelectedGameCategory() {
            return selectedGameCategory;
        }

        public boolean redirect() {
            return redirect;
        }
    }
}