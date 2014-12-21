package de.jhbruhn.moin.gui;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.jhbruhn.moin.MoinApplication;
import de.jhbruhn.moin.R;
import de.jhbruhn.moin.data.MoinError;
import de.jhbruhn.moin.data.Session;
import de.jhbruhn.moin.data.User;
import de.jhbruhn.moin.data.api.MoinService;
import hugo.weaving.DebugLog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SignInActivity extends AccountAuthenticatorActivity {

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";

    private final int REQ_SIGNUP = 1;

    @Inject
    MoinService mMoinService;



    @InjectView(R.id.sign_in_username)
    EditText mUsername;
    @InjectView(R.id.sign_in_password)
    EditText mPassword;
    @InjectView(R.id.sign_in_action_sign_in)
    Button mSignInButton;

    @InjectView(R.id.register_username)
    EditText mRegisterUsername;
    @InjectView(R.id.register_password)
    EditText mRegisterPassword;
    @InjectView(R.id.register_email)
    EditText mRegisterEmail;
    @InjectView(R.id.sign_in_action_register)
    Button mRegisterButton;

    private String mAuthTokenType;
    private AccountManager mAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MoinApplication.get(this).inject(this);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.inject(this);

        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
        mAccountManager = AccountManager.get(this);
    }

    private void showAlertDialog(int errorTitle, int errorText) {
        showAlertDialog(getString(errorTitle), getString(errorText));
    }

    private void showAlertDialog(String title, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(text)
                .setTitle(title);

        builder.setNeutralButton(R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @OnClick(R.id.sign_in_action_register)
    public void actionRegister() {
        final String username = mRegisterUsername.getText().toString();
        final String password = mRegisterPassword.getText().toString();
        final String email = mRegisterEmail.getText().toString();

        if(username.isEmpty()) {
            mRegisterUsername.setError(getString(R.string.activity_sign_in_error_no_username));
            return;
        }

        if(password.isEmpty()) {
            mRegisterPassword.setError(getString(R.string.activity_sign_in_error_no_password));
            return;
        }

        if(email.isEmpty()) {
            mRegisterEmail.setError(getString(R.string.activity_sign_in_error_no_email));
            return;
        }

        final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

        final Bundle data = new Bundle();

        mSignInButton.setEnabled(false);
        mRegisterButton.setEnabled(false);

        User u = new User(username, password);
        u.email = email;

        mMoinService.register(u, new Callback<Session>() {
            @Override
            public void success(Session session, Response response) {
                data.putString(AccountManager.KEY_ACCOUNT_NAME, username);
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                data.putString(AccountManager.KEY_AUTHTOKEN, session.token);
                data.putString(PARAM_USER_PASS, password);

                final Intent res = new Intent();
                res.putExtras(data);
                finishLogin(res);
            }

            @Override
            public void failure(RetrofitError error) {
                mSignInButton.setEnabled(true);
                mRegisterButton.setEnabled(true);

                MoinError e = (MoinError) error.getBodyAs(MoinError.class);
                showAlertDialog("Error!", e.message);

                error.printStackTrace();
            }
        });
    }

    @OnClick(R.id.sign_in_action_sign_in)
    public void actionSignIn() {
        final String username = mUsername.getText().toString();
        final String password = mPassword.getText().toString();

        if(username.isEmpty()) {
            mUsername.setError(getString(R.string.activity_sign_in_error_no_username));
            return;
        }

        if(password.isEmpty()) {
            mPassword.setError(getString(R.string.activity_sign_in_error_no_password));
            return;
        }

        final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

        final Bundle data = new Bundle();

        mSignInButton.setEnabled(false);
        mRegisterButton.setEnabled(false);

        mMoinService.authenticate(new User(username, password), new Callback<Session>() {
            @Override
            public void success(Session session, Response response) {
                data.putString(AccountManager.KEY_ACCOUNT_NAME, username);
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                data.putString(AccountManager.KEY_AUTHTOKEN, session.token);
                data.putString(PARAM_USER_PASS, password);

                final Intent res = new Intent();
                res.putExtras(data);
                finishLogin(res);
            }

            @Override
            @DebugLog
            public void failure(RetrofitError error) {
                mSignInButton.setEnabled(true);
                mRegisterButton.setEnabled(true);

                MoinError e = (MoinError) error.getBodyAs(MoinError.class);
                showAlertDialog("Error!", e.message);

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
