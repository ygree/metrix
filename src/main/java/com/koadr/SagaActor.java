package com.koadr;


import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

public class SagaActor extends AbstractLoggingActor {

    private final SagaState sagaState = new SagaState(Integer.MAX_VALUE, "");
    private final MetricRegistry registry;
    private final Counter activeSagas;

    static Props props(MetricRegistry registry, Counter activeSagas, ActorRef intActor) {
        return Props.create(SagaActor.class, () -> new SagaActor(registry, activeSagas, intActor));
    }

    SagaActor(MetricRegistry registry, Counter activeSagas, ActorRef intActor) {
        this.registry = registry;
        this.activeSagas = activeSagas;

        receive(
                ReceiveBuilder.
                        match(RandomIntegerActor.GetInteger.class, s -> {
                            intActor.tell(s,self());
                        }).match(Integer.class, n -> {
                            sagaState.setNumber(n);
                            context().stop(self());
                        }).match(
                        String.class, s -> {
                            ActorRef wordAppender = context().actorOf(RandWordAppendActor.props(registry));
                            wordAppender.tell(s,self());
                        }).match(RandWordAppendActor.Word.class, w -> {
                             sagaState.setWord(w.getWord());
                        }).matchAny(o -> log().warning("received unknown message {}", o)).build()
        );


    }

    @Override
    public void preStart() throws Exception {
        activeSagas.inc();
    }

    @Override
    public void postStop() throws Exception {
        activeSagas.dec();
    }
}
