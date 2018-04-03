package com.rrreyes.prototype.timekeeping;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
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
import android.widget.DatePicker;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    ImageView Btn_Settings;
    LinearLayout LL_Screen, LL_DeviceStatus, Btn_TimeIn, Btn_TimeOut, Btn_BreakIn, Btn_BreakOut;
    TextView TV_DateTime, Btn_CurrentDate;
    RecyclerView RV_DTRView;
    boolean isFirstRun;

    SharedData sd;

    Calendar calendar;
    String currentDate;
    String currentViewDate = null;
    MainLogAdapter MLAdapter;

    LocationManager locationManager;
    static LocationListener locationListener;
    double lng, lat;
    int RetryCtr = 1;
    int IdleCtr = 0;

    Handler handler, realmHandler;
    Runnable realmrun;
    boolean mStopHandler = false;
    //boolean isInPremises = false;
    boolean isAutoTime = false;
    boolean isLocationOn = false;

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
        IdleCtr = 0;

        if (realm != null) {
            realm.close();
            realm = null;
        }

        Realm.init(this);

        realm = Realm.getDefaultInstance();

        Constants.InitCloudinary(this);

        DeleteOldPictures();
    }

    void InitViews() {
        LL_Screen = findViewById(R.id.LL_Screen);
        LL_DeviceStatus = findViewById(R.id.LL_DeviceStatus);
        Btn_CurrentDate = findViewById(R.id.Btn_CurrentDate);
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
            /*RunGPS();
            InitGPS();
            if(!(sd.GetCurrentLongitude() + sd.GetCurrentLatitude() == 0.0f) && isLocationOn) {
                lat = sd.GetCurrentLatitude();
                lng = sd.GetCurrentLongitude();
                ComparePosition();
            }*/
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

                if(currentViewDate == null) {
                    currentViewDate = currentDate;
                    Btn_CurrentDate.setText(currentViewDate);
                }

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
                    if(isAutoTime/* && isInPremises*/) {
                        /*Toast.makeText(getApplicationContext(), "LOC: (" + sd.GetLatitude() + ", " + sd.GetLongitude() + ") CURRENT: " +
                                "(" + sd.GetCurrentLatitude() + ", " + sd.GetCurrentLongitude() + ")", Toast.LENGTH_SHORT).show();*/
                        RetryCtr = 1;
                        LL_DeviceStatus.setBackgroundColor(getResources().getColor(R.color.tabTextColorB));
                        sd.SetCurrentLongitude(lng);
                        sd.SetCurrentLatitude(lat);
                        if(IdleCtr % 60 == 0) {
                            IdleCtr = 1;
                            sd.SetCurrentLatitude(0.0f);
                            sd.SetCurrentLongitude(0.0f);
                            /*RunGPS();
                            InitGPS();*/
                        } else {
                            IdleCtr++;
                        }
                    } else {
                        if(/*!isInPremises &&*/ (RetryCtr % 10 == 0)) {
                            /*InitGPS();*/
                            RetryCtr = 1;
                        } else {
                            RetryCtr++;
                        }
                        IdleCtr = 1;
                        LL_DeviceStatus.setBackgroundColor(getResources().getColor(R.color.tabTextColorA));
                    }
                }
                TV_DateTime.setText(dateTime);
                if (!mStopHandler) {
                    handler.postDelayed(this, 1000);
                }
            }
        };

        realmrun = new Runnable() {
            @Override
            public void run() {
                try {
                    realm.beginTransaction();
                    String yesterDate = Constants.DATE_FORMAT.format(GetYesterday());
                    String tomorrDate = Constants.DATE_FORMAT.format(GetTomorrow());
                    Log.e("==DT==", yesterDate + ", " + currentViewDate +", " + tomorrDate);
                    dataList = realm
                            .where(DTRData.class)
                            .beginGroup()
                            .equalTo("Date", tomorrDate)
                            .or()
                            .equalTo("Date", currentViewDate)
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
                    MLAdapter.notifyDataSetChanged();
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
                    && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    ) {
                ShowSettings();
            }
        } else {
            if(isAutoTime/* && isInPremises*/) {
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
                /*if(!isInPremises) {
                    Toast.makeText(this, "Please Turn ON your Location and try again or Contact an Admin", Toast.LENGTH_SHORT).show();
                    *//*Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getApplicationContext().startActivity(myIntent);*//*
                    RunGPS();
                }*/
            }
        }
    }

    void ShowSettings() {
        TKDialogs tkDialogs = new TKDialogs(this, this);
        Dialog dialog = tkDialogs.LoginDialog();
        dialog.show();
    }

    Date GetYesterday() {
        Calendar cal = Calendar.getInstance();
        int year = Integer.parseInt(currentViewDate.substring(0, 4));
        int month = Integer.parseInt(currentViewDate.substring(5, 7));
        int day = Integer.parseInt(currentViewDate.substring(8, 10));
        Log.e("==DT==", year + " - " + month + " - " + day);
        cal.set(year, month - 1, day);
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    Date GetTomorrow() {
        Calendar cal = Calendar.getInstance();
        int year = Integer.parseInt(currentViewDate.substring(0, 4));
        int month = Integer.parseInt(currentViewDate.substring(5, 7));
        int day = Integer.parseInt(currentViewDate.substring(8, 10));
        Log.e("==DT==", year + " - " + month + " - " + day);
        cal.set(year, month - 1, day);
        cal.add(Calendar.DATE, 1);
        return cal.getTime();
    }

    /*void ComparePosition() {
        if((Math.abs(lat - sd.GetLatitude()) < 0.002) &&
                (Math.abs(lng - sd.GetLongitude()) < 0.002)) {
            isInPremises = true;
            if(IdleCtr % 60 == 0) {
                sd.SetCurrentLatitude(lat);
                sd.SetCurrentLongitude(lng);
                IdleCtr = 1;
            }
        } else {
            isInPremises = false;
            InitGPS();
            RunGPS();
        }
    }*/

    /*void RunGPS() {
        if(locationListener == null) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    lng = location.getLongitude();
                    lat = location.getLatitude();
                    if((sd.GetLatitude() == 0.0f) && (sd.GetLongitude() == 0.0f)) {
                        sd.SetLatitude(lat);
                        sd.SetLongitude(lng);
                    }
                    ComparePosition();
                *//*Toast.makeText(getApplicationContext(), "(" + lat + ", " + lng + ") ===" +
                        "(" + sd.GetLatitude() + ", " + sd.GetLongitude() + ")", Toast.LENGTH_SHORT).show();*//*
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
        }
        InitGPS();
    }*/

    /*void InitGPS() {
        locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if(ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED) {
            try {
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    isLocationOn = true;
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                } else {
                    Toast.makeText(this, "GPS NOT ENABLED. TURN ON YOUR LOCATION", Toast.LENGTH_SHORT).show();
                    isLocationOn = false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        1);
            }
            else {
                // Permission automatically granted on sdk<23 upon installation
                Log.v(Constants.LOG_TAG, Constants.GRANTED);
            }
        }
    }*/

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar cal = new GregorianCalendar(year, month, dayOfMonth);
        SetDate(cal, year, month, dayOfMonth);
    }

    private void SetDate(final Calendar calendar, int year, int month, int day) {
        final String date = Constants.DATE_FORMAT.format(calendar.getTime());
        int dateInt = (year * 10000) + ((month + 1) * 100) + (day);
        Log.i("==DATEINT==", dateInt + "");
        currentViewDate = date;
        Btn_CurrentDate.setText(date);
        realmHandler.post(realmrun);
    }

    public void DatePicker(View view){
        if((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED)) {
            try {
                DTRActivity.DatePickerFragment fragment = new DTRActivity.DatePickerFragment();
                fragment.show(getFragmentManager(), "DATE");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.READ_CALENDAR,
                                Manifest.permission.WRITE_CALENDAR
                        },
                        1);
            }
            else {
                // Permission automatically granted on sdk<23 upon installation
                Log.v(Constants.LOG_TAG, Constants.GRANTED);
            }
        }
    }

    private void DeleteOldPictures() {
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Timekeeping/";
        List<DTRData> list = new ArrayList<>();
        try {
            realm.beginTransaction();
            list = realm
                    .where(DTRData.class)
                    .findAll();
            realm.commitTransaction();

            if(list.size() > 3600) {
                for(int i = 0; i < (list.size() - 3600); i++) {
                    String file = dir + list.get(i).getBarcode() + "_" + list.get(i).getDate() + ".jpg";
                    File image = new File(file);
                    if(image.exists()) {
                        if(image.delete()) {
                            Log.i("==IMG=DEL==", "IMAGE DELETED");
                        } else {
                            Log.i("==IMG=DEL==", "IMAGE NOT DELETED");
                        }
                        try {
                            image.getCanonicalFile().delete();
                            if(image.exists()) {
                                if(getApplicationContext().deleteFile(image.getName())) {
                                    Log.i("==IMG=DEL==", "IMAGE DELETED");
                                } else {
                                    Log.i("==IMG=DEL==", "IMAGE NOT DELETED");
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            MediaScannerConnection.scanFile(
                                    getApplicationContext(),
                                    new String[]{file},
                                    null,
                                    new MediaScannerConnection.OnScanCompletedListener() {
                                        @Override
                                        public void onScanCompleted(String path, Uri uri) {
                                            Log.i("==CAM==", "File Scanned and Deleted.");
                                        }
                                    });
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
