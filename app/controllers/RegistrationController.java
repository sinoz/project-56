package controllers;

import forms.RegistrationForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.register.*;

import javax.inject.Inject;

/** A {@link Controller} used for account registration.
 * @author I.A
 */
public final class RegistrationController extends Controller {
	/**
	 * A {@link FormFactory} to produce registration forms.
	 */
	@Inject private FormFactory formFactory;

	/**
	 * Returns an OK result including the {@link views.html.register.index} view.
	 */
	public Result index() {
		return ok(index.render(formFactory.form(RegistrationForm.class)));
	}

	/**
	 * Attempts to register a user. Returns either a {@link Controller#badRequest()} indicating
	 * a failure in registering the user or a {@link Controller#ok()} result, indicating a successful
	 * registration.
	 */
	public Result register() {
		Form<RegistrationForm> formBinding = formFactory.form(RegistrationForm.class).bindFromRequest();
		if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
			return badRequest(index.render(formBinding));
		} else {
			formBinding.get(); // TODO

			return ok(success.render());
		}
	}
}
