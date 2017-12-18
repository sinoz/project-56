package controllers;

import forms.LoginForm;
import forms.RegistrationForm;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import services.AccountService;
import util.RecaptchaUtils;
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

	private RecaptchaUtils recaptcha;

	/**
	 * Creates a new {@link RegistrationController}.
	 */
	@Inject
	public RegistrationController(FormFactory formFactory, AccountService accounts, RecaptchaUtils recaptcha) {
		this.formFactory = formFactory;
		this.accounts = accounts;
		this.recaptcha = recaptcha;

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
			DynamicForm requestData = formFactory.form().bindFromRequest();
			String recaptchaResponse = requestData.get("g-recaptcha-response");
			if (!recaptcha.isRecaptchaValid(recaptchaResponse)) {
				return badRequest(views.html.register.index.render(formBinding, session()));
			} else {
				RegistrationForm form = formBinding.get();

				// TODO thread this
				if (accounts.userExists(form.getName().toLowerCase())) {
					formBinding = formBinding.withError(new ValidationError("name", "This username already exists."));

					return badRequest(index.render(formBinding, session()));
				} else {
					accounts.registered(form);
					return ok(views.html.login.registered.render(formFactory.form(LoginForm.class), form.name, session()));
				}
			}
		}
	}
}
