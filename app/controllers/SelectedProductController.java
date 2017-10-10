package controllers;

import forms.SearchForm;
import play.data.Form;
import play.data.FormFactory;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.selectedproduct.index;

import javax.inject.Inject;

/**
 * A {@link Controller} that handles searches.
 *
 * @author: Joris Stander
 *
 */
public class SelectedProductController extends Controller {
    /**
     * A {@link FormFactory} to use search forms.
     */
    private FormFactory formFactory;

    /**
     * The required {@link Database} dependency to fetch database connections.
     */
    private Database database;

    @Inject
    public SelectedProductController(FormFactory formFactory, Database database){
        this.formFactory = formFactory;
        this.database = database;
    }

    public Result index(String token) {
        return ok(views.html.selectedproduct.index.render(token, session()));
    }
}
