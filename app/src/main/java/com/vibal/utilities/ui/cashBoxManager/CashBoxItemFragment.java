package com.vibal.utilities.ui.cashBoxManager;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.preference.PreferenceManager;
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
import com.vibal.utilities.modelsNew.Entry;
import com.vibal.utilities.ui.settings.SettingsActivity;
import com.vibal.utilities.ui.swipeController.CashBoxAdapterSwipable;
import com.vibal.utilities.ui.swipeController.CashBoxSwipeController;
import com.vibal.utilities.ui.viewPager.PagerFragment;
import com.vibal.utilities.util.DiffCallback;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.util.MyDialogBuilder;
import com.vibal.utilities.util.Util;
import com.vibal.utilities.viewModels.CashBoxViewModel;
import com.vibal.utilities.workaround.LinearLayoutManagerWrapper;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public abstract class CashBoxItemFragment extends PagerFragment {
    public static final int REMINDER_ID = 1;
    private static final double MAX_SHOW_CASH = 99999999;
    private static final String TAG = "PruebaItemFragment";
    @NonNull
    private final NumberFormat formatCurrency = NumberFormat.getCurrencyInstance();
    protected CompositeDisposable compositeDisposable = new CompositeDisposable();
    @BindView(R.id.itemCash)
    TextView itemCash;
    @BindView(R.id.rvCashBoxItem)
    RecyclerView rvCashBoxItem;
    private CashBoxItemRecyclerAdapter adapter;
    private ShareActionProvider shareActionProvider;
    private SharedPreferences sharedPrefNot;
    private boolean notificationEnabled = false; //By default, the icon is set to alarm off
    private MenuItem menuItemNotification;

    @NonNull
    static AlertDialog getAddEntryDialog(long cashBoxId, @NonNull Context context,
                                         @NonNull CashBoxViewModel viewModel,
                                         CompositeDisposable compositeDisposable) {
        return new MyDialogBuilder(context)
                .setTitle(R.string.newEntry)
                .setView(R.layout.cash_box_item_entry_input)
                .setPositiveButton(R.string.addEntryDialog, null)
                .setActions(dialog -> {
                    Button positive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    TextInputEditText inputInfo = ((AlertDialog) dialog).findViewById(R.id.inputTextInfo);
                    TextInputEditText inputAmount = ((AlertDialog) dialog).findViewById(R.id.inputTextAmount);
                    TextInputLayout layoutAmount = ((AlertDialog) dialog).findViewById(R.id.inputLayoutAmount);
                    MaterialTextView inputDate = ((AlertDialog) dialog).findViewById(R.id.inputDate);

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
                                compositeDisposable.add(viewModel.addEntry(cashBoxId, new Entry(
                                        amount, inputInfo.getText().toString(), calendarListener.getCalendar()))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(dialog::dismiss));
                            }
                        } catch (NumberFormatException e) {
                            layoutAmount.setError(context.getString(R.string.errorMessageAmount));
                            inputAmount.selectAll();
                            Util.showKeyboard(context, inputAmount);
                        }
                    });
                }).create();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fragment has options menu
        setHasOptionsMenu(true);
//        View viewLand = getActivity().findViewById(R.id.containerItem);
//        LogUtil.debug(TAG,"IsVisible before onCreate: " + isVisible());
//        LogUtil.debug(TAG,"Rest before onCreate: " + (viewLand==null || viewLand.getVisibility()!=View.VISIBLE));
//        LogUtil.debug(TAG,"Has options menu item: " + (isVisible() && (viewLand==null || viewLand.getVisibility()!=View.VISIBLE)));
//        if(isVisible() && (viewLand==null || viewLand.getVisibility()!=View.VISIBLE)) {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        LogUtil.debug(TAG, "on create:");
        return inflater.inflate(R.layout.cash_box_item_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        //Set up RecyclerView
        rvCashBoxItem.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManagerWrapper(getContext()); // Workaround for recycler error
        rvCashBoxItem.setLayoutManager(layoutManager);
        adapter = new CashBoxItemRecyclerAdapter();
        rvCashBoxItem.setAdapter(adapter);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        CashBoxSwipeController swipeController = new CashBoxSwipeController(adapter, preferences);
        (new ItemTouchHelper(swipeController)).attachToRecyclerView(rvCashBoxItem);
        rvCashBoxItem.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize data
        initializeViewModel().getCurrentCashBox().observe(getViewLifecycleOwner(), cashBox -> {
            LogUtil.debug("PruebaItemFragment", "Is Visible onSubmitList: " + isVisible());

            // Set Title
            ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
            if (actionBar != null && isVisible())
                actionBar.setTitle(cashBox.getName());

            // Update data
            Currency cashBoxCurrency = cashBox.getInfoWithCash().getCashBoxInfo().getCurrency();
            if (!Objects.equals(formatCurrency.getCurrency(), cashBoxCurrency)) {
                formatCurrency.setCurrency(cashBox.getInfoWithCash().getCashBoxInfo().getCurrency());
                adapter.notifyItemRangeChanged(0, adapter.getItemCount());
            }
            adapter.submitList(cashBox.getEntries());
            updateCash(cashBox.getCash());

            // Update ShareIntent
            if (shareActionProvider != null)
                shareActionProvider.setShareIntent(Util.getShareIntent(cashBox));
        });
    }

    @NonNull
    protected abstract CashBoxViewModel getViewModel();

    @NonNull
    protected abstract CashBoxViewModel initializeViewModel();

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    private void updateCash(double cash) {
        if (Math.abs(cash) > MAX_SHOW_CASH)
            itemCash.setText(R.string.outOfRange);
        else {
            itemCash.setText(formatCurrency.format(cash));
            if (cash < 0)
                itemCash.setTextColor(requireActivity()
                        .getColor(R.color.colorNegativeNumber));
            else
                itemCash.setTextColor(requireActivity()
                        .getColor(R.color.colorPositiveNumber));
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (!isOptionsMenuActive())
            return;
        super.onCreateOptionsMenu(menu, inflater);

        // Set Title
//        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
//        if (actionBar != null)
//            actionBar.setTitle(getViewModel().getCurrentCashBox().getValue().getName());

        // Change options menu if not in landscape mode
        View viewLand = requireView().findViewById(R.id.containerItem);
//        LogUtil.debug(TAG,"IsVisible onCreateOptionsMenu: " + isVisible());
//        LogUtil.debug(TAG,"Rest onCreateOptionsMenu: " + (viewLand==null || viewLand.getVisibility()!=View.VISIBLE));
//        LogUtil.debug(TAG,"Has options onCreateOptionsMenu: " + (isVisible() && (viewLand==null || viewLand.getVisibility()!=View.VISIBLE)));
        if (isVisible() && (viewLand == null || viewLand.getVisibility() != View.VISIBLE)) {
            menu.clear();
            inflater.inflate(getMenuRes(), menu);
            // Prepare alarm icon
            menuItemNotification = menu.findItem(R.id.action_item_reminder);
            sharedPrefNot = requireContext().getSharedPreferences(ReminderReceiver.REMINDER_PREFERENCE,
                    Context.MODE_PRIVATE);
            setIconNotification(sharedPrefNot.contains(Long.toString(getViewModel().getCurrentCashBoxId())));
            // Set up ShareActionProvider
            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_item_share));
        }
    }

    @MenuRes
    protected int getMenuRes() {
        return R.menu.menu_toolbar_cash_box_item;
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
            case R.id.action_item_currency:
                showCurrencyChooser();
                return true;
            case R.id.action_item_settings:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Order them and show symbol when possible
    private void showCurrencyChooser() {
        // Adapter to show currencies
        ArrayAdapter<Currency> arrayAdapter = new ArrayAdapter<Currency>(requireContext(),
                R.layout.dialog_currency_chooser, new ArrayList<>(Currency.getAvailableCurrencies())) {
            @NonNull
            @Override
            public Currency getItem(int position) {
                return Objects.requireNonNull(super.getItem(position));
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                Currency currency = Objects.requireNonNull(getItem(position));
                view.setText(getString(R.string.display_currency_list, currency.getDisplayName(),
                        currency.getSymbol()));
                return view;
            }
        };

        // Sort the adapter
        arrayAdapter.sort((o1, o2) -> {
            // If both currencies have symbols defined or do not have them, it returns the
            // compareTo method otherwise, it returns as smaller the one with the symbol defined
            if ((o1.getSymbol().equals(o1.getCurrencyCode())) == (o2.getSymbol().equals(o2.getCurrencyCode())))
                return o1.getCurrencyCode().compareTo(o2.getCurrencyCode());
            else // returns as bigger the one with a symbol defined
                return o1.getSymbol().equals(o1.getCurrencyCode()) ? 1 : -1;
        });

//        new AlertDialog.Builder(requireContext())
        new MyDialogBuilder(requireContext())
                .setTitle(R.string.currency_dialog_title)
                .setPositiveButton(null, null)
                .setSingleChoiceItems(arrayAdapter, -1, (dialog, which) -> {
                    dialog.dismiss();
                    LogUtil.debug(TAG, arrayAdapter.getItem(which).getCurrencyCode());
                    compositeDisposable.add(
                            getViewModel().setCurrentCashBoxCurrency(arrayAdapter.getItem(which))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe());
                }).show();
    }

    private void showReminderDialog() {
        //Check if alarm has been already set
        long timeInMillis = sharedPrefNot.getLong(Long.toString(getViewModel().getCurrentCashBoxId()), 0);
        if (timeInMillis == 0) { //Set reminder dialog
            //Choose the date and time for the reminder
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(requireContext(),
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
            new MyDialogBuilder(requireContext())
                    .setTitle(R.string.reminder_dialog_cancel_title)
                    .setMessage(getString(R.string.reminder_dialog_cancel_message,
                            DateFormat.getDateTimeInstance().format(new Date(timeInMillis))))
                    .setNegativeButton(R.string.reminder_dialog_cancel_keep, null)
                    .setPositiveButton(R.string.reminder_dialog_cancel_cancel, (dialogInterface, i) -> cancelReminder())
                    .show();
    }

    private void scheduleReminder(@NonNull Calendar calendar) {
        long cashBoxId = getViewModel().getCurrentCashBoxId();
        long timeInMillis = calendar.getTimeInMillis();
        //Enable boot receiver
        ReminderReceiver.setBootReceiverEnabled(requireContext(), true);
        //Add reminder to Notification SharedPreferences
        sharedPrefNot.edit().putLong(Long.toString(cashBoxId), timeInMillis).apply();
        setIconNotification(true);

        //Set up the alarm manager for the notification
        ReminderReceiver.setAlarm((AlarmManager) Objects.requireNonNull(requireContext().getSystemService(Context.ALARM_SERVICE)),
                getContext(), cashBoxId, timeInMillis);
        Toast.makeText(getContext(), "New Reminder Set", Toast.LENGTH_SHORT).show();
    }

    private void cancelReminder() {
        //Delete reminder from Notifications SharedPreference
        long cashBoxId = getViewModel().getCurrentCashBoxId();
        sharedPrefNot.edit().remove(Long.toString(cashBoxId)).apply();
        setIconNotification(false);

        //Cancel alarm
        ReminderReceiver.cancelAlarm((AlarmManager) Objects.requireNonNull(requireContext().getSystemService(Context.ALARM_SERVICE)),
                getContext(), cashBoxId);
        Toast.makeText(getContext(), "Reminder Cancelled", Toast.LENGTH_SHORT).show();

        //Disable boot receiver if there are no other alarms
        if (sharedPrefNot.getAll().isEmpty())
            ReminderReceiver.setBootReceiverEnabled(requireContext(), false);
    }

    @OnClick(R.id.fabCBItem)
    void onFabClicked() {
        getAddEntryDialog(getViewModel().getCurrentCashBoxId(), requireContext(), getViewModel(), compositeDisposable)
                .show();
    }

    private void deleteAll() {
        if (adapter.getItemCount() == 0) {
            Toast.makeText(getContext(), R.string.noEntriesDelete, Toast.LENGTH_SHORT).show();
            return;
        }

        List<Entry> deletedEntries = new ArrayList<>(adapter.currentList);
        compositeDisposable.add(getViewModel().deleteAllEntriesFromCurrentCashBox()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer ->
                        Snackbar.make(rvCashBoxItem,
                                getString(R.string.snackbarEntriesDeleted, integer),
                                Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, v ->
                                        compositeDisposable.add(getViewModel().addAllEntriesToCurrentCashBox(deletedEntries)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe()))
                                .show()));
    }

    public class CashBoxItemRecyclerAdapter extends RecyclerView.Adapter<CashBoxItemRecyclerAdapter.ViewHolder> implements CashBoxAdapterSwipable {
        private static final boolean DRAG_ENABLED = false;
        private static final boolean SWIPE_ENABLED = true;
        @NonNull
        private final ConcurrentLinkedQueue<Single<DiffCallback.DiffResultWithList<Entry>>> pendingSubmitted = new ConcurrentLinkedQueue<>();
        @NonNull
        private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        @NonNull
        private List<Entry> currentList = new ArrayList<>();

        /**
         * Submit a new list of elements for the adapter to show.
         * All changes in adapter list must go through submitList.
         *
         * @param newList New list to be submitted
         */
        void submitList(@NonNull List<Entry> newList) {
            pendingSubmitted.add(Single.create(emitter ->
                    emitter.onSuccess(DiffCallback.DiffResultWithList.calculateDiff(
                            currentList, newList, false))));
            // If pending is empty, add and start this work
            // if there is already another work in progress, just add to pending
            if (pendingSubmitted.size() == 1)
                runPendingSubmitted();

//            compositeDisposable.add(Single.just(DiffUtil.calculateDiff(
//                    new DiffCallback<>(currentList, newList), false))
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(diffResult -> {
//                        LogUtil.debug(TAG, "DiffResult calculated");
//                        currentList.clear();
//                        currentList.addAll(newList);
//                        diffResult.dispatchUpdatesTo(CashBoxItemRecyclerAdapter.this);
//                    }));
        }

        private void runPendingSubmitted() {
            Single<DiffCallback.DiffResultWithList<Entry>> single = pendingSubmitted.peek();
            if (single != null) {
                compositeDisposable.add(single
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(diffResultWithList -> {
                            LogUtil.debug(TAG, "DiffResult calculated");
                            // Show diff changes
                            currentList.clear();
                            currentList.addAll(diffResultWithList.getNewList());
                            diffResultWithList.getDiffResult()
                                    .dispatchUpdatesTo(CashBoxItemRecyclerAdapter.this);

                            // Delete this work from pending
                            pendingSubmitted.poll();
                            // Run next pending
                            runPendingSubmitted();
                        }));
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cash_box_item_item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int index) {
            Entry entry = currentList.get(index);

            // Amount
            viewHolder.rvItemAmount.setText(formatCurrency.format(entry.getAmount()));
            if (entry.getAmount() < 0)
                viewHolder.rvItemAmount.setTextColor(requireContext().getColor(R.color.colorNegativeNumber));
            else
                viewHolder.rvItemAmount.setTextColor(requireContext().getColor(R.color.colorPositiveNumber));
            // CashBoxInfo
            viewHolder.rvItemInfo.setText(entry.printInfo());
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
            Entry entry = currentList.get(position);

            if (entry.getGroupId() == Entry.NO_GROUP) {
                deleteEntry(entry);
                return;
            }

            new MyDialogBuilder(requireContext())
                    .setTitle(R.string.titleGroupEntryDelete)
                    .setMessage(R.string.messageGroupDelete)
                    .setOnCancelListener(dialogInterface -> notifyItemChanged(position))   // since the item is deleted from swipping we have to show it back again)
                    .setNegativeButton(R.string.groupEntryIndividual,
                            (dialogInterface, i) -> deleteEntry(entry))
                    .setPositiveButton(R.string.groupEntryAll, (dialogInterface, i) ->
                            compositeDisposable.add(getViewModel().getGroupEntries(entry)
                                    .flatMap(entries -> getViewModel().deleteGroupEntries(entry)
                                            .map(integer -> entries))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(entryList -> Snackbar.make(rvCashBoxItem,
                                            getString(R.string.snackbarEntriesDeleted, 1),
                                            Snackbar.LENGTH_LONG)
                                            .setAction(R.string.undo, (View v) ->
                                                    compositeDisposable.add(getViewModel().addAllEntries(entryList)
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe()))
                                            .show())))
                    .show();
        }

        private void deleteEntry(Entry entry) {
            compositeDisposable.add(getViewModel().deleteEntry(entry)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Snackbar.make(rvCashBoxItem,
                            getString(R.string.snackbarEntriesDeleted, 1),
                            Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, (View v) ->
                                    compositeDisposable.add(getViewModel().addEntryToCurrentCashBox(entry)
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

            new MyDialogBuilder(requireContext())
                    .setTitle(R.string.modifyEntry)
                    .setView(R.layout.cash_box_item_entry_input)
                    .setPositiveButton(R.string.confirm, null)
                    .setActions((DialogInterface dialogInterface) -> {
                        Button positive = ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE);
                        TextInputEditText inputInfo = ((AlertDialog) dialogInterface).findViewById(R.id.inputTextInfo);
                        TextInputEditText inputAmount = ((AlertDialog) dialogInterface).findViewById(R.id.inputTextAmount);
                        TextInputLayout layoutAmount = ((AlertDialog) dialogInterface).findViewById(R.id.inputLayoutAmount);
                        MaterialTextView inputDate = ((AlertDialog) dialogInterface).findViewById(R.id.inputDate);
                        Entry modifiedEntry = currentList.get(position);

                        // Set up Date Picker
                        Util.TextViewDatePickerClickListener calendarListener =
                                new Util.TextViewDatePickerClickListener(requireContext(), inputDate,
                                        modifiedEntry.getDate(), true);
                        inputDate.setOnClickListener(calendarListener);

                        inputInfo.setText(modifiedEntry.getInfo());
                        inputAmount.setText(String.format(Locale.US, "%.2f", modifiedEntry.getAmount()));
                        // Show keyboard and select the whole text
                        inputAmount.selectAll();
                        Util.showKeyboard(requireContext(), inputAmount);

                        positive.setOnClickListener((View v) -> {
                            try {
                                String input = inputAmount.getText().toString().trim();

                                if (input.isEmpty()) {
                                    layoutAmount.setError(getString(R.string.required));
                                    inputAmount.setText("");
                                    Util.showKeyboard(requireContext(), inputAmount);
                                } else {
                                    String info = inputInfo.getText().toString().trim();
                                    double amount = Util.parseExpression(input);

                                    // Not a group entry
                                    if (modifiedEntry.getGroupId() == Entry.NO_GROUP)
                                        modifyEntry(modifiedEntry, dialogInterface, amount, info,
                                                calendarListener.getCalendar());
                                    else { // Group entry
                                        new MyDialogBuilder(requireContext())
                                                .setTitle(R.string.titleGroupEntryModify)
                                                .setMessage(R.string.messageGroupModify)
                                                // Modify only this entry
                                                .setNegativeButton(R.string.groupEntryIndividual,
                                                        (dialogInterfaceGroup, i) -> modifyEntry(modifiedEntry,
                                                                dialogInterface, amount, info,
                                                                calendarListener.getCalendar()))
                                                // Modify all entries of the group
                                                .setPositiveButton(R.string.groupEntryAll, (dialogInterfaceGroup, i) ->
                                                        compositeDisposable.add(getViewModel().getGroupEntries(modifiedEntry)
                                                                .flatMap(entries -> getViewModel().modifyGroupEntry(
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
                                                                                for (Entry k : entryList)
                                                                                    completable = completable.andThen(
                                                                                            getViewModel().updateEntry(k));
                                                                                compositeDisposable.add(completable
                                                                                        .subscribeOn(Schedulers.io())
                                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                                        .subscribe());
                                                                            }).show();
                                                                })))
                                                .show();
                                    }
                                }
                            } catch (NumberFormatException e) {
                                layoutAmount.setError(getString(R.string.errorMessageAmount));
                                inputAmount.selectAll();
                                Util.showKeyboard(requireContext(), inputAmount);
                            }
                        });
                    }).show();

            notifyItemChanged(position);   // since the item is deleted from swipping we have to show it back again
        }

        private void modifyEntry(Entry modifiedEntry, DialogInterface dialogInterface,
                                 double amount, String info, Calendar date) {
            compositeDisposable.add(getViewModel().modifyEntry(modifiedEntry, amount, info, date)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        dialogInterface.dismiss();
                        Snackbar.make(rvCashBoxItem,
                                R.string.snackbarEntryModified, Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, (View v1) ->
                                        compositeDisposable.add(getViewModel().updateEntry(modifiedEntry)
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
