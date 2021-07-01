package com.vibal.utilities.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vibal.utilities.databinding.MainUtilityOptionsBinding;
import com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity;
import com.vibal.utilities.ui.randomChooser.RandomChooserActivity;

public class UtilitiesOptionsFragment extends Fragment {
    private MainUtilityOptionsBinding binding;

    @NonNull
    static UtilitiesOptionsFragment newInstance() {
        return new UtilitiesOptionsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = MainUtilityOptionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.randomChooser.setOnClickListener(v ->
                startActivity(new Intent(getContext(), RandomChooserActivity.class)));
        binding.cashBoxManager.setOnClickListener(v ->
                startActivity(new Intent(getContext(), CashBoxManagerActivity.class)));
    }
}
