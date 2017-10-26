package controllers;

import forms.RegistrationForm;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import services.AccountService;
import views.html.register.index;

import javax.inject.Inject;

/**
 * A {@link Controller} used for account registration.
 *
 * @author I.A
 */
public final class RegistrationController extends Controller {
	/**
	 * A {@link FormFactory} to produce registration forms.
	 */
	private final FormFactory formFactory;

	/**
	 * The {@link AccountService} required to authenticate ReStart accounts.
	 */
	private final AccountService accounts;

	/**
	 * Creates a new {@link RegistrationController}.
	 */
	@Inject
	public RegistrationController(FormFactory formFactory, AccountService accounts) {
		this.formFactory = formFactory;
		this.accounts = accounts;
	}

	/**
	 * Returns an OK result including the {@link views.html.register.index} view.
	 */
	public Result index() {
		return ok(index.render(formFactory.form(RegistrationForm.class), session()));
	}

	/**
	 * Attempts to register a user. Returns either a {@link Controller#badRequest()} indicating
	 * a failure in registering the user or a {@link Controller#ok()} result, indicating a successful
	 * registration.
	 */
	public Result register() {
		Form<RegistrationForm> formBinding = formFactory.form(RegistrationForm.class).bindFromRequest();
		if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
			return badRequest(index.render(formBinding, session()));
		} else {
			RegistrationForm form = formBinding.get();

			// TODO thread this
			if (accounts.userExists(form.getName().toLowerCase())) {
				formBinding = formBinding.withError(new ValidationError("username", "This username already exists."));

				return badRequest(index.render(formBinding, session()));
			} else {
				accounts.registered(form);
				return redirect("/login");
			}
		}
	}
}
