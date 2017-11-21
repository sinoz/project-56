package controllers;

import concurrent.DbExecContext;
import forms.PersonalSettingsForm;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecution;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.AccountService;
import services.AuthenticationService;

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
	public PersonalSettingsController(FormFactory formFactory, AccountService accounts, DbExecContext dbEc, HttpExecutionContext httpEc, AuthenticationService auth) {
		this.formFactory = formFactory;
		this.accounts = accounts;
		this.dbEc = dbEc;
		this.httpEc = httpEc;
		this.auth = auth;
	}

	public Result index() {
		String loggedInAs = session().get("loggedInAs");
		if (loggedInAs == null || loggedInAs.length() == 0) {
			return redirect("/login");
		} else {
			return ok(views.html.personalsettings.index.render(formFactory.form(PersonalSettingsForm.class), session()));
		}
	}

	public CompletionStage<Result> editSettings() {
		Form<PersonalSettingsForm> formBinding = formFactory.form(PersonalSettingsForm.class).bindFromRequest();
		if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
			return completedFuture(badRequest(views.html.personalsettings.index.render(formBinding, session())));
		} else {
			PersonalSettingsForm form = formBinding.get();

			Executor dbExecutor = HttpExecution.fromThread((Executor) dbEc);

			String loggedInAs = session().get("loggedInAs");

			// TODO add password check back in
            Optional<User> user = auth.fetchUser(loggedInAs, form.password);

            if (user.isPresent()) {
                // runs the account update operation on the database pool of threads and then switches
                // to the internal HTTP pool of threads to safely update the session and returning the view
                return runAsync(() -> accounts.updateSettings(loggedInAs, form), dbExecutor).thenApplyAsync(i -> {
                    session().put("loggedInAs", form.usernameToChangeTo);
                    session().put("usedMail", form.emailToChangeTo);
                    session().put("usedPaymentMail", form.paymentMailToChangeTo);
                    return redirect("/myaccount/personalsettings");
                }, httpEc.current());
            } else {
                return completedFuture(badRequest(views.html.personalsettings.index.render(formBinding, session())));
            }
		}
	}
}