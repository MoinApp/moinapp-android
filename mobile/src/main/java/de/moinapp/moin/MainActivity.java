package de.moinapp.moin;

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
import de.moinapp.moin.auth.AccountGeneral;
import de.moinapp.moin.db.DaoSession;
import de.moinapp.moin.db.FriendDao;


public class MainActivity extends Activity {

    @InjectView(R.id.main_list_friends)
    ListView mFriendListView;


    private AccountManager mAccountManager;
    private DaoSession mDaoSession;
    private FriendDao mFriendDao;

    private SimpleCursorAdapter mFriendAdapter;

    private String mAuthToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        mAccountManager = AccountManager.get(this);
        getTokenForAccountCreateIfNeeded(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);

        mDaoSession = ((MoinApplication) getApplication()).getDaoSession();
        mFriendDao = mDaoSession.getFriendDao();

        String textColumn = FriendDao.Properties.Username.columnName;
        String orderBy = textColumn + " COLLATE LOCALIZED ASC";
        Cursor cursor = mDaoSession.getDatabase().query(mFriendDao.getTablename(), mFriendDao.getAllColumns(), null, null, null, null, orderBy);
        String[] from = {textColumn, FriendDao.Properties.Username.columnName};

        int[] to = {android.R.id.text1, android.R.id.text2};

        mFriendAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_expandable_list_item_2, cursor, from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mFriendListView.setAdapter(mFriendAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(this, AddFriendActivity.class), 42);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 42) {
            mFriendAdapter.notifyDataSetChanged();
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
