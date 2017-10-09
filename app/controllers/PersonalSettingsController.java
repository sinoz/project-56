package controllers;

import forms.PersonalSettingsForm;
import play.data.Form;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A {@link Controller} for the PersonalSettings page.
 *
 * @author Melle Nout
 * @author I.A
 */
public final class PersonalSettingsController extends Controller {
	/**
	 * The required {@link FormFactory} to produce forms for modifying a user's personal settings.
	 */
	private final FormFactory formFactory;

	/**
	 * The required {@link Database} dependency to obtain database connections from the pool.
	 */
	private final Database db;

	/**
	 * Creates a new {@link PersonalSettingsController}.
	 */
	@Inject
	public PersonalSettingsController(FormFactory formFactory, Database db) {
		this.formFactory = formFactory;
		this.db = db;
	}

	public Result index() {
		String loggedInAs = session().get("loggedInAs");
		if (loggedInAs == null || loggedInAs.length() == 0) {
			return redirect("/");
		} else {
			return ok(views.html.personalsettings.index.render(formFactory.form(PersonalSettingsForm.class), session()));
		}
	}

	public Result editSettings() {
		Form<PersonalSettingsForm> formBinding = formFactory.form(PersonalSettingsForm.class).bindFromRequest();
		if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
			return badRequest(views.html.personalsettings.index.render(formBinding, session()));
		} else {
			PersonalSettingsForm form = formBinding.get();

			String loggedInAs = session().get("loggedInAs");
			if (updateSettings(loggedInAs, form)) {
				session().put("loggedInAs", form.usernameToChangeTo);
				session().put("usedMail", form.emailToChangeTo);
				session().put("usedPaymentMail", form.paymentMailToChangeTo);


				return redirect("/myaccount");
			} else {
				formBinding = formBinding.withGlobalError("An error has occurred while attrempting to update your settings. Please consult an administrator.");

				return badRequest(views.html.personalsettings.index.render(formBinding, session()));
			}
		}
	}

	public boolean updateSettings(String loggedInAs, PersonalSettingsForm form) {
		return db.withConnection(connection -> {
			PreparedStatement stmt = connection.prepareStatement("UPDATE users SET username = ?, mail = ?, paymentmail = ? WHERE username = ?");
			stmt.setString(1, form.usernameToChangeTo);
			stmt.setString(2, form.emailToChangeTo);
			stmt.setString(3, form.paymentMailToChangeTo);
			stmt.setString(4, loggedInAs);
			return !stmt.execute();
		});
	}
}