package de.moinapp.moin.activities;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import de.greenrobot.event.EventBus;
import de.moinapp.moin.MoinApplication;
import de.moinapp.moin.R;
import de.moinapp.moin.auth.AccountGeneral;
import de.moinapp.moin.data.FriendCursorAdapter;
import de.moinapp.moin.db.DaoSession;
import de.moinapp.moin.db.FriendDao;
import de.moinapp.moin.events.MoinSentEvent;
import de.moinapp.moin.jobs.RegisterGCMJob;
import de.moinapp.moin.jobs.SendMoinJob;


public class FriendListActivity extends Activity {
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    String SENDER_ID = "";
    GoogleCloudMessaging gcm;
    String regid;

    @InjectView(R.id.main_list_friends)
    GridView mFriendListView;

    @InjectView(R.id.main_list_add_friend)
    Button mAddFriendButton;


    private AccountManager mAccountManager;
    private DaoSession mDaoSession;
    private FriendDao mFriendDao;

    private FriendCursorAdapter mFriendAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        ButterKnife.inject(this);

        if (!checkPlayServices()) {
            //TODO: We should notify the user of the failure.
            finish();
        }

        SENDER_ID = getString(R.string.google_project_id);

        gcm = GoogleCloudMessaging.getInstance(this);
        regid = getRegistrationId();

        mAccountManager = AccountManager.get(this);

        mDaoSession = ((MoinApplication) getApplication()).getDaoSession();
        mFriendDao = mDaoSession.getFriendDao();

        mFriendListView.setEmptyView(mAddFriendButton);

        loadFriendsFromDatabase();

        handleIntent(getIntent());

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent.getExtras() != null) {
            Log.d("PEDA", intent.getExtras().getString("senderId") + "COIN");
            MoinApplication.getMoinApplication().getCurrentMoinsPerUser().remove(intent.getExtras().getString("senderId"));
        }
    }

    private void logInCreateIfNeeded() {
        mAccountManager.getAuthTokenByFeatures(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, this, null, null,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        registerGCMToken();
                    }
                }, null);
    }

    private void loadFriendsFromDatabase() {
        String textColumn = FriendDao.Properties.Username.columnName;
        String orderBy = textColumn + " COLLATE LOCALIZED ASC";
        Cursor cursor = mDaoSession.getDatabase().query(mFriendDao.getTablename(), mFriendDao.getAllColumns(), null, null, null, null, orderBy);

        mFriendAdapter = new FriendCursorAdapter(this, cursor, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mFriendListView.setAdapter(mFriendAdapter);
    }

    @OnItemClick(R.id.main_list_friends)
    public void onFriendItemClick(int position) {
        Cursor cursor = (Cursor) mFriendAdapter.getItem(position);
        String userId = cursor.getString(cursor.getColumnIndexOrThrow(FriendDao.Properties.Uuid.columnName));

        MoinApplication.getMoinApplication().getJobManager().addJobInBackground(new SendMoinJob(userId));

        loadFriendsFromDatabase();
    }

    @OnClick(R.id.main_list_add_friend)
    public void onAddFriendClick() {
        showAddFriendActivity();
    }

    public void onEventMainThread(MoinSentEvent e) {
        loadFriendsFromDatabase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.friend_list, menu);
        return true;
    }

    private void showAddFriendActivity() {
        startActivityForResult(new Intent(this, AddFriendActivity.class), 42);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_friend_list_search) {
            showAddFriendActivity();
            return true;
        } else if (id == R.id.action_friend_list_logout) {
            mFriendDao.deleteAll();
            loadFriendsFromDatabase();
            mAccountManager.removeAccount(mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0], new AccountManagerCallback<Boolean>() {
                @Override
                public void run(AccountManagerFuture<Boolean> future) {
                    logInCreateIfNeeded();
                }
            }, null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 42) {
            loadFriendsFromDatabase();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        logInCreateIfNeeded();
    }


    private void registerGCMToken() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(FriendListActivity.this);
                    }
                    regid = gcm.register(SENDER_ID);

                    MoinApplication.getMoinApplication().getJobManager().addJobInBackground(new RegisterGCMJob(regid));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return null;
            }
        }.execute();

    }

    private void storeRegistrationId(String regId) {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion(this);
        Log.i("MOIN", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    private void showMessage(final String msg) {
        if (TextUtils.isEmpty(msg))
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getRegistrationId() {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i("MOIN", "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(this);
        if (registeredVersion != currentVersion) {
            Log.i("MOIN", "App version changed.");
            return "";
        }
        return registrationId;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences() {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(FriendListActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("MOIN", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
