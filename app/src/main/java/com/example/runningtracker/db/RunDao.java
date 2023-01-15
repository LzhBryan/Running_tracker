package com.example.runningtracker.db;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.runningtracker.models.Run;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface RunDao {
    @Query("SELECT * FROM run_table ORDER BY run_id ASC")
    LiveData<List<Run>> getAllOrdered();

    @Insert
    void insert(Run run);

    @Query("UPDATE run_table SET additional_note=:additionalNote, tags " +
            "=:newTags WHERE run_id=:id")
    void update(int id, String additionalNote, ArrayList<String> newTags);

    @Delete
    void delete(Run run);

    @Query("SELECT * FROM run_table")
    Cursor getAllRuns();
}
