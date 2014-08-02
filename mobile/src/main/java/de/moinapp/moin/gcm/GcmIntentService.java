package de.moinapp.moin.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.moinapp.moin.R;
import de.moinapp.moin.ReMoinReceiver;
import de.moinapp.moin.activities.FriendListActivity;

/**
 * Created by jhbruhn on 02.08.14.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 42;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;


    public GcmIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                sendMoinification(extras);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendMoinification(Bundle extras) {

        String sender = extras.getString("username");


        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, FriendListActivity.class), 0);

        int notificationId = (int) System.currentTimeMillis();

        Intent remoinIntent = new Intent(this, ReMoinReceiver.class);
        remoinIntent.putExtra("sender_id", extras.getString("id"));
        remoinIntent.putExtra("notification_id", notificationId);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Moin")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Moin from " + sender))
                        .setContentText("Moin from " + sender)
                        .addAction(R.drawable.ic_action_reply, "Re-Moin", PendingIntent.getBroadcast(this, 0, remoinIntent, 0));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(notificationId, mBuilder.build());
    }
}
