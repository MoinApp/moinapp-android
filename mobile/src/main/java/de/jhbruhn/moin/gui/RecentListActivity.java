package de.jhbruhn.moin.gui;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.ViewFlipper;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.melnykov.fab.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.OnClick;
import de.jhbruhn.moin.MoinApplication;
import de.jhbruhn.moin.R;
import de.jhbruhn.moin.data.GCMToken;
import de.jhbruhn.moin.data.Moin;
import de.jhbruhn.moin.data.User;
import de.jhbruhn.moin.data.api.MoinService;
import de.jhbruhn.moin.data.auth.AccountGeneral;
import de.jhbruhn.moin.gcm.GcmRegistrationService;
import de.jhbruhn.moin.gui.data.UserRecyclerViewAdapter;
import de.jhbruhn.moin.wear.RecentListFetchingService;
import hugo.weaving.DebugLog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RecentListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, UserRecyclerViewAdapter.UserRecyclerViewClickListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";

    private static final int VIEW_FLIPPER_ITEM_LIST = 0;
    private static final int VIEW_FLIPPER_ITEM_LOADING = 1;
    private static final int VIEW_FLIPPER_ITEM_EMPTY = 2;
    private static final int VIEW_FLIPPER_ITEM_NO_RESULTS = 3;

    private static final String STATE_AUTH_TOKEN = "auth_token";
    private static final String STATE_RECENT_USERS = "recent";

    @Inject
    MoinService mMoinService;
    @Inject
    Picasso mPicasso;
    @Inject
    @Named("unreadMoins")
    SharedPreferences mPreferencesUnreadMoins;
    @Inject
    @Named("gcm")
    SharedPreferences mPreferencesGcm;


    AccountManager mAccountManager;

    @Bind(R.id.recent_list_recycler_view)
    RecyclerView mRecentListRecyclerView;
    @Bind(R.id.recent_list_add_friend_button)
    FloatingActionButton mAddFriendButton;
    @Bind(R.id.recent_list_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.recent_list_view_flipper)
    ViewFlipper mViewFlipper;

    private String mAuthToken;
    private UserRecyclerViewAdapter mRecentUsersAdapter;
    private SearchView mSearchView;
    private MenuItem mSearchMenuItem;

    private List<User> mLastRecents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle inState) {
        super.onCreate(inState);

        mAccountManager = AccountManager.get(this);

        mRecentUsersAdapter = new UserRecyclerViewAdapter(mLastRecents, mPicasso, this);

        mRecentListRecyclerView.setHasFixedSize(true);
        mRecentListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecentListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecentListRecyclerView.setAdapter(mRecentUsersAdapter);

        mAddFriendButton.attachToRecyclerView(mRecentListRecyclerView);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent);


        if(inState != null) {
            mAuthToken = inState.getString(STATE_AUTH_TOKEN);
            mLastRecents = inState.getParcelableArrayList(STATE_RECENT_USERS);

            mRecentUsersAdapter.setUsers(mLastRecents);

            registerGCMId();
            loadRecents();
        } else {
            mViewFlipper.setDisplayedChild(VIEW_FLIPPER_ITEM_LOADING);
            logInCreateIfNeeded();

        }

        markAllMoinsAsRead();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_AUTH_TOKEN, mAuthToken);
        outState.putParcelableArrayList(STATE_RECENT_USERS, (ArrayList<? extends android.os.Parcelable>) mLastRecents);

        super.onSaveInstanceState(outState);
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
                            startService(new Intent(RecentListActivity.this, RecentListFetchingService.class));
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
        startService(new Intent(this, GcmRegistrationService.class));
    }

    @DebugLog
    private String getRegistrationId(Context context) {
        String registrationId = mPreferencesGcm.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i("GCM", "Registration not found.");
            return "";
        }

        int registeredVersion = mPreferencesGcm.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i("GCM", "App version changed.");
            return "";
        }
        return registrationId;
    }

    @DebugLog
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
        Collections.reverse(recents);
        mRecentUsersAdapter.setUsers(recents);
        mLastRecents = recents;
        if(recents.size() == 0) {
            mViewFlipper.setDisplayedChild(VIEW_FLIPPER_ITEM_EMPTY);
        } else {
            mViewFlipper.setDisplayedChild(VIEW_FLIPPER_ITEM_LIST);
        }
    }

    @OnClick(R.id.recent_list_add_friend_button)
    @DebugLog
    public void onAddFriendClick() {
        MenuItemCompat.expandActionView(mSearchMenuItem);
    }

    private void sendMoin(User receiver, Callback<Moin> cb) {
        mMoinService.sendMoin(new Moin(receiver.username), mAuthToken, cb);

        MoinApplication.get(this).getTracker()
                .send(new HitBuilders.EventBuilder()
                                .setCategory(getString(R.string.ga_category_social))
                                .setAction(getString(R.string.ga_action_send_moin))
                                .setLabel("to " + receiver.username)
                                .build()
                );
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
        mViewFlipper.setDisplayedChild(VIEW_FLIPPER_ITEM_LOADING);
        mMoinService.findUser(mAuthToken, s, new Callback<List<User>>() {

            @Override
            @DebugLog
            public void success(List<User> users, Response response) {
                if(users.size() == 0) {
                    mViewFlipper.setDisplayedChild(VIEW_FLIPPER_ITEM_NO_RESULTS);
                } else {
                    mViewFlipper.setDisplayedChild(VIEW_FLIPPER_ITEM_LIST);
                }
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
