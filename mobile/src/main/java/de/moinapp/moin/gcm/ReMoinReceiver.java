package de.moinapp.moin.gcm;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import de.moinapp.moin.MoinApplication;
import de.moinapp.moin.jobs.SendMoinJob;

public class ReMoinReceiver extends BroadcastReceiver {
    public ReMoinReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.hasExtra("peda")) return;
        String extra = intent.getStringExtra("peda");
        String senderId = TextUtils.split(extra, "\\|")[0];

        if (TextUtils.isEmpty(senderId)) return;

        MoinApplication.getMoinApplication().getCurrentMoinsPerUser().remove(senderId);

        int notificationId = Integer.valueOf(TextUtils.split(extra, "\\|")[1]);


        ((NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(notificationId);

        sendReMoin(senderId);
    }

    private void sendReMoin(final String receiver) {
        MoinApplication.getMoinApplication().getJobManager().addJobInBackground(new SendMoinJob(receiver));
    }
}
