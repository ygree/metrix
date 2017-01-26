package com.koadr;


import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.codahale.metrics.Gauge;
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
    private final Metrics metrics;
    private final Queue<WordProcess> queue = new LinkedList<>();
    private final String[] words = {"quarter","plug","stage","ground","dependence","patch","concentrate","primary","sentiment","account","cope","ambiguous","snake", "mask","register"};

    static Props props(Metrics metrics) {
        return Props.create(RandWordAppendActor.class, () -> new RandWordAppendActor(metrics));
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

    public static class WordProcess {
        private String word;
        private ActorRef ref;

        public WordProcess(String word, ActorRef ref) {
            this.word = word;
            this.ref = ref;
        }

        public String getWord() {
            return word;
        }
        public ActorRef getRef() {
            return ref;
        }
    }

    RandWordAppendActor(Metrics metrics) {
        this.metrics = metrics;
        registerQueue();

        receive(
            ReceiveBuilder.
                match(String.class, s -> {
                    WordProcess p = new WordProcess(s, sender());
                    queue.add(p);
                    log().info("{} passed", s);
                }).match(Tick.class, t -> {
                    if (!queue.isEmpty()) {
                        WordProcess process = queue.remove();
                        String word = process.getWord();
                        int rnd = new Random().nextInt(words.length);
                        String appendee = words[rnd];
                        log().info("{} processed", process.getWord() + appendee);
                        process.getRef().tell(new Word(word + appendee), ActorRef.noSender());
                    }
                }).matchAny(o -> log().warning("received unknown message")).build()
        );
    }

    private void registerQueue() {
        metrics.measureWordQueue(queue);
    }

    @Override
    public void preStart() throws Exception {
        context().
                system().
                scheduler().
                schedule(
                        Duration.create(500, TimeUnit.MILLISECONDS),
                        Duration.create(50, TimeUnit.MILLISECONDS),
                        () -> self().tell(Tick.getInstance(), sender()),
                        context().dispatcher()
                );
        super.preStart();
    }
}
