package com.mhafizhasan.eventbook;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mhafizhasan.eventbook.utils.BaseArguments;
import com.mhafizhasan.eventbook.utils.DynamicFragment;


public class PostsFragment extends DynamicFragment {

    public static class Arguments extends BaseArguments {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

}
