package org.suresh.learning.akkaflow.mailbox;

import akka.actor.ActorSystem;
import akka.dispatch.PriorityGenerator;
import akka.dispatch.UnboundedPriorityMailbox;

import org.suresh.learning.akkaflow.beans.Task;

import com.typesafe.config.Config;

public class PriorityMailbox extends UnboundedPriorityMailbox {

	public PriorityMailbox(ActorSystem.Settings settings, Config config) {

		// Create a new PriorityGenerator, lower priority means more important
		super(new PriorityGenerator() {

			@Override
			public int gen(Object message) {
				if (message instanceof Task) {
					return ((Task) message).getPriority();
				} else {
					// default
					return 100;
				}
			}
		});

	}
}