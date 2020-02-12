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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.vibal.utilities.R;
import com.vibal.utilities.models.CashBoxManager;
import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.modelsNew.CashBoxInfo;
import com.vibal.utilities.modelsNew.PeriodicEntryPojo;
import com.vibal.utilities.ui.settings.SettingsActivity;
import com.vibal.utilities.ui.swipeController.CashBoxAdapterSwipable;
import com.vibal.utilities.ui.swipeController.CashBoxSwipeController;
import com.vibal.utilities.ui.swipeController.OnStartDragListener;
import com.vibal.utilities.util.DiffCallback;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.util.Util;
import com.vibal.utilities.viewModels.CashBoxViewModel;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.ACTION_ADD_CASHBOX;
import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.ACTION_DETAILS;
import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.CASHBOX_MANAGER_PREFERENCE;
import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.EXTRA_ACTION;
import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.EXTRA_CASHBOX_ID;
import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.GROUP_ADD_MODE_KEY;
import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.GROUP_ID_COUNT_KEY;
import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.NO_ACTION;

public class CashBoxManagerFragment extends Fragment {
    private static final String TAG = "PruebaManagerFragment";

    @BindView(R.id.lyCBM)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.fabCBM_main)
    FloatingActionButton fabMain;
    @BindView(R.id.fabCBM_groupAdd)
    FloatingActionButton fabGroupAdd;
    @BindView(R.id.fabCBM_singleAdd)
    FloatingActionButton fabSingleAdd;
    @BindView(R.id.bgFabMenu_CBM)
    View viewBgFabMenu;

    private CashBoxViewModel viewModel;
    private CashBoxManagerRecyclerAdapter adapter;
    private boolean isFabOpen = false;
//    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    // Contextual toolbars
    @Nullable
    private ActionMode groupAddActionMode;
    private final ActionMode.Callback groupAddModeCallback = new ActionMode.Callback() {
        private boolean dialogOpened = false;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_contextual_group_add_cbm, menu);
            mode.setTitle("Choose CashBoxes:");
            //Hide fab
            fabMain.animate().alpha(0f);
            fabMain.setVisibility(View.GONE);
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
            if (item.getItemId() == R.id.action_add_group_done) {
                if (adapter.selectedItems.size() > 0) {
                    dialogOpened = true;
                    showGroupAddDialog();
                } else
                    Toast.makeText(getContext(), "No items selected", Toast.LENGTH_SHORT).show();
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            groupAddActionMode = null;
            //Show fab
            fabMain.setVisibility(View.VISIBLE);
            fabMain.animate().alpha(1f);
            //If menu was not clicked, clear selection and notify adapter to show images again
            if (!dialogOpened)
                adapter.selectedItems.clear();
            LogUtil.debug(TAG, "" + adapter.getItemCount() + adapter.selectedItems.toString());
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
//            if(!dialogOpened) {
//                for(Integer k:adapter.selectedItems) {
//                    adapter.selectedItems.remove(k);
//                    adapter.notifyItemChanged(k);
//                }
////                adapter.selectedItems.clear();
////                adapter.notifyItemRangeChanged(0, adapter.getItemCount());
//            }
        }
    };

    @Nullable
    private ActionMode actionMode;
    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(@NonNull ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_contextual_toolbar_cash_box_manager, menu);

            //Hide fab
            fabMain.animate().alpha(0f);
            fabMain.setVisibility(View.GONE);

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
                Toast.makeText(CashBoxManagerFragment.this.getContext(), "No item selected", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                if (adapter.selectedItems.size() > 1) //should never happen
                    throw new RuntimeException("Selected Items size has to be 1");
                int position = adapter.selectedItems.iterator().next();

                //Show fab
                fabMain.setVisibility(View.VISIBLE);
                fabMain.animate().alpha(1f);

                switch (item.getItemId()) {
                    case R.id.action_manager_duplicate:
                        adapter.showCloneDialog(position);
                        mode.finish();
                        return true;
                    case R.id.action_manager_addPeriodic:
                        showAddPeriodicDialog(adapter.currentList.get(position));
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            adapter.selectedItems.clear();
            //Show fab
            fabMain.setVisibility(View.VISIBLE);
            fabMain.animate().alpha(1f);
            // Notify adapter to hide images for dragging
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        }
    };

    @NonNull
    static CashBoxManagerFragment newInstance() {
        return new CashBoxManagerFragment();
    }

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
        View view = inflater.inflate(R.layout.cash_box_manager_fragment, container, false);
        ButterKnife.bind(this, view);

        // Set up RecyclerView
        RecyclerView rvCashBoxManager = view.findViewById(R.id.rvCashBoxManager);
        rvCashBoxManager.setHasFixedSize(true);
        rvCashBoxManager.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CashBoxManagerRecyclerAdapter();
        rvCashBoxManager.setAdapter(adapter);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()));
//        CashBoxSwipeController swipeController = new CashBoxSwipeController(adapter,
//                preferences.getBoolean("swipeLeftDelete", true));
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeController);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new CashBoxSwipeController(adapter,
                preferences));
        itemTouchHelper.attachToRecyclerView(rvCashBoxManager);
        adapter.setOnStartDragListener(itemTouchHelper::startDrag);

        //Register listener for settings change
//        preferenceChangeListener = (sharedPreferences, s) -> {
//            if (s.equals("swipeLeftDelete"))
//                swipeController.setSwipeLeftDelete(sharedPreferences.getBoolean("swipeLeftDelete", true));
//        };
//        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        LogUtil.debug(TAG, "onCreate: ");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogUtil.debug(TAG, "onActivityCreated: ");

        AppCompatActivity activity = (AppCompatActivity) Objects.requireNonNull(getActivity());
        // Initialize data
        viewModel = new ViewModelProvider(activity).get(CashBoxViewModel.class);
        viewModel.getCashBoxesInfo().observe(getViewLifecycleOwner(), infoWithCashes ->
                adapter.submitList(infoWithCashes));

        //Set Toolbar as ActionBar
//        activity.setSupportActionBar(getView().findViewById(R.id.toolbar));
//        ActionBar actionBar = activity.getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setTitle(R.string.titleCBM);
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
        activity.getSupportActionBar().setTitle(R.string.titleCBM);

        checkFileForCashBoxes();
    }

    /**
     * Left for compability with previous versions that used file storage
     */
    private void checkFileForCashBoxes() {
        viewModel.addDisposable(Maybe.create((MaybeOnSubscribe<CashBoxManager>) emitter -> {
            LogUtil.debug(TAG, "Inicio check file");
            //Check if the file exists
            File originalFile = getContext().getFileStreamPath("cashBoxManager");
            File tempFile = getContext().getFileStreamPath("cashBoxManagerTemp");
            if (!originalFile.exists() && !tempFile.exists()) {
                LogUtil.debug(TAG, "No files found");
                emitter.onComplete();
                return;
            }
            LogUtil.debug(TAG, Arrays.toString(originalFile.getParentFile().list()));

            //If it does, upload all the cashBoxes to the new DB version
            String fileName = tempFile.lastModified() > originalFile.lastModified() ?
                    "cashBoxManagerTemp" : "cashBoxManager";
            Object cashBoxManager;
            try (ObjectInputStream objectInputStream = new ObjectInputStream(getContext().openFileInput(fileName))) {
                cashBoxManager = objectInputStream.readObject();
                if (cashBoxManager instanceof CashBoxManager)
                    emitter.onSuccess((CashBoxManager) cashBoxManager);
                else
                    emitter.onComplete();
            } catch (@NonNull IOException | ClassNotFoundException e) {
                LogUtil.error(TAG, "loadData: error al leer archivo", e);
                emitter.onError(e);
            }
        }).flatMapCompletable(manager -> {
            LogUtil.debug(TAG, "Analyze cashboxmanager");
            Completable completable = Completable.complete();
            CashBox.InfoWithCash infoWithCash;
            List<CashBox.Entry> entryList;
            com.vibal.utilities.models.CashBox cashBox;
            com.vibal.utilities.models.CashBox.Entry entry;
            for (int k = 0; k < manager.size(); k++) {
                cashBox = manager.get(k);
                infoWithCash = new CashBox.InfoWithCash(cashBox.getName());
                entryList = new ArrayList<>();
                for (int i = 0; i < cashBox.sizeEntries(); i++) {
                    entry = cashBox.getEntry(i);
                    entryList.add(new CashBox.Entry(entry.getAmount(), entry.getInfo(), entry.getDate()));
                }

                completable = completable.andThen(viewModel.addCashBox(new CashBox(infoWithCash, entryList)));
                LogUtil.debug(TAG, new CashBox(infoWithCash, entryList).toString());
            }
            return completable.doOnComplete(() -> {
                //Delete files
                getContext().deleteFile("cashBoxManager");
                getContext().deleteFile("cashBoxManagerTemp");
                LogUtil.debug(TAG, "Success delete");
                LogUtil.debug(TAG, Arrays.toString(getContext().getFileStreamPath("cashBoxManager").getParentFile().list()));
            });
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtil.debug(TAG, "onStart: ");
        // Look at intent
        doIntentAction();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Delete all periodic tasks which are no longer active
        LogUtil.debug(TAG, "On stop: delete periodic inactive");
        viewModel.addDisposable(viewModel.deletePeriodicInactive()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_toolbar_cash_box_manager, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_manager_deleteAll:
                deleteAll();
                return true;
            case R.id.action_manager_help:
                Util.createHelpDialog(getContext(), R.string.cashBoxManager_helpTitle,
                        R.string.cashBoxManager_help).show();
                return true;
            case R.id.action_manager_edit:
                adapter.showActionMode();
                return true;
            case R.id.action_manager_showPeriodic:
                startActivity(new Intent(getContext(), CashBoxPeriodicActivity.class));
                return true;
            case R.id.action_manager_recycleBin:
                startActivity(new Intent(getContext(), CashBoxDeletedActivity.class));
                return true;
            case R.id.action_manager_settings:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void doIntentAction() {
        Intent intent = getActivity().getIntent();
        int action = intent == null ? NO_ACTION : intent.getIntExtra(EXTRA_ACTION, NO_ACTION);
        intent.removeExtra(EXTRA_ACTION); //So it only triggers once

        LogUtil.debug(TAG, "" + (action == ACTION_ADD_CASHBOX) + " " + (action == ACTION_DETAILS));

        if (action == ACTION_ADD_CASHBOX)
            showAddDialog();
        else if (action == ACTION_DETAILS)
            swapToItemFragment(intent.getLongExtra(EXTRA_CASHBOX_ID, CashBoxInfo.NO_CASHBOX));
    }

    private void swapToItemFragment(long cashBoxId) {
        LogUtil.debug(TAG, "Selected " + cashBoxId);
        if (cashBoxId == CashBoxInfo.NO_CASHBOX)
            return;

        viewModel.setCurrentCashBoxId(cashBoxId);
        FragmentTransaction transaction = getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right,
                        R.anim.enter_from_right, R.anim.exit_to_right);

        // In landscape, no backstack so back returns to parent activity
        View landsView = getActivity().findViewById(R.id.containerItem);
        if (landsView != null && landsView.getVisibility() == View.VISIBLE)
            transaction.replace(R.id.containerItem, CashBoxItemFragment.newInstance())
                    .commitNow();
        else
            transaction.replace(R.id.container, CashBoxItemFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
    }

    private void deleteAll() {
        int count = adapter.getItemCount();
        if (count == 0) {
            Toast.makeText(getContext(), "No entries to delete", Toast.LENGTH_SHORT).show();
            return;
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.confirmRecycleAllDialog)
                .setMessage("Are you sure you want to move all entries to the recycle bin?")
                .setNegativeButton(R.string.cancelDialog, null)
                .setPositiveButton(R.string.confirm, (DialogInterface dialog, int which) ->
                        viewModel.addDisposable(viewModel.recycleAllCashBoxes()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> Toast.makeText(getContext(),
                                        getString(R.string.snackbarEntriesMoveToRecycle, count),
                                        Toast.LENGTH_SHORT)
                                        .show())))
                .show();
    }

    @OnClick(R.id.fabCBM_main)
    void toggleFabMenu() {
        if (isFabOpen) {
            closeFabMenu();
            return;
        }

        LogUtil.debug(TAG, "Open FAB Menu");
        //Open FAB Menu
        isFabOpen = true;
        fabGroupAdd.setVisibility(View.VISIBLE);
        fabSingleAdd.setVisibility(View.VISIBLE);
        viewBgFabMenu.setVisibility(View.VISIBLE);

        //Animate
        fabMain.animate().rotation(135f);
        viewBgFabMenu.animate().alpha(1f);
        fabGroupAdd.animate()
                .translationY(-getResources().getDimension(R.dimen.standard_55))
                .rotation(0f);
        fabSingleAdd.animate()
                .translationY(-getResources().getDimension(R.dimen.standard_100))
                .rotation(0f);
    }

    @OnClick(R.id.bgFabMenu_CBM)
    void closeFabMenu() {
        LogUtil.debug(TAG, "Close FAB Menu");
        isFabOpen = false;
        //Animate
        fabMain.animate().rotation(0f);
        viewBgFabMenu.animate().alpha(0f);
        fabGroupAdd.animate()
                .translationY(0f)
                .rotation(90f);
        fabSingleAdd.animate()
                .translationY(0f)
                .rotation(90f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!isFabOpen) {
                            LogUtil.debug(TAG, "Hide fabs");
                            fabGroupAdd.setVisibility(View.GONE);
                            fabSingleAdd.setVisibility(View.GONE);
                            viewBgFabMenu.setVisibility(View.GONE);
                        }
                    }
                });

    }

    @OnClick(R.id.fabCBM_singleAdd)
    void showAddDialog() {
        closeFabMenu();
        LogUtil.debug(TAG, "Single add");
        if (actionMode != null)
            actionMode.finish();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        AlertDialog dialog = builder.setTitle(R.string.newEntry)
                .setView(R.layout.cash_box_new_input)  //use that view from folder layout
                .setNegativeButton(R.string.cancelDialog, null)
                .setPositiveButton(R.string.createCashBoxDialog, null)
                .create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnShowListener(dialog1 -> {
            Button positive = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            TextInputEditText inputTextName = ((AlertDialog) dialog1).findViewById(R.id.inputTextName);
            TextInputLayout inputLayoutName = ((AlertDialog) dialog1).findViewById(R.id.inputLayoutName);
            TextInputEditText inputTextInitCash = ((AlertDialog) dialog1).findViewById(R.id.inputTextInitCash);
            TextInputLayout inputLayoutInitCash = ((AlertDialog) dialog1).findViewById(R.id.inputLayoutInitCash);

            Util.showKeyboard(getContext(), inputTextName);
            positive.setOnClickListener(v -> {
                inputLayoutInitCash.setError(null);
                inputLayoutName.setError(null);
                try {
                    CashBox cashBox = new CashBox(inputTextName.getText().toString());
                    String strInitCash = inputTextInitCash.getText().toString().trim();
                    if (!strInitCash.isEmpty()) {
                        double initCash = Util.parseExpression(strInitCash);
                        if (initCash != 0)
                            cashBox.getEntries().add(new CashBox.Entry(initCash,
                                    "Initial Amount", Calendar.getInstance()));
                    }

                    viewModel.addDisposable(viewModel.addCashBox(cashBox)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(dialog1::dismiss, throwable -> {
                                LogUtil.error(TAG, "Error in add", throwable);
                                inputLayoutName.setError(getContext().getString(R.string.nameInUse));
                                inputTextName.selectAll();
                                Util.showKeyboard(getContext(), inputTextName);
                            }));
                } catch (NumberFormatException e) {
                    LogUtil.error(TAG, "Error in add", e);
                    inputLayoutInitCash.setError(getContext().getString(R.string.errorMessageAmount));
                    inputTextInitCash.selectAll();
                    Util.showKeyboard(getContext(), inputTextInitCash);
                } catch (IllegalArgumentException e) {
                    LogUtil.error(TAG, "Error in add", e);
                    inputLayoutName.setError(e.getMessage());
                    inputTextName.selectAll();
                    Util.showKeyboard(getContext(), inputTextName);
                }
            });
        });
        dialog.show();
    }

    @OnClick(R.id.fabCBM_groupAdd)
    void showContextualModeGroupAdd() {
        closeFabMenu();
        if (groupAddActionMode != null)
            return;
        LogUtil.debug(TAG, "Group add");

        //Choose CashBox in group
        if (actionMode != null)
            actionMode.finish();
        if (!adapter.currentList.isEmpty())
            groupAddActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(groupAddModeCallback);
        else
            Toast.makeText(getContext(), "No available CashBoxes", Toast.LENGTH_SHORT).show();
    }

    private void showGroupAddDialog() {
        if (adapter.selectedItems.size() == 0) //Should never occur
            throw new RuntimeException("At least one item should be selected");

        //Add dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        AlertDialog dialog = builder.setTitle(R.string.newGroupEntry)
                .setView(R.layout.cash_box_group_entry_input)  //use that view from folder layout
                .setNegativeButton(R.string.cancelDialog, null)
                .setPositiveButton(R.string.createCashBoxDialog, null)
                .setOnDismissListener(dialogInterface -> {
                    //Clear selection and notify adapter
                    adapter.selectedItems.clear();
                    adapter.notifyItemRangeChanged(0, adapter.getItemCount());
                }).create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnShowListener(dialogInterface -> {
            Button positive = ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE);
            TextInputEditText inputInfo = ((AlertDialog) dialogInterface).findViewById(R.id.inputTextInfo);
            TextInputEditText inputAmount = ((AlertDialog) dialogInterface).findViewById(R.id.inputTextAmount);
            TextInputLayout layoutAmount = ((AlertDialog) dialogInterface).findViewById(R.id.inputLayoutAmount);
            Spinner spinner = ((AlertDialog) dialogInterface).findViewById(R.id.spinnerModeGroupAdd);
            MaterialTextView inputDate = ((AlertDialog) dialogInterface).findViewById(R.id.inputDate);

            // Set up Date Picker
            Util.TextViewDatePickerClickListener calendarListener =
                    new Util.TextViewDatePickerClickListener(getContext(), inputDate, true);
            inputDate.setOnClickListener(calendarListener);

            SharedPreferences preferences = getActivity().getSharedPreferences(CASHBOX_MANAGER_PREFERENCE,
                    Context.MODE_PRIVATE);
            spinner.setSelection(preferences.getInt(GROUP_ADD_MODE_KEY, 0));

            //Change hint in layoutAmount
            layoutAmount.setHint(getString(R.string.hintAddGroupDialog));

            Util.showKeyboard(getContext(), inputAmount);
            positive.setOnClickListener(v -> {
                try {
                    String input = inputAmount.getText().toString().trim();
                    if (input.isEmpty()) {
                        layoutAmount.setError(getString(R.string.required));
                        Util.showKeyboard(getContext(), inputAmount);
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
                            addEntries = addEntries.andThen(viewModel.addEntry(adapter.currentList.get(k).getId(),
                                    new CashBox.Entry(amount, inputInfo.getText().toString().trim(),
                                            calendarListener.getCalendar(), groupId)));
                        viewModel.addDisposable(addEntries.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                    dialogInterface.dismiss();
                                    preferences.edit()
                                            .putLong(GROUP_ID_COUNT_KEY, groupId + 1)
                                            .putInt(GROUP_ADD_MODE_KEY, mode)
                                            .apply();
                                }));
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

    // todo change into fab
    private void showAddPeriodicDialog(@NonNull CashBox.InfoWithCash infoWithCash) {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
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
            MaterialTextView inputDate = ((AlertDialog) dialogInterface).findViewById(R.id.inputDate);

            // Set up Date Picker
            Util.TextViewDatePickerClickListener calendarListener =
                    new Util.TextViewDatePickerClickListener(getContext(), inputDate, false);
            inputDate.setOnClickListener(calendarListener);

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
                    } else {
                        try {
                            double amount = Util.parseExpression(inputAmount.getText().toString());
                            viewModel.addDisposable(viewModel.addPeriodicEntryWorkRequest(
                                    new PeriodicEntryPojo.PeriodicEntryWorkRequest(infoWithCash.getId(),
                                            amount, inputInfo.getText().toString(),
                                            Long.parseLong(inputPeriod.getText().toString()), repetitions,
                                            calendarListener.getDaysFromCurrent()))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe());
                            dialogInterface.dismiss();
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
        });
        dialog.show();
    }

    public class CashBoxManagerRecyclerAdapter extends RecyclerView.Adapter<CashBoxManagerRecyclerAdapter.ViewHolder> implements CashBoxAdapterSwipable {
        private static final boolean SWIPE_ENABLED = true;
        private static final String TAG = "PruebaManagerActivity";

        private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        private final Set<Integer> selectedItems = new HashSet<>();
        private OnStartDragListener onStartDragListener;
        @NonNull
        private List<CashBox.InfoWithCash> currentList = new ArrayList<>();
//        private LinkedList<CashBox.InfoWithCash> toDelete = new LinkedList<>();

//        // Contextual toolbar
//        @Nullable
//        private ActionMode actionMode;
//        @NonNull
//        private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
//            @Override
//            public boolean onCreateActionMode(@NonNull ActionMode mode, Menu menu) {
//                mode.getMenuInflater().inflate(R.menu.menu_contextual_toolbar_cash_box_manager, menu);
//
//                //Hide fab
//                fabMain.animate().alpha(0f);
//                fabMain.setVisibility(View.GONE);
//
//                // Notify adapter to show images for dragging
//                notifyItemRangeChanged(0, CashBoxManagerRecyclerAdapter.this.getItemCount());
//                return true;
//            }
//
//            @Override
//            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//                return false;
//            }
//
//            @Override
//            public boolean onActionItemClicked(@NonNull ActionMode mode, @NonNull MenuItem item) {
//                if (selectedItems.isEmpty()) {
//                    Toast.makeText(CashBoxManagerFragment.this.getContext(), "No item selected", Toast.LENGTH_SHORT).show();
//                    return true;
//                } else {
//                    if (selectedItems.size() > 1) //should never happen
//                        throw new RuntimeException("Selected Items size has to be 1");
//                    int position = selectedItems.iterator().next();
//
//                    //Show fab
//                    fabMain.setVisibility(View.VISIBLE);
//                    fabMain.animate().alpha(1f);
//
//                    switch (item.getItemId()) {
//                        case R.id.action_manager_duplicate:
//                            showCloneDialog(position);
//                            mode.finish();
//                            return true;
//                        case R.id.action_manager_addPeriodic:
//                            showAddPeriodicDialog(currentList.get(position));
//                            mode.finish();
//                            return true;
//                        default:
//                            return false;
//                    }
//                }
//            }
//
//            @Override
//            public void onDestroyActionMode(ActionMode mode) {
//                actionMode = null;
//                selectedItems.clear();
//                // Notify adapter to hide images for dragging
//                notifyItemRangeChanged(0, CashBoxManagerRecyclerAdapter.this.getItemCount());
//            }
//        };

        void setOnStartDragListener(OnStartDragListener onStartDragListener) {
            this.onStartDragListener = onStartDragListener;
        }

        /**
         * Submit a new list of elements for the adapter to show.
         * All changes in adapter lis must go through submitList.
         *
         * @param newList New list to be submitted
         */
        void submitList(@NonNull List<CashBox.InfoWithCash> newList) {
            LogUtil.debug(TAG, "New list submitted: " + newList.toString());

            viewModel.addDisposable(Single.just(DiffUtil.calculateDiff(
                    new DiffCallback<>(currentList, newList), false))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(diffResult -> {
                        LogUtil.debug(TAG, "DiffResult calculated");
                        currentList.clear();
                        currentList.addAll(newList);
                        diffResult.dispatchUpdatesTo(CashBoxManagerRecyclerAdapter.this);

                        //Update selectedCashBoxes
                        int temp;
                        for (Integer k : selectedItems) {
                            selectedItems.remove(k);
                            temp = diffResult.convertOldPositionToNew(k);
                            if (temp != DiffUtil.DiffResult.NO_POSITION)
                                selectedItems.add(temp);
                        }
                    }));


//            viewModel.addDisposable(Single.create((SingleOnSubscribe<List<CashBox.InfoWithCash>>) emitter -> {
//                //Delete the temporarily deleted entries
//                for(CashBox.InfoWithCash info:toDelete)
//                    newList.remove(info);
//                emitter.onSuccess(newList);
//            }).map(infoWithCashes -> DiffUtil.calculateDiff(
//                    new DiffCallback<>(currentList, infoWithCashes), false))
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(diffResult -> {
//                        LogUtil.debug(TAG, "DiffResult calculated");
//                        currentList.clear();
//                        currentList.addAll(newList);
////                        notifyDataSetChanged();
//                        diffResult.dispatchUpdatesTo(CashBoxManagerRecyclerAdapter.this);
//
//                        //Update selectedCashBoxes
//                        int temp;
//                        for(Integer k:selectedItems) {
//                            selectedItems.remove(k);
//                            temp = diffResult.convertOldPositionToNew(k);
//                            if(temp!= DiffUtil.DiffResult.NO_POSITION)
//                                selectedItems.add(temp);
//                        }
//                    }));
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
            CashBox.InfoWithCash cashBoxInfo = currentList.get(index);
            viewHolder.rvName.setText(cashBoxInfo.getCashBoxInfo().getName());

            // Enable or disable dragging
            if (isDragEnabled()) {
                viewHolder.reorderImage.setVisibility(View.VISIBLE);
                viewHolder.reorderImage.setImageResource(R.drawable.reorder_horizontal_gray_24dp);
                viewHolder.rvAmount.setVisibility(View.GONE);
            } else if (groupAddActionMode != null) {
                viewHolder.reorderImage.setVisibility(View.GONE);
                viewHolder.rvAmount.setVisibility(View.GONE);
            } else {
                viewHolder.reorderImage.setVisibility(View.VISIBLE);
                viewHolder.reorderImage.setImageResource(R.drawable.ic_add);
                viewHolder.rvAmount.setVisibility(View.VISIBLE);
                viewHolder.rvAmount.setText(currencyFormat.format(cashBoxInfo.getCash()));
                int colorRes = cashBoxInfo.getCash() < 0 ? R.color.colorNegativeNumber : R.color.colorPositiveNumber;
                viewHolder.rvAmount.setTextColor(getActivity().getColor(colorRes));
            }

            // Update if item selected
            if (selectedItems.contains(index))
                viewHolder.itemView.setBackgroundResource(R.color.colorRVSelectedCashBox);
            else
                viewHolder.itemView.setBackgroundResource(R.color.colorRVBackgroundCashBox);
        }

        @Override
        public boolean isDragEnabled() {
            return actionMode != null;
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
            CashBox.InfoWithCash infoWithCash = currentList.remove(fromPosition);
            currentList.add(toPosition, infoWithCash);
            viewModel.addDisposable(viewModel.moveCashBox(infoWithCash, toPosition)
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
            if (actionMode != null)
                actionMode.finish();
            LogUtil.debug(TAG, "Delete CashBox");
            viewModel.addDisposable(viewModel.recycleCashBoxInfo(currentList.get(position))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Toast.makeText(getContext(),
                            getString(R.string.snackbarEntriesMoveToRecycle, 1),
                            Toast.LENGTH_SHORT)
                            .show()));


//            if (actionMode != null)
//                actionMode.finish();
////            if(selectedViewHolder!=null && selectedViewHolder.getAdapterPosition()==position)
////                selectedViewHolder=null;
////            CashBox.InfoWithCash deletedCashBoxInfo = currentList.remove(position);
////            notifyItemRemoved(position);
//            CashBox.InfoWithCash removed = currentList.get(position);
//            List<CashBox.InfoWithCash> list = new ArrayList<>(currentList);
//            toDelete.add(removed);
//            submitList(new ArrayList<>(currentList));
//            Snackbar.make(coordinatorLayout,
//                    getString(R.string.snackbarEntriesDeleted, 1), Snackbar.LENGTH_LONG)
//                    .setAction(R.string.undo, v -> {
//                        toDelete.removeFirst();
//                        submitList(list);
////                        currentList.add(position, deletedCashBoxInfo);
////                        notifyItemInserted(position);
//                    }).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
//                @Override
//                public void onDismissed(Snackbar transientBottomBar, int event) {
//                    super.onDismissed(transientBottomBar, event);
//
//                    if (event != DISMISS_EVENT_ACTION) {
//                        LogUtil.debug(TAG, "Delete CashBox");
////                        viewModel.addDisposable(viewModel.recycleCashBoxInfo(deletedCashBoxInfo)
//                        viewModel.addDisposable(viewModel.recycleCashBoxInfo(toDelete.removeFirst())
//                                .subscribeOn(Schedulers.io())
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe());
//                    }
//                }
//            }).show();
        }

        @Override
        public void onItemSecondaryAction(int position) {
            if (actionMode != null)
                actionMode.finish();

            AlertDialog dialogChangeName = inputNameDialog("Change Name", R.string.cashBox_changeNameButton);
            dialogChangeName.setOnShowListener(dialog -> {
                Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                TextInputEditText inputName = ((AlertDialog) dialog).findViewById(R.id.inputTextChangeName);
                TextInputLayout layoutName = ((AlertDialog) dialog).findViewById(R.id.inputLayoutChangeName);
                CashBox.InfoWithCash infoWithCash = currentList.get(position);

                inputName.setText(infoWithCash.getCashBoxInfo().getName());
                layoutName.setCounterMaxLength(CashBoxInfo.MAX_LENGTH_NAME);
                // Show keyboard and select the whole text
                inputName.selectAll();
                Util.showKeyboard(getContext(), inputName);

                positive.setOnClickListener((View v1) -> {
                    String newName = inputName.getText().toString();
                    try {
                        viewModel.addDisposable(
                                viewModel.changeCashBoxName(infoWithCash, newName)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(dialog::dismiss, throwable -> {
                                            layoutName.setError(getString(R.string.nameInUse));
                                            inputName.selectAll();
                                            Util.showKeyboard(getContext(), inputName);
                                        }));
                    } catch (IllegalArgumentException e) {
                        layoutName.setError(e.getMessage());
                        inputName.selectAll();
                        Util.showKeyboard(getContext(), inputName);
                    }
                });
            });
            dialogChangeName.show();
            notifyItemChanged(position); // since the item is deleted from swipping we have to show it back again
        }

        @NonNull
        private AlertDialog inputNameDialog(String title, int resPositiveButton) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            AlertDialog dialog = builder.setTitle(title)
                    .setView(R.layout.cash_box_input_name)
                    .setNegativeButton(R.string.cancelDialog, null)
                    .setPositiveButton(resPositiveButton, null)
                    .create();
            dialog.setCanceledOnTouchOutside(false);

            return dialog;
        }

        private void showCloneDialog(int position) {
            CashBoxInfo cashBoxInfo = currentList.get(position).getCashBoxInfo();

            AlertDialog dialogClone = inputNameDialog("Clone CashBox", R.string.cashBox_cloneButton);
            dialogClone.setOnShowListener(dialog -> {
                Button positive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                TextInputEditText inputName = ((AlertDialog) dialog).findViewById(R.id.inputTextChangeName);
                TextInputLayout layoutName = ((AlertDialog) dialog).findViewById(R.id.inputLayoutChangeName);

                inputName.setText(cashBoxInfo.getName());
                layoutName.setCounterMaxLength(CashBoxInfo.MAX_LENGTH_NAME);
                // Show keyboard and select the whole text
                inputName.selectAll();
                Util.showKeyboard(getContext(), inputName);

                positive.setOnClickListener((View v1) ->
                        viewModel.addDisposable(viewModel.duplicateCashBox(cashBoxInfo.getId(),
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
                                    Util.showKeyboard(getContext(), inputName);
                                })));
            });
            dialogClone.show();
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

        boolean showActionMode() {
            if (actionMode != null)
                return false;
            if (groupAddActionMode != null)
                groupAddActionMode.finish();
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);
            return true;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            @BindView(R.id.rvName)
            TextView rvName;
            @BindView(R.id.rvAmount)
            TextView rvAmount;
            @BindView(R.id.reorderImage)
            ImageView reorderImage;

            ViewHolder(@NonNull View view) {
                super(view);
                ButterKnife.bind(this, view);

                view.setOnClickListener(this);
                view.setOnLongClickListener(this);
            }

            @OnTouch(R.id.reorderImage)
            boolean onImageTouch(@NonNull MotionEvent event) {
                if (actionMode == null) { //Normal behavior
                    if (event.getActionMasked() == MotionEvent.ACTION_UP)
                        CashBoxItemFragment.getAddEntryDialog(currentList.get(getAdapterPosition()).getId(),
                                getContext(), viewModel)
                                .show();
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
                if (groupAddActionMode != null)
                    toggleSelectedCashBox(getAdapterPosition(), true);
                else if (actionMode != null)
                    toggleSelectedCashBox(getAdapterPosition(), false);
                else {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                        toggleSelectedCashBox(getAdapterPosition(), false);
                    swapToItemFragment(currentList.get(getAdapterPosition()).getCashBoxInfo().getId());
                }
            }

            @Override
            public boolean onLongClick(View v) {
                toggleSelectedCashBox(getAdapterPosition(), false);
                showActionMode();
                return true;
            }
        }
    }
}
