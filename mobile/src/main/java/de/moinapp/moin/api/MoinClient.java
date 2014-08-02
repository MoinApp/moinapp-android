package de.moinapp.moin.api;

import android.content.Context;

import de.moinapp.moin.R;
import retrofit.RestAdapter;

/**
 * Created by jhbruhn on 02.08.14.
 */
public class MoinClient {

    public static MoinService getMoinService(Context ctx) {
        return getMoinService(false, ctx);
    }

    public static MoinService getMoinService(boolean debug, Context ctx) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ctx.getString(R.string.production_server))
                .build();
        return restAdapter.create(MoinService.class);
    }
}
