package task;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * A child worker that processes {@link Task}s.
 * @author I.A
 */
public final class TaskProcessor extends AbstractActor {
	/**
	 * A factory method to produce a {@link Props} for this {@link TaskProcessor} actor.
	 * @return The {@link Props} used to configure this {@link TaskProcessor}.
	 */
	public static Props props(ActorRef router) {
		return Props.create(TaskProcessor.class, () -> new TaskProcessor(router));
	}

	/**
	 * The parent router of this child processor.
	 */
	private final ActorRef router;

	/**
	 * Creates a new {@link TaskProcessor}. Access limited to private to prevent public instantiation.
	 * To instantiate this as an actor, call {@link TaskProcessor#props(ActorRef)}.
	 */
	public TaskProcessor(ActorRef router) {
		this.router = router;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(Task.class, this::onTask)
				.build();
	}

	/**
	 * Reacts to receiving a {@link Task}.
	 */
	private void onTask(Task task) {
		System.out.println("Hello World");
	}
}
