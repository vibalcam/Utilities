package com.vibal.utilities.ui.cashBoxManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.vibal.utilities.R;
import com.vibal.utilities.viewModels.CashBoxViewModel;

import io.reactivex.disposables.CompositeDisposable;

public class CashBoxManagerLocalFragment extends CashBoxManagerFragment {
    static CashBoxManagerLocalFragment newInstance(int pagerPosition) {
        CashBoxManagerLocalFragment fragment = new CashBoxManagerLocalFragment();
        fragment.setPositionAsArgument(pagerPosition);
        return fragment;
    }

    @Override
    protected CashBoxViewModel initializeViewModel() {
        return new ViewModelProvider(requireParentFragment()).get(CashBoxViewModel.class);
    }

    @Override
    protected int getTitle() {
        return R.string.titleCBM;
    }

    @Override
    protected boolean isCloneEnabled() {
        return true;
    }

    @Override
    protected CashBoxItemLocalFragment getChildInstance() {
        return CashBoxItemLocalFragment.newInstance(getPagerPosition());
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
