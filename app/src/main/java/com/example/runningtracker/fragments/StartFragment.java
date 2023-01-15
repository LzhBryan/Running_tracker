package com.example.runningtracker.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.runningtracker.R;
import com.example.runningtracker.activities.RunResultActivity;
import com.example.runningtracker.databinding.FragmentStartBinding;
import com.example.runningtracker.services.TrackingService;
import com.example.runningtracker.viewmodels.StartFragmentViewModel;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class StartFragment extends Fragment {
    private static final int REQUEST_CHECK_SETTINGS = 101;
    public static final String TOTAL_DISTANCE = "TOTAL_DISTANCE";
    public static final String AVERAGE_PACE = "AVERAGE_PACE";
    public static final String TOTAL_TIME = "TOTAL_TIME";
    public static final int REQUEST_STOP_SERVICE = 1;
    private StartFragmentViewModel startFragmentViewModel;
    private ImageButton serviceButton;
    private ImageButton stopButton;
    private TextView trackingCounter;
    private TextView averagePace;
    private TextView totalDistance;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (startFragmentViewModel.getIsServiceRunning()) {
            if (startFragmentViewModel.isServiceOnPause()) {
                switchToPlayBtn();
            } else {
                switchToPauseBtn();
            }
        } else {
            switchToPlayBtn();
        }

        final Observer<Integer> trackingCounterObserver =
                lambda -> {
                    int trackSeconds =
                            startFragmentViewModel.getTrackingCounter().getValue();
                    int trackHour = trackSeconds / 3600;
                    int trackMinute = (trackSeconds - (3600 * trackHour)) / 60;
                    trackSeconds =
                            (trackSeconds - (3600 * trackHour) - (trackMinute * 60));
                    trackingCounter.setText(String.format("%02d:%02d:%02d",
                            trackHour, trackMinute, trackSeconds));
                };

        final Observer<Float> trackingPaceObserver =
                lambda -> averagePace.setText(String.format("%.2f minute/km",
                        startFragmentViewModel.getTrackingPace().getValue()));

        final Observer<Float> totalDistanceObserver =
                lambda -> totalDistance.setText(String.format("%.2f km",
                        startFragmentViewModel.getTotalDistance().getValue() / 1000));

        startFragmentViewModel.getTrackingCounter().observe(getViewLifecycleOwner(), trackingCounterObserver);
        startFragmentViewModel.getTrackingPace().observe(getViewLifecycleOwner(), trackingPaceObserver);
        startFragmentViewModel.getTotalDistance().observe(getViewLifecycleOwner(), totalDistanceObserver);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentStartBinding binding = FragmentStartBinding.inflate(inflater,
                container, false);
        startFragmentViewModel =
                new ViewModelProvider(requireActivity()).get(StartFragmentViewModel.class);
        binding.setFragment(this);
        View rootView = binding.getRoot();
        serviceButton = rootView.findViewById(R.id.service);
        stopButton = rootView.findViewById(R.id.stop);
        trackingCounter = rootView.findViewById(R.id.trackingCounter);
        averagePace = rootView.findViewById(R.id.averagePace);
        totalDistance = rootView.findViewById(R.id.totalDistance);
        requireActivity().bindService(new Intent(requireActivity(),
                        TrackingService.class), startFragmentViewModel.getServiceConnection(),
                Context.BIND_AUTO_CREATE);
        return rootView;
    }

    public void onClickRun() {
        if (!startFragmentViewModel.getIsServiceRunning()) {
            checkLocationPermission();
            if (startFragmentViewModel.isLocationPermissionGranted()) {
                createLocationRequest();
                if (startFragmentViewModel.isGpsOn()) {
                    requireActivity().startForegroundService(new Intent(requireActivity()
                            , TrackingService.class));
                    startFragmentViewModel.setServiceRunning(true);
                    startFragmentViewModel.setServiceOnPause(false);
                    switchToPauseBtn();
                }
            }
        } else {
            if (!startFragmentViewModel.isServiceOnPause()) {
                startFragmentViewModel.getMyService().pause();
                switchToPlayBtn();
                startFragmentViewModel.setServiceOnPause(true);
            } else {
                startFragmentViewModel.getMyService().play();
                switchToPauseBtn();
                startFragmentViewModel.setServiceOnPause(false);
            }
        }
    }

    public void onClickStop() {
        startFragmentViewModel.getMyService().pause();
        switchToPlayBtn();
        startFragmentViewModel.setServiceOnPause(true);
        requireActivity().unbindService(startFragmentViewModel.getServiceConnection());
        startFragmentViewModel.setMyService(null);
        Intent runResultIntent = new Intent(requireActivity(),
                RunResultActivity.class);
        runResultIntent.putExtra(TOTAL_DISTANCE,
                startFragmentViewModel.getTotalDistance().getValue());
        runResultIntent.putExtra(AVERAGE_PACE, startFragmentViewModel.getTrackingPace().getValue());
        runResultIntent.putExtra(TOTAL_TIME, startFragmentViewModel.getTrackingCounter().getValue());
        startActivityForResult(runResultIntent, REQUEST_STOP_SERVICE);
    }

    private void switchToPlayBtn() {
        serviceButton.setImageResource(R.drawable.btn_play);
        if (!startFragmentViewModel.getIsServiceRunning()) {
            stopButton.setEnabled(false);
            stopButton.setAlpha(.5f);
            stopButton.setClickable(false);
        }
    }

    private void switchToPauseBtn() {
        serviceButton.setImageResource(R.drawable.btn_pause);
        stopButton.setEnabled(true);
        stopButton.setAlpha(1f);
        stopButton.setClickable(true);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            startFragmentViewModel.setLocationPermissionGranted(true);
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            new MaterialAlertDialogBuilder(requireActivity(), R.style.RoundShapeTheme)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton("OK", (dialogInterface, i) ->
                            requestPermissionLauncher.launch(
                                    Manifest.permission.ACCESS_FINE_LOCATION))
                    .create()
                    .show();
        } else {
            requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startFragmentViewModel.setLocationPermissionGranted(true);
                } else {
                    new MaterialAlertDialogBuilder(requireActivity(),
                            R.style.RoundShapeTheme)
                            .setTitle("Location permission needed")
                            .setMessage("Can't track your running without " +
                                    "location access")
                            .setPositiveButton("OK", (dialogInterface, i) ->
                                    dialogInterface.cancel())
                            .create()
                            .show();
                    startFragmentViewModel.setLocationPermissionGranted(false);
                }
            });

    protected void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(requireActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(requireActivity(),
                locationSettingsResponse -> {
                    if (!startFragmentViewModel.isGpsOn()) {
                        requireActivity().startForegroundService(new Intent(requireActivity()
                                , TrackingService.class));
                        switchToPauseBtn();
                        startFragmentViewModel.setServiceRunning(true);
                        startFragmentViewModel.setServiceOnPause(false);
                        startFragmentViewModel.setGpsOn(true);
                    }
                });
        task.addOnFailureListener(requireActivity(), e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(requireActivity(),
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    sendEx.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                startFragmentViewModel.setGpsOn(true);
                requireActivity().startForegroundService(new Intent(requireActivity()
                        , TrackingService.class));
                switchToPauseBtn();
                startFragmentViewModel.setServiceRunning(true);
                startFragmentViewModel.setServiceOnPause(false);
            }
        } else if (requestCode == REQUEST_STOP_SERVICE) {
            if (resultCode == Activity.RESULT_OK) {
                boolean isServiceRunning =
                        data.getBooleanExtra(RunResultActivity.SERVICE_STATUS
                                , true);
                if (!isServiceRunning) {
                    startFragmentViewModel.setServiceOnPause(false);
                    startFragmentViewModel.setServiceRunning(false);
                    startFragmentViewModel.setTotalDistance(0);
                    startFragmentViewModel
                            .setTrackingCounter(0);
                    startFragmentViewModel.setTrackingPace(0);
                }
            }
        }
    }

    @Override
    public void onResume() {
        switchToPlayBtn();
        if (startFragmentViewModel.getMyService() == null) {
            requireActivity().bindService(new Intent(requireActivity(),
                            TrackingService.class), startFragmentViewModel.getServiceConnection(),
                    Context.BIND_AUTO_CREATE);
        }
        super.onResume();
    }

    @Override
    public void onStop() {
        if (startFragmentViewModel.getMyService() != null) {
            requireActivity().unbindService(startFragmentViewModel.getServiceConnection());
            startFragmentViewModel.setMyService(null);
        }
        super.onStop();
    }
}