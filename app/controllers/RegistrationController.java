package controllers;

import forms.RegistrationForm;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import services.AccountService;
import services.MailerService;
import services.SecurityService;
import util.RecaptchaUtils;
import views.html.register.index;

import javax.inject.Inject;
import java.util.UUID;

/**
 * A {@link Controller} used for account registration.
 *
 * @author I.A
 * @author Maurice van Veen
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

    private final MailerService mails;

	private RecaptchaUtils recaptcha;

	/**
	 * Creates a new {@link RegistrationController}.
	 */
	@Inject
	public RegistrationController(FormFactory formFactory, AccountService accounts, MailerService mails, RecaptchaUtils recaptcha) {
		this.formFactory = formFactory;
		this.accounts = accounts;
        this.mails = mails;
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
			if (RecaptchaUtils.INTEGRATE_RECAPTCHA && !recaptcha.isRecaptchaValid(recaptchaResponse)) {
				return badRequest(views.html.register.index.render(formBinding, session()));
			} else {
				RegistrationForm form = formBinding.get();

				String verification = SecurityService.hash(UUID.randomUUID().toString());

				// TODO thread this
				boolean a = accounts.userExists(form.getName().toLowerCase());
				boolean b = accounts.mailExists(form.getEmail());
				if (a || b) {
					if (a)
						formBinding = formBinding.withError(new ValidationError("name", "This username already exists."));
					if (b)
						formBinding = formBinding.withError(new ValidationError("email", "This email already exists."));

					return badRequest(index.render(formBinding, session()));
				} else {
					accounts.registered(form, verification);
					sendVerifyAccountMail(form.getEmail(), verification, form.getName());
					return redirect("/login/verify/" + form.name);
				}
			}
		}
	}

    private void sendVerifyAccountMail(String mail, String verification, String username) {
        if (!checkMail(mail))
            return;

        String title = "ReStart - Verify your Account - " + username;
        mails.sendEmail(title, mail, "Your account has to be verified. Please verify your account by using verification code: " + verification + " on: restart-webshop.herokuapp.com/login/verify/" + username);
    }

    private boolean checkMail(String mail) {
        return mail != null && mail.contains("@");
    }
}
