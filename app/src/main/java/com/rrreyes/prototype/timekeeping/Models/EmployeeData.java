package com.rrreyes.prototype.timekeeping.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by R. R. Reyes on 12/21/2017.
 */

public class EmployeeData {

    @SerializedName("barcode")
    @Expose
    private String Barcode;

    @SerializedName("branch_name")
    @Expose
    private String BranchName;

    @SerializedName("position")
    @Expose
    private String Position;

    @SerializedName("rowid")
    @Expose
    private int RowID;

    @SerializedName("fname")
    @Expose
    private String FirstName;

    @SerializedName("mname")
    @Expose
    private String MiddleName;

    @SerializedName("lname")
    @Expose
    private String LastName;

    @SerializedName("bday")
    @Expose
    private String Birthday;

    @SerializedName("branch_id")
    @Expose
    private int BranchID;

    @SerializedName("position_id")
    @Expose
    private int PositionID;

    @SerializedName("company_id")
    @Expose
    private int CompanyID;

    @SerializedName("date_created")
    @Expose
    private String DateCreated;

    public int getCompanyID() {
        return CompanyID;
    }

    public int getBranchID() {
        return BranchID;
    }

    public int getPositionID() {
        return PositionID;
    }

    public int getRowID() {
        return RowID;
    }

    public String getBarcode() {
        return Barcode;
    }

    public String getBirthday() {
        return Birthday;
    }

    public String getBranchName() {
        return BranchName;
    }

    public String getDateCreated() {
        return DateCreated;
    }

    public String getFirstName() {
        return FirstName;
    }

    public String getLastName() {
        return LastName;
    }

    public String getMiddleName() {
        return MiddleName;
    }

    public String getPosition() {
        return Position;
    }

    public void setCompanyID(int companyID) {
        CompanyID = companyID;
    }

    public void setBarcode(String barcode) {
        Barcode = barcode;
    }

    public void setBirthday(String birthday) {
        Birthday = birthday;
    }

    public void setBranchID(int branchID) {
        BranchID = branchID;
    }

    public void setBranchName(String branchName) {
        BranchName = branchName;
    }

    public void setDateCreated(String dateCreated) {
        DateCreated = dateCreated;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public void setMiddleName(String middleName) {
        MiddleName = middleName;
    }

    public void setPosition(String position) {
        Position = position;
    }

    public void setPositionID(int positionID) {
        PositionID = positionID;
    }

    public void setRowID(int rowID) {
        RowID = rowID;
    }
}
