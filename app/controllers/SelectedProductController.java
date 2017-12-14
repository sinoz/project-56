package controllers;

import com.google.common.collect.Lists;
import forms.FavouriteForm;
import models.GameCategory;
import models.Product;
import models.Review;
import models.ViewableUser;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import services.ProductService;
import services.SearchService;
import services.SessionService;
import services.UserViewService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * A {@link Controller} that handles searches.
 *
 * @author: Joris Stander
 * @author: D.Bakhuis
 * @author: Maurice van Veen
 *
 */
public class SelectedProductController extends Controller {
    /**
     * A {@link FormFactory} to use forms.
     */
    private FormFactory formFactory;

    /**
     * The {@link services.UserViewService} to obtain data from.
     */
    private UserViewService userViewService;

    /**
     * The {@link services.ProductService} to obtain product data from.
     */
    private ProductService productService;

    private SearchService searchService;

    @Inject
    public SelectedProductController(FormFactory formFactory, UserViewService userViewService, ProductService productService, SearchService searchService){
        this.formFactory = formFactory;
        this.userViewService = userViewService;
        this.productService = productService;
        this.searchService = searchService;
    }

    public Result index(String token) {
        try {
            int productId = Integer.valueOf(token);
            Optional<Product> product = productService.fetchVisibleProduct(productId);
            List<Review> reviewsproduct;
            if (product.isPresent()) {
                Optional<GameCategory> gameCategory = productService.fetchGameCategory(product.get().getGameId());
                if (gameCategory.isPresent()) {
                    reviewsproduct = userViewService.fetchUserReviews(product.get().getUserId());

                    int totalRating = 0;
                    for (Review review : reviewsproduct)
                        totalRating += review.getRating();
                    int rating = (int) (totalRating / (double) reviewsproduct.size());

                    String loggedInAs = SessionService.getLoggedInAs(session());
                    Optional<ViewableUser> user = userViewService.fetchViewableUser(loggedInAs);
                    boolean loggedIn = false;
                    boolean isFavourited = false;
                    if (user.isPresent()) {
                        loggedIn = true;
                        isFavourited = userViewService.fetchProductIsFavourited(user.get().getId(), productId);
                    }

                    List<Product> suggestedProducts = searchService.fetchSuggestedProducts(session(), 3);
                    while (suggestedProducts.size() > 6 * 2)
                        suggestedProducts.remove(suggestedProducts.size() - 1);

                    return ok(views.html.selectedproduct.details.render(gameCategory.get(), product.get(), rating, reviewsproduct, Lists.partition(suggestedProducts, 6), formFactory.form(FavouriteForm.class), loggedIn, isFavourited, session()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Product> suggestedProducts = searchService.fetchSuggestedProducts(session(), 3);
        return ok(views.html.selectedproduct.index.render(token, token, Lists.partition(suggestedProducts, 5), session()));
    }
}