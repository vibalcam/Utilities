package com.utilities.vibal.utilities.ui.cashBoxManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import com.utilities.vibal.utilities.models.CashBoxManager;
import com.utilities.vibal.utilities.ui.cashBoxItem.CashBoxItemActivity;
import com.utilities.vibal.utilities.ui.settings.SettingsActivity;
import com.utilities.vibal.utilities.ui.swipeController.CashBoxSwipeController;
import com.utilities.vibal.utilities.util.LogUtil;
import com.utilities.vibal.utilities.util.Util;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CashBoxManagerActivity extends AppCompatActivity {
    public static final String EXTRA_ACTION = "com.utilities.vibal.utilities.ui.cashBoxManager.action";
    public static final int ACTION_ADD_CASHBOX = 1;

    private static final String TAG = "PruebaManagerActivity";

    @BindView(R.id.lyCBM)
    CoordinatorLayout coordinatorLayout;

    CashBoxViewModel cashBoxViewModel;
    private CashBoxManagerRecyclerAdapter adapter;
    private CashBoxManager cashBoxManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_box_manager);
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
                        .getBoolean("swipeDelete", true)));
        itemTouchHelper.attachToRecyclerView(rvCashBoxManager);
        adapter.setOnStartDragListener(itemTouchHelper::startDrag);

        // Initialize data TODO
//        cashBoxManager = IOCashBoxManager.loadCashBoxManager(this);
        cashBoxManager = new CashBoxManager();

        cashBoxViewModel = ViewModelProviders.of(this).get(CashBoxViewModel.class);
        cashBoxViewModel.getCashBoxes().observe(this, (List<CashBox> cashBoxes) -> {
            adapter.setCashBoxes(cashBoxes);
            Toast.makeText(CashBoxManagerActivity.this, "on changed", Toast.LENGTH_LONG).show();
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

    // Cuando se ponga el widget
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        // Reload the data
////        cashBoxManager = IOCashBoxManager.loadCashBoxManager(this);
//        LogUtil.debug(TAG, "onRestart: ");
//    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.debug(TAG, "onStop: ");
//        IOCashBoxManager.renameCashBoxManagerTemp(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.debug(TAG, "onDestroy: ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        LogUtil.debug(TAG, "onActivityResult: " + cashBoxManager.toString());
        if (data != null && requestCode == CashBoxManagerRecyclerAdapter.REQUEST_CODE_ITEM && resultCode == RESULT_OK) {
            cashBoxManager = data.getParcelableExtra(CashBoxItemActivity.EXTRA_CASHBOX_MANAGER);
//            adapter.setCashBoxes(cashBoxManager); TODO
        }
        LogUtil.debug(TAG, "onActivityResult: " + cashBoxManager.toString());

        super.onActivityResult(requestCode, resultCode, data);
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

    //TODO
    void saveCashBoxManager() {
//        try {
//            IOCashBoxManager.saveCashBoxManagerTemp(cashBoxManager, this);
//        } catch (IOException e) {
//            LogUtil.error(TAG, "onStop: error save", e);
//            e.printStackTrace();
//        }
    }

    private void deleteAll() {
        if (!cashBoxManager.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.confirmDeleteAllDialog)
                    .setMessage("Are you sure you want to delete all entries? This action CANNOT be undone")
                    .setNegativeButton(R.string.cancelDialog, null)
                    .setPositiveButton(R.string.confirmDeleteDialogConfirm, (DialogInterface dialog, int which) -> {
                        int size = cashBoxManager.size();
                        cashBoxManager.clear();
                        adapter.notifyItemRangeRemoved(0, size);
                        saveCashBoxManager();
                        Toast.makeText(this, "Deleted all entries", Toast.LENGTH_SHORT).show();
                    }).show();
        } else
            Toast.makeText(this, "No entries to delete", Toast.LENGTH_SHORT).show();
    }

    private void showAddDialog() {
        if (adapter.actionMode != null)
            adapter.actionMode.finish();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setTitle(R.string.newEntry)
                .setView(R.layout.new_cash_box_input)  //use that view from folder layout
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
                    if (cashBoxManager.add(cashBox)) {
                        adapter.notifyItemInserted(cashBoxManager.size() - 1);
                        dialog1.dismiss();
                        saveCashBoxManager();
                    } else {
                        inputLayoutName.setError(CashBoxManagerActivity.this.getString(R.string.nameInUse));
                        inputTextName.selectAll();
                        Util.showKeyboard(CashBoxManagerActivity.this, inputTextName);
                    }
                } catch (NumberFormatException e) {
                    inputLayoutInitCash.setError(CashBoxManagerActivity.this.getString(R.string.errorMessageAmount));
                    inputTextInitCash.selectAll();
                    Util.showKeyboard(CashBoxManagerActivity.this, inputTextInitCash);
                } catch (IllegalArgumentException e) {
                    inputLayoutName.setError(e.getMessage());
                    inputTextName.selectAll();
                    Util.showKeyboard(CashBoxManagerActivity.this, inputTextName);
                }
            });
        });
        dialog.show();
    }
}
