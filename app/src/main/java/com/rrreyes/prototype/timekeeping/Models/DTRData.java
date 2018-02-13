package com.rrreyes.prototype.timekeeping.Models;

import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by R. R. Reyes on 12/21/2017.
 */

public class DTRData extends RealmObject {

    private String Barcode;
    private String Name;
    private String Date;
    private String Time;
    private String Type;
    private byte[] Image;
    private String ImageURL;

    public String getBarcode() {
        return Barcode;
    }

    public String getName() {
        return Name;
    }

    public String getDate() {
        return Date;
    }

    public String getTime() {
        return Time;
    }

    public String getType() {
        return Type;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public byte[] getImage() {
        return Image;
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

    public void setTime(String time) {
        Time = time;
    }

    public void setType(String type) {
        Type = type;
    }

    public void setImage(byte[] image) {
        Image = image;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }
}
