package controllers;

import concurrent.DbExecContext;
import forms.ReviewForm;
import models.Review;
import play.data.Form;
import play.data.FormFactory;
import play.db.Database;
import play.libs.concurrent.HttpExecution;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.ReviewService;
import views.html.addreview.index;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * A {@link Controller} for the contact page.
 *
 * @author Joris Stander
 * @author D.Bakhuis
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

    /**
     * The execution context used to asynchronously perform database operations.
     */
    private final DbExecContext dbEc;

    /**
     * The execution context used to asynchronously perform operations.
     */
    private final HttpExecutionContext httpEc;


    @Inject
    public AddReviewController(FormFactory formFactory, ReviewService reviews, Database database, DbExecContext dbEc, HttpExecutionContext httpEc) {
        this.formFactory = formFactory;
        this.reviews = reviews;
        this.database = database;
        this.dbEc = dbEc;
        this.httpEc = httpEc;
    }

    public Result index() {
        return ok(views.html.addreview.index.render(formFactory.form(ReviewForm.class), session()));
    }

    public CompletionStage<Result> addReview() {
        Form<ReviewForm> formBinding = formFactory.form(ReviewForm.class).bindFromRequest();
        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return completedFuture(badRequest(views.html.addreview.index.render(formBinding, session())));
        } else {
            Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);

            ReviewForm form = formBinding.get();
            Review review = new Review();

            // TODO: Change userreceiver, usersender based on product
            // TODO: Add rating based on form
            review.setUserReceiverId(1);
            review.setUserSenderId(2);
            review.setTitle(form.title);
            review.setDescription(form.description);
            review.setRating(4);


            return runAsync(() -> reviews.addReview(review), dbExecutor)
                    .thenApplyAsync(i -> redirect("/"), httpEc.current());
        }
    }
}