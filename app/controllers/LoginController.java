package controllers;

import forms.LoginForm;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
	 * The required {@link Database} dependency to fetch database connections.
	 */
	private Database database;

	/**
	 * Creates a new {@link LoginController}.
	 */
	@Inject
	public LoginController(FormFactory formFactory, Database database) {
		this.formFactory = formFactory;
		this.database = database;
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

			Optional<User> user = fetchUser(form.getUsername().toLowerCase(), form.getPassword());
			if (user.isPresent()) {
				session().clear();

				session().put("loggedInAs", user.get().getUsername());
				session().put("profilePictureURL", Optional.ofNullable(user.get().getProfilePicture()).orElse("images/default_profile_pic.png"));
				session().put("usedMail", user.get().getMail());
				session().put("usedPaymentMail", Optional.ofNullable(user.get().getPaymentMail()).orElse(""));


				return redirect("/");
			} else {
				formBinding = formBinding.withGlobalError("Invalid username/password combination.");

				return badRequest(views.html.login.index.render(formBinding, session()));
			}
		}
	}

	/**
	 * Attempts to find a {@link User} that matches the given username and password combination.
	 */
	private Optional<User> fetchUser(String username, String password) {
		return database.withConnection(connection -> {
			Optional<User> user = Optional.empty();

			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
			stmt.setString(1, username);
			stmt.setString(2, password);

			ResultSet results = stmt.executeQuery();

			if (results.next()) {
				User u = new User();

				u.setId(results.getString("id"));
				u.setUsername(results.getString("username"));
				u.setPassword(results.getString("password"));
				u.setMail(results.getString("mail"));
				u.setPaymentMail(results.getString("paymentmail"));
				u.setProfilePicture(results.getString("profilepicture"));

				user = Optional.of(u);
			}

			return user;
		});
	}
}
