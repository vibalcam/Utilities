package com.vibal.utilities.ui.bindingHolder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vibal.utilities.databinding.CashBoxManagerFragmentBinding;
import com.vibal.utilities.databinding.CashBoxOnlineManagerFragmentBinding;

public class CashBoxManagerFragmentBindingHolder {
    private final CoordinatorLayout root;
    public final FloatingActionButton fabCBMMain;
    public final FloatingActionButton fabCBMSingleAdd;
    public final FloatingActionButton fabCBMGroupAdd;
    public final FloatingActionButton fabCBMPeriodicAdd;
    public final View bgFabMenuCBM;
    @Nullable
    public final SwipeRefreshLayout refreshCBM;

    public CashBoxManagerFragmentBindingHolder(@NonNull CashBoxManagerFragmentBinding binding) {
        fabCBMMain = binding.fabCBMMain;
        fabCBMSingleAdd = binding.fabCBMSingleAdd;
        fabCBMGroupAdd = binding.fabCBMGroupAdd;
        fabCBMPeriodicAdd = binding.fabCBMPeriodicAdd;
        bgFabMenuCBM = binding.bgFabMenuCBM;
        root = binding.getRoot();
        refreshCBM = null;
    }

    public CashBoxManagerFragmentBindingHolder(@NonNull CashBoxOnlineManagerFragmentBinding binding) {
        refreshCBM = binding.refreshCBM;
        fabCBMMain = binding.fabCBMMain;
        fabCBMSingleAdd = binding.fabCBMSingleAdd;
        fabCBMGroupAdd = binding.fabCBMGroupAdd;
        fabCBMPeriodicAdd = binding.fabCBMPeriodicAdd;
        bgFabMenuCBM = binding.bgFabMenuCBM;
        root = binding.getRoot();
    }

    public CoordinatorLayout getRoot() {
        return root;
    }
}
