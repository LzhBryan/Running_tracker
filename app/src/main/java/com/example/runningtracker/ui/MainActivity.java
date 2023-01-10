package com.example.runningtracker.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.example.runningtracker.R;
import com.example.runningtracker.services.TrackingService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private TrackingService.MyBinder myService = null;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        replaceFragment(new StartFragment());
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.dashboard:
                    replaceFragment(new DashboardFragment());
                    break;
                case R.id.start:
                    replaceFragment(new StartFragment());
                    break;
                case R.id.target:
                    replaceFragment(new TargetFragment());
                    break;
                case R.id.me:
                    replaceFragment(new MeFragment());
                    break;
            }
            return true;
        });

        this.bindService(new Intent(MainActivity.this, TrackingService.class)
                , serviceConnection, Context.BIND_AUTO_CREATE);
        this.startService(new Intent(this, TrackingService.class));
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

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, null).setReorderingAllowed(true).commit();
    }
}