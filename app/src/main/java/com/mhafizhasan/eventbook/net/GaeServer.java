package com.mhafizhasan.eventbook.net;

import com.mhafizhasan.eventbook.net.model.TokenModel;
import com.mhafizhasan.eventbook.net.model.UserModel;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Dermis on 16,February,2016.
 */
public class GaeServer {

//    public static final String BASE_URL = "http://android-magic.appspot.com/";
//
//    public static final String CLIENT_ID = "android-magic";
//    public static final String CLIENT_SECRET = "magic1234";

    public static final String BASE_URL = "http://wemari.com/";

    public static final String CLIENT_ID = "circlesnearme_android";
    public static final String CLIENT_SECRET = "magic1234";

    public static final String DEFAULT_SCOPE = "readwall writewall profile_picture_edit";

    public interface API {
        @FormUrlEncoded
        @POST("token.php")
        Call<TokenModel> getTokenUsingPassword(
                @Field("grant_type") String grantType,
                @Field("username") String username,
                @Field("password") String password,
                @Field("client_id") String clientId,
                @Field("client_secret") String clientSecret,
                @Field("scope") String scope
        );

        @FormUrlEncoded
        @POST("create-guest.php")
        Call<UserModel> createGuest(
                @Field("firstname") String firstname,
                @Field("lastname") String lastname,
                @Field("password") String password
        );
    }

    public static final API api;

    static {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(API.class);
    }

}
