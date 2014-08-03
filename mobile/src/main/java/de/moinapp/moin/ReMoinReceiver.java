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
        if (!intent.hasExtra("peda")) return;
        String extra = intent.getStringExtra("peda");
        String senderId = TextUtils.split(extra, "\\|")[0];

        if (TextUtils.isEmpty(senderId)) return;


        int notificationId = Integer.valueOf(TextUtils.split(extra, "\\|")[1]);


        ((NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(notificationId);

        sendReMoin(context, senderId);

    }

    private void sendReMoin(final Context ctx, final String receiver) {
        sendReMoin(ctx, receiver, false);
    }

    private void sendReMoin(final Context ctx, final String receiver, final boolean retry) {
        final AccountManager accountManager = AccountManager.get(ctx);
        accountManager.getAuthTokenByFeatures(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, null, null, null,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        Bundle bnd = null;
                        try {
                            bnd = future.getResult();
                            final String mAuthToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);

                            MoinService moin = MoinClient.getMoinService(ctx);
                            moin.sendMoin(new Moin(receiver), mAuthToken, new Callback<Void>() {
                                @Override
                                public void success(Void aVoid, Response response) {

                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    if (error.getResponse().getStatus() == 403) {
                                        accountManager.invalidateAuthToken(AccountGeneral.ACCOUNT_TYPE, mAuthToken);
                                        if (!retry)
                                            sendReMoin(ctx, receiver, true);
                                    }
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
