package controllers;

import forms.LoginForm;
import forms.MailerForm;
import forms.VerifyForm;
import models.User;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import services.AuthenticationService;
import services.SessionService;
import util.RecaptchaUtils;
import views.html.contact.index;

import javax.inject.Inject;
import java.util.Optional;

/**
 * A {@link Controller} for the login page.
 *
 * @author Daryl Bakhuis
 * @author Johan van der Hoeven
 * @author I.A
 */
public final class LoginController extends Controller {
	/**
	 * The required {@link Database} dependency to fetch database connections.
	 */
	private final play.db.Database database;

	/**
	 * A {@link FormFactory} to produce login forms.
	 */
	private FormFactory formFactory;

	private AuthenticationService auth;

	private RecaptchaUtils recaptcha;

	/**
	 * Creates a new {@link LoginController}.
	 */
	@Inject
	public LoginController(RecaptchaUtils recaptcha, play.db.Database database, FormFactory formFactory, AuthenticationService auth) {
		this.database = database;
		this.recaptcha = recaptcha;
		this.formFactory = formFactory;
		this.auth = auth;
	}

	/**
	 * Returns a {@link Result} combined with the default login page.
	 */
	public Result index() {
		return ok(views.html.login.index.render(formFactory.form(LoginForm.class), session()));
	}

	public Result indexVerify(String username) {
	    return ok(views.html.login.waitverify.render(formFactory.form(VerifyForm.class), username, session()));
    }

	public Result callVerify(String username) {
        Form<VerifyForm> formBinding = formFactory.form(VerifyForm.class).bindFromRequest();
        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return badRequest(views.html.login.waitverify.render(formBinding, username, session()));
        } else {
            String verification = formBinding.get().verification;
            return redirect("/login/verify/" + verification + "/" + username);
        }
    }

	public Result verify(String verify, String username) {
	    if (auth.verifyUser(verify, username)) {
	        auth.createUser(verify, username);
            return ok(views.html.login.verify.render(session(), username));
        }
        return ok(views.html.login.noverify.render(session()));
	}

	/**
	 * Attempts to log a user into the application, returning either a {@link Results#redirect(Call)}
	 * which indicates as a successful login, or a {@link Results#badRequest()}} indicating a failure.
	 */
	public Result login() {
		Form<LoginForm> formBinding = formFactory.form(LoginForm.class).bindFromRequest();
		if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
			return badRequest(views.html.login.index.render(formBinding, session()));
		} else {
			DynamicForm requestData = formFactory.form().bindFromRequest();
			String recaptchaResponse = requestData.get("g-recaptcha-response");
			if (!recaptcha.isRecaptchaValid(recaptchaResponse)) {
				return badRequest(views.html.login.index.render(formBinding, session()));
			} else {
				LoginForm form = formBinding.get();

				Optional<User> user = auth.fetchUser(form.getUsername().toLowerCase(), form.getPassword());
				if (user.isPresent()) {
					SessionService.initSession(session(), user.get(), database);
					return redirect("/");
				} else {
					formBinding = formBinding.withGlobalError("Invalid username/password combination.");

					return badRequest(views.html.login.index.render(formBinding, session()));
				}
			}
		}
	}
}
