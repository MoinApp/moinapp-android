package de.jhbruhn.moin.data;


import android.graphics.Bitmap;

/**
 * Created by Jan-Henrik on 19.12.2014.
 */
public class User {
    public String username;
    public String password;
    public String email_hash;
    public String id;
    public String email;
    public Bitmap avatar;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public int getCrazyId() {
        int sum = 0;
        for(Character c : id.toCharArray())
            if(Character.isDigit(c))
                sum += Integer.valueOf(c + "");
        return sum;
    }




}
