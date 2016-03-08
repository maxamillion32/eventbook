package com.mhafizhasan.eventbook.net;

import android.view.View;

import com.mhafizhasan.eventbook.UserLoginDetails;
import com.mhafizhasan.eventbook.net.model.TokenModel;
import com.mhafizhasan.eventbook.utils.CallChannel;

import retrofit2.Call;

/**
 * Created by Dermis on 08,March,2016.
 */
public class TokenUsingPasswordRequest extends GaeRequest<TokenModel> {

    private final String username;
    private final String password;

    public TokenUsingPasswordRequest(CallChannel parent, View view, String username, String password) {
        super(parent, view);

        this.username = username;
        this.password = password;
    }

    @Override
    protected Call<TokenModel> getCall() {
        return GaeServer.api.getTokenUsingPassword(
                "password",
                username,
                password,
                GaeServer.CLIENT_ID,
                GaeServer.CLIENT_SECRET,
                GaeServer.DEFAULT_SCOPE
        );
    }

    @Override
    protected final void onSuccess(TokenModel response) {
        // Successfully granted token from server, save it
        UserLoginDetails login = UserLoginDetails.from(getView().getContext());
        login.token = response;
        login.save(getView().getContext());
        // Inform that access token have been received
        onGrantedAccessToken(response.access_token);
    }

    protected void onGrantedAccessToken(String access_token) {
        // For user implementation
    }
}
