package com.koadr;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class App {
    public static void main(String [] args){
        final ReactiveMetrics metrics = ReactiveMetrics.getInstance();
        final ActorSystem system = ActorSystem.create("MetrixSystem");
        ActorRef intActor = system.actorOf(RandomIntegerActor.props(metrics));
        ActorRef wordActor = system.actorOf(RandWordAppendActor.props(metrics));
        int counter = 100;
        while (counter > 0) {
            ActorRef saga = system.actorOf(SagaActor.props(metrics, intActor, wordActor));
            saga.tell(new SagaActor.Process("HELLO"), ActorRef.noSender());
            counter--;
        }
    }
}
