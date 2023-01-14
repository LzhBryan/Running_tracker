package com.example.runningtracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.runningtracker.R;
import com.example.runningtracker.fragments.StartFragment;
import com.example.runningtracker.services.TrackingService;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

public class RunResultActivity extends AppCompatActivity {

    public static final String SERVICE_STATUS = "SERVICE_STATUS";
    public static final String STOP_SERVICE = "STOP_SERVICE";
    private int customTagColor;
    private String customTagName;
    private List<String> tags = new ArrayList<>();

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

        float totalDistance =
                getIntent().getFloatExtra(StartFragment.TOTAL_DISTANCE, 0);
        float averagePace =
                getIntent().getFloatExtra(StartFragment.AVERAGE_PACE, 0);
        int totalTime = getIntent().getIntExtra(StartFragment.TOTAL_TIME, 0);

        TextView timeText = findViewById(R.id.timeResult);
        TextView distanceText = findViewById(R.id.distanceResult);
        TextView paceText = findViewById(R.id.paceResult);

        int totalHour = totalTime / 3600;
        int totalMinute = (totalTime - (3600 * totalHour)) / 60;
        int totalSeconds =
                (totalTime - (3600 * totalHour) - (totalMinute * 60));

        timeText.setText(String.format("%02d:%02d:%02d",
                totalHour, totalMinute, totalSeconds));
        paceText.setText(String.format("%.2f minute/km", averagePace));
        distanceText.setText(String.format("%.2f km",
                totalDistance / 1000));
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
        stopService();
    }

    public void onClickTagRun(View view) {

        ArrayList<String> selectedItems = new ArrayList();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tags");
        tags.add("Great weather");
        tags.add("Fresh Air");
        builder.setMultiChoiceItems(tags.toArray(new CharSequence[tags.size()]),
                null,
                (dialog, which, isChecked) -> {
        });
        builder.setNeutralButton("Add tags",
                (dialogInterface, i) -> {
                    AlertDialog.Builder builderInner =
                            new AlertDialog.Builder(RunResultActivity.this);
                    final EditText input = new EditText(RunResultActivity.this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    input.setLayoutParams(lp);
                    builderInner.setView(input);
                    builderInner.setPositiveButton("Add",
                            (dialog, which) -> {
                                tags.add(input.getText().toString());
                            });
                    builderInner.setNegativeButton("Cancel", (dialog, i1) -> dialog.dismiss());
                    builderInner.show();
                });

        builder.setPositiveButton("OK", (dialog, which) -> {
            // user clicked OK
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onClickSaveRun(View view) {
        stopService();
        // save into database
    }
}