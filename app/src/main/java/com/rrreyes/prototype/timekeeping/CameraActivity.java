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
import com.rrreyes.prototype.timekeeping.Constants.Constants;
import com.rrreyes.prototype.timekeeping.Models.DTRData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
                if(mType.equals("4")) {
                    String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Timekeeping/";
                    List<Bitmap> imgList = new ArrayList<>();
                    for(int i = 1; i < 5; i++) {
                        File image = new File(dir + mBarcode + "_" + mDate + "_" + i + ".png");
                        if(image.exists()) {
                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                            imgList.add(bitmap);
                        } else {
                            image = new File(dir + mBarcode + "_" + mDate + "_" + 1 + ".png");
                            if(image.exists()) {
                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                                if(bitmap != null) {
                                    imgList.add(CreateBlankImage(bitmap.getWidth(), bitmap.getHeight()));
                                } else {
                                    imgList.add(CreateBlankImage(248, 248));
                                }
                            } else {
                                image = new File(dir + mBarcode + "_" + mDate + "_" + 2 + ".png");
                                if(image.exists()) {
                                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                    Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                                    if(bitmap != null) {
                                        imgList.add(CreateBlankImage(bitmap.getWidth(), bitmap.getHeight()));
                                    } else {
                                        imgList.add(CreateBlankImage(248, 248));
                                    }
                                } else {
                                    image = new File(dir + mBarcode + "_" + mDate + "_" + 3 + ".png");
                                    if(image.exists()) {
                                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                                        if(bitmap != null) {
                                            imgList.add(CreateBlankImage(bitmap.getWidth(), bitmap.getHeight()));
                                        } else {
                                            imgList.add(CreateBlankImage(248, 248));
                                        }
                                    } else {
                                        image = new File(dir + mBarcode + "_" + mDate + "_" + 4 + ".png");
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
                    ProcessImage(imgList);
                } else if(mType.equals("1")) {
                    String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Timekeeping/";
                    List<Bitmap> imgList = new ArrayList<>();
                    for(int i = 1; i < 5; i++) {
                        File image = new File(dir + mBarcode + "_" + mDate + "_" + i + ".png");
                        if(image.exists()) {
                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                            imgList.add(bitmap);
                        } else {
                            image = new File(dir + mBarcode + "_" + mDate + "_" + 1 + ".png");
                            if(image.exists()) {
                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                                if(bitmap != null) {
                                    imgList.add(CreateBlankImage(bitmap.getWidth(), bitmap.getHeight()));
                                } else {
                                    imgList.add(CreateBlankImage(248, 248));
                                }
                            } else {
                                image = new File(dir + mBarcode + "_" + mDate + "_" + 2 + ".png");
                                if(image.exists()) {
                                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                    Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                                    if(bitmap != null) {
                                        imgList.add(CreateBlankImage(bitmap.getWidth(), bitmap.getHeight()));
                                    } else {
                                        imgList.add(CreateBlankImage(248, 248));
                                    }
                                } else {
                                    image = new File(dir + mBarcode + "_" + mDate + "_" + 3 + ".png");
                                    if(image.exists()) {
                                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                                        if(bitmap != null) {
                                            imgList.add(CreateBlankImage(bitmap.getWidth(), bitmap.getHeight()));
                                        } else {
                                            imgList.add(CreateBlankImage(248, 248));
                                        }
                                    } else {
                                        image = new File(dir + mBarcode + "_" + mDate + "_" + 4 + ".png");
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
                    ProcessImage(imgList);
                }
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

    private Bitmap CreateBlankImage(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint);
        canvas.drawBitmap(bitmap, width, height, paint);
        return bitmap;
    }

    private void ProcessImage(List<Bitmap> bitmaps) {
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
}
