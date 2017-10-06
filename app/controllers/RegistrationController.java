package controllers;

import forms.RegistrationForm;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.register.index;

import javax.inject.Inject;
import java.sql.PreparedStatement;

/**
 * A {@link Controller} used for account registration.
 *
 * @author I.A
 */
public final class RegistrationController extends Controller {
	/**
	 * A {@link FormFactory} to produce registration forms.
	 */
	@Inject
	private FormFactory formFactory;

	/**
	 * The required {@link Database} dependency to fetch database connections.
	 */
	private Database database;

	/**
	 * Creates a new {@link RegistrationController}.
	 */
	@Inject
	public RegistrationController(Database database) {
		this.database = database;
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

			if (userExists(form.getName().toLowerCase())) {
				formBinding = formBinding.withError(new ValidationError("username", "This username already exists."));

				return badRequest(index.render(formBinding, session()));
			} else {
				if (registered(form)) {
					return redirect("/login");
				} else {
					return badRequest(index.render(formBinding.withGlobalError("Failed to register account. Please consult an administrator."), session()));
				}
			}
		}
	}

	/**
	 * Checks if a user with the given username exists in the database.
	 */
	private boolean userExists(String username) {
		return database.withConnection(connection -> {
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username=?");

			stmt.setString(1, username);

			return stmt.executeQuery().next();
		});
	}

	/**
	 * Attempts to register a new user and returns whether it has successfully registered the user
	 * using the given {@link RegistrationForm}.
	 */
	private boolean registered(RegistrationForm form) {
		return database.withConnection(connection -> {
			PreparedStatement stmt = connection.prepareStatement("INSERT INTO users (username, password, mail) VALUES(?, ?, ?)");
			stmt.setString(1, form.name.toLowerCase());
			stmt.setString(2, form.password);
			stmt.setString(3, form.email);
			return !stmt.execute();
		});
	}
}
