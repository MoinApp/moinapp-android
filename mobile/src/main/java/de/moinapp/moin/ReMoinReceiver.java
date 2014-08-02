package de.moinapp.moin;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import de.moinapp.moin.api.Moin;
import de.moinapp.moin.api.MoinClient;
import de.moinapp.moin.api.MoinService;
import de.moinapp.moin.auth.AccountGeneral;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ReMoinReceiver extends BroadcastReceiver {
    public ReMoinReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String senderId = intent.getStringExtra("sender_id");
        if (TextUtils.isEmpty(senderId)) return;
        int notificationId = intent.getIntExtra("notification_id", -1);
        ((NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(notificationId);

        sendReMoin(context, senderId);

    }

    private void sendReMoin(final Context ctx, final String receiver) {
        AccountManager.get(ctx).getAuthTokenByFeatures(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, null, null,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        Bundle bnd = null;
                        try {
                            bnd = future.getResult();
                            String mAuthToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);

                            MoinService moin = MoinClient.getMoinService(ctx);
                            moin.sendMoin(new Moin(receiver), mAuthToken, new Callback<Void>() {
                                @Override
                                public void success(Void aVoid, Response response) {

                                }

                                @Override
                                public void failure(RetrofitError error) {

                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                , null);
    }
}
