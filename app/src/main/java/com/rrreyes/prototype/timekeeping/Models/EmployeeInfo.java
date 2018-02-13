package com.rrreyes.prototype.timekeeping.Models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by R. R. Reyes on 12/21/2017.
 */

public class EmployeeInfo extends RealmObject {

    @PrimaryKey
    private String Barcode;
    private String FirstName;
    private String MiddleName;
    private String LastName;
    private String Position;
    private int BranchID;
    private String BranchName;

    public String getBarcode() {
        return Barcode;
    }

    public String getPosition() {
        return Position;
    }

    public String getMiddleName() {
        return MiddleName;
    }

    public String getLastName() {
        return LastName;
    }

    public String getFirstName() {
        return FirstName;
    }

    public String getBranchName() {
        return BranchName;
    }

    public int getBranchID() {
        return BranchID;
    }

    public void setBarcode(String barcode) {
        Barcode = barcode;
    }

    public void setPosition(String position) {
        Position = position;
    }

    public void setMiddleName(String middleName) {
        MiddleName = middleName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public void setBranchName(String branchName) {
        BranchName = branchName;
    }

    public void setBranchID(int branchID) {
        BranchID = branchID;
    }
}
