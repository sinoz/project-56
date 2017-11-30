package controllers;

import forms.ReviewForm;
import models.Review;
import play.data.Form;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;
import services.ReviewService;
import views.html.addreview.index;

import javax.inject.Inject;

/**
 * A {@link Controller} for the contact page.
 *
 * @author Joris Stander
 *
 */
public final class AddReviewController extends Controller {
    /**
     * The required {@link Database} dependency to fetch database connections.
     */
    private final play.db.Database database;
    /**
     * The {@link ReviewService} required to post reviews.
     */
    private final ReviewService reviews;


    /**
     * The required {@link FormFactory} to produce forms for modifying a user's personal settings.
     */
    private final FormFactory formFactory;


    @Inject
    public AddReviewController(FormFactory formFactory, ReviewService reviews, Database database) {
        this.formFactory = formFactory;
        this.reviews = reviews;
        this.database = database;
    }

    public Result index() {
        return ok(views.html.addreview.index.render(formFactory.form(ReviewForm.class), session()));
    }

    public Result addReview() {
        Form<ReviewForm> formBinding = formFactory.form(ReviewForm.class).bindFromRequest();
        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return badRequest(views.html.addreview.index.render(formBinding, session()));
        } else {
            ReviewForm form = formBinding.get();

            reviews.addReview(form);
            return redirect("/user/admin");
        }
    }
}