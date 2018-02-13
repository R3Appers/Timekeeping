package com.rrreyes.prototype.timekeeping.Constants;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rrreyes.prototype.timekeeping.Adapters.DTRSyncTypeAdapter;
import com.rrreyes.prototype.timekeeping.Interfaces.TKService;
import com.rrreyes.prototype.timekeeping.Models.DTRSync;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by R. R. Reyes on 12/21/2017.
 */

public class RetrofitData {

    static final String BASE_URL = "http://18.144.14.140:3000";

    public static Retrofit newInstance = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build();

    public static TKService newInstance(Context context, String token) {

        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.readTimeout(60, TimeUnit.SECONDS);
        builder.connectTimeout(60, TimeUnit.SECONDS);

        final String key_token = token;

        builder.addInterceptor(new Interceptor() {
            @Override public Response intercept(Interceptor.Chain chain) throws IOException {
                Request request = chain.request()
                        .newBuilder()
                        .addHeader("token", key_token).build();
                return chain.proceed(request);
            }
        });

        OkHttpClient client = builder.build();

        /*Gson gson = new GsonBuilder()
                .registerTypeAdapter(DTRSync.class, new DTRSyncTypeAdapter())
                .setPrettyPrinting()
                .create();*/

        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

        return retrofit.create(TKService.class);
    }
}
