package com.rrreyes.prototype.timekeeping.Models;

import io.realm.RealmObject;

/**
 * Created by R. R. Reyes on 1/4/2018.
 */

public class DTRLog extends RealmObject {

    private String StartDate;
    private String EndDate;
    private String DateTimeSync;

    public String getStartDate() {
        return StartDate;
    }

    public String getEndDate() {
        return EndDate;
    }

    public String getDateTimeSync() {
        return DateTimeSync;
    }

    public void setStartDate(String startDate) {
        StartDate = startDate;
    }

    public void setEndDate(String endDate) {
        EndDate = endDate;
    }

    public void setDateTimeSync(String dateTimeSync) {
        DateTimeSync = dateTimeSync;
    }
}
