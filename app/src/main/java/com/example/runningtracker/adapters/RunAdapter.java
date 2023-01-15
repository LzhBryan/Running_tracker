package com.example.runningtracker.adapters;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
    private List<Run> runs;
    private Context context;
    private LayoutInflater layoutInflater;
    private RunViewModel runViewModel;

    public RunAdapter(Context context, RunViewModel runViewModel) {
        this.runViewModel = runViewModel;
        this.runs = new ArrayList<>();
        this.context = context;
        layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setRuns(List<Run> runs) {
        if (this.runs != null) {
            this.runs.clear();
            this.runs.addAll(runs);
            notifyDataSetChanged();
        } else {
            this.runs = runs;
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
        holder.bind(runs.get(position));
    }

    @Override
    public int getItemCount() {
        return runs.size();
    }

    class RunViewHolder extends RecyclerView.ViewHolder {
        private TextView duration;
        private TextView totalDistance;
        private TextView averagePace;
        private TextView additionalNotes;
        private TextView tags;
        private Button update;
        private Button delete;

        public RunViewHolder(View itemView) {
            super(itemView);
            duration = itemView.findViewById(R.id.duration);
            totalDistance = itemView.findViewById(R.id.totalDistanceText);
            averagePace = itemView.findViewById(R.id.averagePaceResultText);
            additionalNotes = itemView.findViewById(R.id.runNotes);
            tags = itemView.findViewById(R.id.runTags);
            update = itemView.findViewById(R.id.updateRecord);
            delete = itemView.findViewById(R.id.deleteRecord);
        }

        void bind(final Run run) {
            if (run != null) {
                int totalTime = run.getRunDuration();
                int totalHour = totalTime / 3600;
                int totalMinute = (totalTime - (3600 * totalHour)) / 60;
                int totalSeconds =
                        (totalTime - (3600 * totalHour) - (totalMinute * 60));

                duration.setText(String.format("Duration: %02d:%02d:%02d",
                        totalHour, totalMinute, totalSeconds));
                totalDistance.setText(String.format("Total distance: %.2f km",
                        run.getTotalDistance()));
                averagePace.setText(String.format("Average pace: %.2f min/km",
                        run.getAveragePace()));
                additionalNotes.setText(String.format("Notes: %s",
                        run.getAdditionalNote()));
                if (run.getTags() != null) {
                    tags.setText(String.format("Tags: %s", run.getTags().toString()));
                } else {
                    tags.setText("Tags: ");
                }
                update.setOnClickListener(v -> {
                    ArrayList<String> selectedItems = new ArrayList<>();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Modify notes");
                    final EditText input = new EditText(context);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.setText(run.getAdditionalNote());
                    builder.setView(input);
                    builder.setPositiveButton("Next", (dialog, which) -> {
                        String userInput = input.getText().toString();

                        AlertDialog.Builder innerBuilder =
                                new AlertDialog.Builder(context);
                        innerBuilder.setTitle("Modify tags");
                        innerBuilder.setMultiChoiceItems(RunResultActivity.tags.toArray(new CharSequence[RunResultActivity.tags.size()]),
                                null, (innerDialog, innerWhich, isChecked) -> {
                                    if (isChecked) {
                                        selectedItems.add(RunResultActivity.tags.get(innerWhich));
                                    } else if (selectedItems.contains(RunResultActivity.tags.get(innerWhich))) {
                                        selectedItems.remove(RunResultActivity.tags.get(innerWhich));
                                    }
                                });
                        innerBuilder.setPositiveButton("OK",
                                (innerDialog, innerWhich) -> runViewModel.update(run.getRunId(), userInput
                                        , selectedItems));
                        innerBuilder.setNegativeButton("Cancel", (dialog1, which1) -> {
                            if (!userInput.equals("") && !userInput.equals(run.getAdditionalNote())) {
                                runViewModel.update(run.getRunId(), userInput
                                        , run.getTags());
                            }
                        });
                        AlertDialog innerDialog = innerBuilder.create();
                        innerDialog.show();

                    });
                    builder.setNegativeButton("Cancel", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                });
                delete.setOnClickListener(v -> runViewModel.delete(run));
            }
        }
    }
}
