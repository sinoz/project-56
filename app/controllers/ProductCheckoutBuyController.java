package controllers;

import forms.CouponCodeForm;
import models.CouponCode;
import models.Review;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;
import services.ProductService;
import services.UserViewService;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
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

    /**
     * TODO
     */
    private final play.db.Database database;

    @Inject
    public ProductCheckoutBuyController(Database database, FormFactory formFactory, UserViewService userViewService, ProductService productService){
        this.formFactory = formFactory;
        this.database = database;
        this.userViewService = userViewService;
        this.productService = productService;
    }

    public Result index(String token) {
        try {
            int t = Integer.valueOf(token);
            return productService
                    .fetchProduct(t)
                    .map(product -> {
                        Optional<User> user = userViewService.fetchUser(product.getUserId());

                        return ok(views.html.checkout.buy.render(product, product.getBuyPrice(), user, getRating(product.getUserId()), session(), token));
                    })
                    .orElse(redirect("/404"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return redirect("/404");
    }

    public Result couponCode(String token) {
        Form<CouponCodeForm> form = formFactory.form(CouponCodeForm.class).bindFromRequest();
        if (form.hasErrors() || form.hasGlobalErrors()) {
            return redirect("/404"); // TODO
        } else {
            CouponCodeForm couponCodeForm = form.get();

            return productService
                .fetchProduct(Integer.valueOf(token))
                .map(product -> {
                    Optional<User> user = userViewService.fetchUser(product.getUserId());
                    Optional<CouponCode> couponCode = getCouponCode(couponCodeForm.coupon);
                    if (!couponCode.isPresent()) {
                        return ok(views.html.checkout.buy.render(product, product.getBuyPrice(), user, getRating(product.getUserId()), session(), token));
                    } else {
                        double regularPrice = product.getBuyPrice();
                        double couponPercentage = couponCode.get().getPercentage();

                        double amountToSubtract = (regularPrice / 100) * couponPercentage;
                        double newPrice = regularPrice - amountToSubtract;

                        // Paypal API only allows 7 digits after the comma
                        double formattedNewPrice = Double.valueOf(new DecimalFormat("0.00").format(newPrice));

                        return ok(views.html.checkout.buy.render(product, formattedNewPrice, user, getRating(product.getUserId()), session(), token));
                    }
                })
                .orElse(redirect("/404"));
        }
    }

    private Optional<CouponCode> getCouponCode(String code) { // TODO move to a service
        return database.withConnection(connection -> {
            Optional<CouponCode> couponCode = Optional.empty();

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM couponcodes WHERE code=?");
            stmt.setString(1, code);

            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                double percentage = results.getDouble("percentage");

                couponCode = Optional.of(new CouponCode(code, percentage));
            }

            return couponCode;
        });
    }

    private int getRating(int userId) {
        List<Review> reviewsproduct = userViewService.fetchUserReviews(userId);

        int totalRating = reviewsproduct.stream().mapToInt(Review::getRating).sum();

        return (int) (totalRating / (double) reviewsproduct.size());

//        List<Review> reviewsproduct = userViewService.fetchUserReviews(userId);
//
//        int totalRating = 0;
//        for (Review review : reviewsproduct) {
//            totalRating += review.getRating();
//        }
//
//        return (int) (totalRating / (double) reviewsproduct.size());
    }
}