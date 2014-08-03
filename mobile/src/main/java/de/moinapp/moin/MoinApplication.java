package de.moinapp.moin;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;

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


    @Override
    public void onCreate() {
        super.onCreate();
        setupDatabase();
        setupJobManager();
        moinApplication = this;
    }

    private void setupJobManager() {
        Configuration configuration = new Configuration.Builder(this)
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
}
