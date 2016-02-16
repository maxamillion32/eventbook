package com.mhafizhasan.eventbook.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by someguy233 on 30-Oct-15.
 */
public class Storage {

    public static String PREF_FILE = "default";

    private static final Gson gson = new Gson();        // Standard gson

    private static WeakHashMap<Object, String> cached = new WeakHashMap<>();

    public static String toJson(Object o) {
        return gson.toJson(o);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    public synchronized static <T> T get(Context context, String key, Class<T> type) {
        for(Map.Entry<Object, String> e : cached.entrySet()) {
            if(e.getValue().contentEquals(key))
                return (T) e.getKey();
        }
        String json = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).getString(key, null);
        if(json == null)
            return null;
        T o = gson.fromJson(json, type);
        // Cache
        cached.put(o, key);
        return o;
    }

    public synchronized static void put(Context context, String key, Object object) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit();
        if(object != null) {
            String json = gson.toJson(object);
            editor.putString(key, json);
            // Cache
            cached.put(object, key);
        }
        else {
            editor.remove(key);
            // Cache
            cached.remove(object);
        }
        editor.apply();
    }


    public static boolean equals(Object object1, Object object2) {
        if(object1 == null || object2 == null)
            return false;
        if(object1.getClass() != object2.getClass())
            return false;
        if(!object1.toString().contentEquals(object2.toString()))
            return false;
        String json1 = gson.toJson(object1);
        String json2 = gson.toJson(object2);
        return json1.equals(json2);
    }
}
