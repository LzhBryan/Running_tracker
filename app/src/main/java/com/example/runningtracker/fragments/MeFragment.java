package com.example.runningtracker.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.library.baseAdapters.BR;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runningtracker.R;
import com.example.runningtracker.adapters.RunAdapter;
import com.example.runningtracker.databinding.FragmentMeBinding;
import com.example.runningtracker.viewmodels.RunViewModel;

import java.util.ArrayList;

public class MeFragment extends Fragment {
    private RunAdapter runAdapter;
    private RunViewModel runViewModel;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentMeBinding meBinding = FragmentMeBinding.inflate(inflater,
                container, false);
        View rootView = meBinding.getRoot();
        meBinding.setVariable(BR.meFragment, this);
        runViewModel = new ViewModelProvider(requireActivity()).get(RunViewModel.class);
        recyclerView = rootView.findViewById(R.id.runList);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        runViewModel.getAllRuns().observe(requireActivity(), runs -> runAdapter.setRuns(runs));
        if (runAdapter == null) {
            runAdapter = new RunAdapter(requireActivity(), runViewModel);
        }
        recyclerView.setAdapter(runAdapter);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}