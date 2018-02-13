package com.rrreyes.prototype.timekeeping.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by R. R. Reyes on 12/22/2017.
 */

public class BasicResponse {

    @SerializedName("code")
    private int Code;

    public int getCode() {
        return Code;
    }

    public void setCode(int code) {
        Code = code;
    }
}
