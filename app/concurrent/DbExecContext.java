package concurrent;

import akka.actor.ActorSystem;
import play.libs.concurrent.CustomExecutionContext;

import javax.inject.Inject;

/**
 * A {@link CustomExecutionContext} to define a custom configured thread pool
 * specifically for blocking database access.
 * @author Sino
 */
public final class DbExecContext extends CustomExecutionContext {
	/**
	 * Creates a new {@link DbExecContext}.
	 */
	@Inject
	public DbExecContext(ActorSystem actorSystem) {
		super(actorSystem, "db-dispatcher");
	}
}
