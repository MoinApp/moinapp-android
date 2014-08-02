package de.moinapp.moin;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.moinapp.moin.api.MoinClient;
import de.moinapp.moin.api.MoinService;
import de.moinapp.moin.api.Session;
import de.moinapp.moin.api.User;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static de.moinapp.moin.AuthenticatorActivity.ARG_ACCOUNT_TYPE;
import static de.moinapp.moin.AuthenticatorActivity.PARAM_USER_PASS;

public class SignUpActivity extends Activity {

    @InjectView(R.id.sign_up_accountname)
    EditText mUsernameText;

    @InjectView(R.id.sign_up_password)
    EditText mPasswordText;

    @InjectView(R.id.sign_up_email)
    EditText mEmailText;


    private String mAccountType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAccountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

        ButterKnife.inject(this);
    }

    @OnClick(R.id.sign_up_sign_in)
    public void alreadyMember() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @OnClick(R.id.sign_up_submit)
    public void signUp() {
        final String username = mUsernameText.getText().toString();
        final String password = mPasswordText.getText().toString();
        final String email = mEmailText.getText().toString();

        MoinService moin = MoinClient.getMoinService(this);

        final Bundle data = new Bundle();

        moin.register(new User(username, password, email), new Callback<Session>() {
            @Override
            public void success(Session session, Response response) {
                if (session.status == 0) {
                    String authToken = session.session;

                    data.putString(AccountManager.KEY_ACCOUNT_NAME, username);
                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, mAccountType);
                    data.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                    data.putString(PARAM_USER_PASS, password);

                    final Intent res = new Intent();
                    res.putExtras(data);

                    setResult(RESULT_OK, res);
                    finish();
                } else {
                    Toast.makeText(getBaseContext(), session.message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getBaseContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sign_up, menu);
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
