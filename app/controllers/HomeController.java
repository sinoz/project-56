package controllers;

import play.mvc.*;

import services.SampleService;
import views.html.*;

import javax.inject.Inject;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public final class HomeController extends Controller {
    /**
     * The {@link SampleService} to demonstrate how to use Guice.
     */
    private final SampleService sampleService;

    /**
     * Creates a new {@link HomeController}.
     */
    @Inject
    public HomeController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        sampleService.doSomething();

        return ok(index.render("Your new application is ready."));
    }
}
