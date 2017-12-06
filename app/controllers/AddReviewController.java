package controllers;

import concurrent.DbExecContext;
import forms.ReviewForm;
import models.Product;
import models.Review;
import models.ReviewToken;
import models.ViewableUser;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.db.Database;
import play.libs.concurrent.HttpExecution;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.ProductService;
import services.ReviewService;
import services.SessionService;
import services.UserViewService;
import util.RecaptchaUtils;

import javax.inject.Inject;
import java.util.Optional;
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

    private final UserViewService userViewService;

    private final ProductService productService;


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

    private RecaptchaUtils recaptcha;


    @Inject
    public AddReviewController(FormFactory formFactory, ReviewService reviews, UserViewService userViewService, ProductService productService, Database database, DbExecContext dbEc, HttpExecutionContext httpEc, RecaptchaUtils recaptcha) {
        this.formFactory = formFactory;
        this.reviews = reviews;
        this.userViewService = userViewService;
        this.productService = productService;
        this.database = database;
        this.dbEc = dbEc;
        this.httpEc = httpEc;
        this.recaptcha = recaptcha;
    }

    public Result index(String token) {
        if (SessionService.redirect(session(), database)) {
            return redirect("/login");
        } else {
            String loggedInAs = SessionService.getLoggedInAs(session());

            Optional<ReviewToken> reviewToken = reviews.getReviewByToken(token);
            Optional<ViewableUser> user = userViewService.fetchViewableUser(loggedInAs);
            if (reviewToken.isPresent() && user.isPresent()) {
                if (reviewToken.get().getUserSenderId() == user.get().getId()) {
                    Optional<Product> product = productService.fetchProduct(reviewToken.get().getProductId());
                    Optional<ViewableUser> receiver = userViewService.fetchViewableUser(reviewToken.get().getUserReceiverId());

                    if (product.isPresent() && receiver.isPresent()) {
                        return ok(views.html.addreview.index.render(formFactory.form(ReviewForm.class), token, product.get(), user.get(), receiver.get(), session()));
                    }
                }
            }
            return redirect("/404");
        }
    }

    public CompletionStage<Result> addReview(String reviewid, String pid, String usid, String urid) {
        int productid;
        int usersenderid;
        int userreceiverid;
        try {
            productid = Integer.valueOf(pid);
            usersenderid = Integer.valueOf(usid);
            userreceiverid = Integer.valueOf(urid);
        } catch (Exception e) {
            return completedFuture(redirect("/404"));
        }

        Optional<ViewableUser> user = userViewService.fetchViewableUser(usersenderid);
        Optional<ViewableUser> receiver = userViewService.fetchViewableUser(userreceiverid);
        Optional<Product> product = productService.fetchProduct(productid);

        if (!(user.isPresent() && receiver.isPresent() && product.isPresent())) {
            return completedFuture(redirect("/404"));
        }

        Form<ReviewForm> formBinding = formFactory.form(ReviewForm.class).bindFromRequest();
        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return completedFuture(badRequest(views.html.addreview.index.render(formBinding, reviewid, product.get(), user.get(), receiver.get(), session())));
        } else {
            DynamicForm requestData = formFactory.form().bindFromRequest();
            String recaptchaResponse = requestData.get("g-recaptcha-response");
            if (!recaptcha.isRecaptchaValid(recaptchaResponse)) {
                return completedFuture(badRequest(views.html.addreview.index.render(formBinding, reviewid, product.get(), user.get(), receiver.get(), session())));
            } else {

                Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);

                ReviewForm form = formBinding.get();
                Review review = new Review();

                review.setUserSenderId(usersenderid);
                review.setUserReceiverId(userreceiverid);
                review.setRating(form.rating);
                review.setTitle(form.title);
                review.setDescription(form.description);
                review.setRating(form.rating);


                return runAsync(() -> { reviews.disableReviewToken(reviewid); reviews.addReview(review); }, dbExecutor)
                        .thenApplyAsync(i -> redirect("/"), httpEc.current());
            }
        }
    }
}