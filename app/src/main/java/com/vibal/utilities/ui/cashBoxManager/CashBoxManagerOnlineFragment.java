package com.vibal.utilities.ui.cashBoxManager;

import android.content.Intent;
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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vibal.utilities.R;
import com.vibal.utilities.databinding.CashBoxOnlineManagerFragmentBinding;
import com.vibal.utilities.exceptions.UtilAppException;
import com.vibal.utilities.models.CashBoxInfo;
import com.vibal.utilities.models.InfoWithCash;
import com.vibal.utilities.persistence.repositories.CashBoxOnlineRepository;
import com.vibal.utilities.ui.bindingHolder.CashBoxManagerFragmentBindingHolder;
import com.vibal.utilities.ui.settings.SettingsActivity;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.util.MyDialogBuilder;
import com.vibal.utilities.util.Util;
import com.vibal.utilities.viewModels.CashBoxOnlineViewModel;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

//imp accept cashbox by clicking

public class CashBoxManagerOnlineFragment extends CashBoxManagerFragment implements CashBoxType.ONLINE {
    private static final String TAG = "PruebaOnlineManFrag";

    private static boolean startUp = true;
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
        binding = new CashBoxManagerFragmentBindingHolder(
                CashBoxOnlineManagerFragmentBinding.inflate(inflater, container, false));
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.refreshCBM.setOnRefreshListener(this::onRefresh);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Show to add dialog when first go in
        if (!CashBoxOnlineRepository.isOnlineIdSet())
            showAddDialog();
        else if (startUp) {
            binding.refreshCBM.setRefreshing(true);
            onRefresh();
            startUp = false;
        }
    }

    private void onRefresh() {
        compositeDisposable.add(viewModel.getChanges()
//                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        LogUtil.error(TAG, "Error on refresh: ", throwable);
                        binding.refreshCBM.setRefreshing(false);
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
                        if (binding != null)
                            binding.refreshCBM.setRefreshing(false);
                    }
                }));
    }

    @Override
    protected int getMenuRes() {
        return R.menu.menu_toolbar_cash_box_manager_online;
    }

//    @Override
//    protected int getCashBoxType() {
//        return CashBoxManagerActivity.ONLINE;
//    }

    @Nullable
    @Override
    protected MyDialogBuilder getDeleteAllDialog() {
        MyDialogBuilder builder = super.getDeleteAllDialog();
        if (builder != null)
            builder = builder.setTitle(R.string.confirmDeleteAllDialog)
                    .setMessage("Are you sure you want to delete all cashBoxes?\nThis cannot be undone.");
        return builder;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!isOptionsMenuActive())
            return false;

        switch (item.getItemId()) {
            case R.id.action_manager_refresh_online:
                binding.refreshCBM.setRefreshing(true);
                onRefresh();
                return true;
            case R.id.action_manager_deleteAll_online:
                AlertDialog.Builder builder = getDeleteAllDialog();
                if (builder != null)
                    builder.show();
                return true;
            case R.id.action_manager_help_online:
                Util.createHelpDialog(requireContext(), R.string.cashBoxManager_helpTitle,
                        R.string.cashBoxManager_help).show();
                return true;
            case R.id.action_manager_edit_online:
                startActionMode(EDIT_MODE);
                return true;
            case R.id.action_manager_settings_online:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    @NonNull
    private CashBoxManagerActivity requireCashBoxActivity() {
        return (CashBoxManagerActivity) requireActivity();
    }

    @Override
    protected void showAddDialog() {
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
                }).setOnDismissListener(dialog -> requireCashBoxActivity().selectTab(CashBoxType.LOCAL))
                        .show();
            else
                showSelectUsername();
        } else
            super.showAddDialog();
    }

    @Override
    protected void deleteCashBox(int position) {
        new MyDialogBuilder(requireContext())
                .setTitle(R.string.confirmDeleteDialog)
                .setMessage("Are you sure you want to delete this cashBox?\nThis cannot be undone.")
                .setPositiveButton((dialog, which) -> super.deleteCashBox(position))
                .setNegativeButton((dialog, which) -> adapter.notifyItemChanged(position))
                .show();
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
                }).setOnDismissListener(dialog -> requireCashBoxActivity().selectTab(CashBoxType.LOCAL))
                .show();
    }

    @Override
    protected void showInvitationDialog(@NonNull InfoWithCash infoWithCash) {
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
    protected void showChangesDialog(InfoWithCash infoWithCash) {
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
