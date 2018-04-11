package com.rrreyes.prototype.timekeeping;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.android.MediaManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.rrreyes.prototype.timekeeping.Constants.Constants;
import com.rrreyes.prototype.timekeeping.Constants.RetrofitData;
import com.rrreyes.prototype.timekeeping.Constants.SharedData;
import com.rrreyes.prototype.timekeeping.Interfaces.TKService;
import com.rrreyes.prototype.timekeeping.Models.BasicResponse;
import com.rrreyes.prototype.timekeeping.Models.DTRData;
import com.rrreyes.prototype.timekeeping.Models.DTRDataSync;
import com.rrreyes.prototype.timekeeping.Models.DTRDataSyncV2;
import com.rrreyes.prototype.timekeeping.Models.DTRLog;
import com.rrreyes.prototype.timekeeping.Models.DTRLogV2;
import com.rrreyes.prototype.timekeeping.Models.DTRSync;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataManagerActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    Activity thisActivity = this;
    Button Btn_StartDate, Btn_EndDate, Btn_Export, Btn_Import;
    TextView Btn_Logout;
    View selectedView;
    boolean hasStart, hasEnd;

    private SharedData sd;

    List<DTRDataSyncV2> datas = new ArrayList<>();

    ProgressDialog progressDialog;

    int StartDate = 0;
    int EndDate = 0;

    final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Timekeeping/Data/";

    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dtr);

        Init();
        InitViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    void Init() {
        this.sd = new SharedData(this);
        Btn_StartDate = findViewById(R.id.Btn_StartDate);
        Btn_EndDate = findViewById(R.id.Btn_EndDate);
        Btn_Export = findViewById(R.id.Btn_Export);
        Btn_Import = findViewById(R.id.Btn_Import);
        Btn_Logout = findViewById(R.id.Btn_Logout);

        Realm.init(this);

        realm = Realm.getDefaultInstance();

        StartDate = 0;
        EndDate = 0;
    }

    void InitViews() {
        Btn_Export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Btn_Export.setEnabled(false);
                Btn_Export.setText(Constants.WAIT);
                if(hasStart && hasEnd) {
                    if(StartDate <= EndDate) {
                        SendV2();
                    }
                } else {
                    Btn_Export.setEnabled(true);
                    Btn_Export.setText(R.string.exportData);
                }
            }
        });
        Btn_Import.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Btn_Import.setEnabled(false);
                Btn_Import.setText(Constants.WAIT);
                if(hasStart && hasEnd) {
                    if(StartDate <= EndDate) {
                        try {
                            ImportDTR();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Btn_Import.setEnabled(true);
                    Btn_Import.setText(R.string.exportData);
                }
            }
        });
        Btn_Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(thisActivity, MainActivity.class);
                startActivity(i);
            }
        });
    }

    void SendV2() {
        try {
            progressDialog = new ProgressDialog(this);

            realm.beginTransaction();
            datas = new ArrayList<>();

            for(int i = StartDate; i <= EndDate; i++) {
                int temp = i % 100;
                int mtemp = (i % 10000) / 100;
                if (temp % 32 == 0) {
                    if (mtemp >= 12) {
                        i -= i % 10000;
                        i += 10101;
                    } else {
                        i -= i % 100;
                        i += 101;
                    }
                }

                Log.i("==DATETIME==", (i / 10000)
                        + "-"
                        + String.format(Locale.US,"%02d", (i % 10000) / 100)
                        + "-"
                        + String.format(Locale.US,"%02d", (i % 100)));

                List<DTRData> tempDatas =
                        realm.where(DTRData.class)
                                .contains("Date",
                                        (i / 10000)
                                                + "-"
                                                + String.format(Locale.US,"%02d", (i % 10000) / 100)
                                                + "-"
                                                + String.format(Locale.US,"%02d", (i % 100)))
                                .findAll();
                ExportDTR(tempDatas);
            }
            realm.commitTransaction();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void ExportDTR(List<DTRData> data) throws IOException {
        String filepath = dir +
                sd.GetCompanyID() + "-" +
                sd.GetUserID() + "_" +
                Btn_StartDate.getText() + "_" +
                Btn_EndDate.getText() + ".csv";
        File f = new File(filepath);
        CSVWriter writer;
        // File exist
        if(f.exists() && !f.isDirectory()){
            FileWriter mFileWriter = new FileWriter(filepath, true);
            writer = new CSVWriter(mFileWriter);
        } else {
            writer = new CSVWriter(new FileWriter(filepath));
        }
        for(int i = 0; i < data.size(); i++) {
            String[] line = {
                    data.get(i).getDate(),
                    data.get(i).getBarcode(),
                    data.get(i).getName(),
                    data.get(i).getType(),
                    data.get(i).getTime()
            };
            writer.writeNext(line);
        }
        writer.close();
    }

    void ImportDTR() throws IOException {
        String filepath = dir +
                sd.GetCompanyID() + "-" +
                sd.GetUserID() + "_" +
                Btn_StartDate.getText() + "_" +
                Btn_EndDate.getText() + ".csv";
        CSVReader reader = new CSVReader(new FileReader(filepath));
        String[] nextLine;
        realm.beginTransaction();
        while ((nextLine = reader.readNext()) != null) {
            DTRData data = new DTRData();
            data.setDate(nextLine[0]);
            data.setBarcode(nextLine[1]);
            data.setName(nextLine[2]);
            data.setType(nextLine[3]);
            data.setTime(nextLine[4]);
            try {
                realm.copyToRealm(data);
                realm.commitTransaction();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        realm.close();
    }

    public void DatePicker(View view){
        DTRActivity.DatePickerFragment fragment = new DTRActivity.DatePickerFragment();
        fragment.show(getFragmentManager(), "DATE");

        if(view == Btn_StartDate) {
            this.selectedView = Btn_StartDate;
        }
        if(view == Btn_EndDate) {
            this.selectedView = Btn_EndDate;
        }
    }

    private void SetDate(final Calendar calendar, int year, int month, int day) {
        final String date = Constants.DATE_FORMAT.format(calendar.getTime());
        int dateInt = (year * 10000) + ((month + 1) * 100) + (day);
        Log.i("==DATEINT==", dateInt + "");
        if(selectedView == Btn_StartDate) {
            StartDate = dateInt;
            Btn_StartDate.setText(date);
            hasStart = true;
        }
        if(selectedView == Btn_EndDate) {
            if(dateInt < StartDate) {
                Toast.makeText(this, Constants.DATE_ERROR, Toast.LENGTH_SHORT).show();
            } else {
                EndDate = dateInt;
                Btn_EndDate.setText(date);
                hasEnd = true;
            }
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar cal = new GregorianCalendar(year, month, dayOfMonth);
        SetDate(cal, year, month, dayOfMonth);
    }

    public static class DatePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(),
                    (DatePickerDialog.OnDateSetListener)
                            getActivity(), year, month, day);
        }

    }
}
