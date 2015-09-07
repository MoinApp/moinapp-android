package de.jhbruhn.moin.gcm;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import de.jhbruhn.moin.MoinApplication;
import de.jhbruhn.moin.R;
import de.jhbruhn.moin.data.GCMToken;
import de.jhbruhn.moin.data.api.MoinService;
import de.jhbruhn.moin.data.auth.AccountGeneral;
import de.jhbruhn.moin.gui.RecentListActivity;
import hugo.weaving.DebugLog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class GcmRegistrationService extends IntentService {

    @Inject
    MoinService mMoinService;

    @Inject
    @Named("gcm")
    SharedPreferences mPreferencesGcm;

    public GcmRegistrationService() {
        super("RegisterGCM");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((MoinApplication) getApplication()).inject(this);
    }

    @Override
    @DebugLog
    protected void onHandleIntent(Intent intent) {
        InstanceID instanceID = InstanceID.getInstance(this);
        try {
            final String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            AccountManager accountManager = AccountManager.get(this);
            Account[] accounts = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
            if (accounts.length == 0) return;

            accountManager.getAuthToken(accounts[0], AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, false, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> bundleAccountManagerFuture) {
                    try {
                        String authToken = bundleAccountManagerFuture.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                        mMoinService.addGCMID(authToken, new GCMToken(token), new Callback<GCMToken>() {
                            @Override
                            public void success(GCMToken gcmToken, Response response) {
                                int appVersion = getAppVersion(GcmRegistrationService.this);
                                Log.i("GCM", "Saving regId on app version " + appVersion);
                                SharedPreferences.Editor editor = mPreferencesGcm.edit();
                                editor.putString(RecentListActivity.PROPERTY_REG_ID, token);
                                editor.putInt(RecentListActivity.PROPERTY_APP_VERSION, appVersion);
                                editor.commit();
                            }

                            @Override
                            @DebugLog
                            public void failure(RetrofitError error) {

                            }
                        });

                    } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                        e.printStackTrace();
                    }
                }
            }, null);

        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
