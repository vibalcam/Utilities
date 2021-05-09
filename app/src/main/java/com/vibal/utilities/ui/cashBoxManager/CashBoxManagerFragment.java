package com.vibal.utilities.ui.cashBoxManager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.vibal.utilities.R;
import com.vibal.utilities.databinding.CashBoxManagerFragmentBinding;
import com.vibal.utilities.databinding.CashBoxManagerItemBinding;
import com.vibal.utilities.exceptions.UtilAppException;
import com.vibal.utilities.models.CashBox;
import com.vibal.utilities.models.CashBoxInfo;
import com.vibal.utilities.models.EntryBase;
import com.vibal.utilities.models.EntryInfo;
import com.vibal.utilities.models.InfoWithCash;
import com.vibal.utilities.models.PeriodicEntryPojo;
import com.vibal.utilities.ui.bindingHolder.CashBoxManagerFragmentBindingHolder;
import com.vibal.utilities.ui.settings.SettingsActivity;
import com.vibal.utilities.ui.swipeController.CashBoxAdapterSwipable;
import com.vibal.utilities.ui.swipeController.CashBoxSwipeController;
import com.vibal.utilities.ui.swipeController.OnStartDragListener;
import com.vibal.utilities.ui.viewPager.PagerFragment;
import com.vibal.utilities.util.DiffCallback;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.util.MyDialogBuilder;
import com.vibal.utilities.util.Util;
import com.vibal.utilities.viewModels.CashBoxViewModel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.ACTION_ADD_CASHBOX;
import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.ACTION_DETAILS;
import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.CASHBOX_MANAGER_PREFERENCE;
import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.EXTRA_ACTION;
import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.EXTRA_CASHBOX_ID;
import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.EXTRA_CASHBOX_TYPE;
import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.GROUP_ADD_MODE_KEY;
import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.GROUP_ID_COUNT_KEY;
import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.NO_ACTION;

public abstract class CashBoxManagerFragment extends PagerFragment implements CashBoxType {
    // Simulate enum
    static final int EDIT_MODE = 0;
    static final int GROUP_ADD_MODE = 1;
    static final int PERIODIC_ADD_MODE = 2;
    private static final String TAG = "PruebaManagerFragment";
    protected CompositeDisposable compositeDisposable = new CompositeDisposable();
    @Nullable
    protected ActionMode actionMode;
    protected CashBoxManagerRecyclerAdapter adapter;
    protected CashBoxManagerFragmentBindingHolder binding;

    private final ActionMode.Callback periodicAddModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_contextual_confirm, menu);
            mode.setTitle("Choose CashBox:");
            //Hide fab
            binding.fabCBMMain.animate().alpha(0f);
            binding.fabCBMMain.setVisibility(View.GONE);
            // Hide TabLayout
            setTabLayoutVisibility(View.GONE);
            // Notify adapter to hide images for choosing
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (adapter.selectedItems.isEmpty()) {
                Toast.makeText(getContext(), "No items selected", Toast.LENGTH_SHORT).show();
                return true;
            } else if (adapter.selectedItems.size() > 1) { //should never happen
                throw new RuntimeException("Selected Items size has to be 1");
            } else if (item.getItemId() == R.id.action_confirm) {
                showAddPeriodicDialog(adapter.currentList.get(adapter.selectedItems.iterator().next()));
                mode.finish();
                return true;
            } else
                return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            //Show fab
            binding.fabCBMMain.setVisibility(View.VISIBLE);
            binding.fabCBMMain.animate().alpha(1f);
            // Hide TabLayout
            setTabLayoutVisibility(View.VISIBLE);
            //If menu was not clicked, clear selection and notify adapter to show images again
            adapter.selectedItems.clear();
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
            LogUtil.debug(TAG, "" + adapter.getItemCount() + adapter.selectedItems.toString());
        }
    };
    private final ActionMode.Callback groupAddModeCallback = new ActionMode.Callback() {
        private boolean dialogOpened = false;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_contextual_confirm, menu);
            mode.setTitle("Choose CashBoxes:");
            //Hide fab
            binding.fabCBMMain.animate().alpha(0f);
            binding.fabCBMMain.setVisibility(View.GONE);
            // Hide TabLayout
            setTabLayoutVisibility(View.GONE);
            // Notify adapter to hide images for choosing
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (adapter.selectedItems.isEmpty()) {
                Toast.makeText(getContext(), "No items selected", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.action_confirm) {
                dialogOpened = true;
                showGroupAddDialog();
                mode.finish();
                return true;
            } else
                return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            //Show fab
            binding.fabCBMMain.setVisibility(View.VISIBLE);
            binding.fabCBMMain.animate().alpha(1f);
            // Hide TabLayout
            setTabLayoutVisibility(View.VISIBLE);
            //If menu was not clicked, clear selection and notify adapter to show images again
            if (!dialogOpened) // if dialog opened do not delete selection, the dialog will do it
                adapter.selectedItems.clear();
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
            LogUtil.debug(TAG, "" + adapter.getItemCount() + adapter.selectedItems.toString());
        }
    };
    private final ActionMode.Callback editModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(@NonNull ActionMode mode, Menu menu) {
            if (isCloneEnabled())
                mode.getMenuInflater().inflate(R.menu.menu_contextual_toolbar_cash_box_manager, menu);
            //Hide fab
            binding.fabCBMMain.animate().alpha(0f);
            binding.fabCBMMain.setVisibility(View.GONE);
            // Hide TabLayout
            setTabLayoutVisibility(View.GONE);
            // Notify adapter to show images for dragging
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(@NonNull ActionMode mode, @NonNull MenuItem item) {
            if (adapter.selectedItems.isEmpty()) {
                Toast.makeText(getContext(), "No item selected", Toast.LENGTH_SHORT).show();
                return true;
            } else if (adapter.selectedItems.size() > 1) //should never happen
                throw new RuntimeException("Selected Items size has to be 1");
            else if (item.getItemId() == R.id.action_manager_duplicate && isCloneEnabled()) {
                adapter.showCloneDialog(adapter.selectedItems.iterator().next());
                mode.finish();
                return true;
            } else
                return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            //Show fab
            binding.fabCBMMain.setVisibility(View.VISIBLE);
            binding.fabCBMMain.animate().alpha(1f);
            // Hide TabLayout
            setTabLayoutVisibility(View.VISIBLE);
            // Notify adapter to hide images for dragging
            adapter.selectedItems.clear();
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        }
    };
    private boolean isFabOpen = false;
    // Contextual toolbars
    private int actionModeType = EDIT_MODE;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fragment has options menu
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        LogUtil.debug(TAG, "onCreate: ");
        binding = new CashBoxManagerFragmentBindingHolder(
                CashBoxManagerFragmentBinding.inflate(inflater, container, false));
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
        binding.fabCBMMain.setOnClickListener(v -> toggleFabMenu());
        binding.bgFabMenuCBM.setOnClickListener(v -> closeFabMenu());
        binding.fabCBMSingleAdd.setOnClickListener(v -> showAddDialog());
        binding.fabCBMGroupAdd.setOnClickListener(v -> showContextualModeGroupAdd());
        binding.fabCBMPeriodicAdd.setOnClickListener(v -> showContextualModePeriodicAdd());

        // Set up RecyclerView
        RecyclerView rvCashBoxManager = view.findViewById(R.id.rvCashBoxManager);
        rvCashBoxManager.setHasFixedSize(true);
        rvCashBoxManager.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CashBoxManagerRecyclerAdapter();
        rvCashBoxManager.setAdapter(adapter);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new CashBoxSwipeController(adapter,
                preferences));
        itemTouchHelper.attachToRecyclerView(rvCashBoxManager);
        adapter.setOnStartDragListener(itemTouchHelper::startDrag);
        ViewCompat.setNestedScrollingEnabled(rvCashBoxManager, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogUtil.debug(TAG, "onActivityCreated: ");

        // Initialize data
        initializeViewModel().getCashBoxesInfo().observe(getViewLifecycleOwner(), infoWithCashes ->
                adapter.submitList(infoWithCashes));

//        checkFileForCashBoxes();
    }

    @NonNull
    protected abstract CashBoxViewModel getViewModel();

    @NonNull
    abstract protected CashBoxViewModel initializeViewModel();

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtil.debug(TAG, "onStart: ");
        // Look at intent
        doIntentAction();
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.debug("PruebaView", getParentFragmentManager().getFragments().toString());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (!isOptionsMenuActive())
            return;
        super.onCreateOptionsMenu(menu, inflater);

        //Set Toolbar title since in onCreateOptionsMenu doesn't work
        if (getParentFragmentManager().getFragments().size() < 2)
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(getTitle());
        menu.clear();
        inflater.inflate(getMenuRes(), menu);
    }

    @MenuRes
    protected abstract int getMenuRes();

    @StringRes
    abstract protected int getTitle();

    abstract protected boolean isCloneEnabled();

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!isOptionsMenuActive())
            return false;

        switch (item.getItemId()) {
            case R.id.action_manager_deleteAll:
                AlertDialog.Builder builder = getDeleteAllDialog();
                if (builder != null)
                    builder.show();
                return true;
            case R.id.action_manager_help:
                Util.createHelpDialog(requireContext(), R.string.cashBoxManager_helpTitle,
                        R.string.cashBoxManager_help).show();
                return true;
            case R.id.action_manager_edit:
                startActionMode(EDIT_MODE);
                return true;
            case R.id.action_manager_settings:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    protected abstract @CashBoxManagerActivity.CashBoxType
//    int getCashBoxType();

    private void doIntentAction() {
        Intent intent = requireActivity().getIntent();
        if (intent == null || intent.getIntExtra(EXTRA_CASHBOX_TYPE, LOCAL) != getCashBoxType())
            return;

        int action = intent.getIntExtra(EXTRA_ACTION, NO_ACTION);
        intent.removeExtra(EXTRA_ACTION); //So it only triggers once

        LogUtil.debug(TAG, "" + (action == ACTION_ADD_CASHBOX) + " " + (action == ACTION_DETAILS));

        if (action == ACTION_ADD_CASHBOX)
            showAddDialog();
        else if (action == ACTION_DETAILS)
            swapToItemFragment(intent.getLongExtra(EXTRA_CASHBOX_ID, CashBoxInfo.NO_ID));
    }

    private void swapToItemFragment(long cashBoxId) {
        LogUtil.debug(TAG, "Selected " + cashBoxId);
        if (cashBoxId == CashBoxInfo.NO_ID)
            return;

        getViewModel().setCurrentCashBoxId(cashBoxId);
        FragmentTransaction transaction = getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right,
                        R.anim.enter_from_right, R.anim.exit_to_right);

        // In landscape, no backstack so back returns to parent activity
        View landsView = requireParentFragment().getView().findViewById(R.id.containerItem); // view in parent fragment
        if (landsView != null && landsView.getVisibility() == View.VISIBLE)
            transaction.replace(R.id.containerItem, getChildInstance())
                    .commitNow();
        else
            transaction.replace(R.id.container, getChildInstance())
                    .addToBackStack(null)
                    .commit();
    }

    abstract protected CashBoxItemFragment getChildInstance();

    boolean startActionMode(@ActionModeType int type) {
        if (adapter.currentList.isEmpty()) { // Check if there are any CashBoxes
            Toast.makeText(getContext(), "No available CashBoxes", Toast.LENGTH_SHORT).show();
            return false;
        } else if (actionMode != null) { // Check if already running
            if (actionModeType == type)
                return false;
            else
                actionMode.finish();
        }

        // Start action mode
        actionModeType = type;
        switch (type) {
            case EDIT_MODE:
                actionMode = ((AppCompatActivity) requireActivity()).startSupportActionMode(editModeCallback);
                return true;
            case GROUP_ADD_MODE:
                actionMode = ((AppCompatActivity) requireActivity()).startSupportActionMode(groupAddModeCallback);
                return true;
            case PERIODIC_ADD_MODE:
                actionMode = ((AppCompatActivity) requireActivity()).startSupportActionMode(periodicAddModeCallback);
                return true;
            default: // should never happen
                return false;
        }
    }

    @Nullable
    protected MyDialogBuilder getDeleteAllDialog() {
        int count = adapter.getItemCount();
        if (count == 0) {
            Toast.makeText(getContext(), "No entries to delete", Toast.LENGTH_SHORT).show();
            return null;
        }

        return new MyDialogBuilder(requireContext())
                .setTitle(R.string.confirmRecycleAllDialog)
                .setMessage("Are you sure you want to move all entries to the recycle bin?")
                .setCancelOnTouchOutside(true)
                .setPositiveButton((dialog, which) ->
                        compositeDisposable.add(getViewModel().deleteAllCashBoxes()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> {
                                    dialog.dismiss();
                                    Toast.makeText(getContext(),
                                            getString(R.string.snackbarEntriesMoveToRecycle, count),
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }, this::defaultDoOnRxError)));
    }

    private void toggleFabMenu() {
        if (isFabOpen) {
            closeFabMenu();
            return;
        }

        LogUtil.debug(TAG, "Open FAB Menu");
        //Open FAB Menu
        isFabOpen = true;
        binding.fabCBMPeriodicAdd.setVisibility(View.VISIBLE);
        binding.fabCBMGroupAdd.setVisibility(View.VISIBLE);
        binding.fabCBMSingleAdd.setVisibility(View.VISIBLE);
        binding.bgFabMenuCBM.setVisibility(View.VISIBLE);

        //Animate
        binding.fabCBMMain.animate().rotation(135f);
        binding.bgFabMenuCBM.animate().alpha(1f);
        binding.fabCBMPeriodicAdd.animate()
                .translationY(-getResources().getDimension(R.dimen.standard_55))
                .rotation(0f);
        binding.fabCBMGroupAdd.animate()
                .translationY(-getResources().getDimension(R.dimen.standard_100))
                .rotation(0f);
        binding.fabCBMSingleAdd.animate()
                .translationY(-getResources().getDimension(R.dimen.standard_145))
                .rotation(0f);
    }

    protected void closeFabMenu() {
        LogUtil.debug(TAG, "Close FAB Menu");
        isFabOpen = false;
        //Animate
        binding.fabCBMMain.animate().rotation(0f);
        binding.bgFabMenuCBM.animate().alpha(0f);
        binding.fabCBMPeriodicAdd.animate()
                .translationY(0f)
                .rotation(90f);
        binding.fabCBMGroupAdd.animate()
                .translationY(0f)
                .rotation(90f);
        binding.fabCBMSingleAdd.animate()
                .translationY(0f)
                .rotation(90f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!isFabOpen) {
                            LogUtil.debug(TAG, "Hide fabs");
                            binding.fabCBMPeriodicAdd.setVisibility(View.GONE);
                            binding.fabCBMGroupAdd.setVisibility(View.GONE);
                            binding.fabCBMSingleAdd.setVisibility(View.GONE);
                            binding.bgFabMenuCBM.setVisibility(View.GONE);
                        }
                    }
                });

    }

    protected void showAddDialog() {
        closeFabMenu();
        if (actionMode != null)
            actionMode.finish();
        LogUtil.debug(TAG, "Single add");

        new MyDialogBuilder(requireContext())
                .setTitle(R.string.newEntry)
                .setView(R.layout.cash_box_new_input)
                .setPositiveButton(R.string.createCashBoxDialog, null)
                .setActions(dialog -> {
                    Button positive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                    TextInputEditText inputTextName = ((AlertDialog) dialog).findViewById(R.id.inputTextName);
                    TextInputLayout inputLayoutName = ((AlertDialog) dialog).findViewById(R.id.inputLayoutName);
                    TextInputEditText inputTextInitCash = ((AlertDialog) dialog).findViewById(R.id.inputTextInitCash);
                    TextInputLayout inputLayoutInitCash = ((AlertDialog) dialog).findViewById(R.id.inputLayoutInitCash);

                    Util.showKeyboard(requireContext(), inputTextName);
                    positive.setOnClickListener(v -> {
                        inputLayoutInitCash.setError(null);
                        inputLayoutName.setError(null);
                        try {
                            CashBox cashBox = CashBox.create(inputTextName.getText().toString());
                            String strInitCash = inputTextInitCash.getText().toString().trim();

                            if (!strInitCash.isEmpty()) {
                                double initCash = Util.parseExpression(strInitCash);
                                if (initCash != 0) {
                                    ArrayList<EntryBase<?>> arrayList = new ArrayList<>();
                                    arrayList.add(EntryBase.getInstance(new EntryInfo(initCash,
                                            "Initial Amount", Calendar.getInstance())));
                                    cashBox.setEntries(arrayList);
                                }
                            }

                            compositeDisposable.add(getViewModel().addCashBox(cashBox)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(dialog::dismiss, throwable -> {
                                        LogUtil.error(TAG, "Error in add", throwable);
                                        if (throwable instanceof SQLiteConstraintException)
                                            inputLayoutName.setError(getString(R.string.nameInUse));
                                        else
                                            inputLayoutName.setError(throwable.getLocalizedMessage());
                                        inputTextName.selectAll();
                                        Util.showKeyboard(requireContext(), inputTextName);
                                    }));
                        } catch (NumberFormatException e) {
                            LogUtil.error(TAG, "Error in add", e);
                            inputLayoutInitCash.setError(getString(R.string.errorMessageAmount));
                            inputTextInitCash.selectAll();
                            Util.showKeyboard(requireContext(), inputTextInitCash);
                        } catch (IllegalArgumentException e) {
                            LogUtil.error(TAG, "Error in add", e);
                            inputLayoutName.setError(e.getMessage());
                            inputTextName.selectAll();
                            Util.showKeyboard(requireContext(), inputTextName);
                        }
                    });
                }).show();
    }

    private void showContextualModeGroupAdd() {
        LogUtil.debug(TAG, "Group add");
        closeFabMenu();
        startActionMode(GROUP_ADD_MODE);
    }

    private void showContextualModePeriodicAdd() {
        LogUtil.debug(TAG, "Periodic add");
        closeFabMenu();
        startActionMode(PERIODIC_ADD_MODE);
    }

    private void showGroupAddDialog() {
        if (adapter.selectedItems.size() == 0) //Should never occur
            throw new RuntimeException("At least one item should be selected");

        //Add dialog
        new MyDialogBuilder(requireContext())
                .setTitle(R.string.newGroupEntry)
                .setView(R.layout.cash_box_group_entry_input)  //use that view from folder layout
                .setPositiveButton(R.string.createCashBoxDialog, null)
                .setOnDismissListener(dialogInterface -> {
                    //Clear selection and notify adapter
                    adapter.selectedItems.clear();
                    adapter.notifyItemRangeChanged(0, adapter.getItemCount());
                }).setActions(dialog -> {
            Button positive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
            TextInputEditText inputInfo = ((AlertDialog) dialog).findViewById(R.id.inputTextInfo);
            TextInputEditText inputAmount = ((AlertDialog) dialog).findViewById(R.id.inputTextAmount);
            TextInputLayout layoutAmount = ((AlertDialog) dialog).findViewById(R.id.inputLayoutAmount);
            Spinner spinner = ((AlertDialog) dialog).findViewById(R.id.spinnerModeGroupAdd);
            MaterialTextView inputDate = ((AlertDialog) dialog).findViewById(R.id.inputDate);

            // Set up Date Picker
            Util.TextViewDatePickerClickListener calendarListener =
                    new Util.TextViewDatePickerClickListener(requireContext(), inputDate, true);
            inputDate.setOnClickListener(calendarListener);

            SharedPreferences preferences = requireActivity().getSharedPreferences(CASHBOX_MANAGER_PREFERENCE,
                    Context.MODE_PRIVATE);
            spinner.setSelection(preferences.getInt(GROUP_ADD_MODE_KEY, 0));

            //Change hint in layoutAmount
            layoutAmount.setHint(getString(R.string.hintAddGroupDialog));

            Util.showKeyboard(requireContext(), inputAmount);
            positive.setOnClickListener(v -> {
                try {
                    String input = inputAmount.getText().toString().trim();
                    if (input.isEmpty()) {
                        layoutAmount.setError(getString(R.string.required));
                        Util.showKeyboard(requireContext(), inputAmount);
                    } else {
                        // Calculate Amount
                        double amount = Util.parseExpression(inputAmount.getText().toString());
                        LogUtil.debug(TAG, getResources().getStringArray(R.array.groupAddBehaviour_entries)[spinner.getSelectedItemPosition()]);
                        int mode = spinner.getSelectedItemPosition();
                        if (mode == 1)
                            amount /= adapter.selectedItems.size();
                        else if (mode == 2)
                            amount /= adapter.selectedItems.size() + 1;

                        // Get next usable Group ID
                        long groupId = preferences.getLong(GROUP_ID_COUNT_KEY, 1);

                        // Add entries
                        Completable addEntries = Completable.complete();
                        for (int k : adapter.selectedItems)
                            addEntries = addEntries.andThen(getViewModel().addEntry(adapter.currentList.get(k).getId(),
                                    EntryBase.getInstance(new EntryInfo(amount, inputInfo.getText().toString().trim(),
                                            calendarListener.getCalendar(), groupId))));
                        compositeDisposable.add(addEntries.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                    dialog.dismiss();
                                    preferences.edit()
                                            .putLong(GROUP_ID_COUNT_KEY, groupId + 1)
                                            .putInt(GROUP_ADD_MODE_KEY, mode)
                                            .apply();
                                }, throwable -> {
                                    dialog.dismiss();
                                    defaultDoOnRxError(throwable);
                                }));
                    }
                } catch (NumberFormatException e) {
                    layoutAmount.setError(getString(R.string.errorMessageAmount));
                    inputAmount.selectAll();
                    Util.showKeyboard(requireContext(), inputAmount);
                }
            });
        }).show();
    }

    private void showAddPeriodicDialog(@NonNull InfoWithCash infoWithCash) {
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
                    MaterialTextView inputDate = ((AlertDialog) dialog).findViewById(R.id.inputDate);

                    // Set up Date Picker
                    Util.TextViewDatePickerClickListener calendarListener =
                            new Util.TextViewDatePickerClickListener(requireContext(), inputDate, false);
                    inputDate.setOnClickListener(calendarListener);

                    Util.showKeyboard(requireContext(), inputAmount);
                    positive.setOnClickListener((View v) -> {
                        try {
                            String input = inputAmount.getText().toString().trim();
                            int repetitions = Integer.parseInt(inputRepetitions.getText().toString());
                            if (input.isEmpty()) {
                                layoutAmount.setError(getString(R.string.required));
                                Util.showKeyboard(requireContext(), inputAmount);
                            } else if (repetitions < 1) {
                                layoutRepetitions.setError("Min. 1");
                                Util.showKeyboard(requireContext(), inputRepetitions);
                            } else {
                                try {
                                    double amount = Util.parseExpression(inputAmount.getText().toString());
                                    compositeDisposable.add(getViewModel().addPeriodicEntryWorkRequest(
                                            new PeriodicEntryPojo.PeriodicEntryWorkRequest(infoWithCash.getId(),
                                                    amount, inputInfo.getText().toString(),
                                                    Long.parseLong(inputPeriod.getText().toString()), repetitions,
                                                    calendarListener.getDaysFromCurrent()))
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(dialog::dismiss, throwable -> {
                                                dialog.dismiss();
                                                defaultDoOnRxError(throwable);
                                            }));
                                } catch (NumberFormatException e) {
                                    layoutAmount.setError(getString(R.string.errorMessageAmount));
                                    inputAmount.selectAll();
                                    Util.showKeyboard(requireContext(), inputAmount);
                                }
                            }
                        } catch (NumberFormatException e) {
                            layoutRepetitions.setError(getString(R.string.errorMessageAmount));
                            inputRepetitions.selectAll();
                            Util.showKeyboard(requireContext(), inputRepetitions);
                        }
                    });
                }).show();
    }

    protected void doOnDelete(InfoWithCash infoWithCash) {
        Toast.makeText(getContext(),
                getString(R.string.snackbarEntriesDeleted, 1),
                Toast.LENGTH_SHORT)
                .show();
    }

    protected void deleteCashBox(int position) {
        if (actionMode != null)
            actionMode.finish();
        LogUtil.debug(TAG, "Delete CashBox");
        InfoWithCash infoWithCash = adapter.currentList.get(position);
        compositeDisposable.add(getViewModel().deleteCashBoxInfo(infoWithCash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> doOnDelete(infoWithCash), this::defaultDoOnRxError));
    }

    protected void defaultDoOnRxError(Throwable throwable) {
        Toast.makeText(requireContext(), UtilAppException.getErrorMsg(throwable), Toast.LENGTH_SHORT).show();
        LogUtil.error(TAG, "RxJava error: ", throwable);
    }

    /**
     * Default implementation is empty
     */
    protected void showInvitationDialog(InfoWithCash infoWithCash) {
    }

    /**
     * Default implementation is empty
     */
    protected void showChangesDialog(InfoWithCash infoWithCash) {
    }

    /**
     * Modes for the Action Mode
     */
    @IntDef({EDIT_MODE, GROUP_ADD_MODE, PERIODIC_ADD_MODE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionModeType {
    }

    public class CashBoxManagerRecyclerAdapter extends RecyclerView.Adapter<CashBoxManagerRecyclerAdapter.ViewHolder> implements CashBoxAdapterSwipable {
        private static final boolean SWIPE_ENABLED = true;
        private static final String TAG = "PruebaManagerActivity";

        private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        @NonNull
        private final ConcurrentLinkedQueue<Single<DiffCallback.DiffResultWithList<InfoWithCash>>> pendingSubmitted = new ConcurrentLinkedQueue<>();
        private final Set<Integer> selectedItems = new HashSet<>();
        private OnStartDragListener onStartDragListener;
        @NonNull
        private final List<InfoWithCash> currentList = new ArrayList<>();

        void setOnStartDragListener(OnStartDragListener onStartDragListener) {
            this.onStartDragListener = onStartDragListener;
        }

        /**
         * Submit a new list of elements for the adapter to show.
         * All changes in adapter list must go through submitList.
         *
         * @param newList New list to be submitted
         */
        void submitList(@NonNull List<InfoWithCash> newList) {
            LogUtil.debug(TAG, "New list submitted: " + newList.toString());
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
//                        diffResult.dispatchUpdatesTo(CashBoxManagerRecyclerAdapter.this);
//                    }));
        }

        private void runPendingSubmitted() {
            Single<DiffCallback.DiffResultWithList<InfoWithCash>> single = pendingSubmitted.peek();
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
                                    .dispatchUpdatesTo(CashBoxManagerRecyclerAdapter.this);

                            // Delete this work from pending
                            pendingSubmitted.poll();
                            // Run next pending
                            runPendingSubmitted();
                        }, CashBoxManagerFragment.this::defaultDoOnRxError));
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cash_box_manager_item,
                    viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int index) {
            InfoWithCash cashBoxInfo = currentList.get(index);
            viewHolder.binding.rvName.setText(cashBoxInfo.getCashBoxInfo().getName());

            if (isDragEnabled()) { // drag mode
                // Image show reorder
                viewHolder.binding.reorderImage.setVisibility(View.VISIBLE);
                viewHolder.binding.reorderImage.setImageResource(R.drawable.reorder_horizontal_gray_24dp);
                viewHolder.binding.rvAmount.setVisibility(View.GONE);
            } else if (actionMode != null && actionModeType == GROUP_ADD_MODE) { // action mode
                viewHolder.binding.reorderImage.setVisibility(View.GONE);
                viewHolder.binding.rvAmount.setVisibility(View.GONE);
            } else { // regular mode
                // Image show add
                viewHolder.binding.reorderImage.setVisibility(View.VISIBLE);
                if (cashBoxInfo.hasChanges())
                    viewHolder.binding.reorderImage.setImageResource(R.drawable.ic_fiber_new_white_24dp);
                else
                    viewHolder.binding.reorderImage.setImageResource(R.drawable.ic_add);
                // Amount show
                viewHolder.binding.rvAmount.setVisibility(View.VISIBLE);
                currencyFormat.setCurrency(cashBoxInfo.getCashBoxInfo().getCurrency());
                viewHolder.binding.rvAmount.setText(currencyFormat.format(cashBoxInfo.getCash()));
                viewHolder.binding.rvAmount.setTextColor(requireContext().getColor(cashBoxInfo.getCash() < 0 ?
                        R.color.colorNegativeNumber : R.color.colorNeutralNumber));
            }

            // Update if item selected
            if (selectedItems.contains(index))
                viewHolder.itemView.setBackgroundResource(R.color.colorRVSelectedCashBox);
            else
                viewHolder.itemView.setBackgroundResource(R.color.colorRVBackgroundCashBox);
        }

        @Override
        public boolean isDragEnabled() {
            return actionMode != null && actionModeType == EDIT_MODE;
        }

        @Override
        public boolean isSwipeEnabled() {
            return SWIPE_ENABLED;
        }

        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onItemDrop(int fromPosition, int toPosition) {
            // In order for the animations to not occur, oldList and newList have to be the same
            InfoWithCash infoWithCash = currentList.remove(fromPosition);
            currentList.add(toPosition, infoWithCash);
            // Since we are manually changing the lists, manually change selectedCashBoxes
            if (selectedItems.remove(fromPosition))
                selectedItems.add(toPosition);

            compositeDisposable.add(getViewModel().moveCashBox(infoWithCash, toPosition)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe());
        }

        @Override
        public int getItemCount() {
            return currentList.size();
        }

        @Override
        public void onItemDelete(int position) {
            deleteCashBox(position);
            // since the item is deleted from swipping we have to show it back again
            notifyItemChanged(position);
        }

        @Override
        public void onItemSecondaryAction(int position) {
            if (actionMode != null)
                actionMode.finish();

            new MyDialogBuilder(requireContext())
                    .setTitle("Change Name")
                    .setView(R.layout.cash_box_input_name)
                    .setPositiveButton(R.string.cashBox_changeNameButton, null)
                    .setActions(dialog -> {
                        Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        TextInputEditText inputName = ((AlertDialog) dialog).findViewById(R.id.inputTextChangeName);
                        TextInputLayout layoutName = ((AlertDialog) dialog).findViewById(R.id.inputLayoutChangeName);
                        InfoWithCash infoWithCash = currentList.get(position);

                        inputName.setText(infoWithCash.getCashBoxInfo().getName());
                        layoutName.setCounterMaxLength(CashBoxInfo.MAX_LENGTH_NAME);
                        // Show keyboard and select the whole text
                        inputName.selectAll();
                        Util.showKeyboard(requireContext(), inputName);

                        positive.setOnClickListener((View v1) -> {
                            String newName = inputName.getText().toString();
                            try {
                                compositeDisposable.add(
                                        getViewModel().changeCashBoxName(infoWithCash, newName)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(dialog::dismiss, throwable -> {
                                                    String message = throwable instanceof UtilAppException ?
                                                            throwable.getLocalizedMessage() :
                                                            getString(R.string.nameInUse);
                                                    layoutName.setError(message);
                                                    inputName.selectAll();
                                                    Util.showKeyboard(requireContext(), inputName);
                                                }));
                            } catch (IllegalArgumentException e) {
                                layoutName.setError(e.getMessage());
                                inputName.selectAll();
                                Util.showKeyboard(requireContext(), inputName);
                            }
                        });
                    }).show();
            notifyItemChanged(position); // since the item is deleted from swipping we have to show it back again
        }

        private void showCloneDialog(int position) {
            if (!isCloneEnabled())
                return;

            CashBoxInfo cashBoxInfo = currentList.get(position).getCashBoxInfo();

            new MyDialogBuilder(requireContext())
                    .setTitle("Clone CashBox")
                    .setView(R.layout.cash_box_input_name)
                    .setPositiveButton(R.string.cashBox_cloneButton, null)
                    .setActions(dialog -> {
                        Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        TextInputEditText inputName = ((AlertDialog) dialog).findViewById(R.id.inputTextChangeName);
                        TextInputLayout layoutName = ((AlertDialog) dialog).findViewById(R.id.inputLayoutChangeName);

                        inputName.setText(cashBoxInfo.getName());
                        layoutName.setCounterMaxLength(CashBoxInfo.MAX_LENGTH_NAME);
                        // Show keyboard and select the whole text
                        inputName.selectAll();
                        Util.showKeyboard(requireContext(), inputName);

                        positive.setOnClickListener((View v1) ->
                                compositeDisposable.add(getViewModel().duplicateCashBox(cashBoxInfo.getId(),
                                        inputName.getText().toString())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(() -> {
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), "Entry Cloned",
                                                    Toast.LENGTH_SHORT).show();
                                        }, throwable -> {
                                            LogUtil.error(TAG, "Clone: ", throwable);
                                            if (throwable instanceof SQLiteConstraintException)
                                                layoutName.setError("Name in use");
                                            else
                                                layoutName.setError(throwable.getMessage());
                                            inputName.selectAll();
                                            Util.showKeyboard(requireContext(), inputName);
                                        })));
                    }).show();
        }

        private void toggleSelectedCashBox(int position, boolean multipleSelection) {
            if (multipleSelection) {
                if (!selectedItems.remove(position))
                    selectedItems.add(position);
            } else {
                for (Integer k : selectedItems) {
                    selectedItems.remove(k);
                    notifyItemChanged(k);
                }
                selectedItems.add(position);
            }
            notifyItemChanged(position);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            private final CashBoxManagerItemBinding binding;

            ViewHolder(@NonNull View view) {
                super(view);
                binding = CashBoxManagerItemBinding.bind(view);
                binding.reorderImage.setOnTouchListener((v, event) -> onImageTouch(event));

                view.setOnClickListener(this);
                view.setOnLongClickListener(this);
            }

            boolean onImageTouch(@NonNull MotionEvent event) {
                if (actionMode == null) { //Normal behavior
                    if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                        InfoWithCash infoWithCash = currentList.get(getAdapterPosition());
                        if (infoWithCash.isNew())
                            showInvitationDialog(infoWithCash);
                        else if (infoWithCash.hasChanges())
                            showChangesDialog(infoWithCash);
                        else
                            CashBoxItemFragment.getAddEntryDialog(currentList.get(getAdapterPosition()).getId(),
                                    requireContext(), getViewModel(), compositeDisposable, null)
                                    .show();
                    }
                    return true; //to consume the touch action so it does not count as a click on the view
                } else { //While in edit mode/action mode
                    if (onStartDragListener != null && event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        onStartDragListener.onStartDrag(this);
                        return true;
                    } else
                        return false;
                }
            }

            @Override
            public void onClick(View v) {
                if (actionMode != null) {
                    if (actionModeType == GROUP_ADD_MODE)
                        toggleSelectedCashBox(getAdapterPosition(), true);
                    else if (actionModeType == EDIT_MODE || actionModeType == PERIODIC_ADD_MODE)
                        toggleSelectedCashBox(getAdapterPosition(), false);
                } else {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                        toggleSelectedCashBox(getAdapterPosition(), false);
                    swapToItemFragment(currentList.get(getAdapterPosition()).getCashBoxInfo().getId());
                }
            }

            @Override
            public boolean onLongClick(View v) {
                toggleSelectedCashBox(getAdapterPosition(), false);
                startActionMode(EDIT_MODE);
                return true;
            }
        }
    }
}
