import com.google.inject.AbstractModule;
import services.AccountService;
import services.AuthenticationService;
import services.ProductService;

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

	private void bindServices() {
		bind(AccountService.class).asEagerSingleton();
		bind(AuthenticationService.class).asEagerSingleton();
		bind(ProductService.class).asEagerSingleton();
	}

	private void bindOthers() {
		bind(Clock.class).toInstance(Clock.systemDefaultZone());
	}
}
