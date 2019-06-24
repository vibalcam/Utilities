package com.utilities.vibal.utilities.ui.cashBoxManager;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.models.CashBoxViewModel;
import com.utilities.vibal.utilities.ui.swipeController.CashBoxAdapterSwipable;
import com.utilities.vibal.utilities.ui.swipeController.CashBoxSwipeController;
import com.utilities.vibal.utilities.util.LogUtil;
import com.utilities.vibal.utilities.util.Util;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CashBoxItemFragment extends Fragment {
    //DiffUtil Callback
    private static final DiffUtil.ItemCallback<CashBox.Entry> DIFF_CALLBACK = new DiffUtil.ItemCallback<CashBox.Entry>() {
        @Override
        public boolean areItemsTheSame(@NonNull CashBox.Entry oldItem, @NonNull CashBox.Entry newItem) {
            return oldItem.getId()==newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull CashBox.Entry oldItem, @NonNull CashBox.Entry newItem) {
            return oldItem.getAmount()==newItem.getAmount() &&
                    oldItem.getDate().equals(newItem.getDate()) &&
                    oldItem.getInfo().equals(newItem.getInfo());
        }
    };
    private static final double MAX_SHOW_CASH = 99999999;
    private static final String TAG = "PruebaItemFragment";

    @BindView(R.id.itemCash) TextView itemCash;
//    @BindView(R.id.itemCBCoordinatorLayout) CoordinatorLayout itemCBCoordinatorLayout;
    @BindView(R.id.rvCashBoxItem) RecyclerView rvCashBoxItem;

    private CashBoxItemRecyclerAdapter adapter;
    private NumberFormat formatCurrency = NumberFormat.getCurrencyInstance();
    private CashBoxViewModel viewModel;
    private CompositeDisposable disposable;
    private ShareActionProvider shareActionProvider;

    public static CashBoxItemFragment newInstance() {
        return new CashBoxItemFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cash_box_item_fragment, container, false);
        ButterKnife.bind(this,view);

        //Set up RecyclerView
//        rvCashBoxItem.setNestedScrollingEnabled(true);
        rvCashBoxItem.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvCashBoxItem.setLayoutManager(layoutManager);
        adapter = new CashBoxItemRecyclerAdapter();
        rvCashBoxItem.setAdapter(adapter);
        boolean swipeLeftDelete = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean("swipeLeftDelete", true);
        (new ItemTouchHelper(new CashBoxSwipeController(adapter, swipeLeftDelete)))
                .attachToRecyclerView(rvCashBoxItem);
        rvCashBoxItem.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        LogUtil.debug(TAG, "on create:");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Initialize data
        viewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(CashBoxViewModel.class);
        viewModel.getCurrentCashBox().observe(getViewLifecycleOwner(), cashBox -> {
                    if(cashBox==null) {
                        LogUtil.debug("Prueba","CashBox is null");
                        return;
                    }

                    //TODO: separacion info y entries

                    // Set Title
                    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                    if(actionBar!=null)
                        actionBar.setTitle(cashBox.getName());

                    updateCash(cashBox.getCash());
                    adapter.submitList(cashBox.getEntries());

                    // Update ShareIntent
                    if (shareActionProvider != null)
                        shareActionProvider.setShareIntent(Util.getShareIntent(cashBox));
                });
        disposable = ((CashBoxManagerActivity) getActivity()).getDisposable();

//        //Set Toolbar as ActionBar
//        setSupportActionBar(findViewById(R.id.toolbarCBItem));
//        ActionBar actionBar = getSupportActionBar();
//        if(actionBar!=null)
//            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void updateCash(double cash) {
        if (Math.abs(cash) > MAX_SHOW_CASH)
            itemCash.setText(R.string.outOfRange);
        else {
            itemCash.setText(formatCurrency.format(cash));
            if(cash<0)
                itemCash.setTextColor(Objects.requireNonNull(getActivity())
                        .getColor(R.color.colorNegativeNumber));
            else
                itemCash.setTextColor(Objects.requireNonNull(getActivity())
                        .getColor(R.color.colorPositiveNumber));
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_toolbar_cash_box_item, menu);
//
//        // Set up ShareActionProvider
//        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_item_share));
//        updateShareIntent();
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_item_deleteAll:
//                deleteAll();
//                return true;
//            case R.id.action_item_settings:
//                startActivity(new Intent(getContext(), SettingsActivity.class));
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    @OnClick(R.id.fabCBItem)
    void onFabClicked() {
        showAddDialog();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        AlertDialog dialog = builder.setTitle(R.string.newEntry)
                .setView(R.layout.cash_box_item_entry_input)
                .setNegativeButton(R.string.cancelDialog, null)
                .setPositiveButton(R.string.addEntryDialog, null)
                .create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnShowListener((DialogInterface dialog1) -> {
            Button positive = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            TextInputEditText inputInfo = ((AlertDialog) dialog1).findViewById(R.id.inputTextInfo);
            TextInputEditText inputAmount = ((AlertDialog) dialog1).findViewById(R.id.inputTextAmount);
            TextInputLayout layoutAmount = ((AlertDialog) dialog1).findViewById(R.id.inputLayoutAmount);

            Util.showKeyboard(getContext(), inputAmount);
            positive.setOnClickListener((View v) -> {
                try {
                    String input = inputAmount.getText().toString().trim();
                    if (input.isEmpty()) {
                        layoutAmount.setError(getString(R.string.required));
                        Util.showKeyboard(getContext(), inputAmount);
                    } else {
                        double amount = Util.parseDouble(inputAmount.getText().toString());
                        disposable.add(viewModel.addEntryToCurrentCashBox(new CashBox.Entry(
                                amount,inputInfo.getText().toString(),Calendar.getInstance()))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(dialog1::dismiss));
                        rvCashBoxItem.scrollToPosition(0);
                    }
                } catch (NumberFormatException e) {
                    layoutAmount.setError(getString(R.string.errorMessageAmount));
                    inputAmount.selectAll();
                    Util.showKeyboard(getContext(), inputAmount);
                }
            });
        });
        dialog.show();
    }

    private void deleteAll() {
        if (adapter.getItemCount()==0) {
            Toast.makeText(getContext(), R.string.noEntriesDelete, Toast.LENGTH_SHORT).show();
            return;
        }

        List<CashBox.Entry> deletedEntries = adapter.getCurrentList();
        disposable.add(viewModel.deleteAllEntriesFromCurrentCashBox()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer ->
                        Snackbar.make(rvCashBoxItem,
                                getString(R.string.snackbarEntriesDeleted, integer),
                                Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, v ->
                                disposable.add(viewModel.addAllEntriesToCurrentCashBox(deletedEntries)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe()))
                                .show()));
    }

    public class CashBoxItemRecyclerAdapter extends ListAdapter<CashBox.Entry, CashBoxItemRecyclerAdapter.ViewHolder> implements CashBoxAdapterSwipable {
        private static final boolean DRAG_ENABLED = false;
        private static final boolean SWIPE_ENABLED = true;

        private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

        CashBoxItemRecyclerAdapter() {
            super(DIFF_CALLBACK);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cash_box_item_item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int index) {
            CashBox.Entry entry = getItem(index);

            // Amount
            viewHolder.rvItemAmount.setText(formatCurrency.format(entry.getAmount()));
            if (entry.getAmount() < 0)
                viewHolder.rvItemAmount.setTextColor(getActivity().getColor(R.color.colorNegativeNumber));
            else
                viewHolder.rvItemAmount.setTextColor(getActivity().getColor(R.color.colorPositiveNumber));
            // CashBoxInfo
            if (entry.getInfo().isEmpty())
                viewHolder.rvItemInfo.setText(R.string.noInfoEntered);
            else
                viewHolder.rvItemInfo.setText(entry.getInfo());
            // Date
            viewHolder.rvItemDate.setText(dateFormat.format(entry.getDate().getTime()));
        }

        @Override
        public boolean isDragEnabled() {
            return DRAG_ENABLED;
        }

        @Override
        public boolean isSwipeEnabled() {
            return SWIPE_ENABLED;
        }

        @Override
        public void onItemDelete(int position) {
            CashBox.Entry entry = getItem(position);
            disposable.add(viewModel.deleteEntry(entry)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Snackbar.make(rvCashBoxItem,
                            getString(R.string.snackbarEntriesDeleted, 1),
                            Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, (View v) ->
                                disposable.add(viewModel.addEntryToCurrentCashBox(entry)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe()))
                            .show()));
        }

        @Override
        public void onItemModify(int position) {
            LogUtil.debug(TAG, "onItemModify: ");

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            AlertDialog dialog = builder.setTitle(R.string.modifyEntry)
                    .setView(R.layout.cash_box_item_entry_input)
                    .setNegativeButton(R.string.cancelDialog, null)
                    .setPositiveButton(R.string.confirm, null)
                    .create();
            dialog.setCanceledOnTouchOutside(false);

            dialog.setOnShowListener((DialogInterface dialog1) -> {
                Button positive = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
                TextInputEditText inputInfo = ((AlertDialog) dialog1).findViewById(R.id.inputTextInfo);
                TextInputEditText inputAmount = ((AlertDialog) dialog1).findViewById(R.id.inputTextAmount);
                TextInputLayout layoutAmount = ((AlertDialog) dialog1).findViewById(R.id.inputLayoutAmount);

                CashBox.Entry modifiedEntry = getItem(position);
                inputInfo.setText(modifiedEntry.getInfo());
                inputAmount.setText(String.format(Locale.getDefault(), "%.2f", modifiedEntry.getAmount()));

                inputAmount.selectAll();
                Util.showKeyboard(getContext(), inputAmount);
                positive.setOnClickListener((View v) -> {
                    try {
                        LogUtil.debug(TAG, "showAddDialog: cause" + (inputInfo.getText() == null) + (inputInfo.getText().toString().isEmpty()));
                        String input = inputAmount.getText().toString();
                        if (input.trim().isEmpty()) {
                            layoutAmount.setError(getString(R.string.required));
                            inputAmount.setText("");
                            Util.showKeyboard(getContext(), inputAmount);
                        } else {
                            double amount = Util.parseDouble(inputAmount.getText().toString());
                            CashBox.Entry entry = new CashBox.Entry(modifiedEntry.getId(),amount,
                                    inputInfo.getText().toString(),modifiedEntry.getDate());
                            disposable.add(viewModel.updateEntry(entry)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() ->
                                        Snackbar.make(rvCashBoxItem,
                                                R.string.snackbarEntryModified, Snackbar.LENGTH_LONG)
                                                .setAction(R.string.undo, (View v1) ->
                                                    disposable.add(viewModel.updateEntry(modifiedEntry)
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe()))
                                                .show()));
                        }
                    } catch (NumberFormatException e) {
                        layoutAmount.setError(getString(R.string.errorMessageAmount));
                        inputAmount.selectAll();
                        Util.showKeyboard(getContext(), inputAmount);
                    }
                });
            });
            dialog.show();

            notifyDataSetChanged();   // since the item is deleted from swipping we have to show it back again
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.rvItemDate) TextView rvItemDate;
            @BindView(R.id.rvItemAmount) TextView rvItemAmount;
            @BindView(R.id.rvItemInfo) TextView rvItemInfo;

            ViewHolder(@NonNull View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }
}
