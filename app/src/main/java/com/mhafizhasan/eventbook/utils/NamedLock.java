package com.mhafizhasan.eventbook.utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by someguy233 on 17-Nov-15.
 */
public class NamedLock {

    private static final ConcurrentHashMap<String, NamedLock> locks = new ConcurrentHashMap<>();

    public static NamedLock acquire(String name) {
        return acquire(name, Long.MAX_VALUE);
    }

    public static NamedLock acquire(String name, long millis) {
        long startTime;
        if (millis <= 0 || millis == Long.MAX_VALUE)
            startTime = -1;
        else
            startTime = System.currentTimeMillis();
        NamedLock lock = new NamedLock(name);
        NamedLock existingLock;
        while ((existingLock = locks.putIfAbsent(name, lock)) != null) {
            if (existingLock.thread == lock.thread && existingLock.lock())
                return existingLock;        // locked by current thread and successfully incremented usage counter
            // Else not locked by current thread, need to wait
            if (millis != Long.MAX_VALUE && (millis <= 0 || (System.currentTimeMillis() - startTime) > millis))
                return null;        // not acquired and done waiting
            // Else wait and try again
            Thread.yield();
        }
        // Done, using new lock
        return lock;
    }

    private final String name;
    private final Thread thread;
    private int count = 1;

    private NamedLock(String name) {
        this.name = name;
        this.thread = Thread.currentThread();
    }

    public synchronized boolean lock() {
        if (count == 0)
            return false;
        count++;
        return true;
    }


    public synchronized void unlock() {
        if (count == 0)
            throw new IllegalStateException("Already unlocked");
        count--;
        if (count == 0)
            locks.remove(name, this);
    }
}
