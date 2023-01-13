package com.example.runningtracker.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

public class StartFragmentViewModel extends ViewModel {

    private SavedStateHandle savedState;
    private MutableLiveData<Boolean> isServiceRunning;
    private boolean isGpsOn;
    private boolean isLocationPermissionGranted;

    public StartFragmentViewModel(){
    }

    public MutableLiveData<Boolean> getIsServiceRunning() {
        if (isServiceRunning == null) {
            isServiceRunning = new MutableLiveData<>();
            isServiceRunning.setValue(false);
        }
        return isServiceRunning;
    }

    public void setServiceRunning(boolean isServiceRunning) {
        getIsServiceRunning().setValue(isServiceRunning);
    }

    public boolean isGpsOn() {
        return isGpsOn;
    }

    public void setGpsOn(boolean isGpsOn) {
        this.isGpsOn = isGpsOn;
    }

    public boolean isLocationPermissionGranted() {
        return isLocationPermissionGranted;
    }

    public void setLocationPermissionGranted(boolean locationPermissionGranted) {
        isLocationPermissionGranted = locationPermissionGranted;
    }
}

