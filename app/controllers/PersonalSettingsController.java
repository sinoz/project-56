package controllers;

import concurrent.DbExecContext;
import forms.PersonalSettingsForm;
import forms.PersonalSettingsPasswordForm;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.db.Database;
import play.libs.concurrent.HttpExecution;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.AccountService;
import services.AuthenticationService;
import services.SessionService;
import views.html.register.index;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * A {@link Controller} for the PersonalSettings page.
 *
 * @author Joris Stander
 * @author Melle Nout
 * @author I.A
 */
public final class PersonalSettingsController extends Controller {
    /**
     * The required {@link Database} dependency to fetch database connections.
     */
    private final play.db.Database database;

	/**
	 * The required {@link FormFactory} to produce forms for modifying a user's personal settings.
	 */
	private final FormFactory formFactory;

	/**
	 * The {@link AccountService} required to authenticate ReStart accounts.
	 */
	private final AccountService accounts;

	/**
	 * The execution context used to asynchronously perform database operations.
	 */
	private final DbExecContext dbEc;

	/**
	 * The execution context used to asynchronously perform operations.
	 */
	private final HttpExecutionContext httpEc;

    /**
     * The {@link services.AuthenticationService} to obtain data from.
     */
    private AuthenticationService auth;

	/**
	 * Creates a new {@link PersonalSettingsController}.
	 */
	@Inject
	public PersonalSettingsController(play.db.Database database, FormFactory formFactory, AccountService accounts, DbExecContext dbEc, HttpExecutionContext httpEc, AuthenticationService auth) {
	    this.database = database;
		this.formFactory = formFactory;
		this.accounts = accounts;
		this.dbEc = dbEc;
		this.httpEc = httpEc;
		this.auth = auth;
	}

	public Result index() {
		if (SessionService.redirect(session(), database)) {
			return redirect("/login");
		} else {
			return ok(views.html.personalsettings.index.render(formFactory.form(PersonalSettingsForm.class), session()));
		}
	}

	public Result indexPassword() {
		if (SessionService.redirect(session(), database)) {
			return redirect("/login");
		} else {
			return ok(views.html.personalsettings.password.render(formFactory.form(PersonalSettingsPasswordForm.class), session()));
		}
	}

	public CompletionStage<Result> editSettings() {
		Form<PersonalSettingsForm> formBinding = formFactory.form(PersonalSettingsForm.class).bindFromRequest();
		if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
			return completedFuture(badRequest(views.html.personalsettings.index.render(formBinding, session())));
		} else {
			PersonalSettingsForm form = formBinding.get();

			Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);

			String loggedInAs = SessionService.getLoggedInAs(session());

            Optional<User> user = auth.fetchUser(loggedInAs, form.password);

            if (accounts.userExists(form.usernameToChangeTo.toLowerCase()) && !loggedInAs.equals(form.usernameToChangeTo.toLowerCase())) {
                formBinding = formBinding.withError(new ValidationError("currentPassword", "This username already exists."));
                return completedFuture(badRequest(views.html.personalsettings.index.render(formBinding, session())));
            }

            if (user.isPresent()) {
                if (accounts.mailExists(form.emailToChangeTo.toLowerCase()) && !user.get().getMail().equals(form.emailToChangeTo)) {
                    formBinding = formBinding.withError(new ValidationError("emailToChangeTo", "This email already exists."));
                    return completedFuture(badRequest(views.html.personalsettings.index.render(formBinding, session())));
                }

                // runs the account update operation on the database pool of threads and then switches
                // to the internal HTTP pool of threads to safely update the session and returning the view
                return runAsync(() -> accounts.updateSettings(loggedInAs, form), dbExecutor).thenApplyAsync(i -> {
					SessionService.updateSession(session(), form.usernameToChangeTo, form.emailToChangeTo, form.paymentMailToChangeTo);
                    return redirect("/myaccount/personalsettings");
                }, httpEc.current());
            } else {
                return completedFuture(badRequest(views.html.personalsettings.index.render(formBinding, session())));
            }
		}
	}

    public CompletionStage<Result> editPassword() {
        Form<PersonalSettingsPasswordForm> formBinding = formFactory.form(PersonalSettingsPasswordForm.class).bindFromRequest();
        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return completedFuture(badRequest(views.html.personalsettings.password.render(formBinding, session())));
        } else {
            PersonalSettingsPasswordForm form = formBinding.get();

            Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);

            String loggedInAs = SessionService.getLoggedInAs(session());

            Optional<User> user = auth.fetchUser(loggedInAs, form.currentPassword);

            if (user.isPresent() && form.password.equals(form.repeatPassword)) {
                // runs the account update operation on the database pool of threads and then switches
                // to the internal HTTP pool of threads to safely update the session and returning the view
                return runAsync(() -> accounts.updatePassword(user.get(), form), dbExecutor).thenApplyAsync(i -> redirect("/myaccount/personalsettings"), httpEc.current());
            } else {
                return completedFuture(badRequest(views.html.personalsettings.password.render(formBinding, session())));
            }
        }
    }
}