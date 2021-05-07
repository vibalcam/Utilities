package com.vibal.utilities.ui.bindingHolder;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vibal.utilities.databinding.CashBoxItemFragmentBinding;
import com.vibal.utilities.databinding.CashBoxOnlineItemFragmentBinding;

public class CashBoxItemFragmentBindingHolder {
    private final CoordinatorLayout root;
    public final FloatingActionButton fabCBItem;
    public final FloatingActionButton balancesCB;
    public final RecyclerView rvCashBoxItem;
    public final TextView itemCash;
    @Nullable
    public final SwipeRefreshLayout refreshCBItem;

    public CashBoxItemFragmentBindingHolder(@NonNull CashBoxItemFragmentBinding binding) {
        fabCBItem = binding.fabCBItem;
        rvCashBoxItem = binding.rvCashBoxItem;
        itemCash = binding.itemCash;
        balancesCB = binding.balancesCB;
        root = binding.getRoot();
        refreshCBItem = null;
    }

    public CashBoxItemFragmentBindingHolder(@NonNull CashBoxOnlineItemFragmentBinding binding) {
        refreshCBItem = binding.refreshCBItem;
        fabCBItem = binding.fabCBItem;
        rvCashBoxItem = binding.rvCashBoxItem;
        itemCash = binding.itemCash;
        balancesCB = binding.balancesCB;
        root = binding.getRoot();
    }

    public CoordinatorLayout getRoot() {
        return root;
    }
}
