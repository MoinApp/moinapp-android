package de.moinapp.moin.activities;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import de.moinapp.moin.MoinApplication;
import de.moinapp.moin.R;
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

    private void getTokenForAccountCreateIfNeeded(String accountType, String authTokenType) {
        mAccountManager.getAuthTokenByFeatures(accountType, authTokenType, null, this, null, null,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        Bundle bnd = null;
                        try {
                            bnd = future.getResult();
                            mAuthToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);

                        } catch (Exception e) {
                            e.printStackTrace();
                            showMessage(e.getMessage());
                        }
                    }
                }
                , null);
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
}
