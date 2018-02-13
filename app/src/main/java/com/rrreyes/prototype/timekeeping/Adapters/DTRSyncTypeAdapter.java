package com.rrreyes.prototype.timekeeping.Adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.rrreyes.prototype.timekeeping.Models.DTRDataSync;
import com.rrreyes.prototype.timekeeping.Models.DTRSync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by R. R. Reyes on 12/29/2017.
 */

public class DTRSyncTypeAdapter extends TypeAdapter<DTRSync> {

    @Override
    public void write(JsonWriter out, DTRSync value) throws IOException {
        out.beginObject();
        out.name("start_date").value(value.getStartDate());
        out.name("end_date").value(value.getEndDate());
        out.name("branch_id").value(value.getBranchID());
        out.name("company_id").value(value.getCompanyID());
        out.name("user_id").value(value.getUserID());
        out.name("dtr").beginArray();
        /*for(final DTRDataSync dataSync : value.getData()) {
            out.beginObject();
            out.name("barcode").value(dataSync.getBarcode());
            out.name("name").value(dataSync.getName());
            out.name("date").value(dataSync.getDate());
            out.name("time").value(dataSync.getTime());
            out.name("type").value(dataSync.getType());
            out.name("img").value(dataSync.getImage());
            out.endObject();
        }*/
        out.endArray();
        out.endObject();
    }

    @Override
    public DTRSync read(JsonReader in) throws IOException {
        final DTRSync sync = new DTRSync();

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "start_date" :
                    sync.setStartDate(in.nextString());
                    break;
                case "end_date" :
                    sync.setEndDate(in.nextString());
                    break;
                case "branch_id" :
                    sync.setBranchID(in.nextInt());
                    break;
                case "company_id" :
                    sync.setCompanyID(in.nextInt());
                    break;
                case "user_id" :
                    sync.setUserID(in.nextInt());
                    break;
                case "dtr" :
                    final List<DTRDataSync> list = new ArrayList<>();
                    while(in.hasNext()) {
                        final String barcode = in.nextString();
                        final String name = in.nextString();
                        final String date = in.nextString();
                        final String time = in.nextString();
                        final String type = in.nextString();
                        final String img = in.nextString();
                        list.add(new DTRDataSync(barcode, name, date, time, type, img));
                    }
                    //sync.setData(list);
                    break;
            }
        }
        in.endObject();
        return sync;
    }
}
