package de.jhbruhn.moin.data.api;

import java.util.List;

import de.jhbruhn.moin.data.GCMToken;
import de.jhbruhn.moin.data.Moin;
import de.jhbruhn.moin.data.Session;
import de.jhbruhn.moin.data.User;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Jan-Henrik on 19.12.2014.
 */
public interface MoinService {
    @POST("/moin")
    void sendMoin(@Body Moin moin, @Header("Authorization") String session, Callback<Moin> callback);

    @POST("/auth")
    void authenticate(@Body User user, Callback<Session> callback);

    @POST("/auth")
    Session authenticate(@Body User user);

    @POST("/signup")
    void register(@Body User user, Callback<Session> callback);

    @GET("/user/:username")
    User getUser(@Path("username") String username);

    @GET("/user")
    void findUser(@Header("Authorization") String session, @Query("username") String username, Callback<List<User>> callback);

    @GET("/user/recents")
    void getRecents(@Header("Authorization") String session, Callback<List<User>> callback);

    @POST("/user/addgcm")
    GCMToken addGCMID(@Header("Authorization") String session, @Body GCMToken gcmToken);
}
