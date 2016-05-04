package de.jhbruhn.moin.data.api;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.google.gson.Gson;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.jhbruhn.moin.ApplicationModule;
import de.jhbruhn.moin.BuildConfig;
import de.jhbruhn.moin.data.api.MoinService;
import de.jhbruhn.moin.data.auth.MoinAccountAuthenticator;
import de.jhbruhn.moin.data.auth.MoinAccountAuthenticatorService;
import de.jhbruhn.moin.gcm.GcmIntentService;
import de.jhbruhn.moin.gcm.GcmRegistrationService;
import de.jhbruhn.moin.gcm.ReMoinReceiver;
import de.jhbruhn.moin.gui.RecentListActivity;
import de.jhbruhn.moin.gui.SignInActivity;
import de.jhbruhn.moin.wear.RecentListFetchingService;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;

/**
 * Created by Jan-Henrik on 19.12.2014.
 */
@Module(
        complete = true,
        includes = {
                ApplicationModule.class
        },
        injects = {
                RecentListActivity.class,
                SignInActivity.class,
                MoinAccountAuthenticator.class,
                MoinAccountAuthenticatorService.class,
                GcmIntentService.class,
                ReMoinReceiver.class,
                RecentListFetchingService.class,
                GcmRegistrationService.class
        }
)
public class ApiModule {
    static final int DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB

    @Provides @Singleton
    OkHttpClient provideOkHttpClient(Application app) {
        return createOkHttpClient(app);
    }

    @Provides
    @Singleton
    Picasso providePicasso(Application app, OkHttpClient client) {
        return new Picasso.Builder(app).downloader(new OkHttpDownloader(client)).build();
    }

    @Provides
    @Singleton
    @Named("unreadMoins")
    SharedPreferences provideUnreadMoinsPreferences(Application application) {
        return application.getSharedPreferences("UnreadMoins", Application.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    @Named("receivedMoins")
    SharedPreferences provideMoinsPreferences(Application application) {
        return application.getSharedPreferences("moins", Application.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    @Named("gcm")
    SharedPreferences provideGCMPreferences(Application application) {
        return application.getSharedPreferences(RecentListActivity.class.getSimpleName(), Application.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    Converter provideResponseConverter() {
        return new GsonConverter(new Gson());
    }

    @Provides @Singleton
    Client provideClient(OkHttpClient client) {
        return new OkClient(client);
    }

    @Provides
    @Singleton
    MoinService provideMoinService(Converter responseConverter, Client client) {

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(client)
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .setConverter(responseConverter)
                .setEndpoint("https://moinapp-staging.herokuapp.com/api/v4")
                .build();

        return restAdapter.create(MoinService.class);
    }

    static OkHttpClient createOkHttpClient(Application app) {
        OkHttpClient client = new OkHttpClient();
        client.networkInterceptors().add(new StethoInterceptor());
        client.setReadTimeout(60, TimeUnit.SECONDS);
        client.setConnectTimeout(60, TimeUnit.SECONDS);
        File cacheDir = new File(app.getCacheDir(), "http");
        Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);
        client.setCache(cache);

        return client;
    }
}
