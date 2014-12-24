package de.jhbruhn.moin.wear;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.jhbruhn.moin.MoinApplication;
import de.jhbruhn.moin.api.GravatarApi;
import de.jhbruhn.moin.data.Constants;
import de.jhbruhn.moin.data.User;
import de.jhbruhn.moin.data.api.MoinService;
import de.jhbruhn.moin.data.auth.AccountGeneral;

/**
 * Created by Jan-Henrik on 23.12.2014.
 */
public class RecentListFetchingService extends IntentService implements GoogleApiClient.ConnectionCallbacks {


    private static final String TAG = "RECENTS";

    @Inject
    MoinService mMoinService;

    @Inject
    Picasso mPicasso;


    private GoogleApiClient mGoogleApiClient;

    public RecentListFetchingService() {
        super("Recent List Fetcher");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        MoinApplication.get(this).inject(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        } else {
            fetchRecents();
        }
    }

    private void fetchRecents() {
        Log.d(TAG, "Fetching Recents...");

        AccountManager accountManager = AccountManager.get(this);

        accountManager.getAuthToken(accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)[0], AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, true, new AccountManagerCallback<Bundle>(){
            @Override
            public void run(final AccountManagerFuture<Bundle> bundleAccountManagerFuture) {
                new Thread() {
                    @Override
                    public void run() {
                        String authToken = null;
                        try {
                            Bundle b = bundleAccountManagerFuture.getResult();
                            authToken = b.getString(AccountManager.KEY_AUTHTOKEN);

                        } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                            e.printStackTrace();
                        }

                        if(authToken == null) return;

                        List<User> recentUsers = mMoinService.getRecents(authToken);
                        if(recentUsers == null || recentUsers.size() == 0) return;

                        ArrayList<DataMap> recentUserMaps = new ArrayList<>();

                        for(User u : recentUsers) {
                            DataMap m = userToDataMap(u);

                            Bitmap avatar = null;

                            try {
                                avatar = mPicasso.load(GravatarApi.getGravatarURL(u.email_hash, 128)).get();
                            } catch (IOException ignored) {
                            }

                            if(avatar != null) {
                                final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                                avatar.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
                                m.putAsset("avatar", Asset.createFromBytes(byteStream.toByteArray()));
                            }

                            recentUserMaps.add(m);
                        }

                        PutDataMapRequest dataMap = PutDataMapRequest.create(Constants.DATA_PATH_RECENTS);
                        dataMap.getDataMap().putDataMapArrayList("recents", recentUserMaps);

                        PutDataRequest request = dataMap.asPutDataRequest();
                        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                                .putDataItem(mGoogleApiClient, request);
                        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                            @Override
                            public void onResult(DataApi.DataItemResult dataItemResult) {
                                Log.d(TAG, "Saved!" + dataItemResult.getStatus());

                            }
                        });

                    }
                }.start();
            }
        }, null);
    }

    public DataMap userToDataMap(User user) {
        DataMap map = new DataMap();

        map.putString("username", user.username);
        map.putString("email_hash", user.email_hash);
        map.putString("id", user.id);

        return map;
    }

    @Override
    public void onConnected(Bundle bundle) {
        fetchRecents();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
