package de.moinapp.moin.activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.andreabaccega.widget.FormEditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.moinapp.moin.R;
import de.moinapp.moin.api.APIError;
import de.moinapp.moin.api.MoinClient;
import de.moinapp.moin.api.MoinService;
import de.moinapp.moin.api.Session;
import de.moinapp.moin.api.User;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by jhbruhn on 02.08.14.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {
    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";

    private final int REQ_SIGNUP = 1;


    @InjectView(R.id.sign_in_accountname)
    FormEditText mAccountName;

    @InjectView(R.id.sign_in_password)
    FormEditText mAccountPassword;

    @InjectView(R.id.sign_in_submit)
    Button mSubmitButton;


    private String mAuthTokenType;
    private AccountManager mAccountManager;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.inject(this);

        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
        mAccountManager = AccountManager.get(getBaseContext());
    }

    @OnClick(R.id.sign_in_sign_up)
    public void signUp() {
        Intent signup = new Intent(getBaseContext(), SignUpActivity.class);
        signup.putExtras(getIntent().getExtras());
        startActivityForResult(signup, REQ_SIGNUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
            finishLogin(data);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.sign_in_submit)
    public void submit() {
        if (!mAccountName.testValidity()) return;
        if (!mAccountPassword.testValidity()) return;

        final String username = mAccountName.getText().toString();
        final String password = mAccountPassword.getText().toString();

        final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

        MoinService moin = MoinClient.getMoinService(this);

        final Bundle data = new Bundle();

        mSubmitButton.setEnabled(false);

        moin.login(new User(username, password), new Callback<Session>() {
            @Override
            public void success(Session session, Response response) {
                data.putString(AccountManager.KEY_ACCOUNT_NAME, username);
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                data.putString(AccountManager.KEY_AUTHTOKEN, session.session);
                data.putString(PARAM_USER_PASS, password);
                final Intent res = new Intent();
                res.putExtras(data);
                finishLogin(res);
            }

            @Override
            public void failure(RetrofitError error) {
                mSubmitButton.setEnabled(true);
                APIError e = (APIError) error.getBodyAs(APIError.class);
                if (e.code != null && e.code.equals("CredentialsWrong")) {
                    mAccountPassword.setError(getString(R.string.sign_in_error_credentials_wrong));
                    mAccountPassword.requestFocus();
                }
                error.printStackTrace();
            }
        });
    }

    private void finishLogin(Intent intent) {
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = mAuthTokenType;

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
        } else {
            mAccountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }
}
