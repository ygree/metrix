package com.koadr;


public class ReactiveCounter implements Counter {
    private com.codahale.metrics.Counter counter;

    public ReactiveCounter(com.codahale.metrics.Counter counter) {
        this.counter = counter;
    }

    @Override
    public void increment() {
        counter.inc();
    }

    @Override
    public void decrement() {
        counter.dec();
    }
}
