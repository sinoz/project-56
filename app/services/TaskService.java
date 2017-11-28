package services;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import task.Task;
import task.TaskRouter;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A service that supervises over task processors.
 * @author I.A
 */
@Singleton
public final class TaskService {
	/**
	 * The router of task processors.
	 */
	private final ActorRef processorsRouter;

	/**
	 * Creates a new {@link TaskService}.
	 */
	@Inject
	public TaskService(ActorSystem system) {
		this.processorsRouter = system.actorOf(TaskRouter.props());
	}

	/**
	 * Sends the given {@link Task} to a child processor in a fire-and forget fashion.
	 */
	public void tell(Task task) {
		processorsRouter.tell(task, ActorRef.noSender());
	}

	// TODO implement the ask pattern to get a result back
}
