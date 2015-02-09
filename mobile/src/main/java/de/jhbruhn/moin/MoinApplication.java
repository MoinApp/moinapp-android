package de.jhbruhn.moin;

import android.app.Application;
import android.content.Context;

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

        mTracker = GoogleAnalytics.getInstance(this).newTracker(R.xml.tracker);
        mTracker.enableAdvertisingIdCollection(true);

        graph = ObjectGraph.create(getModules().toArray());
        inject(this);
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
