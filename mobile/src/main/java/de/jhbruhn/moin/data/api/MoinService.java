package de.jhbruhn.moin.data.api;

import java.util.List;

import de.jhbruhn.moin.data.User;
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
    void sendMoin(@Body Moin moin, @Header("Session") String session, Callback<Moin> callback);

    @POST("/users/auth")
    void authenticate(@Body User user, Callback<Session> callback);

    @POST("/users/auth")
    Session authenticate(@Body User user);

    @POST("/users/signup")
    void register(@Body User user, Callback<Session> callback);

    @GET("/user/:username")
    User getUser(@Path("username") String username);

    @GET("/users")
    void findUser(@Header("Session") String session, @Query("username") String username, Callback<List<User>> callback);

    @GET("/users/recents")
    void getRecents(@Header("Session") String session, Callback<List<User>> callback);

    @GET("/user/recents")
    List<User> getRecents(@Header("Session") String session);

    @POST("/users/addPush")
    GCMToken addGCMID(@Header("Session") String session, @Body GCMToken gcmToken);

    @POST("/users/addPush")
    void addGCMID(@Header("Session") String session, @Body GCMToken gcmToken, Callback<GCMToken> callback);
}
