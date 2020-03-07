package com.vibal.utilities.ui.cashBoxManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.vibal.utilities.viewModels.CashBoxViewModel;

public class CashBoxItemOnlineFragment extends CashBoxItemFragment {

    @NonNull
    static CashBoxItemOnlineFragment newInstance(int pagerPosition) {
        CashBoxItemOnlineFragment fragment = new CashBoxItemOnlineFragment();
        fragment.setPositionAsArgument(pagerPosition);
        return fragment;
    }

    @Override
    protected CashBoxViewModel initializeViewModel() { //todo
        return new ViewModelProvider(requireParentFragment()).get(CashBoxViewModel.class);
    }

    //todo change adapter to entryonline
}
