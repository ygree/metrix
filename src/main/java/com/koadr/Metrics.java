package com.koadr;


import akka.actor.ActorRef;

import java.util.Queue;

import static com.koadr.RandWordAppendActor.WordProcess;


public interface Metrics {
    void measureIntegerQueue(Queue<ActorRef> queue);
    void measureWordQueue(Queue<WordProcess> queue);
    Timer processTime(String name);
    Counter startCounter(String name);
}
