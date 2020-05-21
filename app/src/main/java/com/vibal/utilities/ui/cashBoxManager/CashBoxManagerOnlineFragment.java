package com.vibal.utilities.ui.cashBoxManager;

import android.content.SharedPreferences;
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
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vibal.utilities.R;
import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.modelsNew.CashBoxInfo;
import com.vibal.utilities.persistence.repositories.CashBoxOnlineRepository;
import com.vibal.utilities.persistence.retrofit.UtilAppException;
import com.vibal.utilities.ui.settings.SettingsActivity;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.util.MyDialogBuilder;
import com.vibal.utilities.util.Util;
import com.vibal.utilities.viewModels.CashBoxOnlineViewModel;

import butterknife.BindView;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CashBoxManagerOnlineFragment extends CashBoxManagerFragment {
    private static final String TAG = "PruebaOnlineManFrag";
    @BindView(R.id.refreshCBM)
    SwipeRefreshLayout refreshLayout;
    private CashBoxOnlineViewModel viewModel;

    @NonNull
    static CashBoxManagerOnlineFragment newInstance(int pagerPosition) {
        CashBoxManagerOnlineFragment fragment = new CashBoxManagerOnlineFragment();
        fragment.setPositionAsArgument(pagerPosition);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        LogUtil.debug(TAG, "onCreate: ");
        return inflater.inflate(R.layout.cash_box_online_manager_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshLayout.setOnRefreshListener(this::onRefresh);
    }

    private void onRefresh() {
        compositeDisposable.add(viewModel.getChanges()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> refreshLayout.setRefreshing(false),
                        throwable -> {
                            LogUtil.error(TAG, "Error on refresh: ", throwable);
                            refreshLayout.setRefreshing(false);
                            Toast.makeText(requireContext(),
                                    throwable instanceof UtilAppException ?
                                            throwable.getLocalizedMessage() :
                                            "An unexpected error occurred",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }));
    }

    @Override
    protected int getMenuRes() {
        return R.menu.menu_toolbar_cash_box_manager_online;
    }

    @Nullable
    @Override
    protected MyDialogBuilder getDeleteAllDialog() {
        MyDialogBuilder builder = super.getDeleteAllDialog();
        if (builder != null)
            builder = builder.setTitle(R.string.confirmDeleteAllDialog)
                    .setMessage("Are you sure you want to delete all entries?\nThis cannot be undone.");
        return builder;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_manager_refresh) {
            refreshLayout.setRefreshing(true);
            onRefresh();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    protected CashBoxOnlineViewModel getViewModel() {
        return viewModel;
    }

    @NonNull
    @Override
    protected CashBoxOnlineViewModel initializeViewModel() {
        viewModel = new ViewModelProvider(requireParentFragment()).get(CashBoxOnlineViewModel.class);
        return viewModel;
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
    void showAddDialog() {
        closeFabMenu();
        if (actionMode != null)
            actionMode.finish();

        if (!CashBoxOnlineRepository.isOnlineIdSet()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            // Check if accepted online cashboxes
            if (!preferences.getBoolean(SettingsActivity.KEY_ONLINE, false))
                SettingsActivity.acceptOnlineMode(requireContext(), (dialog, which) -> {
                    preferences.edit()
                            .putBoolean(SettingsActivity.KEY_ONLINE, true)
                            .apply();
                    showSelectUsername();
                });
            else
                showSelectUsername();
        } else
            super.showAddDialog();
    }

    private void showSelectUsername() {
        new MyDialogBuilder(requireContext())
                .setTitle(R.string.dialog_selectUsername)
                .setCancelOnTouchOutside(false)
                .setView(R.layout.dialog_new_username)
                .setActions(dialog -> {
                    Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    TextInputEditText inputUsername = ((AlertDialog) dialog).findViewById(R.id.inputTextUsername);
                    TextInputLayout layoutUsername = ((AlertDialog) dialog).findViewById(R.id.inputLayoutUsername);

                    // Show keyboard and select the whole text
                    inputUsername.selectAll();
                    Util.showKeyboard(requireContext(), inputUsername);

                    positive.setOnClickListener(v -> {
                        String username = inputUsername.getText().toString().trim();
                        try {
                            compositeDisposable.add(
                                    viewModel.signUp(username)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(() -> {
                                                // dismiss and show add dialog
                                                dialog.dismiss();
                                                super.showAddDialog();
                                            }, throwable -> {
                                                LogUtil.error(TAG, "Sign up:", throwable);
                                                if (throwable instanceof UtilAppException) {
                                                    layoutUsername.setError(throwable.getLocalizedMessage());
                                                    inputUsername.selectAll();
                                                    Util.showKeyboard(requireContext(), inputUsername);
                                                } else { // should never happen
                                                    dialog.dismiss();
                                                    Toast.makeText(requireContext(),
                                                            "Unexpected error", Toast.LENGTH_SHORT)
                                                            .show();
                                                }
                                            }));
                        } catch (UtilAppException e) {
                            layoutUsername.setError(e.getLocalizedMessage());
                            inputUsername.selectAll();
                            Util.showKeyboard(requireContext(), inputUsername);
                        }
                    });
                }).show();
    }

    @Override
    protected void showInvitationDialog(CashBox.InfoWithCash infoWithCash) {
        // Get the username without the added fix character
        String name = infoWithCash.getCashBoxInfo().getName();
        int index = name.indexOf(CashBoxInfo.FIX_NAME_CHARACTER);
        if (index != -1)
            name = name.substring(0, index);
        new MyDialogBuilder(requireContext())
                .setTitle(R.string.dialog_acceptInvitation_title)
                .setMessage(getString(R.string.dialog_acceptInvitation_message, name))
                .setCancelOnTouchOutside(true)
                .setPositiveButton(R.string.accept, (dialog, which) ->
                        compositeDisposable.add(subscribeAndShowToast(
                                viewModel.acceptInvitation(infoWithCash.getId()),
                                "Invitation accepted")))
                .setNegativeButton(R.string.reject, (dialog, which) ->
                        compositeDisposable.add(subscribeAndShowToast(
                                viewModel.deleteCashBoxInfo(infoWithCash),
                                "Invitation rejected")))
                .setNeutralButton(R.string.cancelDialog, null)
                .show();
    }

    @Override
    protected void showChangesDialog(CashBox.InfoWithCash infoWithCash) {
        CashBoxChangesDialogFragment.newInstance(infoWithCash.getId())
                .show(getChildFragmentManager().beginTransaction(), "dialog");
    }

    private Disposable subscribeAndShowToast(Completable completable, String successText) {
        return completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    if (successText != null)
                        Toast.makeText(requireContext(),
                                successText, Toast.LENGTH_SHORT).show();
                }, throwable -> {
                    LogUtil.error(TAG, successText + ":", throwable);
                    if (throwable instanceof UtilAppException)
                        Toast.makeText(requireContext(), throwable.getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(requireContext(), "Unexpected error",
                                Toast.LENGTH_SHORT).show();
                });
    }
}
