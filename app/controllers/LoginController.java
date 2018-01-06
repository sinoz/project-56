package controllers;

import forms.*;
import models.User;
import models.ViewableUser;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import services.*;
import util.RecaptchaUtils;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

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

	private UserViewService userViewService;

    private MailerService mails;

    private AccountService accountService;

	private RecaptchaUtils recaptcha;

	/**
	 * Creates a new {@link LoginController}.
	 */
	@Inject
	public LoginController(
	        RecaptchaUtils recaptcha,
            play.db.Database database,
            FormFactory formFactory,
            AuthenticationService auth,
            UserViewService userViewService,
            MailerService mails,
            AccountService accountService) {
		this.database = database;
		this.recaptcha = recaptcha;
		this.formFactory = formFactory;
		this.auth = auth;
		this.userViewService = userViewService;
		this.mails = mails;
		this.accountService = accountService;
	}

	/**
	 * Returns a {@link Result} combined with the default login page.
	 */
	public Result index() {
		return ok(views.html.login.index.render(formFactory.form(LoginForm.class), session()));
	}

	public Result indexForgotPassword() {
		return ok(views.html.login.forgotpassword.render(formFactory.form(ForgotPasswordForm.class), session()));
	}

    public Result indexChangeForgotPassword(String username) {
	    Optional<ViewableUser> user = userViewService.fetchViewableUser(username);
	    if (user.isPresent())
            if (accountService.verifyCanChangePassword(user.get().getId())) {
                return ok(views.html.login.changepassword.render(formFactory.form(VerifyChangePasswordForm.class), username, session()));
            }
        return redirect("/404");
    }

    public Result resetChangeForgotPassword(String username) {
        Optional<ViewableUser> user = userViewService.fetchViewableUser(username);
        if (user.isPresent())
            if (accountService.verifyCanChangePassword(user.get().getId())) {
                accountService.removeChangePassword(user.get().getId());
            }
        return redirect("/login");
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
            DynamicForm requestData = formFactory.form().bindFromRequest();
            String recaptchaResponse = requestData.get("g-recaptcha-response");
            if (!recaptcha.isRecaptchaValid(recaptchaResponse)) {
                return badRequest(views.html.login.waitverify.render(formBinding, username, session()));
            } else {
                return verify(verification, username);
            }
        }
    }

    public Result callForgotPassword() {
        Form<ForgotPasswordForm> formBinding = formFactory.form(ForgotPasswordForm.class).bindFromRequest();
        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return badRequest(views.html.login.forgotpassword.render(formBinding, session()));
        } else {
            String mail = formBinding.get().email;
            DynamicForm requestData = formFactory.form().bindFromRequest();
            String recaptchaResponse = requestData.get("g-recaptcha-response");
            if (!recaptcha.isRecaptchaValid(recaptchaResponse)) {
                return badRequest(views.html.login.forgotpassword.render(formBinding, session()));
            } else {
                Optional<ViewableUser> user = userViewService.fetchViewableUserMail(mail);
                if (user.isPresent()) {
                    String verification = SecurityService.hash(UUID.randomUUID().toString());
                    accountService.saveChangePassword(verification, user.get().getUsername(), mail, user.get().getId());
                    sendForgotPasswordMail(mail, verification, user.get());
                    return redirect("/login/changepassword/" + user.get().getUsername());
                } else {
                    return badRequest(views.html.login.forgotpassword.render(formBinding, session()));
                }
            }
        }
    }

    public Result callChangeForgotPassword(String username) {
        Optional<User> user = userViewService.fetchUser(username);
        if (!user.isPresent()) {
            return redirect("/404");
        }

        Form<VerifyChangePasswordForm> formBinding = formFactory.form(VerifyChangePasswordForm.class).bindFromRequest();
        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return badRequest(views.html.login.changepassword.render(formBinding, username, session()));
        } else {
            DynamicForm requestData = formFactory.form().bindFromRequest();
            String recaptchaResponse = requestData.get("g-recaptcha-response");
            if (!recaptcha.isRecaptchaValid(recaptchaResponse)) {
                return badRequest(views.html.login.changepassword.render(formBinding, username, session()));
            } else {
                String verification = formBinding.get().verification;
                int id = user.get().getId();
                if (accountService.verifyChangePassword(verification, id)) {
                    accountService.removeChangePassword(id);
                    accountService.updatePassword(user.get(), formBinding.get());
                    return redirect("/login");
                } else {
                    return badRequest(views.html.login.changepassword.render(formBinding, username, session()));
                }
            }
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

    private void sendForgotPasswordMail(String mail, String verification, ViewableUser user) {
        if (!checkMail(mail))
            return;

        String title = "ReStart - Forgot Password - " + user.getUsername();
        mails.sendEmail(title, mail, "A forgot password code has been generated. Please change your password by using verification code: " + verification + " on: restart-webshop.herokuapp.com/login/changepassword/" + user.getUsername() + " . If you do not recognise this activity please go to: restart-webshop.herokuapp.com/login/resetchangepassword/" + user.getUsername() + " to delete this activity.");
    }

    private boolean checkMail(String mail) {
        return mail != null && mail.contains("@");
    }
}
