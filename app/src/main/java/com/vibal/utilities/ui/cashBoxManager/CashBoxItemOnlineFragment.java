package com.vibal.utilities.ui.cashBoxManager;

import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vibal.utilities.R;
import com.vibal.utilities.persistence.retrofit.UtilAppAPI;
import com.vibal.utilities.persistence.retrofit.UtilAppException;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.util.MyDialogBuilder;
import com.vibal.utilities.util.Util;
import com.vibal.utilities.viewModels.CashBoxOnlineViewModel;
import com.vibal.utilities.viewModels.CashBoxViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CashBoxItemOnlineFragment extends CashBoxItemFragment {
    private static final String TAG = "PruebaOnlineItemFrag";
    private CashBoxOnlineViewModel viewModel;

    @NonNull
    static CashBoxItemOnlineFragment newInstance(int pagerPosition) {
        CashBoxItemOnlineFragment fragment = new CashBoxItemOnlineFragment();
        fragment.setPositionAsArgument(pagerPosition);
        return fragment;
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
        if(item.getItemId() == R.id.action_item_invite) {
            showInviteDialog();
            return true;
        } else
            return super.onOptionsItemSelected(item);
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
                                },throwable -> {
                                    LogUtil.error(TAG,"Invite dialog:",throwable);
                                    if(throwable instanceof UtilAppException) {
                                        layoutUsername.setError(throwable.getLocalizedMessage());
                                        inputUsername.selectAll();
                                        Util.showKeyboard(requireContext(),inputUsername);
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
}
