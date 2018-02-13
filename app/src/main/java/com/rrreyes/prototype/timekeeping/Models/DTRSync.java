package com.rrreyes.prototype.timekeeping.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by R. R. Reyes on 12/21/2017.
 */

public class DTRSync {

    @SerializedName("start_date")
    private String StartDate;

    @SerializedName("end_date")
    private String EndDate;

    @SerializedName("branch_id")
    private int BranchID;

    @SerializedName("company_id")
    private int CompanyID;

    @SerializedName("user_id")
    private int UserID;

    @SerializedName("dtr")
    private String Data;

    public DTRSync() {
    }

    public int getBranchID() {
        return BranchID;
    }

    public int getCompanyID() {
        return CompanyID;
    }

    public int getUserID() {
        return UserID;
    }

    public String getStartDate() {
        return StartDate;
    }

    public String getEndDate() {
        return EndDate;
    }

    public String getData() {
        return Data;
    }

    public void setBranchID(int branchID) {
        BranchID = branchID;
    }

    public void setCompanyID(int companyID) {
        CompanyID = companyID;
    }

    public void setUserID(int userID) {
        UserID = userID;
    }

    public void setStartDate(String startDate) {
        StartDate = startDate;
    }

    public void setEndDate(String endDate) {
        EndDate = endDate;
    }

    public void setData(String data) {
        Data = data;
    }

    /*public static class DTRSyncList {

        private List<DTRDataSync> list;

        public DTRSyncList() {
            list = new ArrayList<>();
        }

        public List<DTRDataSync> getList() {
            return list;
        }

        public void setList(List<DTRDataSync> list) {
            this.list = list;
        }
    }*/
}
