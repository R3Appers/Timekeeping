package com.rrreyes.prototype.timekeeping.Adapters;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.rrreyes.prototype.timekeeping.Constants.RetrofitData;
import com.rrreyes.prototype.timekeeping.Constants.SharedData;
import com.rrreyes.prototype.timekeeping.Interfaces.TKService;
import com.rrreyes.prototype.timekeeping.Models.BasicResponse;
import com.rrreyes.prototype.timekeeping.Models.DTRDataSyncV2;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by R. R. Reyes on 4/3/2018.
 */

public class AutoDTRSync {

    private static Queue<DTRDataSyncV2> dtrList = new LinkedList<>();
    private Activity activity;
    private Handler handler;
    private static AutoDTRSync thisInstance;
    private static boolean mStopHandler = false;
    private static boolean isSending = false;

    private TKService service;
    private SharedData sd;

    private Realm realm;

    private AutoDTRSync(Activity activity) {
        this.activity = activity;
        sd = new SharedData(activity);
        handler = new Handler();
        service = RetrofitData.newInstance(activity, sd.GetToken());

        if (realm != null) {
            realm.close();
            realm = null;
        }

        Realm.init(activity);

        realm = Realm.getDefaultInstance();
    }

    public static AutoDTRSync getInstance(Activity activity) {
        if(thisInstance == null) {
            thisInstance = new AutoDTRSync(activity);
            return thisInstance;
        } else {
            return thisInstance;
        }
    }

    public static void AddDataToSync(DTRDataSyncV2 data) {
        dtrList.add(data);
    }

    public void RunAutoSync() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(dtrList.size() != 0) {
                    if(haveNetworkConnection() && !isSending) {
                        SendDTR();
                    }
                }
                if (!mStopHandler) {
                    handler.postDelayed(this, 1000);
                }
            }
        };

        handler.post(runnable);
    }

    public static void ToggleSync() {
        mStopHandler = !mStopHandler;
    }

    private DTRDataSyncV2 GetHead() {
        return dtrList.peek();
    }

    private DTRDataSyncV2 RemoveHead() {
        return dtrList.remove();
    }

    private void SendDTR() {
        isSending = true;
        DTRDataSyncV2 item = GetHead();
        String ImageURL = UploadImage(item.getImageUrl(), item.getBarcode(), item.getDate());
        service.sendDTR(
                sd.GetCompanyID(),
                sd.GetUserID(),
                item.getDate(),
                item.getBarcode(),
                item.getTimeIn(),
                item.getTimeOut(),
                item.getLunchIn(),
                item.getLunchOut(),
                ImageURL)
                .enqueue(new Callback<BasicResponse>() {
                    @Override
                    public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                        isSending = false;
                        RemoveHead();
                    }

                    @Override
                    public void onFailure(Call<BasicResponse> call, Throwable t) {
                        isSending = false;
                    }
                });
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
                        .append(sd.GetUserID())
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
        }
        return imgUrl;
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) this.activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
