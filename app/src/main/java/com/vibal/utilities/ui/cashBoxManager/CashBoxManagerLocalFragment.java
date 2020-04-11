package com.vibal.utilities.ui.cashBoxManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.vibal.utilities.R;
import com.vibal.utilities.viewModels.CashBoxLocalViewModel;
import com.vibal.utilities.viewModels.CashBoxViewModel;

import io.reactivex.disposables.CompositeDisposable;

public class CashBoxManagerLocalFragment extends CashBoxManagerFragment {
    private CashBoxLocalViewModel viewModel;

    static CashBoxManagerLocalFragment newInstance(int pagerPosition) {
        CashBoxManagerLocalFragment fragment = new CashBoxManagerLocalFragment();
        fragment.setPositionAsArgument(pagerPosition);
        return fragment;
    }

    @NonNull
    @Override
    protected CashBoxLocalViewModel getViewModel() {
        return viewModel;
    }

    @NonNull
    @Override
    protected CashBoxLocalViewModel initializeViewModel() {
        viewModel = new ViewModelProvider(requireParentFragment()).get(CashBoxLocalViewModel.class);
        return viewModel;
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
