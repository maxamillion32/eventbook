package com.mhafizhasan.eventbook.net;

import android.view.View;

import com.mhafizhasan.eventbook.net.model.EventModel;
import com.mhafizhasan.eventbook.utils.CallChannel;

import retrofit2.Call;

/**
 * Created by Dermis on 12,March,2016.
 */
public class NearestEventRequest extends GaeRequest<EventModel[]> {

    private final String accessToken;
    private final double latitude;
    private final double longitude;
    private final String scope;
    private final boolean publicOnly;
    private int page;

    public NearestEventRequest(CallChannel parent, View view, String accessToken, double latitude, double longitude, String scope, boolean publicOnly, int page) {
        super(parent, view);
        this.accessToken = accessToken;
        this.latitude = latitude;
        this.longitude = longitude;
        this.scope = scope;
        this.publicOnly = publicOnly;
        this.page = page;
    }

    @Override
    protected Call<EventModel[]> getCall() {
        return GaeServer.api.getNearestEvents(
                accessToken,
                latitude,
                longitude,
                scope,
                publicOnly,
                page
        );
    }
}
