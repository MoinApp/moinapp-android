package de.jhbruhn.moin.wear;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import de.jhbruhn.moin.data.Constants;
import de.jhbruhn.moin.gcm.ReMoinReceiver;

/**
 * Created by Jan-Henrik on 24.12.2014.
 */
public class MoinWearableListenerService extends WearableListenerService {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        if(messageEvent.getPath().equals(Constants.MESSAGE_PATH_MOIN)) {
            Intent i = new Intent(this, ReMoinReceiver.class);
            i.putExtra(ReMoinReceiver.KEY_USERNAME, new String(messageEvent.getData()));
            sendBroadcast(i);
        }
        startService(new Intent(this, RecentListFetchingService.class));
    }
}
