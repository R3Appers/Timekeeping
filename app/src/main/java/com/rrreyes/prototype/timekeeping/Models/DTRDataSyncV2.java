package com.rrreyes.prototype.timekeeping.Models;

/**
 * Created by R. R. Reyes on 1/10/2018.
 */

public class DTRDataSyncV2 {

    private String Date = null;
    private String Barcode = null;
    private String TimeIn = null;
    private String TimeOut = null;
    private String LunchIn = null;
    private String LunchOut = null;
    private String ImageUrl = null;

    public String getDate() {
        return Date;
    }

    public String getBarcode() {
        return Barcode;
    }

    public String getTimeIn() {
        return TimeIn;
    }

    public String getTimeOut() {
        return TimeOut;
    }

    public String getLunchIn() {
        return LunchIn;
    }

    public String getLunchOut() {
        return LunchOut;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setDate(String date) {
        Date = date;
    }

    public void setBarcode(String barcode) {
        Barcode = barcode;
    }

    public void setTimeIn(String timeIn) {
        TimeIn = timeIn;
    }

    public void setTimeOut(String timeOut) {
        TimeOut = timeOut;
    }

    public void setLunchIn(String lunchIn) {
        LunchIn = lunchIn;
    }

    public void setLunchOut(String lunchOut) {
        LunchOut = lunchOut;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }
}
