package com.example.runningtracker.viewmodels;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.runningtracker.interfaces.ICallback;
import com.example.runningtracker.services.TrackingService;

public class StartFragmentViewModel extends ViewModel {

    private boolean isServiceRunning;
    private boolean isGpsOn;
    private boolean isLocationPermissionGranted;
    private boolean isServiceOnPause;
    private TrackingService.MyBinder myService = null;
    private MutableLiveData<Integer> trackingCounter;
    private MutableLiveData<Float> trackingPace;
    private MutableLiveData<Float> totalDistance;

    public boolean getIsServiceRunning() {
        return isServiceRunning;
    }

    public void setServiceRunning(boolean isServiceRunning) {
        this.isServiceRunning = isServiceRunning;
    }

    public boolean isServiceOnPause() {
        return isServiceOnPause;
    }

    public void setServiceOnPause(boolean serviceOnPause) {
        isServiceOnPause = serviceOnPause;
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

    public MutableLiveData<Integer> getTrackingCounter() {
        if (trackingCounter == null) {
            trackingCounter = new MutableLiveData<>();
            trackingCounter.postValue(0);
        }
        return trackingCounter;
    }

    public void setTrackingCounter(int trackingCounter) {
        getTrackingCounter().postValue(trackingCounter);
    }

    public MutableLiveData<Float> getTrackingPace() {
        if (trackingPace == null) {
            trackingPace = new MutableLiveData<>();
            trackingPace.postValue((float) 0);
        }
        return trackingPace;
    }

    public void setTrackingPace(float trackingPace) {
        getTrackingPace().postValue(trackingPace);
    }

    public MutableLiveData<Float> getTotalDistance() {
        if (totalDistance == null) {
            totalDistance = new MutableLiveData<>();
            totalDistance.postValue((float) 0);
        }
        return totalDistance;
    }

    public void setTotalDistance(float totalDistance) {
        getTotalDistance().postValue(totalDistance);
    }

    public TrackingService.MyBinder getMyService() {
        return myService;
    }

    public void setMyService(TrackingService.MyBinder myService) {
        this.myService = myService;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myService = (TrackingService.MyBinder) iBinder;
            myService.registerCallback(callback);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myService.unRegisterCallback();
            myService = null;
        }
    };

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    ICallback callback = new ICallback() {
        @Override
        public void trackingTimeEvent(int trackingSeconds) {
            setTrackingCounter(trackingSeconds);
        }

        @Override
        public void trackingPaceEvent(float averagePace) {
            setTrackingPace(averagePace);
        }

        @Override
        public void trackingDistanceEvent(float totalDistance) {
            setTotalDistance(totalDistance);
        }

        @Override
        public void trackingServiceStatus(boolean isServiceRunning) {
            setServiceRunning(isServiceRunning);
        }
    };
}