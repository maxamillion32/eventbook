package com.mhafizhasan.eventbook.utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by someguy233 on 06-Nov-15.
 */
public class CallChannel implements Runnable {
    static final String TAG = "CallChannel";

    public static class Call {
        public static Object invoke(Object o, String name, Object ... params) {
            Class<?> type = o.getClass();
            Method method = null;
            int numParams = -1;
            do {
                Method[] methods = type.getDeclaredMethods();
                for (int c = 0; c < methods.length; c++) {
                    Method m = methods[c];
                    if (!m.getName().contentEquals(name))
                        continue;       // wrong name
                    Class<?>[] methodParams = m.getParameterTypes();
                    if(methodParams.length > params.length)
                        continue;       // not right number of parameters
                    // Else matched name and number of parameters
                    method = m;
                    numParams = methodParams.length;
                    if (!m.isAccessible())
                        m.setAccessible(true);
                    break;
                }
            } while(method == null && (type = type.getSuperclass()) != null);
            if(method == null)
                throw new IllegalArgumentException("No method found named " + name + " with " + params.length + " parameters in class " + o.getClass().getSimpleName());
            try {
                if(numParams < params.length)
                    params = Arrays.copyOf(params, numParams);      // method underloading
                return method.invoke(o, params);
            } catch (Throwable e) {
                throw new RuntimeException("Exception in call", e);
            }
        }

        public final String methodName;
        public final Object[] params;
        public final long millis;
        public final Object target;
        private boolean isCanceled = false;

        public Call(long millis, Object target, String methodName, Object[] params) {
            this.millis = millis;
            this.target = target;
            this.methodName = methodName;
            this.params = params;
        }

        public void cancel() {
            isCanceled = true;
        }

        public void call(Object listener) {
            if(isCanceled)
                return;
            invoke(target != null ? target : listener, methodName, params);
        }
    }

    public static class Request implements Runnable {

        public final CallChannel channel;
        public final Object target;
        public final String methodName;
        public final Object source;
        public final String requestMethodName;
        public final Object[] requestParams;

        public Request(CallChannel channel, Object target, String methodName, Object source, String requestMethodName, Object[] requestParams) {
            this.channel = channel;
            this.target = target;
            this.methodName = methodName;
            this.source = source;
            this.requestMethodName = requestMethodName;
            this.requestParams = requestParams;
        }

        public void send() {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(this);
        }

        @Override
        public void run() {
            Object result = null;
            Throwable t = null;
            try {
                result = Call.invoke(source, requestMethodName, requestParams);
            } catch (Throwable e) {
                t = e;
            }
            // Done, callback
            channel.callTarget(target, methodName, result, t, this);
        }
    }



    /* TODO: continue this
    public static class Transform {

        private Throwable e;

        public Throwable getError() {
            return e;
        }

        protected boolean prepareAsync() {
            return true;
        }

        protected void executeAsync() throws Throwable {
            // nothing
        }
    }
    */

    public static class ChanneledInvocationHandler implements InvocationHandler {

        private final CallChannel channel;
        private final Object target;

        public ChanneledInvocationHandler(CallChannel channel, Object target) {
            this.channel = channel;
            this.target = target;
        }

        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            if(objects == null)
                objects = new Object[0];        // no arguments
            channel.callTarget(target, method.getName(), objects);
            return null;        // always null
        }
    }

    public static class TransformerInvocationHandler implements InvocationHandler {

        public TransformerInvocationHandler(CallChannel channel, Object target) {
            this.channel = channel;
            this.target = target;
        }

        private static class TransformRunnable implements Runnable {

            private final Runnable process;
            private final CallChannel channel;
            private final Object target;
            private final String callbackName;

            private TransformRunnable(Runnable process, CallChannel channel, Object target, String callbackName) {
                this.process = process;
                this.channel = channel;
                this.target = target;
                this.callbackName = callbackName;
            }

            @Override
            public void run() {
                try {
                    process.run();
                } catch (Throwable e) {
                    Log.e(TAG, "Exception executing runnable for transform " + process, e);
                }
                channel.callTarget(target, callbackName, process);
            }
        }

        private final CallChannel channel;
        private final Object target;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(args == null || args.length != 1)
                throw new IllegalArgumentException("Invalid number of parameters for transform method " + method.getName());
            if(!(args[0] instanceof Runnable))
                throw new IllegalArgumentException("Runnable expected as parameter for transform method " + method.getName());
            Runnable process = (Runnable) args[0];
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new TransformRunnable(process, channel, target, method.getName()));
            return null;            // always null
        }
    }

    private final ArrayList<Call> queued = new ArrayList<>();
    private CallChannel parent = null;
    private Object listener = null;
    private Thread listenerThread = null;
    private Handler listenerHandler = null;
    private long nextExecution = Long.MAX_VALUE;

    public synchronized void cancelScheduledExecution() {
        if(listenerHandler == null)
            return;     // No handler to remove execution from
        nextExecution = Long.MAX_VALUE;
        listenerHandler.removeCallbacks(this);
    }

    public void scheduleExecution(long millis) {
        if(millis >= nextExecution)
            return;
        if(listenerHandler == null) {
            Log.d(TAG, "No handler to schedule execution for " + this);
            return;
        }
        nextExecution = millis;
        long delay = Math.max(millis - System.currentTimeMillis(), 0);
        listenerHandler.removeCallbacks(this);
        if(delay == 0)
            listenerHandler.post(this);
        else
            listenerHandler.postDelayed(this, delay);
        Log.d(TAG, "Scheduling execution in " + delay + " for " + this);
    }

    public <T> T adapt(Class<T> type) {
        return adapt(type, null);
    }

    public <T> T adapt(Class<T> type, Object target) {
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { type } , new ChanneledInvocationHandler(this, target));
    }

    public <T> T transform(Class<T> type) {
        return transform(type, null);
    }

    public <T, U extends T> T transform(Class<T> type, U target) {
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { type } , new TransformerInvocationHandler(this, target));
    }


    public Request request(String methodName, Object source, String requestMethodName, Object ... requestParams) {
        return request(null, methodName, source, requestMethodName, requestParams);
    }

    public Request request(Object target, String methodName, Object source, String requestMethodName, Object ... requestParams) {
        Request request = new Request(this, target, methodName, source, requestMethodName, requestParams);
        request.send();
        return request;
    }

    public synchronized boolean cancel(Call call) {
        if(parent != null)
            return parent.cancel(call);
        if(call == null)
            return false;
        return queued.remove(call);
    }

    public synchronized Call call(String methodName, Object ... params) {
        return callTargetDelayed(-1, null, methodName, params);
    }

    public synchronized Call callTarget(Object target, String methodName, Object ... params) {
        return callTargetDelayed(-1, target, methodName, params);
    }

    public synchronized Call callTargetDelayed(long millis, Object target, String methodName, Object... params) {
        if(listener == null) {
            // Channel not yet open, so queue up
            Call call = new Call(millis, target, methodName, params);
            queued.add(call);
            return call;
        }
        if(parent != null)
            return parent.callTargetDelayed(millis, target != null ? target : listener, methodName, params);     // Reroute to parent
        // Else running on this channel
        if(millis > 0) {
            millis += System.currentTimeMillis();
            // Call is delayed
            Call call = new Call(millis, target, methodName, params);
            queued.add(call);
            scheduleExecution(millis);
            return call;
        }
        // Else can execute now
        if(!isRunnable()) {
            // Cannot execute directly, queue
            Call call = new Call(-1, target, methodName, params);
            queued.add(call);
            Log.d(TAG, "Queueing call to " + methodName + " for " + this);
            // Queue call
            scheduleExecution(0);
            return call;
        }
        // Else is listening and is on the current thread, just invoke
        Call.invoke(target != null ? target : listener, methodName, params);
        return null;
    }

    @Override
    public synchronized void run() {
        if (listener == null || Thread.currentThread() != listenerThread)
            return;     // Invalid context
        cancelScheduledExecution();
        long earliest = executeQueue(listener);
        scheduleExecution(earliest);
    }

    private synchronized long executeQueue(Object listener) {
        // Execute all queued
        long current = System.currentTimeMillis();
        long earliest = Long.MAX_VALUE;
        int size = queued.size();
        for(int c = 0; c < size; c++) {
            Call call = queued.get(c);
            if(call.millis <= current) {
                queued.remove(c);
                call.call(listener);
                c--;
                size--;
            }
            else if(call.millis < earliest)
                earliest = call.millis;
            // Else not yet time
        }
        return earliest;
    }

    public synchronized void open(Object listener) {
        open(listener, null);
    }

    public synchronized void open(Object listener, CallChannel parent) {
        if(listener == null)
            throw new IllegalArgumentException("listener cannot be null");
        if(this.listener != null)
            throw new IllegalStateException("Channel is already open");
        if(parent != null) {
            // Using parent, for each queued call, post to parent
            while(queued.size() > 0) {
                Call call = queued.remove(0);
                Object target = call.target != null ? call.target : listener;
                parent.callTargetDelayed(call.millis, target, call.methodName, call.params);
            }
            // Done
            this.listener = listener;
            this.parent = parent;
            return;
        }
        // Else using this thread, execute queued calls, passthrough exceptions
        long earliest = executeQueue(listener);
        // Cleared queue, register listener
        this.listener = listener;
        this.listenerThread = Thread.currentThread();
        Looper currentLooper = Looper.myLooper();
        if (currentLooper != null)
            listenerHandler = new Handler(currentLooper);
        // Schedule delayed calls
        scheduleExecution(earliest);
    }

    public synchronized void close(Object listener) {
        if(listener == null)
            throw new IllegalArgumentException("listener cannot be null");
        if(this.listener == null)
            throw new IllegalStateException("Channel is not open");
        if(this.listener != listener)
            throw new IllegalStateException("Channel is open on another listener");
        if(parent != null) {
            parent = null;
            this.listener = null;
            // Done
            return;
        }
        // Else using this thread, check thread
        if (listenerThread != Thread.currentThread())
            throw new IllegalStateException("Can only close() on the same thread that called open()");
        cancelScheduledExecution();
        this.listener = null;
        listenerThread = null;
        listenerHandler = null;
    }

    public synchronized boolean isRunnable() {
        return listener != null && listenerThread == Thread.currentThread();
    }

    public synchronized boolean isOpen() {
        return listener != null;
    }

}
