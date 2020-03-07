package com.vibal.utilities.ui.cashBoxManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.vibal.utilities.R;
import com.vibal.utilities.viewModels.CashBoxViewModel;

import io.reactivex.disposables.CompositeDisposable;

public class CashBoxManagerOnlineFragment extends CashBoxManagerFragment {
    static CashBoxManagerOnlineFragment newInstance(int pagerPosition) {
        CashBoxManagerOnlineFragment fragment = new CashBoxManagerOnlineFragment();
        fragment.setPositionAsArgument(pagerPosition);
        return fragment;
    }

    @Override
    protected CashBoxViewModel initializeViewModel() {
        return new ViewModelProvider(requireParentFragment()).get(CashBoxViewModel.class);
    }

    @Override
    protected int getTitle() {
        return R.string.titleCBM_online;
    }

    @Override
    protected boolean isCloneEnabled() {
        return false;
    }

    @Override
    protected CashBoxItemOnlineFragment getChildInstance() {
        return CashBoxItemOnlineFragment.newInstance(getPagerPosition());
    }

    @Override
    protected int getSideImageResource() {
        return R.drawable.ic_add;
    }

    @Override
    protected void onImageClick(long cashBoxId, @NonNull CashBoxViewModel viewModel,
                                CompositeDisposable compositeDisposable) {
        CashBoxItemFragment.getAddEntryDialog(cashBoxId, requireContext(), viewModel,
                compositeDisposable).show();
    }
}
