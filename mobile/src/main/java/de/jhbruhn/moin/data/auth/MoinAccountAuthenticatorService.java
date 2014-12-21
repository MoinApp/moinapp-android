package de.jhbruhn.moin.data.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Jan-Henrik on 19.12.2014.
 */
public class MoinAccountAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        MoinAccountAuthenticator a = new MoinAccountAuthenticator(this);
        return a.getIBinder();
    }
}
