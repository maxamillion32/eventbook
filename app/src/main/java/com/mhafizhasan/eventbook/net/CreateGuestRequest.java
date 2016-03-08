package com.mhafizhasan.eventbook.net;

import android.view.View;

import com.mhafizhasan.eventbook.net.model.UserModel;
import com.mhafizhasan.eventbook.utils.CallChannel;

import retrofit2.Call;

/**
 * Created by Dermis on 08,March,2016.
 */
public class CreateGuestRequest extends GaeRequest<UserModel> {

    private final String firstname;
    private final String lastname;
    private final String password;

    public CreateGuestRequest(CallChannel parent, View view, String firstname, String lastname, String password) {
        super(parent, view);

        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
    }

    @Override
    protected Call<UserModel> getCall() {
        return GaeServer.api.createGuest(firstname, lastname, password);
    }
}
