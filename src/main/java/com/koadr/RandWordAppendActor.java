package com.koadr;


import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.codahale.metrics.MetricRegistry;
import scala.concurrent.duration.Duration;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;

// Active Sagas
// Failed Sagas
// Monitor Queue
// Time to Process

public class RandWordAppendActor extends AbstractLoggingActor {
    private final MetricRegistry registry;
    private final Queue<String> queue = new LinkedList<>();
    private final String[] words = {"quarter","plug","stage","ground","dependence","patch","concentrate","primary","sentiment","account","cope","ambiguous","snake", "mask","register"};

    enum Ticker {
        TICKER
    }

    static Props props(MetricRegistry registry) {
        return Props.create(RandWordAppendActor.class, () -> new RandWordAppendActor(registry));
    }

    public static class Word {
        private String word;

        public Word(String word) {
            this.word = word;
        }

        public String getWord() {
            return word;
        }
    }

    RandWordAppendActor(MetricRegistry registry) {
        this.registry = registry;

        receive(
            ReceiveBuilder.
                match(String.class, s -> {
                    queue.add(s);
                    log().info("{} passed", s);
                }).match(Ticker.class, t -> {
                    if (!queue.isEmpty()) {
                        String s = queue.remove();
                        int rnd = new Random().nextInt(words.length);
                        String word = words[rnd];
                        log().info("{} processed", s + word);
                        sender().tell(new Word(s + word), ActorRef.noSender());
                    }
                }).matchAny(o -> log().warning("received unknown message")).build()
        );
    }

    @Override
    public void preStart() throws Exception {
        context().
                system().
                scheduler().
                schedule(
                        Duration.create(500, TimeUnit.MILLISECONDS),
                        Duration.create(50, TimeUnit.MILLISECONDS),
                        () -> self().tell(Ticker.TICKER, sender()),
                        context().dispatcher()
                );
        super.preStart();
    }
}
