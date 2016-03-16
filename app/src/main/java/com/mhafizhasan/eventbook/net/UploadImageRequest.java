package com.mhafizhasan.eventbook.net;

import android.view.View;

import com.mhafizhasan.eventbook.utils.CallChannel;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * Created by Dermis on 12,March,2016.
 */
public class UploadImageRequest extends GaeRequest<Object> {

    private final String uploadUrl;
    private final File imageFile;

    public UploadImageRequest(CallChannel parent, View view, String uploadUrl, File imageFile) {
        super(parent, view);
        this.uploadUrl = uploadUrl;
        this.imageFile = imageFile;
    }

    @Override
    protected Call<Object> getCall() {
        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);
        return GaeServer.api.uploadImage(uploadUrl, fileBody);
    }
}
