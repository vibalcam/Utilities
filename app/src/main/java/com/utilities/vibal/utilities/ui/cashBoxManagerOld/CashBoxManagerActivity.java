package com.utilities.vibal.utilities.ui.cashBoxManagerOld;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.models.CashBoxViewModel;
import com.utilities.vibal.utilities.ui.settings.SettingsActivity;
import com.utilities.vibal.utilities.ui.swipeController.CashBoxSwipeController;
import com.utilities.vibal.utilities.util.LogUtil;
import com.utilities.vibal.utilities.util.Util;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CashBoxManagerActivity extends AppCompatActivity {
    public static final String EXTRA_ACTION = "com.utilities.vibal.utilities.ui.cashBoxManager.action";
    public static final int ACTION_ADD_CASHBOX = 1;

    private static final String TAG = "PruebaManagerActivity";

    @BindView(R.id.lyCBM)
    CoordinatorLayout coordinatorLayout;

    CashBoxViewModel viewModel;
    CompositeDisposable disposable = new CompositeDisposable();
    private CashBoxManagerRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cash_box_manager_fragment);
        ButterKnife.bind(this);

        //Set Toolbar as ActionBar
        setSupportActionBar(findViewById(R.id.toolbarCBManager));
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            getSupportActionBar().setTitle(R.string.titleCBM);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set up RecyclerView
        RecyclerView rvCashBoxManager = findViewById(R.id.rvCashBoxManager);
        rvCashBoxManager.setHasFixedSize(true);
        rvCashBoxManager.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CashBoxManagerRecyclerAdapter(this);
        rvCashBoxManager.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new CashBoxSwipeController(adapter,
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean("swipeLeftDelete", true)));
        itemTouchHelper.attachToRecyclerView(rvCashBoxManager);
        adapter.setOnStartDragListener(itemTouchHelper::startDrag);

        // Initialize data
        viewModel = ViewModelProviders.of(this).get(CashBoxViewModel.class);
        viewModel.getCashBoxesInfo().observe(this, (List<CashBox.CashBoxInfo> cashBoxesInfo) -> {
//            adapter.submitList(cashBoxesInfo);
            Toast.makeText(CashBoxManagerActivity.this, "on changed", Toast.LENGTH_LONG).show(); //TODO
        });

        // Look at intent
        doIntentAction();

        LogUtil.debug(TAG, "onCreate: ");
    }

    private void doIntentAction() {
        Intent intent = getIntent();
        if (intent.getIntExtra(EXTRA_ACTION, 0) == 1)
            showAddDialog();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Clear all subscriptions
        disposable.clear();
        LogUtil.debug(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.debug(TAG, "onDestroy: ");
    }

    @OnClick(R.id.fabCBManager)
    void onFabClicked() {
        showAddDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_cash_box_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_manager_deleteAll:
                deleteAll();
                return true;
            case R.id.action_manager_help:
                Util.getHelpDialog(this, R.string.cashBoxManager_helpTitle, R.string.cashBoxManager_help).show();
                return true;
            case R.id.action_manager_reorder:
                return adapter.showActionMode();
            case R.id.action_manager_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAll() {
        if(adapter.getItemCount()==0) {
            Toast.makeText(this, "No entries to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirmDeleteAllDialog)
                .setMessage("Are you sure you want to delete all entries? This action CANNOT be undone")
                .setNegativeButton(R.string.cancelDialog, null)
                .setPositiveButton(R.string.confirmDeleteDialogConfirm, (DialogInterface dialog, int which) ->
                        disposable.add(viewModel.deleteAllCashBoxes()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(integer -> Toast.makeText(this,
                                        "Deleted all entries", Toast.LENGTH_SHORT).show())))
                .show();
    }

    private void showAddDialog() {
        if (adapter.actionMode != null)
            adapter.actionMode.finish();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

            Util.showKeyboard(CashBoxManagerActivity.this, inputTextName);
            positive.setOnClickListener(v -> {
                inputLayoutInitCash.setError(null);
                inputLayoutName.setError(null);
                try {
                    CashBox cashBox = new CashBox(inputTextName.getText().toString());
                    String strInitCash = inputTextInitCash.getText().toString().trim();
                    if (!strInitCash.isEmpty() && Util.parseDouble(strInitCash) != 0)
                        cashBox.add(Util.parseDouble(strInitCash), "Initial Amount", Calendar.getInstance());

                    disposable.add(viewModel.addCashBox(cashBox)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(dialog1::dismiss, throwable -> {
                                LogUtil.error(TAG,"Error in add", throwable);
                                inputLayoutName.setError(CashBoxManagerActivity.this.getString(R.string.nameInUse));
                                inputTextName.selectAll();
                                Util.showKeyboard(CashBoxManagerActivity.this, inputTextName);
                            }));
                } catch (NumberFormatException e) {
                    LogUtil.error(TAG,"Error in add", e);
                    inputLayoutInitCash.setError(CashBoxManagerActivity.this.getString(R.string.errorMessageAmount));
                    inputTextInitCash.selectAll();
                    Util.showKeyboard(CashBoxManagerActivity.this, inputTextInitCash);
                } catch (IllegalArgumentException e) {
                    LogUtil.error(TAG,"Error in add", e);
                    inputLayoutName.setError(e.getMessage());
                    inputTextName.selectAll();
                    Util.showKeyboard(CashBoxManagerActivity.this, inputTextName);
                }
            });
        });
        dialog.show();
    }
}
