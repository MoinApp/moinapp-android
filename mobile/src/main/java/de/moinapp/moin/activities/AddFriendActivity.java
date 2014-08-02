package de.moinapp.moin.activities;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.andreabaccega.widget.FormEditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.moinapp.moin.MoinApplication;
import de.moinapp.moin.R;
import de.moinapp.moin.api.MoinClient;
import de.moinapp.moin.api.MoinService;
import de.moinapp.moin.api.User;
import de.moinapp.moin.auth.AccountGeneral;
import de.moinapp.moin.db.DaoSession;
import de.moinapp.moin.db.Friend;
import de.moinapp.moin.db.FriendDao;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class AddFriendActivity extends Activity {

    @InjectView(R.id.add_friend_username)
    FormEditText mUsernameText;

    @InjectView(R.id.add_friend_submit)
    Button mSubmitButton;


    private FriendDao mFriendDao;

    private String mAuthToken;
    private AccountManager mAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        ButterKnife.inject(this);

        DaoSession mDaoSession = ((MoinApplication) getApplication()).getDaoSession();
        mFriendDao = mDaoSession.getFriendDao();

        mAccountManager = AccountManager.get(this);
        getTokenForAccountCreateIfNeeded(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
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
                        }
                    }
                }
                , null);
    }

    @OnClick(R.id.add_friend_submit)
    public void searchFriend() {
        if (!mUsernameText.testValidity()) return;

        mSubmitButton.setEnabled(false);

        String username = mUsernameText.getText().toString();

        MoinService moin = MoinClient.getMoinService(this);
        moin.getUser(username, mAuthToken, new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                onUserFound(user);
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
                onFindUserError(error);
            }
        });
    }

    private void onFindUserError(RetrofitError e) {
        mSubmitButton.setEnabled(true);
        mUsernameText.setError(getString(R.string.add_friend_error_not_found));
    }

    private void onUserFound(User user) {
        if (mFriendDao.queryBuilder().where(FriendDao.Properties.Uuid.eq(user.id)).count() == 0) {
            Friend friend = new Friend();
            friend.setEmail(user.email_hash);
            friend.setUuid(user.id);
            friend.setUsername(user.username);
            mFriendDao.insertOrReplace(friend);
        }
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_friend, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
