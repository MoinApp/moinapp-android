package de.jhbruhn.moin;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.jhbruhn.moin.data.User;

public class RecentListActivity extends Activity implements WearableListView.ClickListener {

    private static final String TAG = "PEDA";

    private GoogleApiClient mGoogleApiClient;

    private RecentUsersAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_list);

        WearableListView listView =
                (WearableListView) findViewById(R.id.activity_recent_users_list);

        listView.setAdapter(mAdapter = new RecentUsersAdapter(this, new ArrayList<User>()));

        listView.setClickListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        loadData();
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                        // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

    }

    private void loadData() {
        PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient);
        results.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(final DataItemBuffer dataItems) {
                final List<User> recents = new ArrayList<>();

                Log.d(TAG, "dataitems: " + dataItems.getCount());
                new Thread() {
                    @Override
                    public void run() {
                        if (dataItems.getCount() != 0) {
                            for(int i = 0; i < dataItems.getCount(); i++) {
                                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(i));
                                Log.d(TAG, "dataitem: " + dataMapItem.getUri());

                                if(dataMapItem.getUri().toString().contains("/recents")) {
                                    List<DataMap> recentDataMaps = dataMapItem.getDataMap().getDataMapArrayList("recents");
                                    Log.d(TAG, "dataitems: " + recentDataMaps.size());

                                    for(DataMap m : recentDataMaps) {
                                        recents.add(userFromDataMap(m));
                                    }
                                }

                            }


                        }

                        Log.d("P", "Setting recents..." + recents.size());

                        setRecents(recents);
                        dataItems.release();

                    }
                }.start();

            }
        });
    }

    private void setRecents(final List<User> recents) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.setUsers(recents);
            }
        });
    }

    private User userFromDataMap(DataMap m) {
        User u = new User(m.getString("username"), null);

        u.email_hash = m.getString("email_hash");
        u.id = m.getString("id");
        u.avatar = loadBitmapFromAsset(m.getAsset("avatar"));

        return u;
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            return null;
        }

        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {

    }

    @Override
    public void onTopEmptyRegionClick() {

    }
}
