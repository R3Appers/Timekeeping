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
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.utils.ObjectUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rrreyes.prototype.timekeeping.Adapters.Stringify;
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

public class DTRActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    Activity thisActivity = this;
    Button Btn_StartDate, Btn_EndDate, Btn_Send;
    ScrollView DTR_Logs;
    TextView TV_DTRLogView, Btn_Logout;
    View selectedView;
    boolean hasStart, hasEnd;

    Handler realmHandler;

    private TKService service;
    private SharedData sd;
    private DTRSync syncData;

    private List<DTRLogV2> logs;

    List<DTRLog> dtrLogs = new ArrayList<>();
    List<DTRLogV2> dtrLogsV2 = new ArrayList<>();
    List<DTRDataSyncV2> datas = new ArrayList<>();

    ProgressDialog progressDialog;

    int StartDate = 0;
    int EndDate = 0;
    int DataCounter = 0;

    final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Timekeeping/";

    Realm realm;
    Cloudinary cloudinary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dtr);

        Init();
        InitViews();
        cloudinary = new Cloudinary(Constants.InitCloudinary(this));
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
        Btn_Send = findViewById(R.id.Btn_Send);
        Btn_Logout = findViewById(R.id.Btn_Logout);

        DTR_Logs = findViewById(R.id.DTR_Logs);
        TV_DTRLogView = findViewById(R.id.TV_DTRLogView);

        Realm.init(this);

        realm = Realm.getDefaultInstance();

        service = RetrofitData.newInstance(this, sd.GetToken());

        StartDate = 0;
        EndDate = 0;
    }

    void InitViews() {
        InitLogsV2();
        Btn_Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Btn_Send.setEnabled(false);
                Btn_Send.setText(Constants.WAIT);
                if(hasStart && hasEnd) {
                    if(StartDate <= EndDate) {
                        SendV2();
                    }
                } else {
                    Btn_Send.setEnabled(true);
                    Btn_Send.setText(R.string.senddtr);
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

    void InitLogs() {
        realmHandler = new Handler();
        Runnable realmRun = new Runnable() {
            @Override
            public void run() {
                try {
                    realm.beginTransaction();
                    dtrLogs = realm.where(DTRLog.class).findAll();
                    StringBuilder logBuilder = new StringBuilder();
                    for(int i = 0; i < dtrLogs.size(); i++) {
                        logBuilder
                                .append(dtrLogs.get(i).getStartDate())
                                .append(" - ")
                                .append(dtrLogs.get(i).getEndDate())
                                .append(" | ")
                                .append(dtrLogs.get(i).getDateTimeSync())
                                .append("\n");
                    }
                    TV_DTRLogView.setText(logBuilder.toString());
                    realm.commitTransaction();
                } catch (Exception ex) {
                    Log.e("==REALM==", ex.getMessage());
                }
                DTR_Logs.fullScroll(View.FOCUS_DOWN);
            }
        };
        realmHandler.post(realmRun);
    }

    void InitLogsV2() {
        realmHandler = new Handler();
        Runnable realmRun = new Runnable() {
            @Override
            public void run() {
                try {
                    realm.beginTransaction();
                    dtrLogsV2 = realm.where(DTRLogV2.class).findAll();
                    StringBuilder logBuilder = new StringBuilder();
                    for(int i = 0; i < dtrLogsV2.size(); i++) {
                        logBuilder
                                .append(dtrLogsV2.get(i).getDate())
                                .append(" | ")
                                .append(dtrLogsV2.get(i).getBarcode())
                                .append(" | ")
                                .append(dtrLogsV2.get(i).getStatus())
                                .append("\n");
                    }
                    TV_DTRLogView.setText(logBuilder.toString());
                    realm.commitTransaction();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    DTR_Logs.fullScroll(View.FOCUS_DOWN);
                }
            }
        };
        realmHandler.post(realmRun);
    }

    void Send() {
        try {
            realm.beginTransaction();
            List<DTRDataSync> datas = new ArrayList<>();

            for(int i = StartDate; i <= EndDate; i++) {
                int temp = i % 100;
                int mtemp = (i % 10000) / 100;
                if(temp % 32 == 0) {
                    if(mtemp >= 12) {
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

                if(tempDatas.size() != 0) {
                    Log.i("==DTRSIZE==", "Size: " + tempDatas.size());
                    List<DTRDataSync> tDatas = new ArrayList<>();
                    for(int j = 0; j < tempDatas.size(); j++) {
                        DTRDataSync sync = new DTRDataSync();
                        DTRData data = tempDatas.get(j);
                        sync.setBarcode(data.getBarcode());
                        sync.setName(data.getName());
                        sync.setDate(data.getDate());
                        sync.setTime(data.getTime());
                        sync.setType(data.getType());
                        sync.setImage(Base64.encodeToString(data.getImage(), Base64.NO_WRAP | Base64.URL_SAFE));

                        tDatas.add(sync);
                    }
                    datas.addAll(tDatas);
                }
            }

            syncData = new DTRSync();
            syncData.setStartDate(Btn_StartDate.getText().toString());
            syncData.setEndDate(Btn_EndDate.getText().toString());
            syncData.setCompanyID(sd.GetCompanyID());
            syncData.setUserID(sd.GetUserID());
            JSONArray dataArray = new JSONArray();
            for (int i=0; i < datas.size(); i++) {
                dataArray.put(datas.get(i).getJSONObject());
            }
            syncData.setData(dataArray.toString());
        } catch (Exception ex) {
            Log.e("==DTR==", ex.getMessage());
        }
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        String src = gson.toJson(syncData);
        //Log.i("==SYNCSIZE==", "Sync Data Size: " + syncData.getData().size());
        Log.i("==SYNCDATA==", "Sync Data: " + src.length());

        if(!syncData.getData().equals("") && !syncData.getData().equals(" ")) {
            service.submitDTR(sd.GetCompanyID(), syncData).enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                    BasicResponse basicResponse = response.body();
                    Btn_Send.setEnabled(true);
                    Btn_Send.setText(R.string.senddtr);
                    if(response.body() != null) {
                        basicResponse = response.body();
                    }
                    if(basicResponse != null) {
                        if(basicResponse.getCode() == 200) {
                            Toast.makeText(thisActivity, Constants.SUCCESS_SYNC, Toast.LENGTH_LONG).show();
                            Intent i = new Intent(thisActivity, MainActivity.class);
                            startActivity(i);
                        } else {
                            Log.e("==RTGSError==", "Code Error: " + basicResponse.getCode());
                        }
                    } else {
                        Log.e("==RTGSError==", "No Response: " + response.raw().message());
                    }
                }

                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    Btn_Send.setEnabled(true);
                    Btn_Send.setText(R.string.senddtr);
                    t.printStackTrace();
                    Log.e("==DTRSEND==", t.getLocalizedMessage());
                    Toast.makeText(thisActivity, Constants.ERROR_SYNC, Toast.LENGTH_LONG).show();
                    Intent i = new Intent(thisActivity, MainActivity.class);
                    startActivity(i);
                }
            });
        } else {
            Btn_Send.setEnabled(true);
            Btn_Send.setText(R.string.senddtr);
            Toast.makeText(thisActivity, Constants.SEND_ERROR, Toast.LENGTH_LONG).show();
            Intent i = new Intent(thisActivity, MainActivity.class);
            startActivity(i);
        }
        realm.commitTransaction();

        try {
            realm.beginTransaction();
            Calendar calendar = Calendar.getInstance();
            String currentDate = Constants.DATE_FORMAT.format(new Date());
            String currentTime = Constants.TIME_FORMAT.format(calendar.getTime());
            String dateTime = new StringBuilder()
                    .append(currentDate)
                    .append(" : ")
                    .append(currentTime)
                    .toString();
            DTRLog dtrLog = new DTRLog();
            dtrLog.setStartDate(Btn_StartDate.getText().toString());
            dtrLog.setEndDate(Btn_EndDate.getText().toString());
            dtrLog.setDateTimeSync(dateTime);
            realm.copyToRealm(dtrLog);
            realm.commitTransaction();
        } catch (Exception ex) {
            Log.e("==DTRSAVE==", ex.getLocalizedMessage());
        }
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
                ProcessDTR(tempDatas);
            }
            realm.commitTransaction();

            DataCounter = 0;
            if(datas.size() != 0) {
                progressDialog.setMessage("Please Wait. Sending: " + DataCounter + "/"+ datas.size());
                progressDialog.show();
                logs.clear();
                for(int a = 0; a < datas.size(); a++) {
                    service.sendDTR(
                            sd.GetCompanyID(),
                            sd.GetUserID(),
                            datas.get(a).getDate(),
                            datas.get(a).getBarcode(),
                            datas.get(a).getTimeIn(),
                            datas.get(a).getTimeOut(),
                            datas.get(a).getLunchIn(),
                            datas.get(a).getLunchOut(),
                            datas.get(a).getImageUrl())
                            .enqueue(new Callback<BasicResponse>() {
                                @Override
                                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                                    DataCounter++;
                                    if(DataCounter >= datas.size() - 1) {
                                        FinishedSending(response);
                                        progressDialog.dismiss();
                                    } else {
                                        progressDialog.setMessage("Please Wait. Sending: " + DataCounter + "/"+ datas.size());
                                        if(response.body() != null) {
                                            DTRLogV2 log = new DTRLogV2();
                                            log.setDate(response.body().getDate());
                                            log.setBarcode(response.body().getBarcode());
                                            log.setStatus("OK");
                                            logs.add(log);
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<BasicResponse> call, Throwable t) {

                                }
                            });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void ProcessDTR(List<DTRData> tempDatas) {
        if(tempDatas.size() != 0) {
            List<DTRDataSyncV2> tDatas = new ArrayList<>();
            for(int a = 0; a < tempDatas.size(); a++) {
                DTRData data = tempDatas.get(a);
                DTRDataSyncV2 tSync = new DTRDataSyncV2();
                if(tDatas.size() != 0) {
                    int ctr = 0;
                    for(int j = 0; j < tDatas.size(); j++) {
                        if(tDatas.get(j).getDate().equals(data.getDate())
                                && tDatas.get(j).getBarcode().equals(data.getBarcode())) {
                            ctr++;
                            tSync = GetTimeByType(tDatas.get(j), data);
                            if(tSync.getImageUrl() == null) {
                                String imgPath = new StringBuilder(dir)
                                        .append(data.getBarcode())
                                        .append("_")
                                        .append(data.getDate())
                                        .append(".jpg")
                                        .toString();
                                tSync.setImageUrl(UploadImage(
                                        imgPath,
                                        data.getBarcode(),
                                        data.getDate()));
                            }
                            tDatas.remove(j);
                            tDatas.add(tSync);
                        }
                    }
                    if(ctr == 0) {
                        tSync.setDate(data.getDate());
                        tSync.setBarcode(data.getBarcode());
                        tSync = GetTimeByType(tSync, data);
                        if(tSync.getImageUrl() == null) {
                            String imgPath = new StringBuilder(dir)
                                    .append(data.getBarcode())
                                    .append("_")
                                    .append(data.getDate())
                                    .append(".jpg")
                                    .toString();
                            tSync.setImageUrl(UploadImage(
                                    imgPath,
                                    data.getBarcode(),
                                    data.getDate()));
                        }
                        tDatas.add(tSync);
                    }
                } else {
                    tSync.setDate(data.getDate());
                    tSync.setBarcode(data.getBarcode());
                    tSync = GetTimeByType(tSync, data);
                    if(tSync.getImageUrl() == null) {
                        String imgPath = new StringBuilder(dir)
                                .append(data.getBarcode())
                                .append("_")
                                .append(data.getDate())
                                .append(".jpg")
                                .toString();
                        tSync.setImageUrl(UploadImage(
                                imgPath,
                                data.getBarcode(),
                                data.getDate()));
                    }
                    tDatas.add(tSync);
                }
            }
            datas.addAll(tDatas);
        }
    }

    private void ProcessLogs() {
        try {
            realm.beginTransaction();
            for(int i = 0; i < logs.size(); i++) {
                DTRLogV2 log = logs.get(i);
                realm.copyToRealm(log);
            }
            realm.commitTransaction();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String UploadImage(String filepath, String barcode, String date) {
        File img = new File(filepath);
        String imgUrl = null;
        if(img.exists()) {
            try {
                String pid = new StringBuilder(sd.GetCompanyID())
                        .append("/")
                        .append(sd.GetCompanyID())
                        .append("/")
                        .append(date)
                        .toString();
                MediaManager
                        .get()
                        .upload(filepath)
                        .unsigned("pmtkv6wv")
                        .option("folder", pid)
                        .option("public_id", barcode)
                        .option("resource_type", "auto")
                        .dispatch();

                imgUrl = MediaManager.get().url().generate(pid + "/" + barcode + ".jpg");
                Log.e("=====URL=====", imgUrl);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Timekeeping/";
            List<Bitmap> imgList = new ArrayList<>();
            try {
                for(int i = 1; i < 5; i++) {
                    File image = new File(dir + barcode + "_" + date + "_" + i + ".png");
                    if(image.exists()) {
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                        imgList.add(bitmap);
                    } else {
                        image = new File(dir + barcode + "_" + date + "_" + 1 + ".png");
                        if(image.exists()) {
                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                            if(bitmap != null) {
                                imgList.add(CreateBlankImage(bitmap.getWidth(), bitmap.getHeight()));
                            } else {
                                imgList.add(CreateBlankImage(248, 248));
                            }
                        } else {
                            image = new File(dir + barcode + "_" + date + "_" + 2 + ".png");
                            if(image.exists()) {
                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                                if(bitmap != null) {
                                    imgList.add(CreateBlankImage(bitmap.getWidth(), bitmap.getHeight()));
                                } else {
                                    imgList.add(CreateBlankImage(248, 248));
                                }
                            } else {
                                image = new File(dir + barcode + "_" + date + "_" + 3 + ".png");
                                if(image.exists()) {
                                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                    Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                                    if(bitmap != null) {
                                        imgList.add(CreateBlankImage(bitmap.getWidth(), bitmap.getHeight()));
                                    } else {
                                        imgList.add(CreateBlankImage(248, 248));
                                    }
                                } else {
                                    image = new File(dir + barcode + "_" + date + "_" + 4 + ".png");
                                    if(image.exists()) {
                                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                                        if(bitmap != null) {
                                            imgList.add(CreateBlankImage(bitmap.getWidth(), bitmap.getHeight()));
                                        } else {
                                            imgList.add(CreateBlankImage(248, 248));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ProcessImage(imgList, barcode, date);
        }
        return imgUrl;
    }

    private Bitmap CreateBlankImage(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint);
        canvas.drawBitmap(bitmap, width, height, paint);
        return bitmap;
    }

    private void ProcessImage(List<Bitmap> bitmaps, String mBarcode, String mDate) {
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Timekeeping/";
        List<Bitmap> bits = new ArrayList<>();
        bits = bitmaps;
        Bitmap result = Bitmap.createBitmap(
                bitmaps.get(0).getWidth() * 2,
                bitmaps.get(0).getHeight() * 2,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        for (int i = 0; i < bits.size(); i++) {
            canvas.drawBitmap(
                    bits.get(i),
                    bits.get(i).getWidth() * (i % 2),
                    bits.get(i).getHeight() * (i / 2),
                    paint);
        }

        FileOutputStream outStream = null;
        String outFile = dir + mBarcode + "_" + mDate + ".jpg";
        try {
            outStream = new FileOutputStream(outFile);
            result.compress(Bitmap.CompressFormat.JPEG, 25, outStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        MediaScannerConnection.scanFile(
                getApplicationContext(),
                new String[]{outFile},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("==CAM==", "File Created and Scanned.");
                    }
                });
        File image = new File(outFile);
        if(image.exists()) {
            for(int i = 0; i < 5; i++) {
                File imgtemp = new File(dir + mBarcode + "_" + mDate + "_" + i + ".png");
                if(imgtemp.exists()) {
                    if(imgtemp.delete()) {
                        Log.i("==IMG=DEL==", "IMAGE DELETED");
                    } else {
                        Log.i("==IMG=DEL==", "IMAGE NOT DELETED");
                    }
                    try {
                        imgtemp.getCanonicalFile().delete();
                        if(imgtemp.exists()) {
                            if(getApplicationContext().deleteFile(imgtemp.getName())) {
                                Log.i("==IMG=DEL==", "IMAGE DELETED");
                            } else {
                                Log.i("==IMG=DEL==", "IMAGE NOT DELETED");
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Log.i("==IMG=DEL==", "INVALID IMAGE");
                }
            }
        }
    }

    private DTRDataSyncV2 GetTimeByType(DTRDataSyncV2 sync, DTRData data) {
        DTRDataSyncV2 tSync = sync;
        switch (data.getType()) {
            case "1" :
                if(tSync.getTimeIn() == null) {
                    tSync.setTimeIn(data.getTime());
                }
                break;
            case "2" :
                if(tSync.getLunchOut() == null) {
                    tSync.setLunchOut(data.getTime());
                }
                break;
            case "3" :
                tSync.setLunchIn(data.getTime());
                break;
            case "4" :
                tSync.setTimeOut(data.getTime());
                break;
        }
        return tSync;
    }

    private void FinishedSending(Response<BasicResponse> response) {
        BasicResponse basicResponse = response.body();
        Btn_Send.setEnabled(true);
        Btn_Send.setText(R.string.senddtr);
        if(response.body() != null) {
            basicResponse = response.body();
        }
        if(basicResponse != null) {
            if(basicResponse.getCode() == 200) {
                ProcessLogs();
                Toast.makeText(thisActivity, Constants.SUCCESS_SYNC, Toast.LENGTH_LONG).show();
                Intent i = new Intent(thisActivity, MainActivity.class);
                startActivity(i);
            } else {
                Log.e("==RTGSError==", "Code Error: " + basicResponse.getCode());
            }
        } else {
            Log.e("==RTGSError==", "No Response: " + response.raw().message());
        }
    }

    public void DatePicker(View view){
        DatePickerFragment fragment = new DatePickerFragment();
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
