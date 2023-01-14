package com.example.runningtracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.runningtracker.R;
import com.example.runningtracker.fragments.StartFragment;
import com.example.runningtracker.services.TrackingService;
import com.example.runningtracker.viewmodels.StartFragmentViewModel;

public class RunResultActivity extends AppCompatActivity {

    public static final String SERVICE_STATUS = "SERVICE_STATUS";
    private StartFragmentViewModel startFragmentViewModel;
    public static final String STOP_SERVICE = "STOP_SERVICE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_result);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.runResultToolbar);
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        startFragmentViewModel =
                new ViewModelProvider(this).get(StartFragmentViewModel.class);

        float totalDistance =
                getIntent().getFloatExtra(StartFragment.TOTAL_DISTANCE, 0);
        float averagePace =
                getIntent().getFloatExtra(StartFragment.AVERAGE_PACE, 0);
        int totalTime = getIntent().getIntExtra(StartFragment.TOTAL_TIME, 0);
    }

    public void stopService() {
        Intent stop = new Intent(this, TrackingService.class);
        stop.putExtra(STOP_SERVICE, 1);
        this.startService(stop);

        Intent result = new Intent(this, StartFragment.class);
        result.putExtra(SERVICE_STATUS, false);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onClickDiscardRun(View view) {
        System.out.println("Discard");
        stopService();
    }
}