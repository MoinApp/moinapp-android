package de.jhbruhn.moin;

import android.app.Application;
import android.content.Context;
import android.support.v4.BuildConfig;

import com.facebook.stetho.Stetho;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;
import de.jhbruhn.moin.data.api.ApiModule;

/**
 * Created by Jan-Henrik on 19.12.2014.
 */
public class MoinApplication extends Application {

    private ObjectGraph graph;
    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();

        if(BuildConfig.DEBUG)
            initStetho();

        mTracker = GoogleAnalytics.getInstance(this).newTracker(R.xml.tracker);
        mTracker.enableAdvertisingIdCollection(true);

        graph = ObjectGraph.create(getModules().toArray());
        inject(this);
    }

    private void initStetho() {
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());
    }

    public Tracker getTracker() {
        return mTracker;
    }

    List<Object> getModules() {

        return Arrays.asList(
                new ApiModule(),
                new ApplicationModule(this)
        );
    }

    public void inject(Object object) {
        graph.inject(object);
    }

    public static MoinApplication get(Context context) {
        return (MoinApplication) context.getApplicationContext();
    }
}
