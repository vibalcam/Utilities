package com.vibal.utilities.ui.cashBoxManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vibal.utilities.R;
import com.vibal.utilities.exceptions.NonExistentException;
import com.vibal.utilities.exceptions.UtilAppException;
import com.vibal.utilities.models.Entry;
import com.vibal.utilities.persistence.retrofit.UtilAppAPI;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.util.MyDialogBuilder;
import com.vibal.utilities.util.Util;
import com.vibal.utilities.viewModels.CashBoxOnlineViewModel;
import com.vibal.utilities.viewModels.CashBoxViewModel;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class CashBoxItemOnlineFragment extends CashBoxItemFragment {
    private static final String TAG = "PruebaOnlineItemFrag";
    @BindView(R.id.refreshCBItem)
    SwipeRefreshLayout refreshLayout;
    private CashBoxOnlineViewModel viewModel;

    @NonNull
    static CashBoxItemOnlineFragment newInstance(int pagerPosition) {
        CashBoxItemOnlineFragment fragment = new CashBoxItemOnlineFragment();
        fragment.setPositionAsArgument(pagerPosition);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        LogUtil.debug(TAG, "on create:");
        return inflater.inflate(R.layout.cash_box_online_item_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshLayout.setOnRefreshListener(this::onRefresh);
    }

    private void onRefresh() {
        compositeDisposable.add(viewModel.getChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        LogUtil.error(TAG, "Error on refresh: ", throwable);
                        refreshLayout.setRefreshing(false);
                        Toast.makeText(requireContext(),
                                throwable instanceof UtilAppException ?
                                        throwable.getLocalizedMessage() :
                                        "An unexpected error occurred",
                                Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(requireContext(), "Up to date!", Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    }
                }));
    }

    @NonNull
    @Override
    protected CashBoxViewModel getViewModel() {
        return viewModel;
    }

    @NonNull
    @Override
    protected CashBoxViewModel initializeViewModel() {
        viewModel = new ViewModelProvider(requireParentFragment()).get(CashBoxOnlineViewModel.class);
        return viewModel;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!isOptionsMenuActive())
            return false;

        switch (item.getItemId()) {
            case R.id.action_item_invite:
                showInviteDialog();
                return true;
            case R.id.action_item_refresh:
                refreshLayout.setRefreshing(true);
                onRefresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showInviteDialog() {
        new MyDialogBuilder(requireContext())
                .setTitle(R.string.dialog_inviteTitle)
                .setView(R.layout.dialog_invite_user)
                .setPositiveButton(R.string.invite, null)
                .setCancelOnTouchOutside(true)
                .setActions(dialog -> {
                    Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    TextInputEditText inputUsername = ((AlertDialog) dialog).findViewById(R.id.inputTextUsername);
                    TextInputLayout layoutUsername = ((AlertDialog) dialog).findViewById(R.id.inputLayoutUsername);

                    layoutUsername.setCounterMaxLength(UtilAppAPI.MAX_LENGTH_USERNAME);
                    // Show keyboard and select the whole text
                    inputUsername.selectAll();
                    Util.showKeyboard(requireContext(), inputUsername);

                    positive.setOnClickListener(v -> {
                        String username = inputUsername.getText().toString();
                        compositeDisposable.add(viewModel.sendInvitation(username)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                    dialog.dismiss();
                                    Toast.makeText(requireContext(),
                                            "Invitation sent successfully",
                                            Toast.LENGTH_SHORT).show();
                                }, throwable -> {
                                    LogUtil.error(TAG, "Invite dialog:", throwable);
                                    if (throwable instanceof UtilAppException) {
                                        layoutUsername.setError(throwable.getLocalizedMessage());
                                        inputUsername.selectAll();
                                        Util.showKeyboard(requireContext(), inputUsername);
                                    } else { // should never happen
                                        dialog.dismiss();
                                        Toast.makeText(requireContext(), "Unexpected error",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }));
                    });
                }).show();
    }

    @Override
    protected int getMenuRes() {
        return R.menu.menu_toolbar_cash_box_item_online;
    }

    @Override
    protected void doOnModifyEntryError(Throwable throwable, Entry entry) {
        if (!(throwable instanceof NonExistentException))
            super.doOnModifyEntryError(throwable, entry);

        new MyDialogBuilder(requireContext())
                .setTitle(R.string.dialog_nonExistentModify)
                .setMessage(R.string.dialog_nonExistentModify_message)
                .setNegativeButton(R.string.cancelDialog, null)
                .setPositiveButton(R.string.add, (dialog, which) ->
                        compositeDisposable.add(viewModel.addEntry(entry.getCashBoxId(), entry)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(dialog::dismiss, throwable2 -> {
                                    dialog.dismiss();
                                    doOnRxError(throwable2);
                                }))).show();
    }
}
