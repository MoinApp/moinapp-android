package de.moinapp.moin.api;

/**
 * Created by jhbruhn on 02.08.14.
 */
public class User {
    public String username;
    public String id;
    public String password;
    public String email;
    public String email_hash;

    public User() {

    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String username, String password, String email) {
        this(username, password);
        this.email = email;
    }
}
