package controllers;

import forms.MailerForm;
import models.ViewableUser;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import services.MailerService;
import services.SessionService;
import services.UserViewService;
import views.html.contact.index;

import javax.inject.Inject;
import java.util.Optional;

/**
 * A {@link Controller} for the contact page.
 *
 * @author Melle Nout
 * @author I.A
 * @author Joris Stander
 */
public final class ContactController extends Controller {
	/**
	 * A {@link FormFactory} to produce registration forms.
	 */
	private final FormFactory formFactory;

	private final UserViewService userViewService;
	private final MailerService mails;

	@Inject
	public ContactController(FormFactory formFactory, UserViewService userViewService, MailerService mails) {
		this.formFactory = formFactory;
		this.userViewService = userViewService;
		this.mails = mails;
	}

	/**
	 * Returns an OK result including the {@link views.html.contact.index} view.
	 */
	public Result index() {
		return ok(index.render(formFactory.form(MailerForm.class), getMail(), session()));
	}

	/**
	 * Attempts to send a mail. Returns either a {@link Controller#badRequest()} indicating
	 * a failure in sending a mail or a {@link Controller#ok()} result, indicating an
	 * email has been send.
	 */
	public Result mail() {
		Form<MailerForm> formBinding = formFactory.form(MailerForm.class).bindFromRequest();
		if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
			return badRequest(index.render(formBinding, getMail(), session()));
		} else {
			MailerForm form = formBinding.get();
			mails.sendEmail(form);
			return redirect("/");
		}
	}

	private String getMail() {
        if (SessionService.redirect(session())) {
            return "";
        } else {
			String loggedInAs = SessionService.getLoggedInAs(session());
            Optional<ViewableUser> user = userViewService.fetchViewableUser(loggedInAs);
            if (user.isPresent()) {
                return user.get().getMail();
            } else {
                return "";
            }
        }
    }
}