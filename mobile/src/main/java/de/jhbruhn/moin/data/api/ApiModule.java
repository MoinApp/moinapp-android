package de.jhbruhn.moin.data.api;

import android.app.Application;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

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
import de.jhbruhn.moin.gcm.ReMoinReceiver;
import de.jhbruhn.moin.gui.RecentListActivity;
import de.jhbruhn.moin.gui.SignInActivity;
import retrofit.RestAdapter;
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
                ReMoinReceiver.class
        }
)
public class ApiModule {

    @Provides
    @Singleton
    Picasso providePicasso(Application app) {
        return new Picasso.Builder(app).loggingEnabled(BuildConfig.DEBUG).build();
    }

    @Provides
    @Singleton
    @Named("unreadMoins")
    SharedPreferences provideUnreadMoinsPreferences(Application application) {
        return application.getSharedPreferences("UnreadMoins", Application.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    Converter provideResponseConverter() {
        return new GsonConverter(new Gson());
    }


    @Provides
    @Singleton
    MoinService provideMoinService(Converter responseConverter) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .setConverter(responseConverter)
                .setEndpoint("https://moinapp.herokuapp.com/api/") //TODO: Use real URL.
                .build();

        return restAdapter.create(MoinService.class);
    }
}
