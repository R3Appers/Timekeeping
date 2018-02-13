package com.rrreyes.prototype.timekeeping.Models;

import io.realm.RealmObject;

/**
 * Created by R. R. Reyes on 1/10/2018.
 */

public class DTRLogV2 extends RealmObject {

    private String Date;
    private String Barcode;
    private String Status;

    public String getDate() {
        return Date;
    }

    public String getBarcode() {
        return Barcode;
    }

    public String getStatus() {
        return Status;
    }

    public void setDate(String date) {
        Date = date;
    }

    public void setBarcode(String barcode) {
        Barcode = barcode;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
