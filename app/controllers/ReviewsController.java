package controllers;

import models.Review;
import models.User;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import services.SessionService;
import services.UserViewService;

import javax.inject.Inject;
import java.util.ArrayList;
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

                List<List<Review>> reviewList = new ArrayList<>();
                reviewList.add(new ArrayList<>());
                reviewList.add(new ArrayList<>());

                for (int i = 0; i < reviewsproduct.size(); i++) {
                    if (i % 2 == 0)
                        reviewList.get(0).add(reviewsproduct.get(i));
                    else
                        reviewList.get(1).add(reviewsproduct.get(i));
                }

                return ok(views.html.reviews.index.render(user.get(), rating, reviewList, session()));
            } else {
                return redirect("/404");
            }
        }
    }
}