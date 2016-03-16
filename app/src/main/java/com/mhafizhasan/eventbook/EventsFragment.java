package com.mhafizhasan.eventbook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mhafizhasan.CreateEventActivity;
import com.mhafizhasan.eventbook.net.NearestEventRequest;
import com.mhafizhasan.eventbook.net.model.EventModel;
import com.mhafizhasan.eventbook.utils.BaseArguments;
import com.mhafizhasan.eventbook.utils.CallChannel;
import com.mhafizhasan.eventbook.utils.DynamicFragment;
import com.mhafizhasan.eventbook.utils.DynamicRecyclerViewAdapter;
import com.mhafizhasan.eventbook.utils.LinearLayoutStreamer;
import com.mhafizhasan.eventbook.utils.StreamConverter;
import com.orhanobut.logger.Logger;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class EventsFragment extends DynamicFragment {

    public static class Arguments extends BaseArguments {

    }

    // Context
    Activity activity;
    private final CallChannel channel = new CallChannel();
    UserLoginDetails login;

    // Views
    @Bind(R.id.recyclerView) RecyclerView recyclerView;
    View rootView;

    // RecyclerView
    LinearLayoutManager linearLayoutManager;
    DynamicRecyclerViewAdapter adapter;
    LinearLayoutStreamer linearLayoutStreamer;
    EventsStream stream;

    private class EventsStream extends StreamConverter<EventModel> {


        public EventsStream() {
            super(adapter, "EventsFragment/nearest", EventModel.class);
            Logger.d("11111111111");
        }

        @Override
        protected void onRequestUpdate(final int page) {
            new NearestEventRequest(
                    channel,
                    rootView,
                    login.token.access_token,
                    2.685911,                       // TODO: replace with actual device location
                    101.886472,
                    "now_future",
                    true,
                    page
            ) {
                @Override
                protected void onSuccess(EventModel[] response) {
                    Logger.d("=====>" + response.toString());
                    update(Arrays.asList(response), page);
                }
            }.send();
        }

        @Override
        protected int onShow(EventModel item, int index, int count, int total) {
            EventContent content = new EventContent(item);
            if(!adapter.update(index, content))
                return  REMOVED;
            return index;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        Logger.d("on create 222222222222");
        login = UserLoginDetails.from(activity);

        // Inflate and bind
        rootView = inflater.inflate(R.layout.fragment_events, container, false);
        ButterKnife.bind(this, rootView);

        // Setup recyclerView
        linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Adapter
        adapter = new DynamicRecyclerViewAdapter(recyclerView);
        adapter.register(EventContent.class, new EventContent.ViewHolder(activity, channel));

        // Streaming
        stream = new EventsStream();
        linearLayoutStreamer = new LinearLayoutStreamer(
                adapter,
                linearLayoutManager,
                null,
                10,             // keep at least 10 rows for buffer
                stream
        );
        recyclerView.addOnScrollListener(linearLayoutStreamer);

        // Fill
        linearLayoutStreamer.refreshStreams();

        return rootView;
    }

    @Override
    public void onResumeVisible() {

        channel.open(this);
        stream.refresh();
    }

    @Override
    public void onPauseVisible() {
        channel.close(this);

        stream.cache();
    }

    @OnClick(R.id.fab)
    void onClickView() {
        Intent intent = new Intent(activity, CreateEventActivity.class);
        activity.startActivity(intent);
    }

}
