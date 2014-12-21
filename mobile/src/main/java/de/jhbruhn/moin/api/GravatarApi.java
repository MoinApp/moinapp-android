package de.jhbruhn.moin.api;

/**
 * Created by Jan-Henrik on 20.12.2014.
 */
public class GravatarApi {
    private static final String GRAVATAR_BASE = "https://www.gravatar.com/avatar/";

    public static String getGravatarURL(String emailHash) {
        return getGravatarURL(emailHash, 512);
    }
    public static String getGravatarURL(String emailHash, int size) {
        return GRAVATAR_BASE + emailHash + "?s=" + size + "&d=404";
    }
}
