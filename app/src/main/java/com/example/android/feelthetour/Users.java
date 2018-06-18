package com.example.android.feelthetour;

/**
 * Created by Vibhuti Singhania on 27-05-2018.
 */

public class Users {
    private int id;
    private String username, useremail;

    public Users(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.useremail = useremail;

    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return useremail;
    }

}
