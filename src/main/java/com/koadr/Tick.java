package com.koadr;


public class Tick {
    private static Tick ourInstance = new Tick();

    public static Tick getInstance() {
        return ourInstance;
    }

    private Tick() {
    }
}
