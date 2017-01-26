package com.koadr;


import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

import static com.koadr.RandomIntegerActor.GetInteger;

public class SagaActor extends AbstractLoggingActor {

    private final SagaState sagaState = new SagaState();
    private final Metrics metrics;
    private Timer timer;
    private final Counter counter;
    private final RateMeter sagaCreateRate;
    private final RateMeter sagaDeleteRate;
    private final ActorRef int2Actor;
    private final ActorRef word2Actor;
    private final ActorRef int3Actor;

    static Props props(Metrics metrics,ActorRef intActor, ActorRef wordActor, ActorRef int2Actor, ActorRef word2Actor, ActorRef int3Actor) {
        return Props.create(SagaActor.class, () -> new SagaActor(metrics, intActor, wordActor, int2Actor, word2Actor, int3Actor));
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

    SagaActor(Metrics metrics, ActorRef intActor, ActorRef wordActor, ActorRef int2Actor, ActorRef word2Actor, ActorRef int3Actor) {
        this.int2Actor = int2Actor;
        this.word2Actor = word2Actor;
        this.int3Actor = int3Actor;
        this.metrics = metrics;
        this.counter = metrics.startCounter(this.getClass().getCanonicalName() + ".active");
        this.sagaCreateRate = metrics.measureRate("Saga/CreateRate");
        this.sagaDeleteRate = metrics.measureRate("Saga/DeleteRate");

        receive(ReceiveBuilder
                .match(Process.class, p -> {
                    intActor.tell(RandomIntegerActor.GetInteger.getInstance(), self());
                    wordActor.tell(p.getWord(), self());
                })
                .match(Integer.class, n -> {
                    sagaState.setNumber(n);
                    moveToStep2();
                })
                .match(RandWordAppendActor.Word.class, w -> {
                    sagaState.setWord(w.getWord());
                    moveToStep2();
                })
                .matchAny(o -> log().warning("received unknown message {}", o))
                .build()
        );


    }

    private void moveToStep2() {
        if (sagaState.isCompleteStep1()) {
            log().info("Step1 finished: {}", sagaState);
            int2Actor.tell(RandomIntegerActor.GetInteger.getInstance(), self());
            word2Actor.tell("STEP2Word", self());
            context().become(ReceiveBuilder
                    .match(Integer.class, n -> {
                        sagaState.setNumber2(n);
                        moveToStep3();
                    })
                    .match(RandWordAppendActor.Word.class, w -> {
                        sagaState.setWord2(w.getWord());
                        moveToStep3();
                    })
                    .matchAny(o -> log().warning("received unknown message {}", o))
                    .build()
            );
        }
    }
    private void moveToStep3() {
        if (sagaState.isCompleteStep2()) {
            log().info("Step2 finished: {}", sagaState);
            int3Actor.tell(RandomIntegerActor.GetInteger.getInstance(), self());
            context().become(ReceiveBuilder
                    .match(Integer.class, n -> {
                        sagaState.setNumber3(n);
                        if (sagaState.isCompleteStep3()) {
                            log().info("Step3 finished: {}", sagaState);
                            context().stop(self());
                        }
                    })
                    .matchAny(o -> log().warning("received unknown message {}", o))
                    .build()
            );
        }
    }

    @Override
    public void preStart() throws Exception {
        timer = metrics.processTime(this.getClass().getCanonicalName() + ".time");
        counter.increment();
        sagaCreateRate.mark();
    }

    @Override
    public void postStop() throws Exception {
        timer.stop();
        counter.decrement();
        sagaDeleteRate.mark();
    }
}
