package org.suresh.learning.akkaflow;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.suresh.learning.akkaflow.beans.Task;
import org.suresh.learning.akkaflow.extension.SpringExtension;

import java.util.Random;

@Configuration
@EnableAutoConfiguration
@ComponentScan("org.suresh.learning.akkaflow.configuration")
public class RunAkkaApplication {

    public static void main(String[] args) throws Exception {        
        
        ApplicationContext context = SpringApplication.run(RunAkkaApplication.class, args);

        ActorSystem system = context.getBean(ActorSystem.class);
        
        final LoggingAdapter log = Logging.getLogger(system, "Application");
        
        log.info("[Actor System :]", system);
        
        log.info("Starting up");

        SpringExtension ext = context.getBean(SpringExtension.class);

        ActorRef supervisor = system.actorOf(ext.props("supervisor").withMailbox("akka.priority-mailbox"));
        
        log.info("[Supervisor :]", supervisor);
        
        for (int i = 1; i < 1000000; i++) {
            Task task = new Task("payload " + i, new Random().nextInt(99));
            //log.info("[Task Created : ]",task.toString());
            supervisor.tell(task, null);
        }

        supervisor.tell(PoisonPill.getInstance(), null);

        while (!supervisor.isTerminated()) {
            Thread.sleep(100);
        }

        log.info("Created {} tasks", context.getBean(JdbcTemplate.class)
            .queryForObject("SELECT COUNT(*) FROM tasks", Integer.class));

        log.info("Shutting down");

        system.shutdown();
        system.awaitTermination();
    }
}
