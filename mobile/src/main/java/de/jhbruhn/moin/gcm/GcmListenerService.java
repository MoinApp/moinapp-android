package de.jhbruhn.moin.gcm;

import android.content.Intent;
import android.os.Bundle;

import hugo.weaving.DebugLog;

public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {
    public GcmListenerService() {
    }

    @Override
    @DebugLog
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);

        Intent i = new Intent(this, GcmIntentService.class);
        i.putExtras(data);
        startService(i);
    }

}
