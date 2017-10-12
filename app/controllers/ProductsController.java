package controllers;

import com.google.common.collect.Lists;
import forms.SearchForm;
import models.GameCategory;
import models.Product;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
     * The required {@link Database} dependency to fetch database connections.
     */
    private Database database;

    @Inject
    public ProductsController(FormFactory formFactory, Database database){
        this.formFactory = formFactory;
        this.database = database;
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
    public static Form<SearchForm> getSearchForm(FormFactory formFactory){
        return formFactory.form(SearchForm.class);
    }

    public Result index(String token) {
        // TODO: connection stuff
        if (token.startsWith("game=")) {
            token = token.replaceFirst("game=", "").replace("_", " ");
            // database stuff
            Optional<GameCategory> gameCategory = fetchGameCategory(token);
            if (gameCategory.isPresent()) {
                List<Product> products = fetchProducts(gameCategory.get());
                if (products.size() > 0)
                    return ok(views.html.products.game.render(gameCategory.get(), Lists.partition(products, 2), session()));
                else
                    return ok(views.html.products.gameError.render(gameCategory.get(), session()));
            } else {
                return redirect("/404");
            }
        } else if (token.startsWith("input=")) {
            token = token.replaceFirst("input=", "");
            return processInput(token);
        }
        return ok(views.html.selectedproduct.index.render(token, session()));
    }

    private Result processInput(String token) {
        List<GameCategory> gameCategories = fetchGameCategories();
        List<Product> products = fetchProducts();
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
                        score += productScores.get(product);
                        cnt++;
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


        if (products.size() > 0)
            if (selectedGameCategory != null)
                return ok(views.html.products.game.render(selectedGameCategory, Lists.partition(products, 2), session()));
            else
                return ok(views.html.products.products.render(Lists.partition(products, 2), session()));
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

    /**
     * Attempts to find a {@link GameCategory} that matches the given game name.
     */
    private Optional<GameCategory> fetchGameCategory(String gameName) {
        return database.withConnection(connection -> {
            Optional<GameCategory> gameCategory = Optional.empty();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gamecategories WHERE name=?");
            stmt.setString(1, gameName);

            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                GameCategory gc = new GameCategory();

                gc.setId(results.getInt("id"));
                gc.setName(results.getString("name"));
                gc.setImage(results.getString("image"));
                gc.setDescription(results.getString("description"));

                gameCategory = Optional.of(gc);
            }

            return gameCategory;
        });
    }

    /**
     * Attempts to find a {@link GameCategory} that matches the given game name.
     */
    private List<GameCategory> fetchGameCategories() {
        return database.withConnection(connection -> {
            List<GameCategory> list = new ArrayList<>();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gamecategories");

            ResultSet results = stmt.executeQuery();

            while (results.next()) {
                GameCategory gc = new GameCategory();

                gc.setId(results.getInt("id"));
                gc.setName(results.getString("name"));
                gc.setImage(results.getString("image"));
                gc.setDescription(results.getString("description"));

                list.add(gc);
            }

            return list;
        });
    }

    /**
     * Attempts to find a {@link User} that matches the given username and password combination.
     */
    private Optional<User> fetchUser(int id) {
        return database.withConnection(connection -> {
            Optional<User> user = Optional.empty();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE id=?");
            stmt.setInt(1, id);

            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                User u = new User();

                u.setId(results.getString("id"));
                u.setUsername(results.getString("username"));
                u.setPassword(results.getString("password"));
                u.setMail(results.getString("mail"));
                u.setPaymentMail(results.getString("paymentmail"));
                u.setProfilePicture(results.getString("profilepicture"));

                user = Optional.of(u);
            }

            return user;
        });
    }

    /**
     * Attempts to find a {@link Product} that matches the given game category.
     */
    private List<Product> fetchProducts(GameCategory gameCategory) {
        return database.withConnection(connection -> {
            List<Product> list = new ArrayList<>();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts WHERE gameid=?;");
            stmt.setInt(1, gameCategory.getId());

            ResultSet results = stmt.executeQuery();

            while (results.next()) {
                Product product = new Product();

                product.setId(results.getInt("id"));
                product.setUserId(results.getInt("userid"));
                product.setGameId(results.getInt("gameid"));
                product.setVisible(results.getBoolean("visible"));
                product.setDisabled(results.getBoolean("disabled"));
                product.setTitle(results.getString("title"));
                product.setDescription(results.getString("description"));
                product.setAddedSince(results.getDate("addedsince"));
                product.setCanBuy(results.getBoolean("canbuy"));
                product.setBuyPrice(results.getDouble("buyprice"));
                product.setCanTrade(results.getBoolean("cantrade"));
                product.setMailLast(results.getString("maillast"));
                product.setMailCurrent(results.getString("mailcurrent"));
                product.setPasswordCurrent(results.getString("passwordcurrent"));

                Optional<User> user = fetchUser(product.getUserId());
                user.ifPresent(product::setUser);
                list.add(product);
            }

            return list;
        });
    }

    /**
     * Attempts to find all {@link Product}.
     */
    private List<Product> fetchProducts() {
        return database.withConnection(connection -> {
            List<Product> list = new ArrayList<>();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM gameaccounts;");

            ResultSet results = stmt.executeQuery();

            while (results.next()) {
                Product product = new Product();

                product.setId(results.getInt("id"));
                product.setUserId(results.getInt("userid"));
                product.setGameId(results.getInt("gameid"));
                product.setVisible(results.getBoolean("visible"));
                product.setDisabled(results.getBoolean("disabled"));
                product.setTitle(results.getString("title"));
                product.setDescription(results.getString("description"));
                product.setAddedSince(results.getDate("addedsince"));
                product.setCanBuy(results.getBoolean("canbuy"));
                product.setBuyPrice(results.getDouble("buyprice"));
                product.setCanTrade(results.getBoolean("cantrade"));
                product.setMailLast(results.getString("maillast"));
                product.setMailCurrent(results.getString("mailcurrent"));
                product.setPasswordCurrent(results.getString("passwordcurrent"));

                Optional<User> user = fetchUser(product.getUserId());
                user.ifPresent(product::setUser);
                list.add(product);
            }

            return list;
        });
    }
}
