package de.moinapp.moin.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by jhbruhn on 02.08.14.
 */
public interface MoinService {
    @POST("/moin")
        //TODO: wont work because of field.
    void sendMoin(@Field("to") String receiver, @Query("session") String session, Callback<Void> cb);

    @GET("/user/{name}")
    void getUser(@Path("name") String name, Callback<User> cb);

    @POST("/user")
    void register(@Body User user, Callback<Session> cb);

    @POST("/user/session")
    void login(@Body User user, Callback<Session> cb);

    @POST("/user/session")
    Session login(@Body User user);

    @POST("/user/gcm")
    void addGCMId(@Body GCMID gcmid, @Query("session") String session, Callback<Void> cb);
}
