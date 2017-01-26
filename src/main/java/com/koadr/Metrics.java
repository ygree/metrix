package com.koadr;


import java.util.Queue;



public interface Metrics {
    void measureQueue(String name, Queue<?> queue);
    Timer processTime(String name);
    Counter startCounter(String name);
    RateMeter measureRate(String name);
}
