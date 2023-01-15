package com.example.runningtracker.adapters;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runningtracker.R;
import com.example.runningtracker.activities.RunResultActivity;
import com.example.runningtracker.models.Run;
import com.example.runningtracker.viewmodels.RunViewModel;

import java.util.ArrayList;
import java.util.List;

public class RunAdapter extends RecyclerView.Adapter<RunAdapter.RunViewHolder> {
    private List<Run> allRunningRecords;
    private final Context context;
    private final LayoutInflater layoutInflater;
    private final RunViewModel runViewModel;

    public RunAdapter(Context context, RunViewModel runViewModel) {
        this.runViewModel = runViewModel;
        this.allRunningRecords = new ArrayList<>();
        this.context = context;
        layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setAllRunningRecords(List<Run> allRunningRecords) {
        if (this.allRunningRecords != null) {
            this.allRunningRecords.clear();
            this.allRunningRecords.addAll(allRunningRecords);
            notifyDataSetChanged();
        } else {
            this.allRunningRecords = allRunningRecords;
        }
    }

    @NonNull
    @Override
    public RunViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.run_item, parent,
                false);
        return new RunViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RunViewHolder holder, int position) {
        holder.bind(allRunningRecords.get(position));
    }

    @Override
    public int getItemCount() {
        return allRunningRecords.size();
    }

    class RunViewHolder extends RecyclerView.ViewHolder {
        private TextView duration;
        private TextView totalDistance;
        private TextView averagePace;
        private TextView additionalNotes;
        private TextView tags;
        private Button updateRecordBtn;
        private Button deleteRecordBtn;

        public RunViewHolder(View itemView) {
            super(itemView);
            duration = itemView.findViewById(R.id.duration);
            totalDistance = itemView.findViewById(R.id.totalDistanceText);
            averagePace = itemView.findViewById(R.id.averagePaceResultText);
            additionalNotes = itemView.findViewById(R.id.runNotes);
            tags = itemView.findViewById(R.id.runTags);
            updateRecordBtn = itemView.findViewById(R.id.updateRecord);
            deleteRecordBtn = itemView.findViewById(R.id.deleteRecord);
        }

        void bind(final Run runRecord) {
            if (runRecord != null) {
                int totalTime = runRecord.getRunDuration();
                int totalHour = totalTime / 3600;
                int totalMinute = (totalTime - (3600 * totalHour)) / 60;
                int totalSeconds =
                        (totalTime - (3600 * totalHour) - (totalMinute * 60));

                duration.setText(String.format("Duration: %02d:%02d:%02d",
                        totalHour, totalMinute, totalSeconds));
                totalDistance.setText(String.format("Total distance: %.2f km",
                        runRecord.getTotalDistance()));
                averagePace.setText(String.format("Average pace: %.2f min/km",
                        runRecord.getAveragePace()));
                additionalNotes.setText(String.format("Notes: %s",
                        runRecord.getAdditionalNote()));

                if (runRecord.getTags() != null) {
                    tags.setText(String.format("Tags: %s", runRecord.getTags().toString()));
                } else {
                    tags.setText("Tags: ");
                }
                updateRecordBtn.setOnClickListener(v -> {
                    ArrayList<String> selectedTags = new ArrayList<>();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Modify notes");
                    final EditText notesInput = new EditText(context);
                    notesInput.setInputType(InputType.TYPE_CLASS_TEXT);
                    notesInput.setText(runRecord.getAdditionalNote());
                    builder.setView(notesInput);
                    builder.setPositiveButton("Next", (dialog, which) -> {
                        String notes = notesInput.getText().toString();

                        AlertDialog.Builder innerBuilder =
                                new AlertDialog.Builder(context);
                        innerBuilder.setTitle("Modify tags");
                        innerBuilder.setMultiChoiceItems(RunResultActivity.tags.toArray(new CharSequence[RunResultActivity.tags.size()]),
                                null, (innerDialog, innerWhich, isChecked) -> {
                                    if (isChecked) {
                                        selectedTags.add(RunResultActivity.tags.get(innerWhich));
                                    } else if (selectedTags.contains(RunResultActivity.tags.get(innerWhich))) {
                                        selectedTags.remove(RunResultActivity.tags.get(innerWhich));
                                    }
                                });
                        innerBuilder.setPositiveButton("Update",
                                (innerDialog, innerWhich) -> runViewModel.update(runRecord.getRunId(), notes
                                        , selectedTags));
                        innerBuilder.setNegativeButton("Cancel", (dialog1, which1) -> {
                            // only update notes if user make changes to notes
                            if (!notes.equals("") && !notes.equals(runRecord.getAdditionalNote())) {
                                runViewModel.update(runRecord.getRunId(), notes
                                        , runRecord.getTags());
                                Toast.makeText(context, "Successfully updated" +
                                                " this run record",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        AlertDialog tagDialog = innerBuilder.create();
                        tagDialog.show();
                    });
                    builder.setNegativeButton("Cancel", null);
                    AlertDialog notesDialog = builder.create();
                    notesDialog.show();
                });

                deleteRecordBtn.setOnClickListener(v -> {
                    runViewModel.delete(runRecord);
                    Toast.makeText(context, "Successfully deleted this run record",
                            Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
}
