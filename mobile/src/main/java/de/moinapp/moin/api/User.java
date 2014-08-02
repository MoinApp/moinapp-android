package de.moinapp.moin.api;

/**
 * Created by jhbruhn on 02.08.14.
 */
public class User {
    String username;
    String id;
    String password;
    String email;
    String email_hash;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String username, String password, String email) {
        this(username, password);
        this.email = email;
    }
}
