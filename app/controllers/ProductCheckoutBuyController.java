package controllers;

import forms.CouponCodeForm;
import forms.SessionMailForm;
import models.*;
import play.data.Form;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;
import services.*;

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
     * The {@link OrderService} used for accessing orders in de database
     */
    private final OrderService orderService;

    private final CouponCodeService couponCodeService;

    /**
     * The required {@link Database} dependency to fetch database connections.
     */
    private final play.db.Database database;

    @Inject
    public ProductCheckoutBuyController(play.db.Database database, FormFactory formFactory, UserViewService userViewService, ProductService productService, OrderService orderService, CouponCodeService couponCodeService) {
        this.formFactory = formFactory;
        this.database = database;
        this.userViewService = userViewService;
        this.productService = productService;
        this.orderService = orderService;
        this.couponCodeService = couponCodeService;
    }

    public Result index(String token) {
        try {
            int t = Integer.valueOf(token);
            return productService
                    .fetchVisibleProduct(t)
                    .map(product -> {
                        Optional<User> user = userViewService.fetchUser(product.getUserId());

                        return open(product, product.getBuyPrice(), user, token, "null");
                    })
                    .orElse(redirect("/404"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return redirect("/404");
    }

    public Result open(Product product, double price, Optional<User> user, String token, String couponCode) {
        String loggedInAs = SessionService.getLoggedInAs(session());
        if (loggedInAs == null && !SessionService.isValidTime(session())) {
            return ok(views.html.checkout.mail.render("/products/checkout/buy/" + product.getId(), formFactory.form(SessionMailForm.class)));
        }

        Optional<ViewableUser> buyer = userViewService.fetchViewableUser(loggedInAs);
        String userId = buyer.map(viewableUser -> viewableUser.getId() + "").orElse("null");
        String sessionToken = SessionService.getSessionToken(session());

        if (sessionToken == null) sessionToken = "null";
        String trackingId = orderService.getNewTrackingId();
        if (couponCode == null) couponCode = "null";
        String mail = SessionService.getMail(session());
        if (mail == null) mail = "null";

        String verification = orderService.createVerification(token, userId, sessionToken, price + "", trackingId, couponCode, mail);

        return ok(views.html.checkout.buy.render(product, price, user, getRating(product.getUserId()), session(), verification, token, userId, sessionToken, trackingId, couponCode, mail));
    }

    public Result couponCode(String token) {
        Form<CouponCodeForm> form = formFactory.form(CouponCodeForm.class).bindFromRequest();
        if (form.hasErrors() || form.hasGlobalErrors()) {
            return redirect("/404"); // TODO
        } else {
            CouponCodeForm couponCodeForm = form.get();

            return productService
                .fetchVisibleProduct(Integer.valueOf(token))
                .map(product -> {
                    Optional<User> user = userViewService.fetchUser(product.getUserId());
                    Optional<CouponCode> couponCode = couponCodeService.getCouponCode(couponCodeForm.coupon);

                    if (!couponCode.isPresent()) {
                        return open(product, product.getBuyPrice(), user, token, "null");
                    } else {
                        double regularPrice = product.getBuyPrice();
                        double couponPercentage = couponCode.get().getPercentage();

                        double amountToSubtract = (regularPrice / 100) * couponPercentage;
                        double newPrice = regularPrice - amountToSubtract;

                        // Paypal API only allows 7 digits after the comma
                        double formattedNewPrice = Double.valueOf(new DecimalFormat("0.00").format(newPrice));

                        return open(product, formattedNewPrice, user, token, couponCode.get().getCode());
                    }
                })
                .orElse(redirect("/404"));
        }
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