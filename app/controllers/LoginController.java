package controllers;

import forms.LoginForm;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import services.AuthenticationService;

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
	 * A {@link FormFactory} to produce login forms.
	 */
	private FormFactory formFactory;

	/**
	 * TODO
	 */
	private AuthenticationService auth;

	/**
	 * Creates a new {@link LoginController}.
	 */
	@Inject
	public LoginController(FormFactory formFactory, AuthenticationService auth) {
		this.formFactory = formFactory;
		this.auth = auth;
	}

	/**
	 * Returns a {@link Result} combined with the default login page.
	 */
	public Result index() {
		return ok(views.html.login.index.render(formFactory.form(LoginForm.class), session()));
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
			LoginForm form = formBinding.get();

			Optional<User> user = auth.fetchUser(form.getUsername().toLowerCase(), form.getPassword());
			if (user.isPresent()) {
				session().clear();

				session().put("loggedInAs", user.get().getUsername());
				session().put("profilePictureURL", Optional.ofNullable(user.get().getProfilePicture()).orElse(""));

				return redirect("/");
			} else {
				formBinding = formBinding.withGlobalError("Invalid username/password combination.");

				return badRequest(views.html.login.index.render(formBinding, session()));
			}
		}
	}
}
