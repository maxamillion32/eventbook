package com.mhafizhasan.eventbook;

import android.content.Context;

import com.mhafizhasan.eventbook.net.model.TokenModel;
import com.mhafizhasan.eventbook.net.model.UserModel;
import com.mhafizhasan.eventbook.utils.Storage;

import java.security.Key;

/**
 * Contains everything about the user
 *
 * Created by Dermis on 08,March,2016.
 */
public class UserLoginDetails {

    public TokenModel token;
    public UserModel me;
    public String password;

    private static UserLoginDetails login = null;

    public static final String KEY = "_userlogindetails";        // key for local storage file


    public static UserLoginDetails from(Context context) {
        if(login != null)
            return login;       // return from memory
        // Else need to deserialize from storage
        login = Storage.get(context, KEY, UserLoginDetails.class);
        if(login == null) {
            // First time tracking this user
            login = new UserLoginDetails();
            Storage.put(context, KEY, login);
        }
        return login;
    }

    public void save(Context context) {
        login  = this;      // save to memory
        Storage.put(context, KEY, this);    // Save to permanent storage
    }
}
