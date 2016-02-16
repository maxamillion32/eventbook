package com.mhafizhasan.eventbook.utils;

import android.support.v4.app.Fragment;

/**
 * Created by Azmi on 4/2/2016.
 */
public class DynamicFragment extends Fragment {


    boolean isVisible = false;
    boolean isSelected = false;


    public boolean onBackPressed() {
        return false;       // to be implemented
    }

    public void onResumeVisible() {
        // to be implemented
    }

    public void onPauseVisible() {
        // to be implemented
    }

    public void onSelected() {
        // to be implemented
    }

    public void onDeselected() {
        // to be implemented
    }
}
