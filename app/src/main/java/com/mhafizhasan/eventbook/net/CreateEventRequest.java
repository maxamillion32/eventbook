package com.mhafizhasan.eventbook.net;

import android.view.View;

import com.mhafizhasan.eventbook.net.model.EventModel;
import com.mhafizhasan.eventbook.utils.CallChannel;

import retrofit2.Call;

/**
 * Created by Dermis on 12,March,2016.
 */
public class CreateEventRequest extends GaeRequest<EventModel> {

    private final String access_token;
    private final String name;
    private final double latitude;
    private final double longitude;
    private final String address;
    private final String startDate;
    private final String endDate;
    private final boolean isPublic;

    public CreateEventRequest(CallChannel parent, View view, String access_token, String name, double latitude, double longitude, String address, String startDate, String endDate, boolean isPublic) {
        super(parent, view);
        this.access_token = access_token;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isPublic = isPublic;
    }

    @Override
    protected Call<EventModel> getCall() {
        return GaeServer.api.createNewEvent(
                access_token,
                name,
                latitude,
                longitude,
                address,
                startDate,
                endDate,
                isPublic
        );
    }
}
