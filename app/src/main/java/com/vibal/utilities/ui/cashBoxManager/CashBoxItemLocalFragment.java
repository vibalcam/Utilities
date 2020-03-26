package com.vibal.utilities.ui.cashBoxManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.vibal.utilities.viewModels.CashBoxLocalViewModel;
import com.vibal.utilities.viewModels.CashBoxViewModel;

public class CashBoxItemLocalFragment extends CashBoxItemFragment {

    @NonNull
    static CashBoxItemLocalFragment newInstance(int pagerPosition) {
        CashBoxItemLocalFragment fragment = new CashBoxItemLocalFragment();
        fragment.setPositionAsArgument(pagerPosition);
        return fragment;
    }

    @Override
    protected CashBoxViewModel initializeViewModel() {
        return new ViewModelProvider(requireParentFragment()).get(CashBoxLocalViewModel.class);
    }
}
