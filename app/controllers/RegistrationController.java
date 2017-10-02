package controllers;

import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.register.*;

import javax.inject.Inject;

public final class RegistrationController extends Controller {
	private final FormFactory formFactory;

	@Inject
	public RegistrationController(FormFactory formFactory) {
		this.formFactory = formFactory;
	}

	public Result index() {
		return ok(index.render());
	}

	public static class UserForm {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public Result submit() {
		Form<UserForm> userForm = formFactory.form(UserForm.class);

		UserForm user = userForm.bindFromRequest().get();

		System.out.println("Hello World: " + user.getName());

		return ok(success.render(user.getName()));
	}
}
