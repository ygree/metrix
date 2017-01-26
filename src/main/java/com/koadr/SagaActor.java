package com.koadr;


import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

import static com.koadr.RandomIntegerActor.GetInteger;

public class SagaActor extends AbstractLoggingActor {

    private final SagaState sagaState = new SagaState(Integer.MAX_VALUE, "");
    private final Metrics metrics;
    private Timer timer;
    private final Counter counter;

    static Props props(Metrics metrics,ActorRef intActor, ActorRef wordActor) {
        return Props.create(SagaActor.class, () -> new SagaActor(metrics, intActor, wordActor));
    }

    public static class Process {
        private String word;

        public Process(String word) {
            this.word = word;
        }

        public String getWord() {
            return word;
        }
    }

    SagaActor(Metrics metrics,ActorRef intActor, ActorRef wordActor) {
        this.metrics = metrics;
        this.counter = metrics.startCounter(this.getClass().getCanonicalName() + ".active");

        receive(
                ReceiveBuilder.match(Process.class, p -> {
                    intActor.tell(GetInteger.getInstance(), self());
                    wordActor.tell(p.getWord(), self());
                }).
                    match(GetInteger.class, s -> {
                        intActor.tell(s,self());
                    }).match(Integer.class, n -> {
                        sagaState.setNumber(n);
                        if (isComplete()) {
                            log().info("Process finished: {}", sagaState);
                            context().stop(self());
                        }
                    }).match(
                    String.class, s -> {
                        wordActor.tell(s,self());
                    }).match(RandWordAppendActor.Word.class, w -> {
                        sagaState.setWord(w.getWord());
                        if (isComplete()) {
                            log().info("Process finished: {}", sagaState);
                            context().stop(self());
                        }
                    }).matchAny(o -> log().warning("received unknown message {}", o)).build()
        );


    }

    private Boolean isComplete() {
        return sagaState.getNumber() < Integer.MAX_VALUE && !sagaState.getWord().isEmpty();
    }

    @Override
    public void preStart() throws Exception {
        timer = metrics.processTime(this.getClass().getCanonicalName() + ".time");
        counter.increment();
    }

    @Override
    public void postStop() throws Exception {
        timer.stop();
        counter.decrement();
    }
}
