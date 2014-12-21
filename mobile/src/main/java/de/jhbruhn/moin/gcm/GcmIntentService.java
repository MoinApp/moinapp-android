package de.jhbruhn.moin.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import de.jhbruhn.moin.MoinApplication;
import de.jhbruhn.moin.R;
import de.jhbruhn.moin.api.GravatarApi;
import de.jhbruhn.moin.data.User;
import de.jhbruhn.moin.gui.RecentListActivity;


public class GcmIntentService extends IntentService {

    private static final String NOTIFICATION_GROUP = "MOIN";

    @Inject
    @Named("unreadMoins")
    SharedPreferences mPreferencesUnreadMoins;

    @Inject
    Picasso mPicasso;

    private NotificationManager mNotificationManager;

    public GcmIntentService() {
        super("GCM STUFF");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        MoinApplication.get(this).inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if(GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                User sender = new User(extras.getString("username"), "");
                sender.email_hash = extras.getString("email_hash");
                sender.id = extras.getString("id");

                int unreadMoins = mPreferencesUnreadMoins.getInt(sender.username, 0);
                mPreferencesUnreadMoins.edit().putInt(sender.username, ++unreadMoins).apply();


                NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                        .setGroup(NOTIFICATION_GROUP)
                        .setNumber(unreadMoins)
                        .setContentTitle(getString(R.string.main_action_name))
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentText(getString(R.string.sent_by, sender.username));

                Intent resultIntent = new Intent(this, RecentListActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addParentStack(RecentListActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                builder.setContentIntent(resultPendingIntent).setAutoCancel(true);

                Intent remoinIntent = new Intent(this, ReMoinReceiver.class);
                remoinIntent.putExtra(ReMoinReceiver.KEY_USERNAME, sender.username);
                remoinIntent.putExtra(ReMoinReceiver.KEY_NOTIFICATION_ID, sender.getCrazyId());
                builder.addAction(R.drawable.ic_reply, getString(R.string.action_reply), PendingIntent.getBroadcast(this, sender.getCrazyId(), remoinIntent, Intent.FILL_IN_DATA));

                if(unreadMoins > 1) {
                    builder.setContentTitle(getString(R.string.main_action_name_multiple, unreadMoins));
                }
                try {
                    String url = GravatarApi.getGravatarURL(sender.email_hash, getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width));
                    Bitmap smallBmp = mPicasso.load(url).get();
                    builder.setLargeIcon(smallBmp);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    String url = GravatarApi.getGravatarURL(sender.email_hash, 256);
                    Bitmap largeBmp;

                    largeBmp = mPicasso.load(url).get();

                    NotificationCompat.WearableExtender wearableExtender =
                            new NotificationCompat.WearableExtender()
                                    .setBackground(largeBmp);

                    builder.extend(wearableExtender);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mNotificationManager.notify(sender.getCrazyId(), builder.build());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}