package com.rrreyes.prototype.timekeeping.Adapters;

import android.util.Log;

import com.rrreyes.prototype.timekeeping.Models.DTRDataSync;

import java.util.List;

/**
 * Created by R. R. Reyes on 12/28/2017.
 */

public class Stringify {

    public static String StringifyDTRList(List<DTRDataSync> list) {
        StringBuilder stringy = new StringBuilder();
        stringy.append("[");
        for(int i = 0; i < list.size(); i++) {
            DTRDataSync data = list.get(i);
            stringy
                    .append("{")
                    .append("\"barcode\": \"")
                    .append(data.getBarcode())
                    .append("\", ")
                    .append("\"name\": \"")
                    .append(data.getName())
                    .append("\", ")
                    .append("\"date\": \"")
                    .append(data.getDate())
                    .append("\", ")
                    .append("\"time\": \"")
                    .append(data.getTime())
                    .append("\", ")
                    .append("\"type\": ")
                    .append(data.getType())
                    .append(", ")
                    .append("\"img\": \"")
                    .append(data.getImage())
                    .append("\"}");
            if(i != (list.size() - 1)) {
                stringy.append(",");
            }
        }
        stringy.append("]");
        return stringy.toString();
    }
}
