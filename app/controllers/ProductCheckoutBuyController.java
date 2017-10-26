package controllers;

import models.GameCategory;
import models.Product;
import models.Review;
import models.User;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;
import services.ProductService;
import services.UserViewService;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A {@link Controller} that handles searches.
 *
 * @author: Maurice van Veen
 * @author D.Bakhuis
 */
public class ProductCheckoutBuyController extends Controller {
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
    public ProductCheckoutBuyController(FormFactory formFactory, UserViewService userViewService, ProductService productService){
        this.formFactory = formFactory;
        this.userViewService = userViewService;
        this.productService = productService;
    }

    public Result index(String token) {
        try {
            int id = Integer.valueOf(token);
            Optional<Product> p = productService.fetchProduct(id);
            if (p.isPresent()) {
                Product product = p.get();
                Optional<User> user = userViewService.fetchUser(product.getUserId());

                List<Review> reviewsproduct = userViewService.fetchUserReviews(product.getUserId());

                int totalRating = 0;
                for (Review review : reviewsproduct)
                    totalRating += review.getRating();
                int rating = (int) (totalRating / (double) reviewsproduct.size());

                return ok(views.html.checkout.buy.render(product, user, rating, session()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return redirect("/404");
    }
}