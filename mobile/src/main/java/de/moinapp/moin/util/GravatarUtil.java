package de.moinapp.moin.util;

/**
 * Created by jhbruhn on 03.08.14.
 */
public class GravatarUtil {
    public static String getAvatarUrl(String emailHash, int size) {
        return "http://www.gravatar.com/avatar/" + emailHash + ".jpg?s=" + size + "&d=identicon";
    }
}
