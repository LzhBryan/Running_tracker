package com.example.runningtracker.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
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
    private ImageButton playButton;
    private ImageButton stopButton;
    private StartFragmentViewModel startFragmentViewModel;
    private boolean isLocationPermissionGranted;
    private boolean isGpsOn;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StartFragment newInstance(String param1, String param2) {
        StartFragment fragment = new StartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startFragmentViewModel =
                new ViewModelProvider(requireActivity()).get(StartFragmentViewModel.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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

        playButton = rootView.findViewById(R.id.play);
        stopButton = rootView.findViewById(R.id.stop);

        return rootView;
    }

    public void onClickRun() {
        checkLocationPermission();
        if (isLocationPermissionGranted) {
            createLocationRequest();
            if (isGpsOn) {
                requireActivity().startForegroundService(new Intent(requireActivity()
                        , TrackingService.class));
            }
        }
    }

    public void onClickStop() {
        requireActivity().stopService(new Intent(requireActivity()
                , TrackingService.class));
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
            isLocationPermissionGranted = true;
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
                    isLocationPermissionGranted = true;
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
                    isLocationPermissionGranted = false;
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
                    isGpsOn = true;
                    requireActivity().startForegroundService(new Intent(requireActivity()
                            , TrackingService.class));
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
                isGpsOn = true;
                requireActivity().startForegroundService(new Intent(requireActivity()
                        , TrackingService.class));
            }
        }
    }
}


