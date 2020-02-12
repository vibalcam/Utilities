package com.vibal.utilities.ui.cashBoxManager;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.vibal.utilities.R;
import com.vibal.utilities.backgroundTasks.ReminderReceiver;
import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.viewModels.CashBoxViewModel;
import com.vibal.utilities.ui.settings.SettingsActivity;
import com.vibal.utilities.ui.swipeController.CashBoxAdapterSwipable;
import com.vibal.utilities.ui.swipeController.CashBoxSwipeController;
import com.vibal.utilities.util.DiffCallback;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.util.Util;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CashBoxItemFragment extends Fragment {
    public static final int REMINDER_ID = 1;
    private static final double MAX_SHOW_CASH = 99999999;
    private static final String TAG = "PruebaItemFragment";

    @BindView(R.id.itemCash)
    TextView itemCash;
    @BindView(R.id.rvCashBoxItem)
    RecyclerView rvCashBoxItem;

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private CashBoxItemRecyclerAdapter adapter;
    @NonNull
    private NumberFormat formatCurrency = NumberFormat.getCurrencyInstance();
    private CashBoxViewModel viewModel;
    private ShareActionProvider shareActionProvider;
    private SharedPreferences sharedPrefNot;
    private boolean notificationEnabled = false; //By default, the icon is set to alarm off
    private MenuItem menuItemNotification;

    @NonNull
    static CashBoxItemFragment newInstance() {
        return new CashBoxItemFragment();
    }

    @NonNull
    static AlertDialog getAddEntryDialog(long cashBoxId, @NonNull Context context,
                                         @NonNull CashBoxViewModel viewModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
            MaterialTextView inputDate = ((AlertDialog) dialog1).findViewById(R.id.inputDate);

            // Set up Date Picker
            Util.TextViewDatePickerClickListener calendarListener =
                    new Util.TextViewDatePickerClickListener(context, inputDate, true);
            inputDate.setOnClickListener(calendarListener);

            Util.showKeyboard(context, inputAmount);
            positive.setOnClickListener((View v) -> {
                try {
                    String input = inputAmount.getText().toString().trim();
                    if (input.isEmpty()) {
                        layoutAmount.setError(context.getString(R.string.required));
                        Util.showKeyboard(context, inputAmount);
                    } else {
                        double amount = Util.parseExpression(inputAmount.getText().toString());
                        viewModel.addDisposable(viewModel.addEntry(cashBoxId, new CashBox.Entry(
                                amount, inputInfo.getText().toString(), calendarListener.getCalendar()))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(dialog1::dismiss));
                    }
                } catch (NumberFormatException e) {
                    layoutAmount.setError(context.getString(R.string.errorMessageAmount));
                    inputAmount.selectAll();
                    Util.showKeyboard(context, inputAmount);
                }
            });
        });
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fragment has options menu
//        View viewLand = getActivity().findViewById(R.id.containerItem);
//        LogUtil.debug(TAG,"IsVisible before onCreate: " + isVisible());
//        LogUtil.debug(TAG,"Rest before onCreate: " + (viewLand==null || viewLand.getVisibility()!=View.VISIBLE));
//        LogUtil.debug(TAG,"Has options menu item: " + (isVisible() && (viewLand==null || viewLand.getVisibility()!=View.VISIBLE)));
//        if(isVisible() && (viewLand==null || viewLand.getVisibility()!=View.VISIBLE)) {
        setHasOptionsMenu(true);
        //Set up SharedPreferences for notifications
        sharedPrefNot = getContext().getSharedPreferences(ReminderReceiver.REMINDER_PREFERENCE,
                Context.MODE_PRIVATE);
//        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cash_box_item_fragment, container, false);
        ButterKnife.bind(this, view);

        //Set up RecyclerView
//        rvCashBoxItem.setNestedScrollingEnabled(true);
        rvCashBoxItem.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvCashBoxItem.setLayoutManager(layoutManager);
        adapter = new CashBoxItemRecyclerAdapter();
        rvCashBoxItem.setAdapter(adapter);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()));
//        CashBoxSwipeController swipeController = new CashBoxSwipeController(adapter,
//                preferences.getBoolean("swipeLeftDelete", true));
        CashBoxSwipeController swipeController = new CashBoxSwipeController(adapter, preferences);
        (new ItemTouchHelper(swipeController)).attachToRecyclerView(rvCashBoxItem);
        rvCashBoxItem.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        //Register listener for settings change
//        preferenceChangeListener = (sharedPreferences, s) -> {
//            if (s.equals("swipeLeftDelete"))
//                swipeController.setSwipeLeftDelete(sharedPreferences.getBoolean("swipeLeftDelete", true));
//        };
//        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        LogUtil.debug(TAG, "on create:");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) Objects.requireNonNull(getActivity());

        //Set Toolbar as ActionBar
        ActionBar actionBar = activity.getSupportActionBar();

        // Initialize data
        viewModel = new ViewModelProvider(Objects.requireNonNull(activity)).get(CashBoxViewModel.class);
        viewModel.getCurrentCashBox().observe(getViewLifecycleOwner(), cashBox -> {
            LogUtil.debug("PruebaItemFragment", "Is Visible onSubmitList: " + isVisible());

            // Set Title
            if (actionBar != null && isVisible())
                actionBar.setTitle(cashBox.getName());

            // Update data
            adapter.submitList(cashBox.getEntries());
            updateCash(cashBox.getCash());

            // Update ShareIntent
            if (shareActionProvider != null)
                shareActionProvider.setShareIntent(Util.getShareIntent(cashBox));
        });
    }

    private void updateCash(double cash) {
        if (Math.abs(cash) > MAX_SHOW_CASH)
            itemCash.setText(R.string.outOfRange);
        else {
            itemCash.setText(formatCurrency.format(cash));
            if (cash < 0)
                itemCash.setTextColor(Objects.requireNonNull(getActivity())
                        .getColor(R.color.colorNegativeNumber));
            else
                itemCash.setTextColor(Objects.requireNonNull(getActivity())
                        .getColor(R.color.colorPositiveNumber));
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Change options menu if not in landscape mode
        View viewLand = getActivity().findViewById(R.id.containerItem);
//        LogUtil.debug(TAG,"IsVisible onCreateOptionsMenu: " + isVisible());
//        LogUtil.debug(TAG,"Rest onCreateOptionsMenu: " + (viewLand==null || viewLand.getVisibility()!=View.VISIBLE));
//        LogUtil.debug(TAG,"Has options onCreateOptionsMenu: " + (isVisible() && (viewLand==null || viewLand.getVisibility()!=View.VISIBLE)));
        if (isVisible() && (viewLand == null || viewLand.getVisibility() != View.VISIBLE)) {
            menu.clear();
            inflater.inflate(R.menu.menu_toolbar_cash_box_item, menu);
            // Prepare alarm icon
            menuItemNotification = menu.findItem(R.id.action_item_reminder);
            setIconNotification(sharedPrefNot.contains(Long.toString(viewModel.getCurrentCashBoxId())));
            // Set up ShareActionProvider
            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_item_share));
        }
    }

    private void setIconNotification(boolean enabled) {
        if (enabled == notificationEnabled)
            return;
        if (enabled) {
            menuItemNotification.setIcon(R.drawable.ic_alarm_on_white_24dp);
            menuItemNotification.setTitle(R.string.menu_item_reminderOn);
        } else {
            menuItemNotification.setIcon(R.drawable.ic_alarm_off_white_24dp);
            menuItemNotification.setTitle(R.string.menu_item_reminderOff);
        }
        notificationEnabled = enabled;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
//            case android.R.id.home:
////                getActivity().onBackPressed();
//                getParentFragmentManager().popBackStack();
//                return true;
            case R.id.action_item_deleteAll:
                deleteAll();
                return true;
            case R.id.action_item_reminder:
                showReminderDialog();
                return true;
            case R.id.action_item_settings:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showReminderDialog() {
        //Check if alarm has been already set
        long timeInMillis = sharedPrefNot.getLong(Long.toString(viewModel.getCurrentCashBoxId()), 0);
        if (timeInMillis == 0) { //Set reminder dialog
            //Choose the date and time for the reminder
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(getContext(),
                    (datePicker, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        Calendar current = Calendar.getInstance();
                        current.add(Calendar.DAY_OF_MONTH, -1);
                        LogUtil.debug(TAG, "Current: " + current.toString());
                        if (calendar.before(current)) {
                            Toast.makeText(getContext(), R.string.reminder_dialog_invalid_date, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        new TimePickerDialog(getContext(), (timePicker, hourOfDay, minute) -> {
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            calendar.set(Calendar.MINUTE, minute);
                            calendar.set(Calendar.SECOND, 0);
                            Calendar current2 = Calendar.getInstance();
                            current2.add(Calendar.MINUTE, -1);

                            if (calendar.before(current2))
                                Toast.makeText(getContext(), R.string.reminder_dialog_invalid_date, Toast.LENGTH_SHORT).show();
                            else
                                scheduleReminder(calendar);
                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                                android.text.format.DateFormat.is24HourFormat(getContext()))
                                .show();
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH))
                    .show();
        } else //Cancel reminder dialog
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.reminder_dialog_cancel_title)
                    .setMessage(getString(R.string.reminder_dialog_cancel_message,
                            DateFormat.getDateTimeInstance().format(new Date(timeInMillis))))
                    .setNegativeButton(R.string.reminder_dialog_cancel_keep, null)
                    .setPositiveButton(R.string.reminder_dialog_cancel_cancel,
                            (dialogInterface, i) -> cancelReminder())
                    .show();
    }

    private void scheduleReminder(@NonNull Calendar calendar) {
        long cashBoxId = viewModel.getCurrentCashBoxId();
        long timeInMillis = calendar.getTimeInMillis();
        //Enable boot receiver
        ReminderReceiver.setBootReceiverEnabled(getContext(), true);
        //Add reminder to Notification SharedPreferences
        sharedPrefNot.edit().putLong(Long.toString(cashBoxId), timeInMillis).apply();
        setIconNotification(true);

        //Set up the alarm manager for the notification
        ReminderReceiver.setAlarm((AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE),
                getContext(), cashBoxId, timeInMillis);
        Toast.makeText(getContext(), "New Reminder Set", Toast.LENGTH_SHORT).show();
    }

    private void cancelReminder() {
        //Delete reminder from Notifications SharedPreference
        long cashBoxId = viewModel.getCurrentCashBoxId();
        sharedPrefNot.edit().remove(Long.toString(cashBoxId)).apply();
        setIconNotification(false);

        //Cancel alarm
        ReminderReceiver.cancelAlarm((AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE),
                getContext(),cashBoxId);
        Toast.makeText(getContext(), "Reminder Cancelled", Toast.LENGTH_SHORT).show();

        //Disable boot receiver if there are no other alarms
        if (sharedPrefNot.getAll().isEmpty())
            ReminderReceiver.setBootReceiverEnabled(getContext(), false);
    }

    @OnClick(R.id.fabCBItem)
    void onFabClicked() {
        getAddEntryDialog(viewModel.getCurrentCashBoxId(), getContext(), viewModel)
                .show();
    }

    private void deleteAll() {
        if (adapter.getItemCount() == 0) {
            Toast.makeText(getContext(), R.string.noEntriesDelete, Toast.LENGTH_SHORT).show();
            return;
        }

        List<CashBox.Entry> deletedEntries = adapter.currentList;
        viewModel.addDisposable(viewModel.deleteAllEntriesFromCurrentCashBox()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer ->
                        Snackbar.make(rvCashBoxItem,
                                getString(R.string.snackbarEntriesDeleted, integer),
                                Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, v ->
                                        viewModel.addDisposable(viewModel.addAllEntriesToCurrentCashBox(deletedEntries)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe()))
                                .show()));
    }

    public class CashBoxItemRecyclerAdapter extends RecyclerView.Adapter<CashBoxItemRecyclerAdapter.ViewHolder> implements CashBoxAdapterSwipable {
        private static final boolean DRAG_ENABLED = false;
        private static final boolean SWIPE_ENABLED = true;

        @NonNull
        private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        @NonNull
        private List<CashBox.Entry> currentList = new ArrayList<>();

        void submitList(@NonNull List<CashBox.Entry> newList) {
            viewModel.addDisposable(Single.just(DiffUtil.calculateDiff(
                    new DiffCallback<>(currentList, newList), false))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(diffResult -> {
                        LogUtil.debug(TAG, "DiffResult calculated");
                        currentList.clear();
                        currentList.addAll(newList);
                        diffResult.dispatchUpdatesTo(CashBoxItemRecyclerAdapter.this);
                    }));
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cash_box_item_item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int index) {
            CashBox.Entry entry = currentList.get(index);

            // Amount
            viewHolder.rvItemAmount.setText(formatCurrency.format(entry.getAmount()));
            if (entry.getAmount() < 0)
                viewHolder.rvItemAmount.setTextColor(getActivity().getColor(R.color.colorNegativeNumber));
            else
                viewHolder.rvItemAmount.setTextColor(getActivity().getColor(R.color.colorPositiveNumber));
            // CashBoxInfo
            viewHolder.rvItemInfo.setText(entry.printInfo());
//            if (entry.getInfo().isEmpty())
//                viewHolder.rvItemInfo.setText(R.string.noInfoEntered);
//            else
//                viewHolder.rvItemInfo.setText(entry.getInfo());
            // Date
            viewHolder.rvItemDate.setText(dateFormat.format(entry.getDate().getTime()));
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
            CashBox.Entry entry = currentList.get(position);

            if (entry.getGroupId() == CashBox.Entry.NO_GROUP) {
                deleteEntry(entry);
                return;
            }

            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.titleGroupEntryDelete)
                    .setMessage(R.string.messageGroupDelete)
                    .setOnCancelListener(dialogInterface -> notifyItemChanged(position))   // since the item is deleted from swipping we have to show it back again)
                    .setNegativeButton(R.string.groupEntryIndividual,
                            (dialogInterface, i) -> deleteEntry(entry))
                    .setPositiveButton(R.string.groupEntryAll, (dialogInterface, i) ->
                            viewModel.addDisposable(viewModel.getGroupEntries(entry)
                                    .flatMap(entries -> viewModel.deleteGroupEntries(entry)
                                            .map(integer -> entries))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(entryList -> Snackbar.make(rvCashBoxItem,
                                            getString(R.string.snackbarEntriesDeleted, 1),
                                            Snackbar.LENGTH_LONG)
                                            .setAction(R.string.undo, (View v) ->
                                                    viewModel.addDisposable(viewModel.addAllEntries(entryList)
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe()))
                                            .show())))
                    .create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        private void deleteEntry(CashBox.Entry entry) {
            viewModel.addDisposable(viewModel.deleteEntry(entry)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Snackbar.make(rvCashBoxItem,
                            getString(R.string.snackbarEntriesDeleted, 1),
                            Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, (View v) ->
                                    viewModel.addDisposable(viewModel.addEntryToCurrentCashBox(entry)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe()))
                            .show()));
        }

        @Override
        public void onItemSecondaryAction(int position) {
            LogUtil.debug(TAG, "onItemSecondaryAction: ID del entry: " + currentList.get(position).getId()
                    + "\nID del cashBox: " + currentList.get(position).getCashBoxId() + "\nID group: " +
                    currentList.get(position).getGroupId());

            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.modifyEntry)
                    .setView(R.layout.cash_box_item_entry_input)
                    .setNegativeButton(R.string.cancelDialog, null)
                    .setPositiveButton(R.string.confirm, null)
                    .create();
            dialog.setCanceledOnTouchOutside(false);

            dialog.setOnShowListener((DialogInterface dialogInterface) -> {
                Button positive = ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE);
                TextInputEditText inputInfo = ((AlertDialog) dialogInterface).findViewById(R.id.inputTextInfo);
                TextInputEditText inputAmount = ((AlertDialog) dialogInterface).findViewById(R.id.inputTextAmount);
                TextInputLayout layoutAmount = ((AlertDialog) dialogInterface).findViewById(R.id.inputLayoutAmount);
                MaterialTextView inputDate = ((AlertDialog) dialogInterface).findViewById(R.id.inputDate);
                CashBox.Entry modifiedEntry = currentList.get(position);

                // Set up Date Picker
                Util.TextViewDatePickerClickListener calendarListener =
                        new Util.TextViewDatePickerClickListener(getContext(), inputDate,
                                modifiedEntry.getDate(),true);
                inputDate.setOnClickListener(calendarListener);

                inputInfo.setText(modifiedEntry.getInfo());
                inputAmount.setText(String.format(Locale.US, "%.2f", modifiedEntry.getAmount()));
                // Show keyboard and select the whole text
                inputAmount.selectAll();
                Util.showKeyboard(getContext(), inputAmount);

                positive.setOnClickListener((View v) -> {
                    try {
                        String input = inputAmount.getText().toString().trim();

                        if (input.isEmpty()) {
                            layoutAmount.setError(getString(R.string.required));
                            inputAmount.setText("");
                            Util.showKeyboard(getContext(), inputAmount);
                        } else {
                            String info = inputInfo.getText().toString().trim();
                            double amount = Util.parseExpression(input);

                            if (modifiedEntry.getGroupId() == CashBox.Entry.NO_GROUP)
                                modifyEntry(modifiedEntry, dialogInterface, amount, info,
                                        calendarListener.getCalendar());
                            else {
                                AlertDialog dialogGroup = new AlertDialog.Builder(getContext())
                                        .setTitle(R.string.titleGroupEntryModify)
                                        .setMessage(R.string.messageGroupModify)
                                        .setNegativeButton(R.string.groupEntryIndividual,
                                                (dialogInterfaceGroup, i) -> modifyEntry(modifiedEntry,
                                                        dialogInterface, amount, info,
                                                        calendarListener.getCalendar()))
                                        .setPositiveButton(R.string.groupEntryAll, (dialogInterfaceGroup, i) ->
                                                viewModel.addDisposable(viewModel.getGroupEntries(modifiedEntry)
                                                        .flatMap(entries -> viewModel.modifyGroupEntry(
                                                                modifiedEntry, amount, info,
                                                                calendarListener.getCalendar())
                                                                .toSingleDefault(entries))
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(entryList -> {
                                                            dialogInterface.dismiss();
                                                            Snackbar.make(rvCashBoxItem,
                                                                    getString(R.string.snackbarEntriesDeleted, 1),
                                                                    Snackbar.LENGTH_LONG)
                                                                    .setAction(R.string.undo, (View v2) -> {
                                                                        Completable completable = Completable.complete();
                                                                        for (CashBox.Entry k : entryList)
                                                                            completable = completable.andThen(
                                                                                    viewModel.updateEntry(k));
                                                                        viewModel.addDisposable(completable
                                                                                .subscribeOn(Schedulers.io())
                                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                                .subscribe());
                                                                    }).show();
                                                        })))
                                        .create();
                                dialogGroup.setCanceledOnTouchOutside(false);
                                dialogGroup.show();
                            }
                        }
                    } catch (NumberFormatException e) {
                        layoutAmount.setError(getString(R.string.errorMessageAmount));
                        inputAmount.selectAll();
                        Util.showKeyboard(getContext(), inputAmount);
                    }
                });
            });
            dialog.show();

            notifyItemChanged(position);   // since the item is deleted from swipping we have to show it back again
        }

        private void modifyEntry(CashBox.Entry modifiedEntry, DialogInterface dialogInterface,
                                 double amount, String info, Calendar date) {
            viewModel.addDisposable(viewModel.modifyEntry(modifiedEntry, amount, info, date)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        dialogInterface.dismiss();
                        Snackbar.make(rvCashBoxItem,
                                R.string.snackbarEntryModified, Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, (View v1) ->
                                        viewModel.addDisposable(viewModel.updateEntry(modifiedEntry)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe()))
                                .show();
                    }));
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.rvItemDate)
            TextView rvItemDate;
            @BindView(R.id.rvItemAmount)
            TextView rvItemAmount;
            @BindView(R.id.rvItemInfo)
            TextView rvItemInfo;

            ViewHolder(@NonNull View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }
}
