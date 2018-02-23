package com.rrreyes.prototype.timekeeping;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
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
import android.widget.TextView;
import android.widget.Toast;

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
    LinearLayout LL_Screen, LL_DeviceStatus, Btn_TimeIn, Btn_TimeOut, Btn_BreakIn, Btn_BreakOut;
    TextView TV_DateTime;
    RecyclerView RV_DTRView;
    boolean isFirstRun;

    SharedData sd;

    Calendar calendar;
    String currentDate;
    MainLogAdapter MLAdapter;

    LocationManager locationManager;
    LocationListener locationListener;
    double lng, lat;

    Handler handler, realmHandler;
    boolean mStopHandler = false;
    boolean isInPremises = false;
    boolean isAutoTime = false;

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
                            Manifest.permission.CHANGE_WIFI_STATE,
                            Manifest.permission.ACCESS_FINE_LOCATION
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
        LL_DeviceStatus = findViewById(R.id.LL_DeviceStatus);
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
            RunGPS();
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
                try {
                    int auto_time = 0;
                    if(Build.VERSION.SDK_INT >= 17) {
                        auto_time = Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME, 0);
                    } else {
                        auto_time = Settings.System.getInt(getContentResolver(), Settings.System.AUTO_TIME, 0);
                    }
                    if(auto_time != 0) {
                        isAutoTime = true;
                    } else {
                        isAutoTime = false;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if(isAutoTime && isInPremises) {
                        LL_DeviceStatus.setBackgroundColor(getResources().getColor(R.color.tabTextColorB));
                        /*Btn_TimeIn.setEnabled(true);
                        Btn_BreakOut.setEnabled(true);
                        Btn_BreakIn.setEnabled(true);
                        Btn_TimeOut.setEnabled(true);*/
                    } else {
                        LL_DeviceStatus.setBackgroundColor(getResources().getColor(R.color.tabTextColorA));
                        /*Btn_TimeIn.setEnabled(false);
                        Btn_BreakOut.setEnabled(false);
                        Btn_BreakIn.setEnabled(false);
                        Btn_TimeOut.setEnabled(false);*/
                    }
                }
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
            if(isAutoTime && isInPremises) {
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
            } else {
                if(!isAutoTime) {
                    Toast.makeText(this, "Gallifreyan Error 001 : Please Contact an Admin", Toast.LENGTH_SHORT).show();
                }
                if(!isInPremises) {
                    Toast.makeText(this, "Please Turn ON your Location and try again or Contact an Admin", Toast.LENGTH_SHORT).show();
                    /*Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getApplicationContext().startActivity(myIntent);*/
                    RunGPS();
                }
            }
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

    void RunGPS() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lng = location.getLongitude();
                lat = location.getLatitude();
                if((sd.GetLatitude() == 0.0f) && (sd.GetLongitude() == 0.0f)) {
                    sd.SetLatitude(lat);
                    sd.SetLongitude(lng);
                }
                if((Math.abs(lat - sd.GetLatitude()) < 0.002) &&
                        (Math.abs(lng - sd.GetLongitude()) < 0.002)) {
                    isInPremises = true;
                } else {
                    isInPremises = false;
                }
                /*Toast.makeText(getApplicationContext(), "(" + lat + ", " + lng + ") ===" +
                        "(" + sd.GetLatitude() + ", " + sd.GetLongitude() + ")", Toast.LENGTH_SHORT).show();*/
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                InitGPS();
            }

            @Override
            public void onProviderDisabled(String provider) {
                InitGPS();
            }
        };
        InitGPS();
    }

    void InitGPS() {
        locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if(ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED) {
            try {
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                } else {
                    Toast.makeText(this, "GPS NOT ENABLED. TURN ON YOUR LOCATION", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
