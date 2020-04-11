package com.vibal.utilities.ui.cashBoxManager;

import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vibal.utilities.R;
import com.vibal.utilities.persistence.repositories.CashBoxOnlineRepository;
import com.vibal.utilities.persistence.retrofit.UtilAppException;
import com.vibal.utilities.ui.settings.SettingsActivity;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.util.MyDialogBuilder;
import com.vibal.utilities.util.Util;
import com.vibal.utilities.viewModels.CashBoxOnlineViewModel;
import com.vibal.utilities.viewModels.CashBoxViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CashBoxManagerOnlineFragment extends CashBoxManagerFragment {
    private static final String TAG = "PruebaOnlineManFrag";
    private CashBoxOnlineViewModel viewModel;

    @NonNull
    static CashBoxManagerOnlineFragment newInstance(int pagerPosition) {
        CashBoxManagerOnlineFragment fragment = new CashBoxManagerOnlineFragment();
        fragment.setPositionAsArgument(pagerPosition);
        return fragment;
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
    protected int getSideImageResource() {
        return R.drawable.ic_add;
    }

    @Override
    protected void onImageClick(long cashBoxId, @NonNull CashBoxViewModel viewModel,
                                CompositeDisposable compositeDisposable) {
        CashBoxItemFragment.getAddEntryDialog(cashBoxId, requireContext(), viewModel,
                compositeDisposable).show();
    }

    @Override
    void showAddDialog() {
        closeFabMenu();
        if (actionMode != null)
            actionMode.finish();

        if(!CashBoxOnlineRepository.isOnlineIdSet()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            // Check if accepted online cashboxes
            if(!preferences.getBoolean(SettingsActivity.KEY_ONLINE,false))
                SettingsActivity.acceptOnlineMode(requireContext(), (dialog, which) -> {
                    preferences.edit()
                            .putBoolean(SettingsActivity.KEY_ONLINE, true)
                            .apply();
                    showSelectUsername();
                });
            else
                showSelectUsername();
//                new MyDialogBuilder(requireContext())
//                        .setPositiveButton("To Settings", (dialog, which) ->
//                                startActivity(new Intent(requireContext(), SettingsActivity.class)))
//                        .setNegativeButton("Cancel", null)
//                        .setTitle("Online mode")
//                        .show();
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
                                            },throwable -> {
                                                LogUtil.error(TAG,"Sign up:",throwable);
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
}
