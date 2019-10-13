package com.vibal.utilities.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vibal.utilities.R;
import com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity;
import com.vibal.utilities.ui.randomChooser.RandomChooserActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class UtilitiesOptionsFragment extends Fragment {

    @NonNull
    static UtilitiesOptionsFragment newInstance() {
        return new UtilitiesOptionsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_utility_options,container,false);
        ButterKnife.bind(this,view);
        return view;
    }

    @OnClick(R.id.randomChooser)
    void startRandomChooser() {
        startActivity(new Intent(getContext(), RandomChooserActivity.class));
    }

    @OnClick(R.id.cashBoxManager)
    void startCashBoxManager() {
        startActivity(new Intent(getContext(), CashBoxManagerActivity.class));
    }
}
