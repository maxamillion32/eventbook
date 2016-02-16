package com.mhafizhasan.eventbook;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.mhafizhasan.eventbook.utils.BaseArguments;
import com.mhafizhasan.eventbook.utils.ContentView;
import com.mhafizhasan.eventbook.utils.DynamicFragment;
import com.mhafizhasan.eventbook.utils.DynamicRecyclerViewAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;


public class EventsFragment extends DynamicFragment {

    public static class Arguments extends BaseArguments {

    }

    public static class EventContent {

        public static class ViewHolder extends ContentView<EventContent> {

            public ViewHolder() {
                super(R.layout.list_event);
            }

            @Override
            public ViewHolder instantiate() {
                return new ViewHolder();
            }
        }
    }

    // Context
    Activity activity;

    // Views
    @Bind(R.id.recyclerView) RecyclerView recyclerView;
    View rootView;

    // RecyclerView
    LinearLayoutManager linearLayoutManager;
    DynamicRecyclerViewAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate and bind
        rootView = inflater.inflate(R.layout.fragment_events, container, false);
        ButterKnife.bind(this, rootView);

        // Setup recyclerView
        linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Adapter
        adapter = new DynamicRecyclerViewAdapter(recyclerView);
        adapter.register(EventContent.class, new EventContent.ViewHolder());

        // TODO: replace with actual data from server
        adapter.put(new EventContent());
        adapter.put(new EventContent());
        adapter.put(new EventContent());
        adapter.put(new EventContent());
        adapter.put(new EventContent());
        adapter.put(new EventContent());
        adapter.put(new EventContent());
        adapter.put(new EventContent());
        adapter.put(new EventContent());

        return rootView;
    }

}
