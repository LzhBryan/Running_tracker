package com.example.runningtracker.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

@Entity(tableName = "run_table")
public class Run {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "run_id")
    private int runId;

    @ColumnInfo(name = "run_duration")
    private int runDuration;

    @ColumnInfo(name = "total_distance")
    private float totalDistance;

    @ColumnInfo(name = "average_pace")
    private float averagePace;

    @ColumnInfo(name = "additional_note")
    private String additionalNote;

    @ColumnInfo(name = "tags")
    private ArrayList<String> tags;

    public Run(int runDuration, float totalDistance, float averagePace,
               String additionalNote, ArrayList<String> tags) {
        this.runDuration = runDuration;
        this.totalDistance = totalDistance;
        this.averagePace = averagePace;
        this.additionalNote = additionalNote;
        this.tags = tags;
    }

    public int getRunId() {
        return runId;
    }

    public void setRunId(int runId) {
        this.runId = runId;
    }

    public int getRunDuration() {
        return runDuration;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public float getAveragePace() {
        return averagePace;
    }

    public String getAdditionalNote() {
        return additionalNote;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    // convert array list to string and vice versa
    public static class TagsTypeConverter {
        @TypeConverter
        public ArrayList<String> fromString(String tags) {
            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
            return new Gson().fromJson(tags, listType);
        }

        @TypeConverter
        public String fromArrayList(ArrayList<String> tags) {
            Gson gson = new Gson();
            return gson.toJson(tags);
        }
    }
}
