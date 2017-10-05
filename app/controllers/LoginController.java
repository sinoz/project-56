package controllers;

import forms.LoginForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import services.auth.PlayAuthenticationService;
import views.html.login.index;
import views.html.login.success;

import javax.inject.Inject;

/**
 * A {@link Controller} for the contact page.
 *
 * @author Daryl Bakhuis
 * @author Johan van der Hoeven
 */
public final class LoginController extends Controller {
    @Inject private FormFactory formFactory;
    PlayAuthenticationService authService;

    public Result index() {
        return ok(index.render(formFactory.form(LoginForm.class)));
    }

    public Result login(){
        Form<LoginForm> formBinding = formFactory.form(LoginForm.class).bindFromRequest();
        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return badRequest(views.html.login.index.render(formBinding));
        } else {
            LoginForm form = formBinding.get();
            authService = new PlayAuthenticationService();

            String username = form.getUsername();
            String password = form.getPassword();
            boolean remember = form.getRemember();

            if(authService.authenticateUser(username, password)){
                //TODO Login User
            }

            return ok(success.render());
        }
    }
}
