package com.rrreyes.prototype.timekeeping;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.rrreyes.prototype.timekeeping.Adapters.DTRDataSorter;
import com.rrreyes.prototype.timekeeping.Adapters.MainLogAdapter;
import com.rrreyes.prototype.timekeeping.Constants.Constants;
import com.rrreyes.prototype.timekeeping.Constants.SharedData;
import com.rrreyes.prototype.timekeeping.Dialogs.TKDialogs;
import com.rrreyes.prototype.timekeeping.Models.DTRData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    ImageView Btn_Settings;
    LinearLayout LL_Screen, Btn_TimeIn, Btn_TimeOut, Btn_BreakIn, Btn_BreakOut;
    TextView TV_DateTime;
    RecyclerView RV_DTRView;
    boolean isFirstRun;

    SharedData sd;

    Calendar calendar;
    String currentDate;
    MainLogAdapter MLAdapter;

    Handler handler, realmHandler;
    boolean mStopHandler = false;

    List<DTRData> dataList = new ArrayList<>();

    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitValues();
        InitViews();
        SetupViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    void GetPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.INTERNET,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_CALENDAR,
                            Manifest.permission.WRITE_CALENDAR,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.CHANGE_WIFI_STATE
                    },
                    1);
        }
        else {
            // Permission automatically granted on sdk<23 upon installation
            Log.v(Constants.LOG_TAG, Constants.GRANTED);
        }
    }

    void InitValues() {
        sd = new SharedData(this);
        isFirstRun = sd.GetFirstTime();

        if (realm != null) {
            realm.close();
            realm = null;
        }

        Realm.init(this);

        realm = Realm.getDefaultInstance();

        Constants.InitCloudinary(this);
    }

    void InitViews() {
        LL_Screen = findViewById(R.id.LL_Screen);
        Btn_Settings = findViewById(R.id.Btn_Settings);
        Btn_TimeIn = findViewById(R.id.Btn_TimeIn);
        Btn_BreakOut = findViewById(R.id.Btn_BreakOut);
        Btn_BreakIn = findViewById(R.id.Btn_BreakIn);
        Btn_TimeOut = findViewById(R.id.Btn_TimeOut);

        RV_DTRView = findViewById(R.id.RV_DTRView);
        TV_DateTime = findViewById(R.id.TV_DateTime);
    }

    void SetupViews() {
        if(isFirstRun) {
            LL_Screen.setVisibility(View.VISIBLE);
            Btn_TimeIn.setVisibility(View.INVISIBLE);
            Btn_BreakOut.setVisibility(View.INVISIBLE);
            Btn_BreakIn.setVisibility(View.INVISIBLE);
            Btn_TimeOut.setVisibility(View.INVISIBLE);
        } else {
            LL_Screen.setVisibility(View.GONE);
        }

        handler = new Handler();
        realmHandler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                calendar = Calendar.getInstance();
                currentDate = Constants.DATE_FORMAT.format(new Date());
                String currentTime = Constants.TIME_FORMAT.format(calendar.getTime());
                String dateTime = new StringBuilder()
                        .append(currentDate)
                        .append(" : ")
                        .append(currentTime)
                        .toString();
                TV_DateTime.setText(dateTime);
                if (!mStopHandler) {
                    handler.postDelayed(this, 1000);
                }
            }
        };

        Runnable realmrun = new Runnable() {
            @Override
            public void run() {
                try {
                    realm.beginTransaction();
                    String yesterDate = Constants.DATE_FORMAT.format(GetYesterday());
                    dataList = realm
                            .where(DTRData.class)
                            .beginGroup()
                            .equalTo("Date", currentDate)
                            .or()
                            .equalTo("Date", yesterDate)
                            .endGroup()
                            .findAll();
                    DTRDataSorter dtrSorter = new DTRDataSorter();
                    MLAdapter = new MainLogAdapter(getApplicationContext(), dtrSorter.SortData(dataList));
                    LinearLayoutManager LLManager = new LinearLayoutManager(getApplicationContext());
                    LLManager.setOrientation(LinearLayoutManager.VERTICAL);
                    LLManager.setStackFromEnd(false);
                    RV_DTRView.setLayoutManager(LLManager);
                    RV_DTRView.setAdapter(MLAdapter);
                    RV_DTRView.scrollToPosition(MLAdapter.getItemCount() - 1);

                    /*StringBuilder logBuilder = new StringBuilder();
                    for(int i = 0; i < dataList.size(); i++) {
                        logBuilder
                                .append(dataList.get(i).getName())
                                .append(" | ")
                                .append(dataList.get(i).getDate())
                                .append(" | ")
                                .append(dataList.get(i).getTime())
                                .append(" | ");
                        switch (dataList.get(i).getType()) {
                            case "1" :
                                logBuilder.append("TIME IN");
                                break;
                            case "2" :
                                logBuilder.append("BREAK OUT");
                                break;
                            case "3" :
                                logBuilder.append("BREAK IN");
                                break;
                            case "4" :
                                logBuilder.append("TIME OUT");
                                break;
                        }
                        logBuilder.append("\n");
                    }*/
                    realm.commitTransaction();
                } catch (Exception ex) {
                    Log.e("==REALM==", ex.getMessage());
                }
            }
        };

        handler.post(runnable);
        realmHandler.post(realmrun);
    }

    public void Btn_Click(View view) {

        if (view == Btn_Settings) {
            GetPermissions();
            if((ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED)
                    ) {
                ShowSettings();
            }
        } else {
            Intent i = new Intent(this, ScanActivity.class);
            if (view == Btn_TimeIn) {
                i.putExtra(Constants.TIME_TYPE, 1);
            }
            if (view == Btn_BreakOut) {
                i.putExtra(Constants.TIME_TYPE, 2);
            }
            if (view == Btn_BreakIn) {
                i.putExtra(Constants.TIME_TYPE, 3);
            }
            if (view == Btn_TimeOut) {
                i.putExtra(Constants.TIME_TYPE, 4);
            }
            startActivity(i);
        }
    }

    void ShowSettings() {
        TKDialogs tkDialogs = new TKDialogs(this);
        Dialog dialog = tkDialogs.LoginDialog();
        dialog.show();
    }

    Date GetYesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }
}
