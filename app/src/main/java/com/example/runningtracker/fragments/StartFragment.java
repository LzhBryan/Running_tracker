package com.example.runningtracker.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.runningtracker.R;
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
    private TrackingService.MyBinder myService = null;
    private StartFragmentViewModel startFragmentViewModel;
    private ImageButton serviceButton;
    private ImageButton stopButton;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startFragmentViewModel =
                new ViewModelProvider(requireActivity()).get(StartFragmentViewModel.class);

        final Observer<Boolean> isServiceRunningObserver = Boolean -> {
            if (startFragmentViewModel.getIsServiceRunning().getValue()) {
                serviceButton.setImageResource(R.drawable.btn_pause);
                stopButton.setEnabled(true);
                stopButton.setAlpha(1f);
                stopButton.setClickable(true);
            } else {
                serviceButton.setImageResource(R.drawable.btn_play);
                stopButton.setEnabled(false);
                stopButton.setAlpha(.5f);
                stopButton.setClickable(false);
            }
        };

        startFragmentViewModel.getIsServiceRunning().observe(getViewLifecycleOwner(),
                isServiceRunningObserver);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentStartBinding binding = FragmentStartBinding.inflate(inflater,
                container, false);
        startFragmentViewModel =
                new ViewModelProvider(this).get(StartFragmentViewModel.class);
        binding.setFragment(this);
        View rootView = binding.getRoot();
        serviceButton = rootView.findViewById(R.id.service);
        stopButton = rootView.findViewById(R.id.stop);
        requireActivity().bindService(new Intent(requireActivity(),
                TrackingService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
        return rootView;
    }

    public void onClickRun() {
        if (!startFragmentViewModel.getIsServiceRunning().getValue()) {
            checkLocationPermission();
            if (startFragmentViewModel.isLocationPermissionGranted()) {
                createLocationRequest();
                if (startFragmentViewModel.isGpsOn()) {
                    requireActivity().startForegroundService(new Intent(requireActivity()
                            , TrackingService.class));
                    startFragmentViewModel.setServiceRunning(true);
                }
            }
        } else {
            // binder
        }
    }

    public void onClickStop() {
        requireActivity().startService(new Intent(requireActivity()
                , TrackingService.class).setAction("stop"));
        startFragmentViewModel.setServiceRunning(false);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myService = (TrackingService.MyBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myService = null;
        }
    };

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
                    startFragmentViewModel.setGpsOn(true);
                    requireActivity().startForegroundService(new Intent(requireActivity()
                            , TrackingService.class));
                    startFragmentViewModel.setServiceRunning(true);
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
                startFragmentViewModel.setServiceRunning(true);
            }
        }
    }
}


