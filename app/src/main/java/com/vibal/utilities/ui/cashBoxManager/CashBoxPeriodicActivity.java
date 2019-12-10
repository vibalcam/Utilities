package com.vibal.utilities.ui.cashBoxManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.vibal.utilities.R;
import com.vibal.utilities.modelsNew.PeriodicEntryPojo;
import com.vibal.utilities.modelsNew.PeriodicEntryWorkViewModel;
import com.vibal.utilities.ui.settings.SettingsActivity;
import com.vibal.utilities.ui.swipeController.CashBoxAdapterSwipable;
import com.vibal.utilities.ui.swipeController.CashBoxSwipeController;
import com.vibal.utilities.util.DiffCallback;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.util.Util;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CashBoxPeriodicActivity extends AppCompatActivity {
    @BindView(R.id.lyCBPeriodic)
    CoordinatorLayout coordinatorLayout;

    private PeriodicEntryWorkViewModel viewModel;
    private CashBoxPeriodicRecyclerAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cash_box_periodic_activity);
        ButterKnife.bind(this);

        // Set ViewModel
        viewModel = new ViewModelProvider(this).get(PeriodicEntryWorkViewModel.class);
        viewModel.getPeriodicEntries().observe(this, periodicEntryPojos -> {
            LogUtil.debug("Prueba", "New list submitted");
            adapter.submitList(periodicEntryPojos);
        });

        //Set Toolbar as ActionBar
        setSupportActionBar(findViewById(R.id.toolbarCBPeriodic));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_periodicEntry);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Set up the RecyclerView
        RecyclerView rvPeriodicEntry = findViewById(R.id.rvCashBoxPeriodic);
        rvPeriodicEntry.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvPeriodicEntry.setLayoutManager(layoutManager);
        rvPeriodicEntry.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
        adapter = new CashBoxPeriodicRecyclerAdapter();
        rvPeriodicEntry.setAdapter(adapter);
        new ItemTouchHelper(new CashBoxSwipeController(adapter,
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean("swipeLeftDelete", true)))
                .attachToRecyclerView(rvPeriodicEntry);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_toolbar_cash_box_periodic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_periodic_deleteAll:
                deleteAll();
                return true;
            case R.id.action_periodic_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAll() {
        int count = adapter.getItemCount();
        if (count == 0) {
            Toast.makeText(this, "No entries to recycle", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirmDeleteAllDialog)
                .setMessage("Are you sure you want to send all entries to the recycle bin?")
                .setNegativeButton(R.string.cancelDialog, null)
                .setPositiveButton(R.string.confirmDeleteDialogConfirm, (DialogInterface dialog, int which) ->
                        viewModel.addDisposable(viewModel.deleteAllPeriodicEntryWorks()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> Toast.makeText(this,
                                        getString(R.string.snackbarEntriesDeleted, count),
                                        Toast.LENGTH_SHORT)
                                        .show())))
                .show();
    }

    public class CashBoxPeriodicRecyclerAdapter
            extends RecyclerView.Adapter<CashBoxPeriodicRecyclerAdapter.ViewHolder>
            implements CashBoxAdapterSwipable {
        private static final boolean SWIPE_ENABLED = true;
        private static final boolean DRAG_ENABLED = false;

        private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        @NonNull
        private List<PeriodicEntryPojo> currentList = new ArrayList<>();
        private LinkedList<PeriodicEntryPojo> toDelete = new LinkedList<>();

        void submitList(@NonNull List<PeriodicEntryPojo> newList) {
            viewModel.addDisposable(Single.create((SingleOnSubscribe<List<PeriodicEntryPojo>>) emitter -> {
                for (PeriodicEntryPojo pojo : toDelete)
                    newList.remove(pojo);
                emitter.onSuccess(newList);
            }).map(periodicEntryPojos -> DiffUtil.calculateDiff(
                    new DiffCallback<>(currentList, periodicEntryPojos), false))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(diffResult -> {
                        currentList.clear();
                        currentList.addAll(newList);
//                        notifyDataSetChanged();
                        diffResult.dispatchUpdatesTo(CashBoxPeriodicRecyclerAdapter.this);

//                        int pos;
//                        for(int k = 0; k< toDelete.size(); k++) {
//                            pos = diffResult.convertOldPositionToNew(toDelete.remove(k));
//                            if(pos!= DiffUtil.DiffResult.NO_POSITION) {
//                                currentList.remove(pos);
//                                notifyItemRemoved(pos);
//                            }
//                        }
                    }));
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cash_box_periodic_item,
                    parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PeriodicEntryPojo pojo = currentList.get(position);
            PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo = pojo.getWorkInfo();
            holder.rvName.setText(pojo.getCashBoxName());
            holder.rvInfo.setText(workInfo.getInfo());
            holder.rvAmountPeriod.setText(getString(R.string.periodic_amountPeriod,
                    currencyFormat.format(workInfo.getAmount()), workInfo.getRepeatInterval()));
            holder.rvRepetitions.setText(getString(R.string.periodic_repetitionsLeft, workInfo.getRepetitions()));
            int colorRes = workInfo.getAmount() < 0 ? R.color.colorNegativeNumber : R.color.colorPositiveNumber;
            holder.rvAmountPeriod.setTextColor(getColor(colorRes));
        }

        @Override
        public int getItemCount() {
            return currentList.size();
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
            PeriodicEntryPojo removed = currentList.get(position);
            List<PeriodicEntryPojo> list = new ArrayList<>(currentList);
            toDelete.add(removed);
            submitList(new ArrayList<>(currentList));
//            PeriodicEntryPojo deletedEntry = currentList.remove(position);
//            notifyItemRemoved(position);
            Snackbar.make(coordinatorLayout,
                    getString(R.string.snackbarEntriesDeleted, 1), Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, view -> {
//                        currentList.add(position, deletedEntry);
                        toDelete.removeFirst();
                        submitList(list);
                    }).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);

                    LogUtil.debug("Prueba", "Deleted entry");
                    if (event != DISMISS_EVENT_ACTION)
                        viewModel.addDisposable(
//                                viewModel.deletePeriodicEntryWorkInfo(deletedEntry.getWorkInfo())
                                viewModel.deletePeriodicEntryWorkInfo(toDelete.removeFirst().getWorkInfo())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe());
                }
            }).show();
        }

        @Override
        public void onItemSecondaryAction(int position) {
            PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo = currentList.get(position).getWorkInfo();

            AlertDialog dialog = new AlertDialog.Builder(CashBoxPeriodicActivity.this)
                    .setTitle(R.string.periodic_dialog_newPeriodic)
                    .setView(R.layout.periodic_new_entry)
                    .setNegativeButton(R.string.cancelDialog, null)
                    .setPositiveButton(R.string.periodic_dialog_create, null)
                    .create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnShowListener(dialogInterface -> {
                Button positive = ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE);
                TextInputEditText inputInfo = ((AlertDialog) dialogInterface).findViewById(R.id.inputTextInfo);
                TextInputEditText inputAmount = ((AlertDialog) dialogInterface).findViewById(R.id.inputTextAmount);
                TextInputLayout layoutAmount = ((AlertDialog) dialogInterface).findViewById(R.id.inputLayoutAmount);
                TextInputEditText inputPeriod = ((AlertDialog) dialogInterface).findViewById(R.id.reminder_inputTextPeriod);
                TextInputEditText inputRepetitions = ((AlertDialog) dialogInterface).findViewById(R.id.reminder_inputTextRepetitions);
                TextInputLayout layoutRepetitions = ((AlertDialog) dialogInterface).findViewById(R.id.reminder_inputLayoutRepetitions);

                // Not show Date Picker
                MaterialTextView inputDate = ((AlertDialog) dialogInterface).findViewById(R.id.inputDate);
                inputDate.setVisibility(View.GONE);

                //Set to the current values
                inputInfo.setText(workInfo.getInfo());
                inputAmount.setText(String.format(Locale.US, "%.2f", workInfo.getAmount()));
                inputPeriod.setText(String.format(Locale.US, "%d", workInfo.getRepeatInterval()));
                inputRepetitions.setText(String.format(Locale.US, "%d", workInfo.getRepetitions()));

                Util.showKeyboard(CashBoxPeriodicActivity.this, inputAmount);
                positive.setOnClickListener((View v) -> {
                    try {
                        String input = inputAmount.getText().toString().trim();
                        int repetitions = Integer.parseInt(inputRepetitions.getText().toString());
                        if (input.isEmpty()) {
                            layoutAmount.setError(getString(R.string.required));
                            Util.showKeyboard(CashBoxPeriodicActivity.this, inputAmount);
                        } else if (repetitions < 1) {
                            layoutRepetitions.setError("Min. 1");
                            Util.showKeyboard(CashBoxPeriodicActivity.this, inputRepetitions);
                        } else { //Change values and do the DB change
                            try {
                                workInfo.setAmount(Util.parseExpression(inputAmount.getText().toString()));
                                workInfo.setInfo(inputInfo.getText().toString());
                                workInfo.setRepeatInterval(Long.parseLong(inputPeriod.getText().toString()));
                                workInfo.setRepetitions(repetitions);

                                viewModel.addDisposable(viewModel.updatePeriodicEntryWorkInfo(workInfo)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe());
                                dialogInterface.dismiss();
                            } catch (NumberFormatException e) {
                                layoutAmount.setError(getString(R.string.errorMessageAmount));
                                inputAmount.selectAll();
                                Util.showKeyboard(CashBoxPeriodicActivity.this, inputAmount);
                            }
                        }
                    } catch (NumberFormatException e) {
                        layoutRepetitions.setError(getString(R.string.errorMessageAmount));
                        inputRepetitions.selectAll();
                        Util.showKeyboard(CashBoxPeriodicActivity.this, inputRepetitions);
                    }
                });
            });
            dialog.show();

            notifyItemChanged(position);   // since the item is deleted from swipping we have to show it back again
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.periodic_rvName)
            TextView rvName;
            @BindView(R.id.periodic_rvAmountPeriod)
            TextView rvAmountPeriod;
            @BindView(R.id.periodic_rvInfo)
            TextView rvInfo;
            @BindView(R.id.periodic_rvRepetitions)
            TextView rvRepetitions;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
