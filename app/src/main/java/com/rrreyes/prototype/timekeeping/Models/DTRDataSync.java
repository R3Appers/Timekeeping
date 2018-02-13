package com.rrreyes.prototype.timekeeping.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static io.realm.log.RealmLog.trace;

/**
 * Created by R. R. Reyes on 12/22/2017.
 */

public class DTRDataSync {

    @SerializedName("barcode")
    private String Barcode;

    @SerializedName("name")
    private String Name;

    @SerializedName("date")
    private String Date;

    @SerializedName("time")
    private String Time;

    @SerializedName("type")
    private String Type;

    @SerializedName("img")
    private String Image;

    public DTRDataSync() {
    }

    public DTRDataSync(String barcode, String name, String date, String time, String type, String img) {
        this.setBarcode(barcode);
        this.setName(name);
        this.setDate(date);
        this.setTime(time);
        this.setType(type);
        //this.setImage(img);
    }

    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("barcode", getBarcode());
            obj.put("name", getName());
            obj.put("date", getDate());
            obj.put("time", getTime());
            obj.put("type", getType());
            obj.put("img", getImage());
        } catch (JSONException e) {
            trace("DefaultListItem.toString JSONException: "+e.getMessage());
        }
        return obj;
    }

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

    public String getImage() {
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

    public void setImage(String image) {
        Image = image;
    }
}
