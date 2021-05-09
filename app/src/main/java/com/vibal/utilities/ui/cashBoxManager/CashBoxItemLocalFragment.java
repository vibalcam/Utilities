package com.vibal.utilities.ui.cashBoxManager;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.vibal.utilities.R;
import com.vibal.utilities.viewModels.CashBoxLocalViewModel;
import com.vibal.utilities.viewModels.CashBoxViewModel;

public class CashBoxItemLocalFragment extends CashBoxItemFragment implements CashBoxType.LOCAL {
    private CashBoxLocalViewModel viewModel;

    @NonNull
    static CashBoxItemLocalFragment newInstance(int pagerPosition) {
        CashBoxItemLocalFragment fragment = new CashBoxItemLocalFragment();
        fragment.setPositionAsArgument(pagerPosition);
        return fragment;
    }

    @NonNull
    @Override
    protected CashBoxViewModel getViewModel() {
        if (viewModel == null)
            viewModel = new ViewModelProvider(requireParentFragment()).get(CashBoxLocalViewModel.class);
        return viewModel;
    }

    @Override
    @MenuRes
    protected int getMenuRes() {
        return R.menu.menu_toolbar_cash_box_item;
    }

//    @Override
//    protected String getReminderType() {
//        return ReminderReceiver.LOCAL;
//    }
}
