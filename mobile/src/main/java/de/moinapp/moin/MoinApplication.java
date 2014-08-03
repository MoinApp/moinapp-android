package de.moinapp.moin;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;

import de.greenrobot.event.EventBus;
import de.moinapp.moin.db.DaoMaster;
import de.moinapp.moin.db.DaoSession;

/**
 * Created by jhbruhn on 02.08.14.
 */
public class MoinApplication extends Application {

    private static MoinApplication moinApplication = null;

    public static MoinApplication getMoinApplication() {
        return moinApplication;
    }


    private DaoSession mDaoSession;
    private JobManager mJobManager;
    private EventBus mEventBus;

    @Override
    public void onCreate() {
        super.onCreate();
        setupDatabase();
        setupJobManager();
        setupEventBus();
        moinApplication = this;
    }

    private void setupEventBus() {
        mEventBus = new EventBus();
    }

    private void setupJobManager() {
        Configuration configuration = new Configuration.Builder(this)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBS";

                    @Override
                    public boolean isDebugEnabled() {
                        return true;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }
                })
                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(3)//up to 3 consumers at a time
                .loadFactor(3)//3 jobs per consumer
                .consumerKeepAlive(120)//wait 2 minute
                .build();
        mJobManager = new JobManager(this, configuration);
    }

    private void setupDatabase() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "moin-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        mDaoSession = daoMaster.newSession();
    }

    public JobManager getJobManager() {
        return mJobManager;
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public EventBus getBus() {
        return mEventBus;
    }
}
