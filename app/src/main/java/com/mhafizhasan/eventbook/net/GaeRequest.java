package com.mhafizhasan.eventbook.net;

import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.mhafizhasan.eventbook.utils.CallChannel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dermis on 20,February,2016.
 */
public abstract class GaeRequest<T> implements Callback<T> {

    static final String TAG = "GaeRequest";

    public static class ErrorModel {
        public String error;
        public String error_description;
    }

    private  static final Gson gson = new Gson();

    private final CallChannel channel = new CallChannel();  // To synchronize server response with activity
    private final View view;        // Activity's layout, used to show "no internet connection" snackbar

    public View getView() {
        return view;
    }

    public GaeRequest(CallChannel parent, View view) {
        channel.open(this, parent);
        this.view = view;
    }

    protected abstract Call<T> getCall();

    public void send() {
        Call<T> call = getCall();
        if(call != null)
            call.enqueue(channel.adapt(Callback.class));
    }

    @Override
    public final void onResponse(Call<T> call, Response<T> response) { // final as cannot overwrite by others function
        if(response.isSuccess())
            onSuccess(response.body());
        else {
                // Else invalid request, parse error response from server
                // Get HTTP error code
                int httpCode = response.code();
                String httpMessage = response.message();
                String errorType;
                String errorMessage;
                try {
                    String errorBody = response.errorBody().string();
                    ErrorModel error = gson.fromJson(errorBody, ErrorModel.class);
                    errorType = error.error;
                    errorMessage = error.error_description;
                } catch (Throwable e) {
                    // Unable to parse error response from server
                    errorType = null;
                    errorMessage = null;
                }
                onError(httpCode, httpMessage, errorType, errorMessage);
            }
    }

    protected void onError(int httpCode, String httpMessage, String errorType, String errorMessage) {
        // Subclasses should only implement this
        Log.d(TAG, "Received replied error: " + httpCode + " " + httpMessage + " " + errorType + " " + errorMessage);
        // Be lazy and assume no internet connection anyway
        onFailure(null, null);
    }

    protected void onSuccess(T response) {
        // Subclasses should only implement this
        Log.d(TAG, "Received response: " + response);
    }



    @Override
    public final void onFailure(Call<T> call, Throwable t) {
        // No internet connection or server down or being blocked
        Log.d(TAG, "Network error: " + t);
        // Allow user to try again
        Snackbar sb = Snackbar.make(view, "No internet connection", Snackbar.LENGTH_INDEFINITE);
        sb.setAction("Try again", new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Nothing
            }
        });
        sb.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                // TODO: Resend request
                send();
            }
        });
        sb.show();
    }
}
