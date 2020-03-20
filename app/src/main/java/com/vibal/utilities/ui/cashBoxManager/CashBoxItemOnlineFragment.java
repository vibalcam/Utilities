package com.vibal.utilities.ui.cashBoxManager;

import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vibal.utilities.R;
import com.vibal.utilities.util.MyDialogBuilder;
import com.vibal.utilities.util.Util;
import com.vibal.utilities.viewModels.CashBoxViewModel;

public class CashBoxItemOnlineFragment extends CashBoxItemFragment {

    @NonNull
    static CashBoxItemOnlineFragment newInstance(int pagerPosition) {
        CashBoxItemOnlineFragment fragment = new CashBoxItemOnlineFragment();
        fragment.setPositionAsArgument(pagerPosition);
        return fragment;
    }

    @Override
    protected CashBoxViewModel initializeViewModel() { //todo
        return new ViewModelProvider(requireParentFragment()).get(CashBoxViewModel.class);
//        return new ViewModelProvider(requireParentFragment()).get(CashBoxViewModelOnline.class);
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
        //todo invite dialog
        new MyDialogBuilder(requireContext())
                .setTitle(R.string.dialog_inviteTitle)
                .setView(R.layout.cash_box_input_name)
                .setPositiveButton(R.string.invite, null)
                .setCancelOnTouchOutside(true)
                .setActions(dialog -> {
                    Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    TextInputEditText inputName = ((AlertDialog) dialog).findViewById(R.id.inputTextChangeName);
                    TextInputLayout layoutName = ((AlertDialog) dialog).findViewById(R.id.inputLayoutChangeName);

//                    layoutName.setCounterMaxLength(CashBoxInfo.MAX_LENGTH_NAME);
                    // Show keyboard and select the whole text
                    inputName.selectAll();
                    Util.showKeyboard(requireContext(), inputName);

//                    positive.setOnClickListener((View v1) -> {
//                        String newName = inputName.getText().toString();
//                        try {
//                            compositeDisposable.add(
//                                    viewModel.changeCashBoxName(infoWithCash, newName)
//                                            .subscribeOn(Schedulers.io())
//                                            .observeOn(AndroidSchedulers.mainThread())
//                                            .subscribe(dialog::dismiss, throwable -> {
//                                                layoutName.setError(getString(R.string.nameInUse));
//                                                inputName.selectAll();
//                                                Util.showKeyboard(requireContext(), inputName);
//                                            }));
//                        } catch (IllegalArgumentException e) {
//                            layoutName.setError(e.getMessage());
//                            inputName.selectAll();
//                            Util.showKeyboard(requireContext(), inputName);
//                        }
//                    });
                }).show();
    }

    @Override
    protected int getMenuRes() {
        return R.menu.menu_toolbar_cash_box_item_online;
    }

    //todo change adapter to entryonline
}
