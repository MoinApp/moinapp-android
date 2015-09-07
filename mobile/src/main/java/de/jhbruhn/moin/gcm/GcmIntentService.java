package de.jhbruhn.moin.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import de.jhbruhn.moin.MoinApplication;
import de.jhbruhn.moin.R;
import de.jhbruhn.moin.api.GravatarApi;
import de.jhbruhn.moin.data.User;
import de.jhbruhn.moin.gui.RecentListActivity;
import hugo.weaving.DebugLog;


public class GcmIntentService extends IntentService {

    private static final String NOTIFICATION_GROUP = "MOIN";

    private static final int[] MOIN_SOUNDS = {R.raw.moin1, R.raw.moin3, R.raw.moin4, R.raw.moin5};

    @Inject
    @Named("unreadMoins")
    SharedPreferences mPreferencesUnreadMoins;

    @Inject
    @Named("receivedMoins")
    SharedPreferences mReceivedMoins;

    @Inject
    Picasso mPicasso;

    public GcmIntentService() {
        super("GCM STUFF");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        MoinApplication.get(this).inject(this);
    }

    @Override
    @DebugLog
    protected void onHandleIntent(Intent intent) {

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        if (!extras.isEmpty()) {
            User sender = new User(extras.getString("username"), "");
            sender.email_hash = extras.getString("email_hash");
            sender.id = extras.getString("id");

            String moinId = extras.getString("moin-uuid");
            Set<String> receivedMoins = mReceivedMoins.getStringSet(sender.username, new HashSet<String>());

            if (receivedMoins.contains(moinId)) return;

            receivedMoins.add(moinId);

            mReceivedMoins.edit().putStringSet(sender.username, receivedMoins).commit();


            int unreadMoins = mPreferencesUnreadMoins.getInt(sender.username, 0);
            mPreferencesUnreadMoins.edit().putInt(sender.username, ++unreadMoins).apply();


            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setGroup(NOTIFICATION_GROUP)
                    .setNumber(unreadMoins)
                    .setContentTitle(getString(R.string.notification_main_action_name))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + MOIN_SOUNDS[((int) Math.floor(Math.random() * MOIN_SOUNDS.length))]))
                    .setContentText(getString(R.string.notification_sent_by, sender.username));

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
            builder.addAction(R.drawable.ic_reply, getString(R.string.notification_action_reply), PendingIntent.getBroadcast(this, sender.getCrazyId(), remoinIntent, Intent.FILL_IN_DATA));

            if (unreadMoins > 1) {
                builder.setContentTitle(getString(R.string.notification_main_action_name_multiple, unreadMoins));
            }
            try {
                String url = GravatarApi.getGravatarURL(sender.email_hash, getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width));
                Bitmap smallBmp = mPicasso.load(url).error(R.drawable.default_avatar).get();
                builder.setLargeIcon(smallBmp);
            } catch (IOException e) {
            }

            try {
                String url = GravatarApi.getGravatarURL(sender.email_hash, 256);
                Bitmap largeBmp;

                largeBmp = mPicasso.load(url).error(R.drawable.default_avatar).get();

                NotificationCompat.WearableExtender wearableExtender =
                        new NotificationCompat.WearableExtender()
                                .setBackground(largeBmp);

                builder.extend(wearableExtender);
            } catch (IOException e) {
            }

            mNotificationManager.notify(sender.getCrazyId(), builder.build());
            Log.d("GCM", "Received Notificaiton!");

        }
    }
}
