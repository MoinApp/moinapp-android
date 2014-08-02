package de.moinapp.moin.api;

import android.content.Context;

import de.moinapp.moin.BuildConfig;
import de.moinapp.moin.R;
import retrofit.RestAdapter;

/**
 * Created by jhbruhn on 02.08.14.
 */
public class MoinClient {

    public static MoinService getMoinService(Context ctx) {
        String endpointUrl = ctx.getString(false ? R.string.debug_server : R.string.production_server);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(endpointUrl)
                .build();
        return restAdapter.create(MoinService.class);
    }
}
