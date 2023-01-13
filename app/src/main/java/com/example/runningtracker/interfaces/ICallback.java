package com.example.runningtracker.interfaces;

public interface ICallback {
    void trackingTimeEvent(int trackingSeconds);

    void trackingPaceEvent(float averagePace);

    void trackingDistanceEvent(float totalDistance);
}
