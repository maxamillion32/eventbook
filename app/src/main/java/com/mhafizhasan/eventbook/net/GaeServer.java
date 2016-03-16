package com.mhafizhasan.eventbook.net;

import com.mhafizhasan.eventbook.net.model.EventModel;
import com.mhafizhasan.eventbook.net.model.TokenModel;
import com.mhafizhasan.eventbook.net.model.UploadUrlModel;
import com.mhafizhasan.eventbook.net.model.UserModel;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

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

        @FormUrlEncoded
        @POST("events-nearest.php")
        Call<EventModel[]> getNearestEvents(
                @Field("access_token") String accessToken,
                @Field("latitude") double latitude,
                @Field("longitude") double longitude,
                @Field("scope") String scope,
                @Field("public_only") boolean publicOnly,
                @Field("page") int page
        );

        @FormUrlEncoded
        @POST("create-event.php")
        Call<EventModel> createNewEvent(
                @Field("access_token") String accessToken,
                @Field("name") String name,
                @Field("latitude") double latitude,
                @Field("longitude") double longitude,
                @Field("address") String addresss,
                @Field("start_date") String startDate,
                @Field("end_date") String endDate,
                @Field("is_public") boolean isPublic
        );

        @Multipart
        @POST
        Call<Object> uploadImage(
                @Url String uploadUrl,
                @Part("file\"; filename=\"image.jpg") RequestBody file
        );

        @FormUrlEncoded
        @POST("event-cover-upload.php")
        Call<UploadUrlModel> getEventCoverUploadUrl(
                @Field("access_token") String accessToken,
                @Field("event_id") String eventId
        );

        @FormUrlEncoded
        @POST("event-icon-upload.php")
        Call<UploadUrlModel> getEventIconUploadUrl(
                @Field("access_token") String accessToken,
                @Field("event_id") String eventId
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
