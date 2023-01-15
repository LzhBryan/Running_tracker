package com.example.runningtracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.runningtracker.R;
import com.example.runningtracker.fragments.StartFragment;
import com.example.runningtracker.models.Run;
import com.example.runningtracker.services.TrackingService;
import com.example.runningtracker.viewmodels.RunResultActivityViewModel;
import com.example.runningtracker.viewmodels.RunViewModel;

import java.util.ArrayList;
import java.util.Arrays;

public class RunResultActivity extends AppCompatActivity {

    public static final String SERVICE_STATUS = "SERVICE_STATUS";
    public static final String STOP_SERVICE = "STOP_SERVICE";
    public static final ArrayList<String> tags = new ArrayList<>(Arrays.asList(
            "sport", "workout", "motivation"
            , "marathon", "instarun", "fitness", "gym", "nike", "good " +
                    "weather", "bad weather", "muscle ache", "sore legs",
            "health", "healthy lifestyle"));
    private RunResultActivityViewModel runResultActivityViewModel;
    private RunViewModel runViewModel;
    private EditText additionalNotesInput;
    private int totalTime;
    private float totalDistance;
    private float averagePace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_result);

        Toolbar myToolbar = findViewById(R.id.runResultToolbar);
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        runResultActivityViewModel =
                new ViewModelProvider(this).get(RunResultActivityViewModel.class);
        runViewModel = new ViewModelProvider(this).get(RunViewModel.class);

        additionalNotesInput = findViewById(R.id.additionalNotesInput);
        TextView timeText = findViewById(R.id.timeResult);
        TextView distanceText = findViewById(R.id.distanceResult);
        TextView paceText = findViewById(R.id.paceResult);

        totalDistance =
                getIntent().getFloatExtra(StartFragment.TOTAL_DISTANCE, 0);
        averagePace =
                getIntent().getFloatExtra(StartFragment.AVERAGE_PACE, 0);
        totalTime = getIntent().getIntExtra(StartFragment.TOTAL_TIME, 0);

        int totalHour = totalTime / 3600;
        int totalMinute = (totalTime - (3600 * totalHour)) / 60;
        int totalSeconds =
                (totalTime - (3600 * totalHour) - (totalMinute * 60));

        timeText.setText(String.format("%02d:%02d:%02d",
                totalHour, totalMinute, totalSeconds));
        paceText.setText(String.format("%.2f min/km", averagePace));
        distanceText.setText(String.format("%.2f km", totalDistance / 1000));
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
        ArrayList<String> selectedItems = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tags");
        builder.setMultiChoiceItems(tags.toArray(new CharSequence[tags.size()]),
                null, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        selectedItems.add(tags.get(which));
                    } else if (selectedItems.contains(tags.get(which))) {
                        selectedItems.remove(tags.get(which));
                    }
                });
        builder.setPositiveButton("OK", (dialog, which) -> runResultActivityViewModel.setTags(selectedItems));
        builder.setNegativeButton("Cancel", null);
        AlertDialog tagDialog = builder.create();
        tagDialog.show();
    }

    public void onClickSaveRun(View view) {
        stopService();
        String userInput = additionalNotesInput.getText().toString();
        runViewModel.insert(new Run(totalTime, totalDistance, averagePace,
                userInput, runResultActivityViewModel.getTags().getValue()));
        Toast.makeText(this, "Successfully recorded this activity",
                Toast.LENGTH_SHORT).show();
    }
}