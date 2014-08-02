package de.moinapp.moin;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import de.moinapp.moin.db.DaoMaster;
import de.moinapp.moin.db.DaoSession;

/**
 * Created by jhbruhn on 02.08.14.
 */
public class MoinApplication extends Application {
    private DaoSession mDaoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        setupDatabase();
    }

    private void setupDatabase() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "moin-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        mDaoSession = daoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }
}
