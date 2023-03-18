package com.lx.rsm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class TickManager {
    public static int ticks = 0;
    public static ConcurrentHashMap<Integer, Runnable> scheduleList = new ConcurrentHashMap<>();

    public static void schedule(int ticksAfter, Runnable callback) {
        scheduleList.put(ticks + ticksAfter, callback);
    }

    public static void onTick() {
        ticks++;
        for(Map.Entry<Integer, Runnable> entry : scheduleList.entrySet()) {
            if(ticks >= entry.getKey()) {
                scheduleList.remove(entry.getKey());
                entry.getValue().run();
            }
        }
    }
}