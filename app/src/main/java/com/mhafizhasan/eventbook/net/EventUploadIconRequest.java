package com.mhafizhasan.eventbook.net;

import android.view.View;

import com.mhafizhasan.eventbook.net.model.UploadUrlModel;
import com.mhafizhasan.eventbook.utils.CallChannel;

import java.io.File;

import retrofit2.Call;

/**
 * Created by Dermis on 12,March,2016.
 */
public class EventUploadIconRequest extends GaeRequest<UploadUrlModel> {

    private final String accessToken;
    private final String eventId;
    private final File imageFile;

    public EventUploadIconRequest(CallChannel parent, View view, String accessToken, String eventId, File imageFile) {
        super(parent, view);
        this.accessToken = accessToken;
        this.eventId = eventId;
        this.imageFile = imageFile;
    }

    @Override
    protected Call<UploadUrlModel> getCall() {
        return GaeServer.api.getEventIconUploadUrl(accessToken, eventId);
    }

    @Override
    protected final void onSuccess(UploadUrlModel response) {
        new UploadImageRequest(getChannel(), getView(), response.upload_url, imageFile) {
            @Override
            protected void onSuccess(Object response) {
                onImageUploaded();
            }
        }.send();
        super.onSuccess(response);
    }

    protected void onImageUploaded() {
        // TODO:
    }
}
