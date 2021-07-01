package com.vibal.utilities.ui.cashBoxManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vibal.utilities.R;
import com.vibal.utilities.databinding.CashBoxOnlineItemFragmentBinding;
import com.vibal.utilities.exceptions.UtilAppException;
import com.vibal.utilities.models.EntryBase;
import com.vibal.utilities.models.EntryInfo;
import com.vibal.utilities.persistence.retrofit.UtilAppAPI;
import com.vibal.utilities.ui.bindingHolder.CashBoxItemFragmentBindingHolder;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.util.MyDialogBuilder;
import com.vibal.utilities.util.Util;
import com.vibal.utilities.viewModels.CashBoxOnlineViewModel;
import com.vibal.utilities.viewModels.CashBoxViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

//imp https://developer.android.com/studio/inspect/database?utm_source=android-studio
public class CashBoxItemOnlineFragment extends CashBoxItemFragment implements CashBoxType.ONLINE {
    private static final String TAG = "PruebaOnlineItemFrag";

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
        binding = new CashBoxItemFragmentBindingHolder(
                CashBoxOnlineItemFragmentBinding.inflate(inflater, container, false));
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
        binding.refreshCBItem.setOnRefreshListener(this::onRefresh);
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
                        binding.refreshCBItem.setRefreshing(false);
                        Toast.makeText(requireContext(), UtilAppException.getErrorMsg(throwable),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(requireContext(), "Up to date!", Toast.LENGTH_SHORT).show();
                        if (binding != null)
                            binding.refreshCBItem.setRefreshing(false);
                    }
                }));
    }

    private void reloadFromServer() {
        compositeDisposable.add(viewModel.hardReload(viewModel.getCurrentCashBoxId())
                .doFinally(() -> {
                    if (binding != null)
                        binding.refreshCBItem.setRefreshing(false);
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> Toast.makeText(requireContext(),
                        "CashBox reloaded from server succesfully", Toast.LENGTH_SHORT).show(),
                        throwable -> {
                            LogUtil.error("", throwable);
                            Toast.makeText(requireContext(),
                                    UtilAppException.getErrorMsg(throwable), Toast.LENGTH_SHORT).show();
                        })
        );
    }

    @NonNull
    @Override
    protected CashBoxViewModel getViewModel() {
        if (viewModel == null)
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
                binding.refreshCBItem.setRefreshing(true);
                onRefresh();
                return true;
            case R.id.action_item_reload:
                new MyDialogBuilder(requireContext())
                        .setTitle(R.string.reload_title)
                        .setMessage(R.string.reload_message)
                        .setPositiveButton((dialog, which) -> {
                            binding.refreshCBItem.setRefreshing(true);
                            reloadFromServer();
                        }).setCancelOnTouchOutside(true)
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showInviteDialog() {
//        DialogInviteUserBinding dialogBinding =
//                DialogInviteUserBinding.inflate(LayoutInflater.from(getContext()));

        new MyDialogBuilder(requireContext())
                .setTitle(R.string.dialog_inviteTitle)
                .setView(R.layout.dialog_invite_user)
//                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.invite, null)
                .setCancelOnTouchOutside(true)
                .setActions(dialog -> {
                    Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    TextInputEditText inputUsername = ((AlertDialog) dialog).findViewById(R.id.inputTextUsername);
//                    TextInputEditText inputUsername = dialogBinding.inputUsername.inputTextUsername;
                    TextInputLayout layoutUsername = ((AlertDialog) dialog).findViewById(R.id.inputLayoutUsername);
//                    TextInputLayout layoutUsername = dialogBinding.inputUsername.inputLayoutUsername;
                    ListView listView = ((AlertDialog) dialog).findViewById(R.id.listViewInvite);
//                    ListView listView = dialogBinding.listViewInvite;
                    compositeDisposable.add(
                            viewModel.getCashBoxParticipants(viewModel.getCurrentCashBoxId())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(usernames -> {
                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                                android.R.layout.simple_list_item_1, usernames);
                                        listView.setAdapter(adapter);
                                        adapter.notifyDataSetChanged();
                                    }, throwable -> Toast.makeText(requireContext(),
                                            throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show())
                    );

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

//    @Override
//    protected String getReminderType() {
//        return ReminderReceiver.ONLINE;
//    }

    @Override
    protected void doOnModifyEntryError(Throwable throwable, EntryInfo entryInfo) {
//        if (!(throwable instanceof NonExistentException))
        if (!(throwable instanceof UtilAppException.NonExistentException))
            super.doOnModifyEntryError(throwable, entryInfo);

        onRefresh();
        new MyDialogBuilder(requireContext())
                .setTitle(R.string.dialog_nonExistentModify)
                .setMessage(R.string.dialog_nonExistentModify_message)
                .setNegativeButton(R.string.cancelDialog, null)
                .setPositiveButton(R.string.add, (dialog, which) ->
                        compositeDisposable.add(viewModel.addEntry(entryInfo.getCashBoxId(), EntryBase.getInstance(entryInfo))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(dialog::dismiss, throwable2 -> {
                                    dialog.dismiss();
                                    doOnRxError(throwable2);
                                }))).show();
    }
}
