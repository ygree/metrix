package com.koadr;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String [] args){
        final MetricRegistry registry = new MetricRegistry();
        final Counter activeSagas = registry.counter(MetricRegistry.name(SagaActor.class,"active-sagas"));
        final ConsoleReporter reporter = ConsoleReporter.forRegistry(registry).build();
        reporter.start(1000, TimeUnit.MILLISECONDS);
        final ActorSystem system = ActorSystem.create("MetrixSystem");
        ActorRef intActor = system.actorOf(RandomIntegerActor.props(registry));
        int counter = 10;
        while (counter > 0) {
            ActorRef saga = system.actorOf(SagaActor.props(registry, activeSagas, intActor));
            saga.tell(RandomIntegerActor.GetInteger.getInstance(), ActorRef.noSender());
            counter--;
        }
    }
}
