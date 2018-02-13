package com.rrreyes.prototype.timekeeping.Models;

/**
 * Created by R. R. Reyes on 2/12/2018.
 */

public class DTRDataSorted {

    private String Barcode;
    private String Name;
    private String Date;
    private String TimeIn;
    private String TimeOut;
    private String BreakOut;
    private String BreakIn;

    public String getBarcode() {
        return Barcode;
    }

    public String getName() {
        return Name;
    }

    public String getDate() {
        return Date;
    }

    public String getTimeOut() {
        return TimeOut;
    }

    public String getTimeIn() {
        return TimeIn;
    }

    public String getBreakIn() {
        return BreakIn;
    }

    public String getBreakOut() {
        return BreakOut;
    }

    public void setBarcode(String barcode) {
        Barcode = barcode;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setDate(String date) {
        Date = date;
    }

    public void setTimeIn(String timeIn) {
        TimeIn = timeIn;
    }

    public void setTimeOut(String timeOut) {
        TimeOut = timeOut;
    }

    public void setBreakIn(String breakIn) {
        BreakIn = breakIn;
    }

    public void setBreakOut(String breakOut) {
        BreakOut = breakOut;
    }
}
