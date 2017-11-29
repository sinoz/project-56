package controllers;

import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;
import services.SessionService;
import views.html.trackorder.index;

import javax.inject.Inject;

/**
 * A {@link Controller} for the track order page.
 *
 * @author Johan van der Hoeven
 * @author Melle Nout
 * @author I.A
 */
public final class TrackOrderController extends Controller {
    /**
     * The required {@link Database} dependency to fetch database connections.
     */
    private final play.db.Database database;

    @Inject
    public TrackOrderController(play.db.Database database){
        this.database = database;
    }

    public Result index() {
        if(SessionService.redirect(session(), database)){
            return redirect("/login");
        } else {
            return ok(index.render(session()));
        }
    }
}