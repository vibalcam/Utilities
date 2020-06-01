package com.vibal.utilities.ui.cashBoxManager;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.vibal.utilities.R;
import com.vibal.utilities.models.CashBox;
import com.vibal.utilities.viewModels.CashBoxLocalViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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
    @MenuRes
    protected int getMenuRes() {
        return R.menu.menu_toolbar_cash_box_manager;
    }

    @Override
    protected void doOnDelete(CashBox.InfoWithCash infoWithCash) {
        Snackbar.make(coordinatorLayout,
                getString(R.string.snackbarEntriesMoveToRecycle, 1),
                Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v ->
                        compositeDisposable.add(getViewModel().restore(infoWithCash)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe()))
                .show();
    }
}
