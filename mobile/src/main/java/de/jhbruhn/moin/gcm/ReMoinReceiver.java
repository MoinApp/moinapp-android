package de.jhbruhn.moin.gcm;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import de.jhbruhn.moin.MoinApplication;
import de.jhbruhn.moin.data.Moin;
import de.jhbruhn.moin.data.api.MoinService;
import de.jhbruhn.moin.data.auth.AccountGeneral;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ReMoinReceiver extends BroadcastReceiver {

    public static final String KEY_USERNAME = "username";
    public static final String KEY_NOTIFICATION_ID = "notification";

    @Inject
    MoinService mMoinService;

    @Inject
    @Named("unreadMoins")
    SharedPreferences mPreferencesUnreadMoins;


    public ReMoinReceiver() {
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        MoinApplication.get(context).inject(this);

        final String receiver = intent.getStringExtra(KEY_USERNAME);
        Integer notificationId = intent.getIntExtra(KEY_NOTIFICATION_ID, 0);

        mPreferencesUnreadMoins.edit().putInt(receiver, 0).apply();

        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        if (accounts.length == 0) return;

        accountManager.getAuthToken(accounts[0], AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, false, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> bundleAccountManagerFuture) {
                try {
                    String authToken = bundleAccountManagerFuture.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                    mMoinService.sendMoin(new Moin(receiver), authToken, new Callback<Moin>() {

                        @Override
                        public void success(Moin moin, Response response) {
                            Log.d("TEST", "Sent remoin.");
                        }

                        @Override
                        public void failure(RetrofitError error) {

                        }
                    });
                } catch (OperationCanceledException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (AuthenticatorException e) {
                    e.printStackTrace();
                }
            }
        }, null);

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(notificationId);

    }
}
