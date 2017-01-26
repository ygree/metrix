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

public class RandomIntegerActor extends AbstractLoggingActor {
    private final int interval;

    private final Queue<ActorRef> queue = new LinkedList<>();

    public static class GetInteger {
        private static GetInteger ourInstance = new GetInteger();

        public static GetInteger getInstance() {
            return ourInstance;
        }

        private GetInteger() {
        }
    }

    static Props props(Metrics metrics, String name, int interval) {
        return Props.create(RandomIntegerActor.class, () -> new RandomIntegerActor(metrics, name, interval));
    }


    RandomIntegerActor(Metrics metrics, String name, int interval) {
        this.interval = interval;
        metrics.measureQueue(name + "/QueueSize", queue);
        RateMeter tickRate = metrics.measureRate(name + "/TickRate");
        RateMeter processingRate = metrics.measureRate(name + "/ProcessingRate");

        receive(
                ReceiveBuilder.
                        match(GetInteger.class, s -> {
                            queue.add(sender());
                        }).match(
                            Tick.class, t -> {
                                tickRate.mark();
                                if (!queue.isEmpty()) {
                                    processingRate.mark();
                                    ActorRef orgSender = queue.remove();
                                    int value = new Random().nextInt();
                                    log().info("{} generated", value);

                                    context().system().scheduler().scheduleOnce(
                                            Duration.create(500, TimeUnit.MILLISECONDS),
                                            orgSender,
                                            value,
                                            context().dispatcher(),
                                            ActorRef.noSender()
                                    );
//                                    Thread.sleep(500);
                                    orgSender.tell(value, ActorRef.noSender());
                                }
                            }
                        ).
                        matchAny(o -> log().warning("received unknown message {}", o)).build()
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
                        () -> self().tell(Tick.getInstance(), ActorRef.noSender()),
                        context().dispatcher()
                );
        super.preStart();
    }
}
