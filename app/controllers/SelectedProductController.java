package controllers;

import models.GameCategory;
import models.Product;
import models.Review;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import services.ProductService;
import services.UserViewService;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
     * A {@link FormFactory} to use search forms.
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

    @Inject
    public SelectedProductController(FormFactory formFactory, UserViewService userViewService, ProductService productService){
        this.formFactory = formFactory;
        this.userViewService = userViewService;
        this.productService = productService;
    }

    public Result index(String token) {
        try {
            int id = Integer.valueOf(token);
            Optional<Product> product = productService.fetchProduct(id);
            List<Review> reviewsproduct;
            if (product.isPresent()) {
                Optional<GameCategory> gameCategory = productService.fetchGameCategory(product.get().getGameId());
                if (gameCategory.isPresent()) {
                    reviewsproduct = userViewService.fetchUserReviews(product.get().getUserId());

                    int totalRating = 0;
                    for (Review review : reviewsproduct)
                        totalRating += review.getRating();
                    int rating = (int) (totalRating / (double) reviewsproduct.size());

                    return ok(views.html.selectedproduct.details.render(gameCategory.get(), product.get(), rating, reviewsproduct, session()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ok(views.html.selectedproduct.index.render(token, session()));
    }
}