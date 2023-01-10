package com.example.runningtracker.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.example.runningtracker.R;
import com.example.runningtracker.services.TrackingService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RunningActivity extends AppCompatActivity {

    private TrackingService.MyBinder myService = null;
    private BottomNavigationView bottomNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationBar = findViewById(R.id.bottom_navigation);

        this.bindService(new Intent(RunningActivity.this, TrackingService.class)
                , serviceConnection, Context.BIND_AUTO_CREATE);
        this.startService(new Intent(this, TrackingService.class));

        bottomNavigationBar.setOnItemSelectedListener(item -> {

            return true;
        });
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myService = (TrackingService.MyBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myService = null;
        }
    };
}