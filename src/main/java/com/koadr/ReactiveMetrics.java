package com.koadr;

import akka.actor.ActorRef;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

class ReactiveMetrics implements Metrics {
    private static MetricRegistry registry = new MetricRegistry();
    private static ReactiveMetrics ourInstance = new ReactiveMetrics();

    public static ReactiveMetrics getInstance() {
        return ourInstance;
    }

    private ReactiveMetrics() {
        final ConsoleReporter reporter = ConsoleReporter.forRegistry(registry).build();
        reporter.start(1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void measureIntegerQueue(Queue<ActorRef> queue) {
        registry.register(name(RandomIntegerActor.class, "size"), (Gauge<Integer>) queue::size);
    }

    @Override
    public void measureWordQueue(Queue<RandWordAppendActor.WordProcess> queue) {
        registry.register(name(RandWordAppendActor.class, "size"), (Gauge<Integer>) queue::size);

    }

    @Override
    public Timer processTime(String name) {
        com.codahale.metrics.Timer timer = registry.timer(name);
        return new ReactiveTimer(timer);
    }

    @Override
    public Counter startCounter(String name) {
        com.codahale.metrics.Counter counter = registry.counter(name);
        return new ReactiveCounter(counter);
    }
}