package com.mhafizhasan.eventbook.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by someguy233 on 23-Nov-15.
 */
public class StreamConverter<T> {
    public static final int REMOVED = -1;

    private final DynamicRecyclerViewAdapter adapter;
    private final String cacheName;
    private final Class<T> type;
    private final WeakHashMap<Object, T> lookup = new WeakHashMap<>();
    // Current updated cursor
    private final DynamicRecyclerViewAdapter.Cursor updatedCursor;
    // Current cached cursor
    private final DynamicRecyclerViewAdapter.Cursor cachedCursor;
    // First item cursor
    private final DynamicRecyclerViewAdapter.Cursor firstCursor;
    // Stats
    private int updatedCount = 0;
    private int cachedCount = 0;
    private int page = 0;
    private boolean hasRequested = false;
    private boolean hasFinishedCached = false;
    private boolean hasFinishedUpdate = false;
    private boolean isCacheCommitted = true;

    public void refresh() {
        // Else reset
        updatedCursor.setIndex(firstCursor.getIndex());
        updatedCount = 0;
        page = 0;
        hasFinishedUpdate = false;
        requestUpdate();
    }

    public DynamicRecyclerViewAdapter getAdapter() {
        return adapter;
    }

    public boolean isCacheCommitted() {
        return isCacheCommitted;
    }

    public boolean hasFinishedUpdate() {
        return hasFinishedUpdate;
    }

    public boolean hasFinishedCached() {
        return hasFinishedCached;
    }

    public boolean hasRequestedUpdate() { return hasRequested; }

    public int getCurrentPage() {
        return page;
    }

    public int getUpdatedIndex() {
        if(updatedCount == 0) {
            if(cachedCount == 0)
                return getStartPosition() - 1;
            else
                return firstCursor.getIndex();
        }
        return updatedCursor.getIndex();
    }

    public int getCachedIndex() {
        if(cachedCount == 0) {
            if(updatedCount == 0)
                return getStartPosition() - 1;
            else
                return updatedCursor.getIndex();
        }
        return cachedCursor.getIndex();
    }

    public StreamConverter(DynamicRecyclerViewAdapter adapter, String cacheName, Class<T> type) {
        this.adapter = adapter;
        this.cacheName = cacheName;
        this.type = type;

        updatedCursor = new DynamicRecyclerViewAdapter.Cursor(adapter);
        cachedCursor = new DynamicRecyclerViewAdapter.Cursor(adapter);
        firstCursor = new DynamicRecyclerViewAdapter.Cursor(adapter);
    }

    public void cache() {
        if(isCacheCommitted)
            return;         // already committed
        ArrayList<T> cached = new ArrayList<>();
        for(int c = 0; c < adapter.getItemCount(); c++) {
            T item = lookup.get(adapter.getItem(c));
            if(item != null)
                cached.add(item);
        }
        // Update
        Cacher.patch(adapter.getRecyclerView().getContext(), cacheName, cached, type);
        isCacheCommitted = true;
    }

    public boolean fill(int count) {
        // Request updated
        if(hasFinishedUpdate)
            return false;       // finished
        boolean hasFilled = false;
        // Load from cache first
        if(!hasFinishedCached) {
            List<T> list = Cacher.fetch(adapter.getRecyclerView().getContext(), cacheName, type, cachedCount, count);
            if(list == null || list.size() == 0) {
                hasFinishedCached = true;       // Exhausted cache
                onFinishedCached(cachedCount, cachedCount + updatedCount);
            }
            else {
                // Refreshed cachedIndex
                for(T item : list) {
                    int cachedIndex = getCachedIndex();
                    int index = onShowCached(item, cachedIndex + 1, cachedCount, cachedCount + updatedCount);
                    if(index != REMOVED) {
                        if(cachedCount == 0 && updatedCount == 0)
                            firstCursor.setIndex(index - 1);           // set first index
                        cachedCursor.setIndex(index);
                        cachedCount++;
                        lookup.put(adapter.getItem(index), item);
                    }
                }
                hasFilled = true;
            }
        }
        requestUpdate();
        return hasFilled;
    }

    public void requestUpdate() {
        if(hasRequested)
            return;     // done or currently requesting
        hasRequested = true;
        onRequestUpdate(page);
    }

    public void cancelRequestedStatus() {
        hasRequested = false;
    }

    protected void update(List<T> items, int requestPage) {
        if(requestPage < 0 || requestPage != page) {
            // Invalid page, request again
            hasRequested = false;
            requestUpdate();
            return;
        }
        if(items == null || items.size() == 0) {
            hasFinishedCached = true;
            hasRequested = false;
            hasFinishedUpdate = true;
            onFinishedUpdate(updatedCount, updatedCount + cachedCount);
            return;
        }
        page++;
        // Refresh updatedIndex
        for(T item : items) {
            int updatedIndex = getUpdatedIndex();
            int index = onShow(item, updatedIndex + 1, updatedCount, updatedCount + cachedCount);
            if(index != REMOVED) {
                if(cachedCount == 0 && updatedCount == 0)
                    firstCursor.setIndex(index - 1);           // set first index
                updatedCursor.setIndex(index);
                updatedCount++;
                lookup.put(adapter.getItem(index), item);
            }
        }
        if(cachedCursor.getIndex() < updatedCursor.getIndex())
            cachedCursor.setIndex(updatedCursor.getIndex());            // Cache is slower than request?
        if(hasRequested && updatedCursor.getIndex() < cachedCursor.getIndex()) {
            // Request for more updates to match cache
            onRequestUpdate(page);
        }
        else {
            hasRequested = false;       // received response
            onFinishedRequesting();
        }
        isCacheCommitted = false;       // updated
    }


    protected int onShowCached(T item, int index, int count, int total) {
        return onShow(item, index, count, total);           // default no difference
    }

    protected int onShow(T item, int index, int count, int total) {
        // implementation
        return REMOVED;
    }

    public int getStartPosition() {
        // implementation
        return 0;
    }

    protected void onFinishedCached(int count, int total) {
        // implementation
    }

    protected void onFinishedUpdate(int count, int total) {
        // implementation
    }

    protected void onRequestUpdate(int page) {
        // implementation
    }

    protected void onFinishedRequesting() {
        // implementation
    }

}
