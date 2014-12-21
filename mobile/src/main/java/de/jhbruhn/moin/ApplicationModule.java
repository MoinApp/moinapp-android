package de.jhbruhn.moin;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Jan-Henrik on 20.12.2014.
 */
@Module(
        injects = {
                MoinApplication.class
        },
        library = true
)
public class ApplicationModule {
    private final MoinApplication app;

    public ApplicationModule(MoinApplication app) {
        this.app = app;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return app;
    }
}
