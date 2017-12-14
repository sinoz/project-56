package task;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.RoundRobinPool;

/**
 * Enroutes {@link Task}s to an internal router of child {@link TaskProcessor}s.
 * @author I.A
 */
public final class TaskRouter extends AbstractActor {
	/**
	 * A factory method to produce a {@link Props} for this {@link TaskRouter} actor.
	 * @return The {@link Props} used to configure this {@link TaskRouter}.
	 */
	public static Props props() {
		return Props.create(TaskRouter.class);
	}

	/**
	 * The amount of child {@link TaskProcessor}s to instantiate for the internal router.
	 */
	private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors();

	/**
	 * The internal router of {@link TaskProcessor}s.
	 */
	private final ActorRef router = getContext().actorOf(new RoundRobinPool(POOL_SIZE).props(TaskProcessor.props(getSelf())), "task-processors");

	/**
	 * Prevents public instantiation. To instantiate this as an actor, call {@link TaskRouter#props()}.
	 */
	private TaskRouter() { }

	@Override
	public Receive createReceive() {
		return receiveBuilder()
					.match(Task.class, this::forwardToChildProcessor)
					.build();
	}

	/**
	 * Forwards the given {@link Task} to a child {@link TaskProcessor} actor.
	 */
	private void forwardToChildProcessor(Task task) {
		router.forward(task, getContext());
	}
}
