package de.moinapp.moin.api;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by jhbruhn on 02.08.14.
 */
public interface MoinService {
    @POST("/moin")
    void sendMoin(@Field("to") String receiver, @Query("session") String session, Callback<Void> cb);

    @GET("/user/{name}")
    void getUser(@Path("name") String name, Callback<User> cb);

    @POST("/user")
    void register(@Field("username") String username, @Field("password") String password, Callback<Session> cb);

    @POST("/user/session")
    void login(@Field("username") String username, @Field("password") String password, Callback<Session> cb);

    @PUT("/user")
    void addGCMId(@Field("gcm_id") String gcmId, @Query("session") String session, Callback<Void> cb);
}
