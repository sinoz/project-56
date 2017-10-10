package controllers;

import forms.SearchForm;
import play.data.Form;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.products.index;

import javax.inject.Inject;

/**
 * A {@link Controller} that handles searches.
 *
 * @author: Maurice van Veen
 * @author Johan van der Hoeven
 */
public class ProductsController extends Controller {
    /**
     * A {@link FormFactory} to use search forms.
     */
    private FormFactory formFactory;

    /**
     * The required {@link Database} dependency to fetch database connections.
     */
    private Database database;

    @Inject
    public ProductsController(FormFactory formFactory, Database database){
        this.formFactory = formFactory;
        this.database = database;
    }

    public Result searchGames(){
        Form<SearchForm> formBinding = formFactory.form(SearchForm.class).bindFromRequest();
        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return badRequest();
        } else {
            SearchForm form = formBinding.get();
            //TODO get search results

            String formInput = form.getInput();
            return index("input=" + formInput);
        }
    }

    @Inject
    public static Form<SearchForm> getSearchForm(FormFactory formFactory){
        return formFactory.form(SearchForm.class);
    }

    public Result index(String token) {
        System.out.println("TOKEN: " + token);
        // TODO: connection stuff
        return ok(index.render(token, session()));
    }
}
