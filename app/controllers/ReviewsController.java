package controllers;

import com.google.common.collect.Lists;
import forms.FavouriteForm;
import models.GameCategory;
import models.Product;
import models.Review;
import models.User;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import services.ProductService;
import services.SessionService;
import services.UserViewService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * A {@link Controller} that handles searches.
 *
 * @author: Maurice van Veen
 *
 */
public class ReviewsController extends Controller {
    /**
     * A {@link FormFactory} to use forms.
     */
    private FormFactory formFactory;

    /**
     * The {@link UserViewService} to obtain data from.
     */
    private UserViewService userViewService;

    private play.db.Database database;

    @Inject
    public ReviewsController(FormFactory formFactory, UserViewService userViewService, play.db.Database database){
        this.formFactory = formFactory;
        this.userViewService = userViewService;
        this.database = database;
    }

    public Result index() {
        if (SessionService.redirect(session(), database)) {
            return redirect("/login");
        } else {
            String loggedInAs = SessionService.getLoggedInAs(session());

            Optional<User> user = userViewService.fetchUser(loggedInAs);

            if (user.isPresent()) {
                List<Review> reviewsproduct = userViewService.fetchUserReviews(user.get().getId());

                int totalRating = 0;
                for (Review review : reviewsproduct)
                    totalRating += review.getRating();
                int rating = (int) (totalRating / (double) reviewsproduct.size());

                return ok(views.html.reviews.index.render(user.get(), rating, Lists.partition(reviewsproduct, 2), session()));
            } else {
                return redirect("/404");
            }
        }
    }
}