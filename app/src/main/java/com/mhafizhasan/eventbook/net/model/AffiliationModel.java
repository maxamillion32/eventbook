package com.mhafizhasan.eventbook.net.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dermis on 12,March,2016.
 */
public class AffiliationModel {

    // use enum for lookup value
    public enum AffiliationStatus {
        @SerializedName("checked_in")
        CHECKED_IN,
        @SerializedName("joined")
        JOINED,
        @SerializedName("watched")
        WATCHED,
        @SerializedName("none")
        NONE,
    }

    public AffiliationStatus status;       // can be either checked_in, joined, watched, or none
    public long time;           // last time checked in
    public long days;           // number of days
    public boolean is_owner;    // if logged in user is the one created this event
}
