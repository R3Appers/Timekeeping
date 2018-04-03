package com.rrreyes.prototype.timekeeping;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.rrreyes.prototype.timekeeping.Constants.Constants;
import com.rrreyes.prototype.timekeeping.Dialogs.TKDialogs;
import com.rrreyes.prototype.timekeeping.Models.DTRData;
import com.rrreyes.prototype.timekeeping.Models.EmployeeInfo;
import com.rrreyes.prototype.timekeeping.Models.TimeData;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;

public class ScanActivity extends AppCompatActivity {

    Activity thisActivity = this;
    SurfaceView QRScanner;
    TextView TV_QRInfo, TV_Status;
    Button Btn_ForgotID;

    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;

    Realm realm;

    int TimeType = 0;

    EmployeeInfo employeeInfo;
    DTRData dtrData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Init();
        InitViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    void Init() {
        Intent i = getIntent();
        TimeType = i.getIntExtra(Constants.TIME_TYPE, 0);

        QRScanner = findViewById(R.id.Camera_QR);
        TV_QRInfo = findViewById(R.id.TV_QRInfo);
        TV_Status = findViewById(R.id.TV_Status);
        Btn_ForgotID = findViewById(R.id.Btn_ForgotID);

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .build();

        Realm.init(this);

        realm = Realm.getDefaultInstance();
    }

    void InitViews() {
        switch (TimeType) {
            case 1 :
                TV_Status.setText(R.string.timein);
                TV_Status.setTextColor(getResources().getColor(R.color.tabTextColorB));
                break;
            case 2 :
                TV_Status.setText(R.string.breakout);
                TV_Status.setTextColor(getResources().getColor(R.color.tabTextColorA));
                break;
            case 3 :
                TV_Status.setText(R.string.breakin);
                TV_Status.setTextColor(getResources().getColor(R.color.tabTextColorB));
                break;
            case 4 :
                TV_Status.setText(R.string.timeout);
                TV_Status.setTextColor(getResources().getColor(R.color.tabTextColorA));
                break;
        }

        Btn_ForgotID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new TKDialogs(thisActivity, thisActivity).ForgotID(TimeType);
                dialog.show();
            }
        });

        QRScanner.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(thisActivity, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    cameraSource.start(QRScanner.getHolder());
                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    TV_QRInfo.post(new Runnable() {
                        public void run() {
                            Calendar calendar = Calendar.getInstance();
                            String currentDate = Constants.DATE_FORMAT.format(new Date());
                            String currentTime = Constants.TIME_FORMAT.format(calendar.getTime());
                            /*if((TimeType == 4) && (GetHour(currentTime)) <= 6) {
                                currentDate = GetYesterday(currentDate);
                            }*/
                            employeeInfo = null;

                            try {
                                realm.beginTransaction();
                                employeeInfo = realm
                                        .where(EmployeeInfo.class)
                                        .equalTo("Barcode",
                                                barcodes.valueAt(0).displayValue)
                                        .findFirst();
                                if(employeeInfo != null) {
                                    StringBuilder builder = new StringBuilder()
                                            .append(employeeInfo.getBarcode())
                                            .append("\n")
                                            .append(employeeInfo.getFirstName().toUpperCase())
                                            .append(" ")
                                            .append(employeeInfo.getMiddleName().toUpperCase())
                                            .append(" ")
                                            .append(employeeInfo.getLastName().toUpperCase())
                                            .append("\n");
                                    switch (TimeType) {
                                        case 1 :
                                            builder.append("TIME IN");
                                            break;
                                        case 2 :
                                            builder.append("BREAK OUT");
                                            break;
                                        case 3 :
                                            builder.append("BREAK IN");
                                            break;
                                        case 4 :
                                            builder.append("TIME OUT");
                                            break;
                                    }
                                    builder
                                            .append(" : ")
                                            .append(currentDate)
                                            .append(" - ")
                                            .append(currentTime);
                                    TV_QRInfo.setText(builder.toString());
                                }

                            } catch (Exception ex) {
                                Log.e(Constants.LOG_TAG, ex.getMessage());
                            } finally {
                                realm.commitTransaction();
                            }

                            try {
                                String name = new StringBuilder()
                                        .append(employeeInfo.getFirstName().toUpperCase())
                                        .append(" ")
                                        .append(employeeInfo.getMiddleName().toUpperCase())
                                        .append(" ")
                                        .append(employeeInfo.getLastName().toUpperCase())
                                        .toString();
                                Intent i = new Intent(thisActivity, CameraActivity.class);
                                i.putExtra(Constants.TAG_BARCODE, employeeInfo.getBarcode());
                                i.putExtra(Constants.TAG_NAME, name);
                                i.putExtra(Constants.TAG_DATE, currentDate);
                                i.putExtra(Constants.TAG_TIME, currentTime);
                                i.putExtra(Constants.TAG_TYPE, TimeType + "");
                                startActivity(i);
                            } catch (Exception ex) {
                                Log.e(Constants.LOG_TAG, ex.getMessage());
                            }
                        }
                    });
                }
            }
        });
    }

    int GetHour(String time) {
        return Integer.parseInt(time.substring(0, 2));
    }

    String GetYesterday(String currentViewDate) {
        Calendar cal = Calendar.getInstance();
        int year = Integer.parseInt(currentViewDate.substring(0, 4));
        int month = Integer.parseInt(currentViewDate.substring(5, 7));
        int day = Integer.parseInt(currentViewDate.substring(8, 10));
        Log.e("==DT==", year + " - " + month + " - " + day);
        cal.set(year, month - 1, day);
        cal.add(Calendar.DATE, -1);
        return Constants.DATE_FORMAT.format(cal.getTime());
    }
}
