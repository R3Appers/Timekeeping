package com.rrreyes.prototype.timekeeping.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by R. R. Reyes on 12/21/2017.
 */

public class Login {

    @SerializedName("code")
    @Expose
    private int Code;

    @SerializedName("message")
    @Expose
    private String Message;

    @SerializedName("data")
    @Expose
    private List<LoginData> Data;

    @SerializedName("token")
    @Expose
    private String Token;

    public void setCode(int code) {
        Code = code;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public void setToken(String token) {
        Token = token;
    }

    public void setData(List<LoginData> data) {
        Data = data;
    }

    public int getCode() {
        return Code;
    }

    public String getMessage() {
        return Message;
    }

    public String getToken() {
        return Token;
    }

    public List<LoginData> getData() {
        return Data;
    }
}
