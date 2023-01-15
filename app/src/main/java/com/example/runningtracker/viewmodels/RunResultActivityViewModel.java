package com.example.runningtracker.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class RunResultActivityViewModel extends ViewModel {

    private MutableLiveData<ArrayList<String>> selectedTags;

    public MutableLiveData<ArrayList<String>> getTags() {
        if (selectedTags == null) {
            selectedTags = new MutableLiveData<>();
            selectedTags.postValue(new ArrayList<>());
        }
        return selectedTags;
    }

    public void setTags(ArrayList<String> selectedTags) {
        getTags().postValue(selectedTags);
    }
}
