package controllers;

import forms.SessionMailForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import services.SessionService;

import javax.inject.Inject;

/**
 * @author: Maurice van Veen
 */
public class ProductCheckoutMailController extends Controller {

    /**
     * A {@link FormFactory} to use search forms.
     */
    private FormFactory formFactory;

    @Inject
    public ProductCheckoutMailController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    public Result index(String redirect) {
        Form<SessionMailForm> form = formFactory.form(SessionMailForm.class).bindFromRequest();
        if (form.hasErrors() || form.hasGlobalErrors()) {
            return ok(views.html.checkout.mail.render(redirect, form));
        } else {
            SessionMailForm sessionMailForm = form.get();

            SessionService.setMail(session(), sessionMailForm.getMail());
            SessionService.setValidTime(session());

            return redirect(redirect);
        }
    }
}