package com.rrreyes.prototype.timekeeping.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by R. R. Reyes on 12/21/2017.
 */

public class LoginData {

    @SerializedName("company_id")
    @Expose
    private int CompanyID;

    @SerializedName("id")
    @Expose
    private int ID;

    public void setCompanyID(int companyID) {
        CompanyID = companyID;
    }

    public int getCompanyID() {
        return CompanyID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }
}
