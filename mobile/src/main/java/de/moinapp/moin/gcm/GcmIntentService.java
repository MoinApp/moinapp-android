package de.moinapp.moin.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Random;

import de.moinapp.moin.MoinApplication;
import de.moinapp.moin.R;
import de.moinapp.moin.activities.FriendListActivity;
import de.moinapp.moin.util.GravatarUtil;

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

        MoinApplication app = MoinApplication.getMoinApplication();

        String sender = extras.getString("username");
        String senderId = extras.getString("id");


        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, FriendListActivity.class), 0);


        int notificationId = Double.valueOf(Math.random() * 200).intValue();

        if (app.getUserNotificationIds().containsKey(senderId)) {
            notificationId = app.getUserNotificationIds().get(senderId);
        } else {
            app.getUserNotificationIds().put(senderId, notificationId);
        }


        int currentMoins = app.getCurrentMoinsPerUser().containsKey(senderId) ? app.getCurrentMoinsPerUser().get(senderId) : 0;
        currentMoins++;
        app.getCurrentMoinsPerUser().put(senderId, currentMoins);


        Intent remoinIntent = new Intent(this, ReMoinReceiver.class);
        String extraString = extras.getString("id") + "|" + notificationId + "";
        remoinIntent.putExtra("peda", extraString);

        int[] sounds = new int[]{R.raw.moin1, R.raw.moin3, R.raw.moin4, R.raw.moin5};

        Bitmap avatar = null;
        Bitmap bigPicture = null;

        Intent activityIntent = new Intent(this, FriendListActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activityIntent.putExtra("senderId", senderId);

        String moinsFrom = currentMoins == 1 ? getString(R.string.moin_from, sender) : getString(R.string.moins_from, currentMoins, sender);

        try {
            avatar = Picasso.with(getApplicationContext()).load(GravatarUtil.getAvatarUrl(extras.getString("email_hash"), getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width))).get();
            bigPicture = Picasso.with(getApplicationContext()).load(GravatarUtil.getAvatarUrl(extras.getString("email_hash"), 512)).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        NotificationCompat.Builder mBuilder;

        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Moin")
                .setContentIntent(PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(moinsFrom))
                .setContentText(moinsFrom)
                .setAutoCancel(true)
                .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + sounds[new Random().nextInt(sounds.length)]))
                .setLargeIcon(avatar)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(bigPicture).setSummaryText(moinsFrom).setBigContentTitle("Moin"))
                .addAction(R.drawable.ic_action_reply, getString(R.string.reply), PendingIntent.getBroadcast(this, notificationId, remoinIntent, PendingIntent.FLAG_ONE_SHOT));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(notificationId, mBuilder.build());


    }
}
