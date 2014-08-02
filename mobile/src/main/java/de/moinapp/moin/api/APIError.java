package de.moinapp.moin.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jhbruhn on 02.08.14.
 */
public class APIError {
    @SerializedName("code")
    public String code;
    @SerializedName("message")
    public String message;
}
