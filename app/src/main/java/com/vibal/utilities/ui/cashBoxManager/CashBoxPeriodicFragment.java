package com.vibal.utilities.ui.cashBoxManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.vibal.utilities.models.PeriodicEntryPojo;
import com.vibal.utilities.ui.settings.SettingsActivity;
import com.vibal.utilities.ui.swipeController.CashBoxAdapterSwipable;
import com.vibal.utilities.ui.swipeController.CashBoxSwipeController;
import com.vibal.utilities.ui.viewPager.PagerFragment;
import com.vibal.utilities.util.DiffCallback;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.util.MyDialogBuilder;
import com.vibal.utilities.util.Util;
import com.vibal.utilities.viewModels.PeriodicEntryWorkViewModel;

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
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CashBoxPeriodicFragment extends PagerFragment {
    @BindView(R.id.lyCBPeriodic)
    CoordinatorLayout coordinatorLayout;

    private PeriodicEntryWorkViewModel viewModel;
    private CashBoxPeriodicRecyclerAdapter adapter;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @NonNull
    static CashBoxPeriodicFragment newInstance(int pagerPosition) {
        CashBoxPeriodicFragment fragment = new CashBoxPeriodicFragment();
        fragment.setPositionAsArgument(pagerPosition);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fragment has options menu
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cash_box_periodic_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        //Set up the RecyclerView
        RecyclerView rvPeriodicEntry = view.findViewById(R.id.rvCashBoxPeriodic);
        rvPeriodicEntry.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvPeriodicEntry.setLayoutManager(layoutManager);
        rvPeriodicEntry.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));
        adapter = new CashBoxPeriodicRecyclerAdapter();
        rvPeriodicEntry.setAdapter(adapter);
        new ItemTouchHelper(new CashBoxSwipeController(adapter,
                PreferenceManager.getDefaultSharedPreferences(getContext())))
                .attachToRecyclerView(rvPeriodicEntry);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set ViewModel
        viewModel = new ViewModelProvider(this).get(PeriodicEntryWorkViewModel.class);
        viewModel.getPeriodicEntries().observe(getViewLifecycleOwner(), periodicEntryPojos -> {
            LogUtil.debug("Prueba", "New list submitted");
            adapter.submitList(periodicEntryPojos);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (!isOptionsMenuActive())
            return;
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_toolbar_cash_box_periodic, menu);

        // Title Toolbar
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(R.string.title_periodicEntry);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!isOptionsMenuActive())
            return false;

        switch (item.getItemId()) {
//            case android.R.id.home:
//                onBackPressed();
//                return true;
            case R.id.action_periodic_deleteAll:
                deleteAll();
                return true;
            case R.id.action_periodic_settings:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAll() {
        int count = adapter.getItemCount();
        if (count == 0) {
            Toast.makeText(getContext(), "No entries to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        new MyDialogBuilder(requireContext())
                .setTitle(R.string.confirmDeleteAllDialog)
                .setMessage("Are you sure you want to send all entries to the recycle bin?")
                .setPositiveButton(R.string.confirmDeleteDialogConfirm, (dialog, which) ->
                        compositeDisposable.add(viewModel.deleteAllPeriodicEntryWorks()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> {
                                    dialog.dismiss();
                                    Toast.makeText(getContext(),
                                            getString(R.string.snackbarEntriesDeleted, count),
                                            Toast.LENGTH_SHORT)
                                            .show();
                                })))
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
            compositeDisposable.add(Single.create((SingleOnSubscribe<List<PeriodicEntryPojo>>) emitter -> {
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
                        diffResult.dispatchUpdatesTo(CashBoxPeriodicRecyclerAdapter.this);
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
            holder.rvAmountPeriod.setTextColor(getContext().getColor(colorRes));
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
            Snackbar.make(coordinatorLayout,
                    getString(R.string.snackbarEntriesDeleted, 1), Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, view -> {
                        toDelete.removeFirst();
                        submitList(list);
                    }).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);

                    LogUtil.debug("Prueba", "Deleted entry");
                    if (event != DISMISS_EVENT_ACTION)
                        compositeDisposable.add(
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

            new MyDialogBuilder(requireContext())
                    .setTitle(R.string.periodic_dialog_newPeriodic)
                    .setView(R.layout.periodic_new_entry)
                    .setPositiveButton(R.string.periodic_dialog_create, null)
                    .setActions(dialog -> {
                        Button positive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                        TextInputEditText inputInfo = ((AlertDialog) dialog).findViewById(R.id.inputTextInfo);
                        TextInputEditText inputAmount = ((AlertDialog) dialog).findViewById(R.id.inputTextAmount);
                        TextInputLayout layoutAmount = ((AlertDialog) dialog).findViewById(R.id.inputLayoutAmount);
                        TextInputEditText inputPeriod = ((AlertDialog) dialog).findViewById(R.id.reminder_inputTextPeriod);
                        TextInputEditText inputRepetitions = ((AlertDialog) dialog).findViewById(R.id.reminder_inputTextRepetitions);
                        TextInputLayout layoutRepetitions = ((AlertDialog) dialog).findViewById(R.id.reminder_inputLayoutRepetitions);

                        // Not show Date Picker
                        MaterialTextView inputDate = ((AlertDialog) dialog).findViewById(R.id.inputDate);
                        inputDate.setVisibility(View.GONE);

                        //Set to the current values
                        inputInfo.setText(workInfo.getInfo());
                        inputAmount.setText(String.format(Locale.US, "%.2f", workInfo.getAmount()));
                        inputPeriod.setText(String.format(Locale.US, "%d", workInfo.getRepeatInterval()));
                        inputRepetitions.setText(String.format(Locale.US, "%d", workInfo.getRepetitions()));

                        Util.showKeyboard(getContext(), inputAmount);
                        positive.setOnClickListener((View v) -> {
                            try {
                                String input = inputAmount.getText().toString().trim();
                                int repetitions = Integer.parseInt(inputRepetitions.getText().toString());
                                if (input.isEmpty()) {
                                    layoutAmount.setError(getString(R.string.required));
                                    Util.showKeyboard(getContext(), inputAmount);
                                } else if (repetitions < 1) {
                                    layoutRepetitions.setError("Min. 1");
                                    Util.showKeyboard(getContext(), inputRepetitions);
                                } else { //Change values and do the DB change
                                    try {
                                        workInfo.setAmount(Util.parseExpression(inputAmount.getText().toString()));
                                        workInfo.setInfo(inputInfo.getText().toString());
                                        workInfo.setRepeatInterval(Long.parseLong(inputPeriod.getText().toString()));
                                        workInfo.setRepetitions(repetitions);

                                        compositeDisposable.add(viewModel.updatePeriodicEntryWorkInfo(workInfo)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe());
                                        dialog.dismiss();
                                    } catch (NumberFormatException e) {
                                        layoutAmount.setError(getString(R.string.errorMessageAmount));
                                        inputAmount.selectAll();
                                        Util.showKeyboard(getContext(), inputAmount);
                                    }
                                }
                            } catch (NumberFormatException e) {
                                layoutRepetitions.setError(getString(R.string.errorMessageAmount));
                                inputRepetitions.selectAll();
                                Util.showKeyboard(getContext(), inputRepetitions);
                            }
                        });
                    }).show();

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
