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
import androidx.lifecycle.Observer;
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
import com.vibal.utilities.databinding.CashBoxItemFragmentBinding;
import com.vibal.utilities.databinding.CashBoxItemItemBinding;
import com.vibal.utilities.exceptions.UtilAppException;
import com.vibal.utilities.models.CashBox;
import com.vibal.utilities.models.EntryBase;
import com.vibal.utilities.models.EntryInfo;
import com.vibal.utilities.models.Participant;
import com.vibal.utilities.ui.NameSelectSpinner;
import com.vibal.utilities.ui.bindingHolder.CashBoxItemFragmentBindingHolder;
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
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public abstract class CashBoxItemFragment extends PagerFragment implements CashBoxType {
    public static final int REMINDER_ID = 1;
    private static final double MAX_SHOW_CASH = 99999999;
    private static final String TAG = "PruebaItemFragment";
    @NonNull
    private final NumberFormat formatCurrency = NumberFormat.getCurrencyInstance();
    protected CompositeDisposable compositeDisposable = new CompositeDisposable();

    protected CashBoxItemFragmentBindingHolder binding;
    private CashBoxItemRecyclerAdapter adapter;
    private ShareActionProvider shareActionProvider;
    private boolean notificationEnabled = false; //By default, the icon is set to alarm off
    private boolean showCashTextTotal = true;
    private MenuItem menuItemNotification;

    private final Observer<Double> balanceObserver = cash -> {
        if (cash != null)
            updateCash(cash);
        else
            binding.itemCash.setText(R.string.error);
    };

    @NonNull
    static AlertDialog getAddEntryDialog(long cashBoxId, @NonNull Context context,
                                         @NonNull CashBoxViewModel viewModel,
                                         CompositeDisposable compositeDisposable,
                                         @Nullable Collection<String> participantNames) {
        return new MyDialogBuilder(context)
                .setTitle(R.string.newEntry)
                .setView(R.layout.cash_box_item_entry_input)
                .setPositiveButton(R.string.addEntryDialog, null)
                .setActions(dialog -> {
                    Button positive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    TextInputEditText inputInfo = Objects.requireNonNull(((AlertDialog) dialog).findViewById(R.id.inputTextInfo));
                    TextInputEditText inputAmount = Objects.requireNonNull(((AlertDialog) dialog).findViewById(R.id.inputTextAmount));
                    TextInputLayout layoutAmount = Objects.requireNonNull(((AlertDialog) dialog).findViewById(R.id.inputLayoutAmount));
                    MaterialTextView inputDate = Objects.requireNonNull(((AlertDialog) dialog).findViewById(R.id.inputDate));
                    NameSelectSpinner spinnerTo = Objects.requireNonNull(((AlertDialog) dialog).findViewById(R.id.spinnerTo));
                    NameSelectSpinner spinnerFrom = Objects.requireNonNull(((AlertDialog) dialog).findViewById(R.id.spinnerFrom));

                    // Set up Date Picker
                    Util.TextViewDatePickerClickListener calendarListener =
                            new Util.TextViewDatePickerClickListener(context, inputDate, true);
                    inputDate.setOnClickListener(calendarListener);
                    // Set up spinners
                    if (participantNames != null && participantNames.size() <= 1) { // Show spinners if more than one participant
                        spinnerTo.setVisibility(View.GONE);
                        spinnerFrom.setVisibility(View.GONE);
                    } else {
                        ArrayList<String> namesList = new ArrayList<>(participantNames);
                        spinnerFrom.config(namesList);
                        spinnerTo.config(namesList);
                    }

                    Util.showKeyboard(context, inputAmount);
                    positive.setOnClickListener((View v) -> {
                        try {
                            String input = inputAmount.getText().toString().trim();
                            if (input.isEmpty()) {
                                layoutAmount.setError(context.getString(R.string.required));
                                Util.showKeyboard(context, inputAmount);
                            } else {
                                double amount = Util.parseExpression(inputAmount.getText().toString());
                                EntryBase<EntryInfo> entry = EntryBase.getInstance(new EntryInfo(
                                                amount, inputInfo.getText().toString(), calendarListener.getCalendar()),
                                        Participant.newFrom(spinnerFrom.getSelectedString()),
                                        Participant.newTo(spinnerTo.getSelectedString()));

                                compositeDisposable.add(viewModel.addEntry(cashBoxId, entry)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(dialog::dismiss, throwable -> {
                                            dialog.dismiss();
                                            Toast.makeText(context, UtilAppException.getErrorMsg(throwable),
                                                    Toast.LENGTH_SHORT).show();

                                            LogUtil.error(TAG, "RxJava error: ", throwable);
                                        }));
                            }
                        } catch (NumberFormatException e) {
                            layoutAmount.setError(context.getString(R.string.errorMessageAmount));
                            inputAmount.selectAll();
                            Util.showKeyboard(context, inputAmount);
                        }
                    });
                }).create();
    }

    @NonNull
    static AlertDialog getAddEntryDialog(@NonNull CashBox cashBox, @NonNull Context context,
                                         @NonNull CashBoxViewModel viewModel,
                                         CompositeDisposable compositeDisposable) {
        return getAddEntryDialog(cashBox.getInfoWithCash().getId(), context, viewModel,
                compositeDisposable, cashBox.getCacheNames());
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
        binding = new CashBoxItemFragmentBindingHolder(
                CashBoxItemFragmentBinding.inflate(inflater, container, false));
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up listeners
        binding.fabCBItem.setOnClickListener(v -> onFabAddClicked());
        binding.balancesCB.setOnClickListener(v -> onFabBalancesClicked());
        binding.itemCashLayout.setOnClickListener(v -> onTextCashClicked());

        //Set up RecyclerView
        binding.rvCashBoxItem.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManagerWrapper(getContext()); // Workaround for recycler error
        binding.rvCashBoxItem.setLayoutManager(layoutManager);
        adapter = new CashBoxItemRecyclerAdapter();
        binding.rvCashBoxItem.setAdapter(adapter);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        CashBoxSwipeController swipeController = new CashBoxSwipeController(adapter, preferences);
        (new ItemTouchHelper(swipeController)).attachToRecyclerView(binding.rvCashBoxItem);
        binding.rvCashBoxItem.addItemDecoration(new DividerItemDecoration(requireContext(),
                layoutManager.getOrientation()));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize data
        getViewModel().getCurrentCashBox().observe(getViewLifecycleOwner(), cashBox -> {
            LogUtil.debug("PruebaItemFragment", "Is Visible onSubmitList: " + isVisible());

            // Set Title
            ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
            if (actionBar != null && isVisible())
                actionBar.setTitle(cashBox.getName());

            // Update data
            Currency cashBoxCurrency = cashBox.getInfoWithCash().getCashBoxInfo().getCurrency();
            if (!Objects.equals(formatCurrency.getCurrency(), cashBoxCurrency)) {
                formatCurrency.setCurrency(cashBoxCurrency);
                adapter.notifyItemRangeChanged(0, adapter.getItemCount());
            }
            adapter.submitList(cashBox.getEntries());
            if (showCashTextTotal)
                updateCash(cashBox.getCash());

            // Show balances fab if more than one participant
            if (cashBox.getCacheNames().size() <= 1)
                binding.balancesCB.setVisibility(View.GONE);
            else
                binding.balancesCB.setVisibility(View.VISIBLE);

            // Update ShareIntent
            if (shareActionProvider != null)
                shareActionProvider.setShareIntent(Util.getShareIntent(cashBox));
        });
    }

    @NonNull
    protected abstract CashBoxViewModel getViewModel();

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    private void onTextCashClicked() {
        if (getViewModel().requireCashBox().getCacheNames().size() <= 1)
            return;

        showCashTextTotal = !showCashTextTotal;
        if (showCashTextTotal) { // show total value
            binding.titleItemCash.setText(R.string.item_totalCash);
            getViewModel().getCurrentSelfCashBalance()
                    .removeObserver(balanceObserver);
            updateCash(getViewModel().requireCashBox().getCash());
        } else { // show just my balance
            binding.titleItemCash.setText(R.string.myBalance);
            getViewModel().getCurrentSelfCashBalance()
                    .observe(getViewLifecycleOwner(), balanceObserver);
        }
    }

    private void updateCash(double cash) {
        if (Math.abs(cash) > MAX_SHOW_CASH)
            binding.itemCash.setText(R.string.outOfRange);
        else {
            binding.itemCash.setText(formatCurrency.format(cash));
            if (cash < 0)
                binding.itemCash.setTextColor(requireActivity()
                        .getColor(R.color.colorNegativeNumber));
            else
                binding.itemCash.setTextColor(requireActivity()
                        .getColor(showCashTextTotal ? R.color.colorNeutralNumber : R.color.colorPositiveNumber));
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (!isOptionsMenuActive())
            return;
        super.onCreateOptionsMenu(menu, inflater);

        // Set Title
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(getViewModel().getCurrentCashBox().getValue().getName());

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
            setIconNotification(ReminderReceiver.hasAlarm(requireContext(),
                    getViewModel().getCurrentCashBoxId(), getReminderType()));
            // Set up ShareActionProvider
            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_item_share));
        }
    }

    @MenuRes
    abstract protected int getMenuRes();

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
        if (!isOptionsMenuActive())
            return false;

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
        long timeInMillis = ReminderReceiver.getTimeInMillis(requireContext(),
                getViewModel().getCurrentCashBoxId(), getReminderType(), 0);
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

//    protected abstract @ReminderReceiver.ReminderType
//    String getReminderType();
//
//    protected abstract @CashBoxManagerActivity.CashBoxType
//    int getCashBoxType();

    private void scheduleReminder(@NonNull Calendar calendar) {
        long cashBoxId = getViewModel().getCurrentCashBoxId();
        long timeInMillis = calendar.getTimeInMillis();

        //Set up the alarm manager for the notification
        boolean done = ReminderReceiver.setAlarm((AlarmManager) Objects.requireNonNull(requireContext().getSystemService(Context.ALARM_SERVICE)),
                requireContext(), cashBoxId, timeInMillis, getReminderType());
        setIconNotification(done);
        Toast.makeText(getContext(), "New Reminder Set", Toast.LENGTH_SHORT).show();
    }

    private void cancelReminder() {
        long cashBoxId = getViewModel().getCurrentCashBoxId();
        ReminderReceiver.cancelAlarm((AlarmManager) Objects.requireNonNull(requireContext().getSystemService(Context.ALARM_SERVICE)),
                requireContext(), cashBoxId, getReminderType());
        setIconNotification(false);
        Toast.makeText(getContext(), "Reminder Cancelled", Toast.LENGTH_SHORT).show();
    }

    void onFabAddClicked() {
        getAddEntryDialog(getViewModel().requireCashBox(), requireContext(), getViewModel(), compositeDisposable)
                .show();
    }

    void onFabBalancesClicked() {
        Intent intent = new Intent(requireContext(), CashBoxBalancesActivity.class);
        intent.putExtra(CashBoxBalancesActivity.CASHBOX_ID_EXTRA, getViewModel().getCurrentCashBoxId());
        intent.putExtra(CashBoxBalancesActivity.CASHBOX_TYPE_EXTRA, getCashBoxType());
        startActivity(intent);
    }

    private void deleteAll() {
        if (adapter.getItemCount() == 0) {
            Toast.makeText(getContext(), R.string.noEntriesDelete, Toast.LENGTH_SHORT).show();
            return;
        }

        List<EntryBase<?>> deletedEntries = new ArrayList<>(adapter.currentList);
        compositeDisposable.add(getViewModel().deleteAllEntriesFromCurrentCashBox()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer ->
                        Snackbar.make(binding.rvCashBoxItem,
                                getString(R.string.snackbarEntriesDeleted, integer),
                                Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, v ->
                                        compositeDisposable.add(getViewModel().addAllEntriesToCurrentCashBox(deletedEntries)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe()))
                                .show(), this::doOnRxError));
    }

    protected void doOnRxError(Throwable throwable) {
        Toast.makeText(requireContext(), UtilAppException.getErrorMsg(throwable), Toast.LENGTH_SHORT).show();

        LogUtil.error(TAG, "RxJava error: ", throwable);
    }

    protected void doOnModifyEntryError(Throwable throwable, EntryInfo entryInfo) {
        doOnRxError(throwable);
    }

    public class CashBoxItemRecyclerAdapter extends RecyclerView.Adapter<CashBoxItemRecyclerAdapter.ViewHolder> implements CashBoxAdapterSwipable {
        private static final boolean DRAG_ENABLED = false;
        private static final boolean SWIPE_ENABLED = true;
        @NonNull
        private final ConcurrentLinkedQueue<Single<DiffCallback.DiffResultWithList<EntryBase<?>>>> pendingSubmitted = new ConcurrentLinkedQueue<>();
        @NonNull
        private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        @NonNull
        private final List<EntryBase<?>> currentList = new ArrayList<>();

        /**
         * Submit a new list of elements for the adapter to show.
         * All changes in adapter list must go through submitList.
         *
         * @param newList New list to be submitted
         */
        void submitList(@NonNull List<? extends EntryBase<?>> newList) {
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
            Single<DiffCallback.DiffResultWithList<EntryBase<?>>> single = pendingSubmitted.peek();
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
                        }, CashBoxItemFragment.this::doOnRxError));
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
            EntryBase<?> entry = currentList.get(index);
            EntryInfo entryInfo = entry.getEntryInfo();

            // Amount
            viewHolder.binding.rvItemAmount.setText(formatCurrency.format(entryInfo.getAmount()));
            if (entryInfo.getAmount() < 0)
                viewHolder.binding.rvItemAmount.setTextColor(requireContext().getColor(R.color.colorNegativeNumber));
            else
                viewHolder.binding.rvItemAmount.setTextColor(requireContext().getColor(R.color.colorNeutralNumber));

            // CashBoxInfo
            viewHolder.binding.rvItemInfo.setText(entryInfo.printInfo());
            // Date
            viewHolder.binding.rvItemDate.setText(dateFormat.format(entryInfo.getDate().getTime()));
            // Item from and amount balance
            if (getViewModel().requireCashBox().getCacheNames().size() <= 1) {
                viewHolder.binding.rvItemFrom.setVisibility(View.GONE);
                viewHolder.binding.rvItemBalance.setVisibility(View.GONE);
            } else {
                viewHolder.binding.rvItemFrom.setVisibility(View.VISIBLE);
                viewHolder.binding.rvItemFrom.setText(getString(R.string.paidBy,
                        EntryBase.formatParticipants(entry.getFromParticipants())));
                // Amount Balance
                viewHolder.binding.rvItemBalance.setVisibility(View.VISIBLE);
                double balance = entry.getParticipantBalance(
                        Participant.createDefaultParticipant(entryInfo.getId(), true));
                viewHolder.binding.rvItemBalance.setText(getString(R.string.between_parenthesis, formatCurrency.format(balance)));

                if (balance == 0)
                    viewHolder.binding.rvItemBalance.setTextColor(requireContext().getColor(R.color.colorNeutralNumber));
                else if (balance < 0)
                    viewHolder.binding.rvItemBalance.setTextColor(requireContext().getColor(R.color.colorNegativeNumber));
                else
                    viewHolder.binding.rvItemBalance.setTextColor(requireContext().getColor(R.color.colorPositiveNumber));
            }
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
            EntryBase<?> entry = currentList.get(position);

            if (entry.getEntryInfo().getGroupId() == EntryInfo.NO_GROUP) {
                // todo check
                deleteEntry(entry, position);
                return;
            }

            new MyDialogBuilder(requireContext())
                    .setTitle(R.string.titleGroupEntryDelete)
                    .setMessage(R.string.messageGroupDelete)
                    .setOnCancelListener(dialogInterface -> notifyItemChanged(position))   // since the item is deleted from swipping we have to show it back again)
                    .setNegativeButton(R.string.groupEntryIndividual,
                            (dialogInterface, i) -> deleteEntry(entry, position))//todo check
                    .setPositiveButton(R.string.groupEntryAll, (dialogInterface, i) ->
                            compositeDisposable.add(getViewModel().getGroupEntries(entry.getEntryInfo())
                                    .flatMap(entries -> getViewModel().deleteGroupEntries(entry)
                                            .map(integer -> entries))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(entryList -> Snackbar.make(binding.rvCashBoxItem,
                                            getString(R.string.snackbarEntriesDeleted, 1),
                                            Snackbar.LENGTH_LONG)
                                            .setAction(R.string.undo, (View v) ->
                                                    compositeDisposable.add(getViewModel().addAllEntries(entryList)
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe()))
                                            .show(), throwable -> {
                                        doOnRxError(throwable);
                                        notifyItemChanged(position);
                                    })))
                    .show();
        }

        // todo check
        private void deleteEntry(EntryBase<?> entry, int position) {
            compositeDisposable.add(getViewModel().deleteEntry(entry)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Snackbar.make(binding.rvCashBoxItem,
                            getString(R.string.snackbarEntriesDeleted, 1),
                            Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, (View v) ->
                                    compositeDisposable.add(getViewModel().addEntryToCurrentCashBox(entry)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe()))
                            .show(), throwable -> {
                        doOnRxError(throwable);
                        notifyItemChanged(position);
                    }));
        }

        @Override
        public void onItemSecondaryAction(int position) {
            LogUtil.debug(TAG, "onItemSecondaryAction: ID del entry: " +
                    currentList.get(position).getEntryInfo().getId()
                    + "\nID del cashBox: " + currentList.get(position).getEntryInfo().getCashBoxId() +
                    "\nID group: " + currentList.get(position).getEntryInfo().getGroupId());

            new MyDialogBuilder(requireContext())
                    .setTitle(R.string.modifyEntry)
                    .setView(R.layout.cash_box_item_entry_modify)
                    .setPositiveButton(R.string.confirm, null)
                    .setActions((DialogInterface dialogInterface) -> {
                        Button positive = ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE);
                        TextInputEditText inputInfo = ((AlertDialog) dialogInterface).findViewById(R.id.inputTextInfo);
                        TextInputEditText inputAmount = ((AlertDialog) dialogInterface).findViewById(R.id.inputTextAmount);
                        TextInputLayout layoutAmount = ((AlertDialog) dialogInterface).findViewById(R.id.inputLayoutAmount);
                        MaterialTextView inputDate = ((AlertDialog) dialogInterface).findViewById(R.id.inputDate);
                        EntryInfo modifiedEntry = currentList.get(position).getEntryInfo();

                        // Set up Date Picker
                        Util.TextViewDatePickerClickListener calendarListener =
                                new Util.TextViewDatePickerClickListener(requireContext(), inputDate,
                                        modifiedEntry.getDate(), true);
                        inputDate.setOnClickListener(calendarListener);
                        // Set up inputs
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
                                    if (modifiedEntry.getGroupId() == EntryInfo.NO_GROUP)
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
                                                                    Snackbar.make(binding.rvCashBoxItem,
                                                                            getString(R.string.snackbarEntriesDeleted, 1),
                                                                            Snackbar.LENGTH_LONG)
                                                                            .setAction(R.string.undo, (View v2) -> {
                                                                                Completable completable = Completable.complete();
                                                                                for (EntryBase<?> k : entryList)
                                                                                    completable = completable.andThen(
                                                                                            getViewModel().updateEntryInfo(k.getEntryInfo()));
                                                                                compositeDisposable.add(completable
                                                                                        .subscribeOn(Schedulers.io())
                                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                                        .subscribe());
                                                                            }).show();
                                                                }, throwable -> {
                                                                    dialogInterface.dismiss();
                                                                    doOnRxError(throwable);
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

        private void modifyEntry(EntryInfo modifiedEntry, DialogInterface dialogInterface,
                                 double amount, String info, Calendar date) {
            compositeDisposable.add(getViewModel().modifyEntryInfo(modifiedEntry, amount, info, date)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        dialogInterface.dismiss();
                        Snackbar.make(binding.rvCashBoxItem,
                                R.string.snackbarEntryModified, Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, (View v1) ->
                                        compositeDisposable.add(getViewModel().updateEntryInfo(modifiedEntry)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe()))
                                .show();
                    }, throwable -> {
                        dialogInterface.dismiss();
                        doOnModifyEntryError(throwable, new EntryInfo(modifiedEntry.getCashBoxId(), amount, info, date));
                    }));
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final CashBoxItemItemBinding binding;

            ViewHolder(@NonNull View view) {
                super(view);
                binding = CashBoxItemItemBinding.bind(view);

                view.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), CashBoxItemDetailsActivity.class);
                intent.putExtra(CashBoxItemDetailsActivity.CASHBOX_ID_EXTRA, getViewModel().getCurrentCashBoxId());
                intent.putExtra(CashBoxItemDetailsActivity.ENTRY_POSITION_EXTRA, getAdapterPosition());
                intent.putExtra(CashBoxItemDetailsActivity.CASHBOX_TYPE_EXTRA, getCashBoxType());
                startActivity(intent);
            }
        }
    }
}
