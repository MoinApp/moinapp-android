package de.jhbruhn.moin.data;

/**
 * Created by Jan-Henrik on 19.12.2014.
 */
public class GCMToken {
    public String token;
    public String type = "gcm";

    public GCMToken(String gcmId) {
        this.token = gcmId;
    }
}
