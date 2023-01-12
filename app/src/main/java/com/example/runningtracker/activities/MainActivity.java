package com.example.runningtracker.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.runningtracker.R;
import com.example.runningtracker.fragments.DashboardFragment;
import com.example.runningtracker.fragments.MeFragment;
import com.example.runningtracker.fragments.StartFragment;
import com.example.runningtracker.fragments.TargetFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        String redirect = getIntent().getStringExtra("redirect");

        if (redirect != null) {
            if (redirect.equals("startFragment")) {
                switchFragment(new StartFragment());
                bottomNavigationView.setSelectedItemId(R.id.start);
            }
        } else {
            switchFragment(new StartFragment());
            bottomNavigationView.setSelectedItemId(R.id.start);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.dashboard:
                    switchFragment(new DashboardFragment());
                    break;
                case R.id.start:
                    switchFragment(new StartFragment());
                    break;
                case R.id.target:
                    switchFragment(new TargetFragment());
                    break;
                case R.id.me:
                    switchFragment(new MeFragment());
                    break;
            }
            return true;
        });

    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, null)
                .setReorderingAllowed(true).commit();
    }

}