package com.example.runningtracker.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class RunResultActivityViewModel extends ViewModel {

    private MutableLiveData<ArrayList<String>> tags;

    public MutableLiveData<ArrayList<String>> getTags() {
        if (tags == null) {
            tags = new MutableLiveData<>();
            ArrayList<String> startingTags = new ArrayList<>();
            startingTags.add("Great Weather");
            startingTags.add("Fresh air");
            startingTags.add("Muscle ache");
            tags.postValue(startingTags);
        }
        return tags;
    }

    public void setTags(String tags) {
    }
}
