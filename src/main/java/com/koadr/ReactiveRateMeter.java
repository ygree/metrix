package com.koadr;

import com.codahale.metrics.Meter;

/**
 * Created by ygribkov on 1/26/17.
 */
public class ReactiveRateMeter implements RateMeter {
    private final com.codahale.metrics.Meter meter;

    public ReactiveRateMeter(Meter meter) {
        this.meter = meter;
    }

    @Override
    public void mark() {
        meter.mark();
    }
}

