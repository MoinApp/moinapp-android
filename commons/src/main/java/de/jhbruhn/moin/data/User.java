package de.jhbruhn.moin.data;


import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jan-Henrik on 19.12.2014.
 */
public class User implements Parcelable {
    public String name;
    public String password;
    public String email_hash;
    public String id;
    public String email;
    public Bitmap avatar;

    public User(String username, String password) {
        this.name = username;
        this.password = password;
    }

    public User(Parcel in) {
        this.name = in.readString();
        this.password = in.readString();
        this.email_hash = in.readString();
        this.id = in.readString();
        this.email = in.readString();
    }

    public int getCrazyId() {
        int sum = 0;
        if(id != null)
            for(Character c : id.toCharArray())
                if(Character.isDigit(c))
                    sum += Integer.valueOf(c + "");
        return sum;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(password);
        parcel.writeString(email_hash);
        parcel.writeString(id);
        parcel.writeString(email);
    }

    public static final Parcelable.Creator<User> CREATOR
            = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
