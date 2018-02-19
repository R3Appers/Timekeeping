package com.rrreyes.prototype.timekeeping;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    Handler handler;
    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Init();
    }

    void Init() {
        handler = new Handler();
        i = new Intent(this, MainActivity.class);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                startActivity(i);
            }
        };

        handler.postDelayed(runnable, 2000);
    }
}
