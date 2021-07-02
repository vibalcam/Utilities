package com.vibal.utilities.ui.bindingHolder;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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
    public final TextView titleItemCash;
    public final ConstraintLayout itemCashLayout;
    @Nullable
    public final SwipeRefreshLayout refreshCBItem;

    public CashBoxItemFragmentBindingHolder(@NonNull CashBoxItemFragmentBinding binding) {
        itemCashLayout = binding.itemCashLayout;
        fabCBItem = binding.fabCBItem;
        rvCashBoxItem = binding.rvCashBoxItem;
        itemCash = binding.itemCash;
        titleItemCash = binding.titleItemCash;
        balancesCB = binding.balancesCB;
        root = binding.getRoot();
        refreshCBItem = null;
    }

    public CashBoxItemFragmentBindingHolder(@NonNull CashBoxOnlineItemFragmentBinding binding) {
        itemCashLayout = binding.itemCashLayout;
        refreshCBItem = binding.refreshCBItem;
        fabCBItem = binding.fabCBItem;
        rvCashBoxItem = binding.rvCashBoxItem;
        itemCash = binding.itemCash;
        titleItemCash = binding.titleItemCash;
        balancesCB = binding.balancesCB;
        root = binding.getRoot();
    }

    public CoordinatorLayout getRoot() {
        return root;
    }
}
