package com.example.runningtracker.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import com.example.runningtracker.interfaces.ICallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class TrackingService extends Service {

    private static final int ONGOING_NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "100";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private NotificationManager notificationManager;
    private Location firstLocation;
    private volatile boolean running;
    private TrackingThread trackingThread;
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
        int stop = intent.getIntExtra("stop", 0);
        if (stop == 1) {
            trackingThread.interrupt();
            running = false;
            trackingThread = null;
            fusedLocationClient.removeLocationUpdates(locationCallback);
            stopForeground(true);
            stopSelf();

        } else {
            trackingThread = new TrackingThread();
            running = true;
            trackingThread.start();
            Notification foregroundNotification = buildForegroundNotification();
            startForeground(ONGOING_NOTIFICATION_ID, foregroundNotification);
            trackLocation();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(ONGOING_NOTIFICATION_ID);
    }

    private void pauseTracking() {
        running = false;
        trackingThread.interrupt();
    }

    private void continueTracking() {
        running = true;
        trackingThread.start();
    }

    public class MyBinder extends Binder implements IInterface {
        public void pause() {
            pauseTracking();
        }

        public void play() {
            continueTracking();
        }

        public void stop() {
            stopTracking();
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
            distance  = 0;
        }

        @Override
        public void run() {
            while (running) {
                System.out.println(currentThread());
                doCallbacks();
                trackingSeconds += 1;
                System.out.println(trackingSeconds);
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

    private Notification buildForegroundNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("redirect", "startFragment");
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Running Tracker")
                .setContentText("Running")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
    }

    public void trackLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = new
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,
                1000).build();

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                firstLocation = location;
                System.out.println(firstLocation);
            }
        }).addOnFailureListener(location -> {

        });


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult
                                                 locationResult) {
                Location previousLocation = locationResult.getLastLocation();

                for (Location location : locationResult.getLocations()) {
                    float distance = location.distanceTo(firstLocation);
                    Log.d("comp3018", "location " + distance);
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        } catch (
                SecurityException e) {
            e.printStackTrace();
        }
    }
}
