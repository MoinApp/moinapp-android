package de.jhbruhn.moin.gcm;

import android.content.Intent;

import hugo.weaving.DebugLog;

public class InstanceIDListenerService extends com.google.android.gms.iid.InstanceIDListenerService {
    @Override
    @DebugLog
    public void onTokenRefresh() {
        super.onTokenRefresh();

        Intent intent = new Intent(this, GcmRegistrationService.class);
        startService(intent);
    }
}
