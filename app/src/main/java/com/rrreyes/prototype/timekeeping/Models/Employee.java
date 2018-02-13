package com.rrreyes.prototype.timekeeping.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by R. R. Reyes on 12/21/2017.
 */

public class Employee {

    @SerializedName("code")
    @Expose
    private int Code;

    @SerializedName("msg")
    @Expose
    private String Message;

    @SerializedName("result")
    @Expose
    private List<EmployeeData> Data;

    public int getCode() {
        return Code;
    }

    public String getMessage() {
        return Message;
    }

    public List<EmployeeData> getData() {
        return Data;
    }
}
