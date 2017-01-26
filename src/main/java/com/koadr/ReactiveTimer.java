package com.koadr;

/**
 * Created by craigpottinger on 1/20/17.
 */
public class ReactiveTimer implements Timer {
    private final com.codahale.metrics.Timer.Context context;


    public ReactiveTimer(com.codahale.metrics.Timer timer) {
        this.context = timer.time();
    }


    @Override
    public void stop() {
        context.stop();
    }
}
