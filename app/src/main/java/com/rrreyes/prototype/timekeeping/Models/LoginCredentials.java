package com.rrreyes.prototype.timekeeping.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by R. R. Reyes on 12/21/2017.
 */

public class LoginCredentials {

    @SerializedName("username")
    @Expose
    private String Username;

    @SerializedName("password")
    @Expose
    private String Password;

    public String getUsername() {
        return Username;
    }

    public String getPassword() {
        return Password;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
