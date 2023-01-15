package com.example.runningtracker.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.runningtracker.models.Run;
import com.example.runningtracker.db.RunDao;
import com.example.runningtracker.db.RunDatabase;

import java.util.ArrayList;
import java.util.List;

public class RunRepository {
    private RunDao runDao;
    private LiveData<List<Run>> allRuns;

    public RunRepository(Application application) {
        RunDatabase runDatabase = RunDatabase.getDatabase(application);
        runDao = runDatabase.runDao();
        allRuns = runDao.getAllOrdered();
    }

    public void insert(Run run) {
        RunDatabase.databaseWriteExecutor.execute(() -> runDao.insert(run));
    }

    public void update(int id, String additionalNote, ArrayList<String> newTags) {
        RunDatabase.databaseWriteExecutor.execute(() -> runDao.update(id, additionalNote, newTags));
    }

    public void delete(Run run) {
        RunDatabase.databaseWriteExecutor.execute(() -> runDao.delete(run));
    }

    public LiveData<List<Run>> getAllRuns() {
        return allRuns;
    }
}
