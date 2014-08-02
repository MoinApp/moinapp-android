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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import de.moinapp.moin.MoinApplication;
import de.moinapp.moin.R;
import de.moinapp.moin.api.GCMID;
import de.moinapp.moin.api.Moin;
import de.moinapp.moin.api.MoinClient;
import de.moinapp.moin.api.MoinService;
import de.moinapp.moin.auth.AccountGeneral;
import de.moinapp.moin.data.FriendCursorAdapter;
import de.moinapp.moin.db.DaoSession;
import de.moinapp.moin.db.FriendDao;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class FriendListActivity extends Activity {
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    String SENDER_ID = "";
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    String regid;

    @InjectView(R.id.main_list_friends)
    ListView mFriendListView;


    private AccountManager mAccountManager;
    private DaoSession mDaoSession;
    private FriendDao mFriendDao;

    private FriendCursorAdapter mFriendAdapter;

    private String mAuthToken;

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
        getTokenForAccountCreateIfNeeded(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);

        mDaoSession = ((MoinApplication) getApplication()).getDaoSession();
        mFriendDao = mDaoSession.getFriendDao();

        loadFriendsFromDatabase();
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

        sendMoin(userId);
    }

    private void sendMoin(String userId) {
        MoinService moin = MoinClient.getMoinService(this);
        moin.sendMoin(new Moin(userId), mAuthToken, new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {
                Toast.makeText(FriendListActivity.this, "Moin Sent!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.friend_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_friend_list_search) {
            startActivityForResult(new Intent(this, AddFriendActivity.class), 42);
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
    }


    private void getTokenForAccountCreateIfNeeded(String accountType, String authTokenType) {
        mAccountManager.getAuthTokenByFeatures(accountType, authTokenType, null, this, null, null,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        Bundle bnd = null;
                        try {
                            bnd = future.getResult();
                            mAuthToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);

                            registerGCMToken();

                        } catch (Exception e) {
                            e.printStackTrace();
                            showMessage(e.getMessage());
                        }
                    }
                }
                , null);
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

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    MoinService moin = MoinClient.getMoinService(FriendListActivity.this);
                    moin.addGCMId(new GCMID(regid), mAuthToken, new Callback<Void>() {
                        @Override
                        public void success(Void aVoid, Response response) {
                            storeRegistrationId(regid);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            error.printStackTrace();
                        }
                    });
                } catch (IOException ex) {
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
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
        editor.commit();
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
