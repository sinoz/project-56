import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import services.auth.AuthenticationService;
import services.Service;
import services.ServiceConstants;
import services.auth.PlayAuthenticationService;

import java.time.Clock;

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.
 * <p>
 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
public final class Module extends AbstractModule {
	@Override
	public void configure() {
		bindServices();
		bindOthers();
	}

	/**
	 * Binds all of the {@link Service} implementations.
	 */
	private void bindServices() {
		bind(AuthenticationService.class).annotatedWith(Names.named(ServiceConstants.DEFAULT_AUTH)).to(PlayAuthenticationService.class);
	}

	/**
	 * Binds all of the other dependencies.
	 */
	private void bindOthers() {
		bind(Clock.class).toInstance(Clock.systemDefaultZone());
	}
}
