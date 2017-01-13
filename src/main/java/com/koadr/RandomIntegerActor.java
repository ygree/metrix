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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RandomIntegerActor extends AbstractLoggingActor {
    private final Queue<String> queue = new LinkedList<>();

    public static class GetInteger {
        private static GetInteger ourInstance = new GetInteger();

        public static GetInteger getInstance() {
            return ourInstance;
        }

        private GetInteger() {
        }
    }

    static Props props(MetricRegistry registry) {
        return Props.create(RandomIntegerActor.class, () -> new RandomIntegerActor(registry));
    }

    enum Ticker {
        TICKER
    }


    RandomIntegerActor(MetricRegistry registry) {
        receive(
                ReceiveBuilder.
                        match(GetInteger.class, s -> {
                            String PID = UUID.randomUUID().toString();
                            log().info("Process {} added to queue", PID);
                            queue.add(PID);
                        }).match(
                            Ticker.class, t -> {
                                if (!queue.isEmpty()) {
                                    queue.remove();
                                    int value = new Random().nextInt();
                                    log().info("{} generated", value);
                                    context().parent().tell(value, ActorRef.noSender());
                                }
                            }
                        ).
                        matchAny(o -> log().warning("received unknown message")).build()
        );
    }

    @Override
    public void preStart() throws Exception {
        context().
                system().
                scheduler().
                schedule(
                        Duration.create(5000, TimeUnit.MILLISECONDS),
                        Duration.create(500, TimeUnit.MILLISECONDS),
                        () -> self().tell(Ticker.TICKER, ActorRef.noSender()),
                        context().dispatcher()
                );
        super.preStart();
    }
}
