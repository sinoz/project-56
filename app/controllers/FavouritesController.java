package controllers;

import forms.FavouriteForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import services.FavouritesService;

import javax.inject.Inject;

/**
 * A {@link Controller} for the Favourites page.
 *
 * @author Johan van der Hoeven
 */
public final class FavouritesController extends Controller{
    /**
     * A {@link FormFactory} to use forms.
     */
    private FormFactory formFactory;

    private FavouritesService favouritesService;

    @Inject
    public FavouritesController(FormFactory formFactory, FavouritesService favouritesService){
        this.formFactory = formFactory;
        this.favouritesService = favouritesService;
    }

    public Result index() {
        String loggedInAs = session().get("loggedInAs");
        if (loggedInAs == null || loggedInAs.length() == 0) {
            return redirect("/login");
        } else {
            return ok(views.html.favourites.index.render(session()));
        }
    }

    /**
     * The method that adds/deletes a game to a users favourites
     */
    public Result addFavourite() {
        Form<FavouriteForm> formBinding = formFactory.form(FavouriteForm.class).bindFromRequest();

        if (formBinding.hasGlobalErrors() || formBinding.hasErrors()) {
            return badRequest();
        } else {
            FavouriteForm form = formBinding.get();
            String prodId = form.getId();

            favouritesService.add(prodId, session().get("loggedInAs"));

            return redirect("/products/selected/" + prodId);
        }
    }
}
