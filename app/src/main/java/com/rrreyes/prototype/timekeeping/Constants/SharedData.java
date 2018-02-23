package com.rrreyes.prototype.timekeeping.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by R. R. Reyes on 12/21/2017.
 */

public class SharedData {

    SharedPreferences sp;

    public SharedData(Context context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean GetFirstTime() {
        return sp.getBoolean(Constants.KEY_FIRST_RUN, true);
    }

    public void SetFirstTime(boolean set) {
        sp.edit().putBoolean(Constants.KEY_FIRST_RUN, set).apply();
    }

    /*public int GetBranchID() {
        return sp.getInt(Constants.KEY_BRANCH_ID, 0);
    }

    public void SetBranchID(int set) {
        sp.edit().putInt(Constants.KEY_BRANCH_ID, set).apply();
    }*/

    public int GetCompanyID() {
        return sp.getInt(Constants.KEY_COMPANY_ID, 0);
    }

    public void SetCompanyID(int set) {
        sp.edit().putInt(Constants.KEY_COMPANY_ID, set).apply();
    }

    public String GetToken() {
        return sp.getString(Constants.KEY_TOKEN, null);
    }

    public void SetToken(String set) {
        sp.edit().putString(Constants.KEY_TOKEN, set).apply();
    }

    public int GetUserID() {
        return sp.getInt(Constants.KEY_USER_ID, 0);
    }

    public void SetUserID(int set) {
        sp.edit().putInt(Constants.KEY_USER_ID, set).apply();
    }

    public double GetLongitude() {
        return sp.getFloat(Constants.KEY_LONGITUDE, 0.0f);
    }

    public void SetLongitude(double set) {
        sp.edit().putFloat(Constants.KEY_LONGITUDE, (float)set).apply();
    }

    public double GetLatitude() {
        return sp.getFloat(Constants.KEY_LATITUDE, 0.0f);
    }

    public void SetLatitude(double set) {
        sp.edit().putFloat(Constants.KEY_LATITUDE, (float)set).apply();
    }


}
