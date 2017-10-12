import com.typesafe.config.Config;
import play.Environment;
import play.api.OptionalSourceMapper;
import play.api.routing.Router;
import play.http.DefaultHttpErrorHandler;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * A {@link DefaultHttpErrorHandler} that catches a page failure to react to it.
 * @author I.A
 */
@Singleton
public final class ErrorHandler extends DefaultHttpErrorHandler {
	@Inject
	public ErrorHandler(Config config, Environment environment, OptionalSourceMapper sourceMapper, Provider<Router> routes) {
		super(config, environment, sourceMapper, routes);
	}

	protected CompletionStage<Result> onNotFound(Http.RequestHeader request, String message) {
		return CompletableFuture.completedFuture(Results.notFound(views.html._error_.index.render()));
	}
}