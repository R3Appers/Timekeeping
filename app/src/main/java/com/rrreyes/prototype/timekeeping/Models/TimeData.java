package com.rrreyes.prototype.timekeeping.Models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by R. R. Reyes on 12/21/2017.
 */

public class TimeData extends RealmObject {

    private int Type;
    private String Date;
    private String TimeDate;

    public int getType() {
        return Type;
    }

    public String getDate() {
        return Date;
    }

    public String getTime() {
        return TimeDate;
    }

    public void setType(int type) {
        Type = type;
    }

    public void setDate(String date) {
        Date = date;
    }

    public void setTime(String time) {
        TimeDate = time;
    }
}
