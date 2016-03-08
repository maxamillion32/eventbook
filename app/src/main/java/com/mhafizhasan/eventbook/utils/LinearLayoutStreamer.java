package com.mhafizhasan.eventbook.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;


/**
 * Created by Azmi on 28/1/2016.
 */
public class LinearLayoutStreamer extends RecyclerView.OnScrollListener {

    private final DynamicRecyclerViewAdapter adapter;
    private final LinearLayoutManager linearLayoutManager;

    private final Object loadingContent;

    private final int bufferSize;
    private final StreamConverter<?>[] streams;

    public LinearLayoutStreamer(DynamicRecyclerViewAdapter adapter, LinearLayoutManager linearLayoutManager, Object loadingContent, int bufferSize, StreamConverter<?>... streams) {
        this.adapter = adapter;
        this.linearLayoutManager = linearLayoutManager;
        this.loadingContent = loadingContent;
        this.bufferSize = bufferSize;
        this.streams = streams;
    }


    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        refreshStreams();
    }

    public void refreshStreams() {
        int visibleItems = linearLayoutManager.getChildCount();
        int pastItems = linearLayoutManager.findFirstVisibleItemPosition();
        int expectedItems = visibleItems + pastItems + bufferSize;
        // Stream all
        boolean hasFinishedUpdate = false;
        for(int c = 0; c < streams.length; c++) {
            StreamConverter<?> stream = streams[c];
            if(expectedItems > stream.getCachedIndex())
                stream.fill(bufferSize);
            if(stream.hasFinishedUpdate())
                hasFinishedUpdate = true;
        }
        // Show loading content if needed
        if(loadingContent != null) {
            if (!hasFinishedUpdate) {
                if (linearLayoutManager.getReverseLayout())
                    adapter.put(loadingContent, 0);
                else
                    adapter.put(loadingContent);
            } else
                adapter.remove(loadingContent);
        }
    }
}
