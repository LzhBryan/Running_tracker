package com.example.runningtracker.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.runningtracker.R;
import com.example.runningtracker.fragments.MeFragment;
import com.example.runningtracker.fragments.StartFragment;
import com.example.runningtracker.services.TrackingService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        String notificationEntry =
                getIntent().getStringExtra(TrackingService.NOTIFICATION_ENTRY);

        if (notificationEntry != null) {
            if (notificationEntry.equals("startFragment")) {
                switchFragment(new StartFragment());
                bottomNavigationView.setSelectedItemId(R.id.start);
            }
        } else {
            switchFragment(new StartFragment());
            bottomNavigationView.setSelectedItemId(R.id.start);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.start:
                    switchFragment(new StartFragment());
                    break;
                case R.id.me:
                    switchFragment(new MeFragment());
                    break;
            }
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    private void switchFragment(Fragment fragment) {
        String meFragmentClassName = "com.example.runningtracker" +
                ".fragments.MeFragment";
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment meFragment =
                fragmentManager.findFragmentByTag(meFragmentClassName);

        if (meFragment != null && fragment.getClass().getName().equals(meFragmentClassName)) {
            fragmentManager.beginTransaction().replace(R.id.fragment_container, meFragment, meFragmentClassName).commit();
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment, fragment.getClass().getName())
                    .setReorderingAllowed(true)
                    .addToBackStack(fragment.getClass().getName())
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment =
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}