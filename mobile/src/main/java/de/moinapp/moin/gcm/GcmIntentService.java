package de.moinapp.moin.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.Random;

import de.moinapp.moin.R;
import de.moinapp.moin.activities.FriendListActivity;

/**
 * Created by jhbruhn on 02.08.14.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 42;
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


        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, FriendListActivity.class), 0);

        int notificationId = Double.valueOf(Math.random() * 200).intValue();

        Intent remoinIntent = new Intent(this, ReMoinReceiver.class);
        String extraString = extras.getString("id") + "|" + notificationId + "";
        remoinIntent.putExtra("peda", extraString);

        int[] sounds = new int[]{R.raw.moin1, R.raw.moin3, R.raw.moin4, R.raw.moin5};

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Moin")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(getString(R.string.moin_from, sender)))
                        .setContentText(getString(R.string.moin_from, sender))
                        .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + sounds[new Random().nextInt(sounds.length)]))
                        .addAction(R.drawable.ic_action_reply, getString(R.string.reply), PendingIntent.getBroadcast(this, notificationId, remoinIntent, PendingIntent.FLAG_ONE_SHOT));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(notificationId, mBuilder.build());
    }
}
