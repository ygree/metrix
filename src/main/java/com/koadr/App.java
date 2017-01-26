package com.koadr;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class App {
    public static void main(String [] args){
        final ReactiveMetrics metrics = ReactiveMetrics.getInstance();
        final ActorSystem system = ActorSystem.create("MetrixSystem");
        ActorRef intActor = system.actorOf(RandomIntegerActor.props(metrics, "1-int-tool-ref", 200));
        ActorRef wordActor = system.actorOf(RandWordAppendActor.props(metrics, "1-word-devs", 200));
        ActorRef int2Actor = system.actorOf(RandomIntegerActor.props(metrics, "2-int-submit", 1));
        ActorRef word2Actor = system.actorOf(RandWordAppendActor.props(metrics, "2-word-idm", 1));
        ActorRef int3Actor = system.actorOf(RandomIntegerActor.props(metrics, "3-int-ais", 1));
        int counter = 10000;
        while (counter > 0) {
            ActorRef saga = system.actorOf(SagaActor.props(metrics, intActor, wordActor, int2Actor, word2Actor, int3Actor));
            saga.tell(new SagaActor.Process("HELLO"), ActorRef.noSender());
            counter--;
        }
    }
}
