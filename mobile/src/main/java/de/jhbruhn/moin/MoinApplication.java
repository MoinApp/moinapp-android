package de.jhbruhn.moin;

import android.app.Application;
import android.content.Context;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;
import de.jhbruhn.moin.data.api.ApiModule;

/**
 * Created by Jan-Henrik on 19.12.2014.
 */
public class MoinApplication extends Application {

    private ObjectGraph graph;

    @Override
    public void onCreate() {
        super.onCreate();

        graph = ObjectGraph.create(getModules().toArray());
        inject(this);
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
