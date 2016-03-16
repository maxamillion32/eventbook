package com.mhafizhasan.eventbook.net.model;

/**
 * Created by Dermis on 12,March,2016.
 */
public class EventModel {

    public String event_id;
    public String hash_id;
    public String owner_id;

    public String name;
    public String description;

    public double latitude;
    public double longitude;
    public String address;

    public long start_time;
    public long end_time;
    public String timezone;

    public boolean is_public;

    public int stars;

    public ImageModel cover_url;
    public ImageModel icon_url;

    public int entry_value;
    public int entry_available;
    public int entry_maximum;

    public AffiliationModel affiliation;
}
