package com.example.runningtracker.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.runningtracker.R;
import com.example.runningtracker.fragments.MeFragment;
import com.example.runningtracker.fragments.StartFragment;
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

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, null)
                .setReorderingAllowed(true).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment =
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        fragment.onActivityResult(requestCode, resultCode, data);
    }
}