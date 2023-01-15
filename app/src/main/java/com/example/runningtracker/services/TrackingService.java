package com.example.runningtracker.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.runningtracker.R;
import com.example.runningtracker.activities.MainActivity;
import com.example.runningtracker.activities.RunResultActivity;
import com.example.runningtracker.interfaces.ICallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class TrackingService extends Service {

    private static final int ONGOING_NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "100";
    public static final String NOTIFICATION_ENTRY = "NOTIFICATION_ENTRY";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private NotificationManager notificationManager;
    private Location previousLocation;
    private final Object pauseLock = new Object();
    private TrackingThread trackingThread;
    private volatile boolean running;
    private volatile boolean pausing;
    private int trackingSeconds = 0;
    private float trackingPace = 0;
    private float distance = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // check if intent is being sent to stop service, otherwise start
        // service
        int stop = intent.getIntExtra(RunResultActivity.STOP_SERVICE, 0);
        if (stop == 1) {
            running = false;
            // call this method to update service status so that UI can update
            doCallbacks();
            // kill service thread
            trackingThread.interrupt();
            trackingThread = null;
            fusedLocationClient.removeLocationUpdates(locationCallback);
            stopForeground(true);
            stopSelf();
        } else {
            // start tracking and new thread
            trackLocation();
            trackingThread = new TrackingThread();
            running = true;
            trackingThread.start();
            Notification foregroundNotification =
                    buildForegroundNotification("Time elapsed: 00:00:00");
            startForeground(ONGOING_NOTIFICATION_ID, foregroundNotification);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(ONGOING_NOTIFICATION_ID);
    }

    private void pauseTracking() {
        pausing = true;
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void continueTracking() {
        synchronized (pauseLock) {
            pausing = false;
            pauseLock.notifyAll();
            trackLocation();
        }
    }

    public class MyBinder extends Binder implements IInterface {
        public void pause() {
            pauseTracking();
        }

        public void play() {
            continueTracking();
        }

        ICallback callback;

        public void registerCallback(ICallback callback) {
            this.callback = callback;
            remoteCallbackList.register(MyBinder.this);
        }

        public void unRegisterCallback() {
            remoteCallbackList.unregister(MyBinder.this);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }
    }

    RemoteCallbackList<MyBinder> remoteCallbackList = new RemoteCallbackList<>();

    public class TrackingThread extends Thread {

        public TrackingThread() {
            trackingSeconds = 0;
            trackingPace = 0;
            distance = 0;
        }

        @Override
        public void run() {
            // thread running and pausing
            while (running) {
                synchronized (pauseLock) {
                    if (!running) {
                        break;
                    }

                    if (pausing) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException ex) {
                            break;
                        }

                        if (!running) {
                            break;
                        }
                    }
                }
                doCallbacks();
                updateNotification();
                trackingSeconds += 1;
                System.out.println(trackingSeconds);
                trackingPace = ((float) trackingSeconds / 60) / (distance / 1000);
                // prevent infinity to be displayed when it was first divided
                // by 0
                if (trackingPace == Float.POSITIVE_INFINITY) {
                    trackingPace = 0;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doCallbacks() {
        final int n = remoteCallbackList.beginBroadcast();
        for (int i = 0; i < n; i++) {
            remoteCallbackList.getBroadcastItem(i).callback.trackingTimeEvent(trackingSeconds);
            remoteCallbackList.getBroadcastItem(i).callback.trackingPaceEvent(trackingPace);
            remoteCallbackList.getBroadcastItem(i).callback.trackingDistanceEvent(distance);
            remoteCallbackList.getBroadcastItem(i).callback.trackingServiceStatus(running);
        }
        remoteCallbackList.finishBroadcast();
    }

    private void createNotificationChannel() {
        CharSequence name = "Running Tracker";
        String description = "Running";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private Notification buildForegroundNotification(String elapsedTime) {
        // notification intent to go back activity
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra(NOTIFICATION_ENTRY, "startFragment");
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        // notification intent to stop service
        Intent stopnotificationIntent = new Intent(this, TrackingService.class);
        stopnotificationIntent.putExtra(RunResultActivity.STOP_SERVICE, 1);
        PendingIntent Intent = PendingIntent.getService(this, 0,
                stopnotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Running Tracker")
                .setContentText("Time elapsed: " + elapsedTime)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.btn_stop, "Stop running", Intent)
                .build();
    }

    // display up to date running elapsed time in foregound notification
    private void updateNotification() {
        int trackHour = trackingSeconds / 3600;
        int trackMinute = (trackingSeconds - (3600 * trackHour)) / 60;
        int trackSeconds = (trackingSeconds - (3600 * trackHour) - (trackMinute * 60));
        String elapsedTime = String.format("%02d:%02d:%02d", trackHour, trackMinute, trackSeconds);

        Notification notification = buildForegroundNotification(elapsedTime);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
    }

    // location tracking implementation
    public void trackLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        if (previousLocation != null) {
                            float distanceTravelled = location.distanceTo(previousLocation);
                            Log.d("comp3018", "distance travelled " + distanceTravelled);
                            distance += distanceTravelled;
                            System.out.println("Total distance" + distance);
                        }
                        previousLocation = location;
                    }
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
