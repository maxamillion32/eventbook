package com.mhafizhasan.eventbook.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by someguy233 on 15-Nov-15.
 */
public class Cacher {
    static final String TAG = "Cacher";
    private static final Gson gson = new Gson();        // Standard gson

    public static final String CACHE_NAME = "jsoncache";
    public static final String CACHE_PREFIX = CACHE_NAME + "/";

    public static long CACHE_SIZE = 10 * 1024 * 1024;       // 10 mb
    public static long COMMIT_INTERVAL = 10 * 1000;       // 5 minutes

    public static final String LIST_LOCK_PREFIX = "Cacher/List/";
    public static final String FILE_LOCK_PREFIX = "Cacher/File/";

    public static void writeFile(File file, String data) throws IOException {
        FileOutputStream s = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(s);
        try {
            writer.write(data);
        } finally {
            writer.close();
        }
    }

    public static String readFile(File file) throws IOException {
        // Open file
        FileInputStream s = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(s));
        // Read every line
        try {
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                buffer.append(line);
            String data = buffer.toString();
            return buffer.toString();
        } finally {
            reader.close();
        }
    }


    private static class Committer implements Runnable {
        final Context context;

        Committer(Context context) {
            this.context = context.getApplicationContext();         // Always use application context for delayed operations
        }


        @Override
        public void run() {
            if(Thread.currentThread() == context.getMainLooper().getThread())
                AsyncTask.THREAD_POOL_EXECUTOR.execute(this);       // Execute in executor
            else
                Cacher.commit(context);
        }
    }

    private static final WeakHashMap<Object, String> cached = new WeakHashMap<>();
    private static final HashMap<String, Object> pending = new HashMap<>();
    private static long lastCommit = -1;
    private static long lastCacheSize = -1;

    public static NamedLock lock(String name) {
        return NamedLock.acquire(FILE_LOCK_PREFIX + Integer.toHexString(name.hashCode()));
    }

    public static void commit(Context context) {
        // First get all pending writes
        HashMap<String, Object> pending = new HashMap<>();
        synchronized(Cacher.class) {
            pending.putAll(Cacher.pending);
            Cacher.pending.clear();
        }
        Log.d(TAG, "Starting commit of " + pending.size() + " changes");
        // Prepare root dir
        File rootDir = new File(context.getCacheDir(), CACHE_NAME);
        if(!rootDir.exists())
            rootDir.mkdirs();
        // Write all
        for(Map.Entry<String, Object> e : pending.entrySet()) {
            String name = e.getKey();
            Object value = e.getValue();
            NamedLock lock = lock(name);
            File file = new File(context.getCacheDir(), CACHE_PREFIX + Integer.toHexString(name.hashCode()));
            try {
                HashMap<String, String> map;
                try {
                    if(!file.exists())
                        map = new HashMap<>();
                    else {
                        String data = readFile(file);
                        if(data.length() == 0 || ((map = gson.fromJson(data, HashMap.class)) == null))
                            map = new HashMap<>();      // new cache
                    }
                } catch (Throwable t) {
                    Log.e(TAG, "Unable to open cache for writing: " + file.getPath(), t);
                    map = new HashMap<>();      // create empty cache
                }
                if (value == null) {
                    if(map.remove(name) == null)
                        return;                 // nothing was removed, no changes
                    if (map.size() == 0) {
                        if(lastCacheSize != -1)
                            lastCacheSize -= file.length();
                        file.delete();          // no more data here
                    }
                } else {
                    if(lastCacheSize != -1)
                        lastCacheSize -= file.length();
                    map.put(name, gson.toJson(value));
                    writeFile(file, gson.toJson(map));
                    file.setLastModified(System.currentTimeMillis());
                    if(lastCacheSize != -1)
                        lastCacheSize += file.length();
                }
            } catch (Throwable t) {
                Log.e(TAG, "Unable to write cache " + file.getPath() + " for " + name, t);
                // ignore
            } finally {
                lock.unlock();
            }
        }
        // Next cleanup cache
        if(lastCacheSize != -1 && lastCacheSize < CACHE_SIZE)
            return;     // no need to cleanup cache
        lastCacheSize = 0;      // reset
        File[] files = rootDir.listFiles();
        long[] timestamps = new long[files.length];
        long[] size = new long[files.length];
        // Collect all data
        for (int c = 0; c < files.length; c++) {
            File file = files[c];
            if (file.isDirectory()) {
                files[c] = null;
                timestamps[c] = Long.MAX_VALUE;
                size[c] = 0;
            } else {
                timestamps[c] = file.lastModified();
                size[c] = file.length();
            }
            lastCacheSize += size[c];
        }
        // Keep trimming as needed
        while (lastCacheSize > CACHE_SIZE) {
            // Select oldest file
            int oldest = -1;
            long oldestTimestamp = Long.MAX_VALUE;
            for (int c = 0; c < files.length; c++) {
                if (timestamps[c] < oldestTimestamp) {
                    oldest = c;
                    oldestTimestamp = timestamps[c];
                }
            }
            // Delete this file
            lastCacheSize -= size[oldest];
            files[oldest].delete();
            timestamps[oldest] = Long.MAX_VALUE;
        }
        Log.d(TAG, "Trimmed cache to " + lastCacheSize + " bytes");
        // Done
    }

    public synchronized static void apply(Context context) {
        context = context.getApplicationContext();
        long currentTime = System.currentTimeMillis();
        if(lastCommit != -1 && (currentTime - lastCommit) < 0)     // Has committed, queue another commit
            return;         // too soon to compact
        Log.d(TAG, "Queueing commit in " + COMMIT_INTERVAL);
        // Post async update
        Handler handler = new Handler(context.getMainLooper());
        handler.postDelayed(new Committer(context), COMMIT_INTERVAL);
        lastCommit = currentTime + COMMIT_INTERVAL;
    }

    public synchronized static void store(Context context, String name, Object o) {
        // Remove previous entry in cached
        for(Map.Entry<Object, String> e : cached.entrySet()) {
            if(e.getValue().contentEquals(name)) {
                // Found cached
                cached.remove(e.getKey());
                break;
            }
        }
        // Put new entry
        if(o != null)
            cached.put(o, name);
        // Post async update
        pending.put(name, o);
        apply(context);
    }

    public synchronized static void remove(Context context, String name) {
        store(context, name, null);
    }

    public static <T> T fetch(Context context, String name, Class<T> type) {
        // Fetch from cache
        synchronized (Cacher.class) {
            for(Map.Entry<Object, String> e : cached.entrySet()) {
                if(e.getValue().contentEquals(name))
                    return (T) e.getKey();
            }
        }
        // Else need to fetch now
        File file = new File(context.getCacheDir(), CACHE_PREFIX + Integer.toHexString(name.hashCode()));
        NamedLock lock = lock(name);
        try {
            if (!file.exists())
                return null;        // does not exist
            HashMap<String, String> map;
            String data = readFile(file);
            if(data.length() == 0 || ((map = gson.fromJson(data, HashMap.class)) == null))
                return null;        // does not exist
            data = map.get(name);
            if (data == null)
                return null;        // does not exist
            // Update usage
            file.setLastModified(System.currentTimeMillis());
            // Save to cache
            T cache = gson.fromJson(data, type);
            synchronized (Cacher.class) {
                cached.put(cache, name);
            }
            // Return
            return cache;
        } catch (Throwable e) {
            Log.e(TAG, "Unable to read cache " + file.getPath() + " for " + name, e);
            return null;
        } finally {
            lock.unlock();
        }
    }

    public static class SequenceDescriptor {
        public int start;
        public int end;
    }

    public static <T> T fetch(Context context, String name, Class<T> type, int index) {
        NamedLock lock = NamedLock.acquire(LIST_LOCK_PREFIX + name);
        try {
            SequenceDescriptor descriptor = Cacher.fetch(context, name, SequenceDescriptor.class);
            if(descriptor == null)
                return null;
            int cursor = descriptor.start + index;
            if(cursor >= descriptor.end)
                return null;        // out of bounds
            return Cacher.fetch(context, name + "/" + cursor, type);
        } finally {
            lock.unlock();
        }
    }

    public static <T> List<T> fetch(Context context, String name, Class<T> type, int start, int count) {
        NamedLock lock = NamedLock.acquire(LIST_LOCK_PREFIX + name);
        try {
            SequenceDescriptor descriptor = Cacher.fetch(context, name, SequenceDescriptor.class);
            if(descriptor == null)
                return null;
            int length = descriptor.end - descriptor.start;
            int expectedLength = start + count;
            if(expectedLength > length) {
                count -= expectedLength - length;
                if(count <= 0)
                    return null;            // out of bounds
            }
            // Now fetch
            ArrayList<T> list = new ArrayList<>(count);
            for(int c = 0; c < count; c++) {
                int cursor = descriptor.start + start + c;
                T o = Cacher.fetch(context, name + "/" + cursor, type);
                if(o == null)
                    break;
                list.add(o);
            }
            return list;
        } finally {
            lock.unlock();
        }
    }

    public static <T> void replace(Context context, String name, T[] array, Class<T> type) {
        NamedLock lock = NamedLock.acquire(LIST_LOCK_PREFIX + name);
        try {
            SequenceDescriptor descriptor = new SequenceDescriptor();
            // Store all
            for(int c = 0; c < array.length; c++)
                Cacher.store(context, name + "/" + (descriptor.start + c), array[c]);
            // Save descriptor
            descriptor.end = array.length;
            Cacher.store(context, name, SequenceDescriptor.class);
        } finally {
            lock.unlock();
        }
    }

    public static <T> void patch(Context context, String name, List<T> list, Class<T> type) {
        NamedLock lock = NamedLock.acquire(LIST_LOCK_PREFIX + name);
        int size = list.size();
        try {
            // Get descriptor
            SequenceDescriptor descriptor = Cacher.fetch(context, name, SequenceDescriptor.class);
            if(descriptor != null) {
                T first = Cacher.fetch(context, name + "/" + descriptor.start, type);
                if (first != null) {
                    // Find the cached first entry in current list
                    int fidx = -1;
                    for (int c = 0; c < size; c++) {
                        if (list.get(c).equals(first)) {
                            fidx = c;
                            break;
                        }
                    }
                    // If can use previous cached data, just extend list
                    if (fidx != -1)
                        descriptor.start -= fidx;
                    else
                        descriptor = new SequenceDescriptor();   // Else not a continuation of previous data, just replace list
                }
                else
                    descriptor = new SequenceDescriptor();      // list decached
            }
            else
                descriptor = new SequenceDescriptor();      // list does not exist, or decached
            // Store all
            for(int c = 0; c < size; c++)
                Cacher.store(context, name + "/" + (descriptor.start + c), list.get(c));
            // Save descriptor
            descriptor.end = Math.max(descriptor.start + size, descriptor.end);
            Cacher.store(context, name, descriptor);
        } finally {
            lock.unlock();
        }
    }

}
