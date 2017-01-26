package com.koadr;


import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
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
    private final int interval;

    private final Queue<WordProcess> queue = new LinkedList<>();
    private final String[] words = {"quarter","plug","stage","ground","dependence","patch","concentrate","primary","sentiment","account","cope","ambiguous","snake", "mask","register"};

    static Props props(Metrics metrics, String name, int interval) {
        return Props.create(RandWordAppendActor.class, () -> new RandWordAppendActor(metrics, name, interval));
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

    RandWordAppendActor(Metrics metrics, String name, int interval) {
        this.interval = interval;
        metrics.measureQueue(name + "/QueueSize", queue);
        RateMeter tickRate = metrics.measureRate(name + "/TickRate");
        RateMeter processingRate = metrics.measureRate(name + "/ProcessingRate");

        receive(
            ReceiveBuilder.
                match(String.class, s -> {
                    WordProcess p = new WordProcess(s, sender());
                    queue.add(p);
                    log().info("{} passed", s);
                }).match(Tick.class, t -> {
                    tickRate.mark();
                    if (!queue.isEmpty()) {
                        processingRate.mark();
                        WordProcess process = queue.remove();
                        String word = process.getWord();
                        int rnd = new Random().nextInt(words.length);
                        String appendee = words[rnd];
                        log().info("{} processed", process.getWord() + appendee);
                        context().system().scheduler().scheduleOnce(
                                Duration.create(500, TimeUnit.MILLISECONDS),
                                process.getRef(),
                                new Word(word + appendee),
                                context().dispatcher(),
                                ActorRef.noSender()
                        );
//                        Thread.sleep(500);
                        process.getRef().tell(new Word(word + appendee), ActorRef.noSender());
                    }
                }).matchAny(o -> log().warning("received unknown message {}", o)).build()
        );
    }

    @Override
    public void preStart() throws Exception {
        context().
                system().
                scheduler().
                schedule(
                        Duration.create(interval, TimeUnit.MILLISECONDS),
                        Duration.create(interval, TimeUnit.MILLISECONDS),
                        () -> self().tell(Tick.getInstance(), sender()),
                        context().dispatcher()
                );
        super.preStart();
    }
}
