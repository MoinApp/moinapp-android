package de.jhbruhn.moin.gui;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.melnykov.fab.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.InjectView;
import butterknife.OnClick;
import de.jhbruhn.moin.R;
import de.jhbruhn.moin.data.GCMToken;
import de.jhbruhn.moin.data.Moin;
import de.jhbruhn.moin.data.User;
import de.jhbruhn.moin.data.api.MoinService;
import de.jhbruhn.moin.data.auth.AccountGeneral;
import de.jhbruhn.moin.gui.data.UserRecyclerViewAdapter;
import hugo.weaving.DebugLog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RecentListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, UserRecyclerViewAdapter.UserRecyclerViewClickListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Inject
    MoinService mMoinService;
    @Inject
    Picasso mPicasso;
    @Inject
    @Named("unreadMoins")
    SharedPreferences mPreferencesUnreadMoins;

    AccountManager mAccountManager;

    @InjectView(R.id.recent_list_recycler_view)
    RecyclerView mRecentListRecyclerView;
    @InjectView(R.id.recent_list_add_friend_button)
    FloatingActionButton mAddFriendButton;
    @InjectView(R.id.recent_list_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private String mAuthToken;
    private UserRecyclerViewAdapter mRecentUsersAdapter;
    private SearchView mSearchView;
    private MenuItem mSearchMenuItem;

    private List<User> mLastRecents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
            getWindow().setEnterTransition(new Explode());
        }

        super.onCreate(savedInstanceState);


        mAccountManager = AccountManager.get(this);

        mRecentUsersAdapter = new UserRecyclerViewAdapter(mLastRecents, mPicasso, this);

        mRecentListRecyclerView.setHasFixedSize(true);
        mRecentListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecentListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecentListRecyclerView.setAdapter(mRecentUsersAdapter);

        mAddFriendButton.attachToRecyclerView(mRecentListRecyclerView);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.accent);

        logInCreateIfNeeded();
        markAllMoinsAsRead();
    }

    private void markAllMoinsAsRead() {
        for(Map.Entry<String, ?> e : mPreferencesUnreadMoins.getAll().entrySet()) {
            mPreferencesUnreadMoins.edit().putInt(e.getKey(), 0).apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_recent_list, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView =
                (SearchView) MenuItemCompat.getActionView(mSearchMenuItem = menu.findItem(R.id.recent_list_action_search));
        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.recent_list_action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                mAddFriendButton.hide(true);
                mSearchView.requestFocusFromTouch();
                mSearchView.setIconified(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                mAddFriendButton.show(true);
                onClose();
                return true;
            }
        });
        mSearchView.setIconifiedByDefault(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.recent_list_action_signout) {
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

    private void logInCreateIfNeeded() {
        mAccountManager.getAuthTokenByFeatures(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, this, null, null,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        Bundle bnd;
                        try {
                            bnd = future.getResult();
                            mAuthToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                            registerGCMId();
                            loadRecents();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, null);
    }

    private void registerGCMId() {
        String regid = getRegistrationId(this);

        if (regid.isEmpty()) {
            registerInBackground();
        }
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(RecentListActivity.this);
                    String regid = gcm.register(getString(R.string.google_cloud_id));

                    mMoinService.addGCMID(mAuthToken, new GCMToken(regid));

                    storeRegistrationId(regid);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return null;
            }

        }.execute(null, null, null);
    }

    private void storeRegistrationId(String regid) {
        final SharedPreferences prefs = getGCMPreferences(this);
        int appVersion = getAppVersion(this);
        Log.i("GCM", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regid);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(RecentListActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i("GCM", "Registration not found.");
            return "";
        }

        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i("GCM", "App version changed.");
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

    private void loadRecents() {
        mMoinService.getRecents(mAuthToken, new Callback<List<User>>() {
            @Override
            public void success(List<User> users, Response response) {
                mSwipeRefreshLayout.setRefreshing(false);
                populateRecentsList(users);
            }

            @Override
            public void failure(RetrofitError error) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @DebugLog
    private void populateRecentsList(List<User> recents) {
        mRecentUsersAdapter.setUsers(recents);
        mLastRecents = recents;
    }

    @OnClick(R.id.recent_list_add_friend_button)
    @DebugLog
    public void onAddFriendClick() {
        MenuItemCompat.expandActionView(mSearchMenuItem);
    }

    private void sendMoin(User receiver, Callback<Moin> cb) {
        mMoinService.sendMoin(new Moin(receiver.username), mAuthToken, cb);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_recent_list;
    }

    @Override
    public void onRefresh() {
        loadRecents();
    }

    @Override
    @DebugLog
    public void onUserClick(User user, int position, final View v) {
        mSearchView.setIconified(true);
        MenuItemCompat.collapseActionView(mSearchMenuItem);

        v.setEnabled(false);
        sendMoin(user, new Callback<Moin>() {
            @Override
            public void success(Moin moin, Response response) {
                v.setEnabled(true);
                loadRecents();
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
                v.setEnabled(true);
            }
        });
    }

    @Override
    @DebugLog
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    @DebugLog
    public boolean onQueryTextChange(String s) {
        if(s.isEmpty()) return true;
        mSwipeRefreshLayout.setEnabled(false);
        mMoinService.findUser(mAuthToken, s, new Callback<List<User>>() {

            @Override
            @DebugLog
            public void success(List<User> users, Response response) {
                mRecentUsersAdapter.setUsers(users);
            }

            @Override
            @DebugLog
            public void failure(RetrofitError error) {

            }
        });
        return true;
    }

    @Override
    public boolean onClose() {
        loadRecents();
        hideKeyboard();
        mSwipeRefreshLayout.setEnabled(true);
        return true;
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}