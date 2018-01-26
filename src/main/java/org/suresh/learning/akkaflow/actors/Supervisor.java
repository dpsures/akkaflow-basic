package org.suresh.learning.akkaflow.actors;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ActorRefRoutee;
import akka.routing.Routee;
import akka.routing.Router;
import akka.routing.SmallestMailboxRoutingLogic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.suresh.learning.akkaflow.beans.Task;
import org.suresh.learning.akkaflow.extension.SpringExtension;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class Supervisor extends UntypedActor {

	private final LoggingAdapter log = Logging.getLogger(getContext().system(), "Supervisor");

	@Autowired
	private SpringExtension springExtension;

	private Router router;

	@Override
	public void preStart() throws Exception {

		log.info("Starting up");

		List<Routee> routees = new ArrayList<Routee>();
		for (int i = 0; i < 100; i++) {
			ActorRef actor = getContext().actorOf(springExtension.props("taskActor"));
			getContext().watch(actor);
			routees.add(new ActorRefRoutee(actor));
		}
		
		router = new Router(new SmallestMailboxRoutingLogic(), routees);
		super.preStart();
	}

	@Override
	public void onReceive(Object message) throws Exception {

		if (message instanceof Task) {
			router.route(message, getSender());
			//log.info("[Task Message : ]", message);
		} else if (message instanceof Terminated) {
			// Readd task actors if one failed
			router = router.removeRoutee(((Terminated) message).actor());
			ActorRef actor = getContext().actorOf(springExtension.props("taskActor"));
			getContext().watch(actor);
			//log.info("[Terminated actor : ]", actor);
			router = router.addRoutee(new ActorRefRoutee(actor));
		} else {
			log.error("Unable to handle message {}", message);
		}
	}

	@Override
	public void postStop() throws Exception {
		log.info("Shutting down");
		super.postStop();
	}
}