package com.rrreyes.prototype.timekeeping;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.rrreyes.prototype.timekeeping.Adapters.AutoDTRSync;
import com.rrreyes.prototype.timekeeping.Constants.Constants;
import com.rrreyes.prototype.timekeeping.Models.DTRData;
import com.rrreyes.prototype.timekeeping.Models.DTRDataSyncV2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import io.realm.Realm;

public class CameraActivity extends AppCompatActivity {

    int TAKE_PHOTO_CODE = 0;
    String mBarcode;
    String mDate;
    String mTime;
    String mName;
    String mType;

    String filepath;

    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    void Init() {
        Intent i = getIntent();
        mBarcode = i.getStringExtra(Constants.TAG_BARCODE);
        mDate = i.getStringExtra(Constants.TAG_DATE);
        mTime = i.getStringExtra(Constants.TAG_TIME);
        mName = i.getStringExtra(Constants.TAG_NAME);
        mType = i.getStringExtra(Constants.TAG_TYPE);

        Realm.init(this);

        realm = Realm.getDefaultInstance();

        final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Timekeeping/";
        File newdir = new File(dir);
        newdir.mkdirs();

        Button Btn_Camera = findViewById(R.id.Btn_Camera);
        Btn_Camera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                filepath = dir + mBarcode + "_" + mDate + "_" + mType + ".png";
                if(mType.equals("4") && GetHour(mTime) <= 6) {
                    filepath = dir + mBarcode + "_" + GetYesterday(mDate) + "_" + mType + ".png";
                }
                File newfile = new File(filepath);
                try {
                    newfile.createNewFile();
                }
                catch (IOException e) {
                    Log.e("CameraError", e.getMessage());
                }

                Uri outputFileUri;
                if(Build.VERSION.SDK_INT >= 24) {
                    outputFileUri = FileProvider.getUriForFile(
                            getApplicationContext(),
                            getApplicationContext().getPackageName() + ".com.rrreyes.prototype.timekeeping",
                            newfile);
                } else {
                    outputFileUri = Uri.fromFile(newfile);
                }

                if(outputFileUri != null) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                    cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                    startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        MediaScannerConnection.scanFile(
                getApplicationContext(),
                new String[]{filepath},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("==CAM==", "File Created and Scanned.");
                    }
                });

        if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            Log.d("==Camera==", "Picture Saved");
            File img = new File(filepath);
            try {
                InitImageProcess(mType);
            } catch (Exception ex) {
                ex.printStackTrace();
                //Toast.makeText(this, Constants.CAMERA_TRY, Toast.LENGTH_LONG).show();
            }
            SaveToRealm();
            Toast.makeText(this, Constants.SUCCESS_LOG, Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
    }

    private void InitImageProcess(String processType) {
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Timekeeping/";
        List<Bitmap> imgList = new ArrayList<>();
        boolean[] ImgPos = {false, false, false, false};
        boolean CanContinue = false;
        for(int i = 1; i < 5; i++) {
            File image = new File(dir + mBarcode + "_" + GetYesterday(mDate) + "_" + i + ".png");
            if(processType.equals("4")) {
                image = new File(dir + mBarcode + "_" + mDate + "_" + i + ".png");
            }
            if(image.exists()) {
                ImgPos[i - 1] = true;
            }
        }
        int ImgCtr = 0;
        int ImgPosition = 0;
        for(int j = 0; 0 < ImgPos.length; j++) {
            if(ImgPos[j]) {
                ImgCtr++;
                ImgPosition = j + 1;
            }
        }
        if(ImgCtr > 0) {
            CanContinue = true;
        }
        if(CanContinue) {
            for (int i = 1; i < 5; i++) {
                File image = new File(dir +
                        mBarcode + "_" +
                        GetYesterday(mDate) + "_" +
                        i + ".png");
                File imageBackup = new File(dir +
                        mBarcode + "_" +
                        GetYesterday(mDate) + "_" +
                        ImgPosition + ".png");
                if(processType.equals("4")) {
                    image = new File(dir +
                            mBarcode + "_" +
                            mDate + "_" +
                            i + ".png");
                    imageBackup = new File(dir +
                            mBarcode + "_" +
                            mDate + "_" +
                            ImgPosition + ".png");
                }

                BitmapFactory.Options bmOptions;
                Bitmap bitmap;
                if (image.exists()) {
                    bmOptions = new BitmapFactory.Options();
                    bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                    imgList.add(bitmap);
                } else if (imageBackup.exists()) {
                    bmOptions = new BitmapFactory.Options();
                    bitmap = BitmapFactory.decodeFile(imageBackup.getAbsolutePath(), bmOptions);
                    imgList.add(CreateBlankImage(bitmap.getWidth(), bitmap.getHeight()));
                }
            }
            ProcessImage(imgList, processType);
            if(processType.equals("1")) {
                AddToAutoSync(mBarcode, GetYesterday(mDate));
            }
        }
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

    private void ProcessImage(List<Bitmap> bitmaps, String processType) {
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
        String outFile = dir + mBarcode + "_" + GetYesterday(mDate) + ".jpg";
        if(processType.equals("4")) {
            outFile = dir + mBarcode + "_" + mDate + ".jpg";
        }
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
                MediaScannerConnection.scanFile(
                        getApplicationContext(),
                        new String[]{imgtemp.getAbsolutePath()},
                        null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("==CAM==", "File Scanned.");
                            }
                        });
            }
        }
    }

    private void AddToAutoSync(String mBarcode, String mDate) {
        List<DTRData> tempDatas =
                realm.where(DTRData.class)
                        .beginGroup()
                        .equalTo("Barcode", mBarcode)
                        .and()
                        .equalTo("Date", mDate)
                        .endGroup()
                        .findAll();
        AutoDTRSync.AddDataToSync(ProcessDTR(tempDatas, mBarcode, mDate));
    }

    private DTRDataSyncV2 ProcessDTR(List<DTRData> tempDatas, String mBarcode, String mDate) {
        final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Timekeeping/";
        DTRDataSyncV2 tSync = new DTRDataSyncV2();
        for(int a = 0; a < tempDatas.size(); a++) {
            DTRData data = tempDatas.get(a);
            tSync.setDate(data.getDate());
            tSync.setBarcode(data.getBarcode());
            tSync = GetTimeByType(tSync, data);
        }
        String imgPath = new StringBuilder(dir)
                .append(mBarcode)
                .append("_")
                .append(mDate)
                .append(".jpg")
                .toString();
        tSync.setImageUrl(imgPath);
        return tSync;
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

    private void SaveToRealm() {
        try {
            realm.beginTransaction();

            DTRData dtrData = new DTRData();
            dtrData.setBarcode(mBarcode);
            dtrData.setName(mName);
            dtrData.setDate(mDate);
            dtrData.setTime(mTime);
            dtrData.setType(mType);
            //dtrData.setImage(imageData);

            realm.copyToRealm(dtrData);

            realm.commitTransaction();
        } catch (Exception ex) {
            Log.e("CameraDB", ex.getMessage());
        }
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
