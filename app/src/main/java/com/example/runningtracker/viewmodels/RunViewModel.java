package com.example.runningtracker.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.runningtracker.models.Run;
import com.example.runningtracker.repositories.RunRepository;

import java.util.ArrayList;
import java.util.List;

public class RunViewModel extends AndroidViewModel {

    private RunRepository repository;

    private final LiveData<List<Run>> allRuns;

    public RunViewModel(@NonNull Application application) {
        super(application);
        repository = new RunRepository(application);
        allRuns = repository.getAllRuns();
    }

    public LiveData<List<Run>> getAllRuns() {
        return allRuns;
    }

    public void insert(Run run) {
        repository.insert(run);
    }

    public void update(int id, String additionalNote, ArrayList<String> newTags) {
        repository.update(id, additionalNote, newTags);
    }

    public void delete(Run run) {
        repository.delete(run);
    }
}
