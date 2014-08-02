package de.moinapp.moin.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by jhbruhn on 02.08.14.
 */
public class MoinAccountAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        MoinAccountAuthenticator a = new MoinAccountAuthenticator(this);
        return a.getIBinder();
    }
}
