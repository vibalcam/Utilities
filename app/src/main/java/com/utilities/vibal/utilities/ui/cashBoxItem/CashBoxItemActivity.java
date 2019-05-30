package com.utilities.vibal.utilities.ui.cashBoxItem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuItemCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.io.IOCashBoxManager;
import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.models.CashBoxManager;
import com.utilities.vibal.utilities.ui.cashBoxManager.CashBoxManagerRecyclerAdapter;
import com.utilities.vibal.utilities.ui.settings.SettingsActivity;
import com.utilities.vibal.utilities.ui.swipeController.CashBoxSwipeController;
import com.utilities.vibal.utilities.util.LogUtil;
import com.utilities.vibal.utilities.util.Util;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CashBoxItemActivity extends AppCompatActivity {
    private static final double MAX_SHOW_CASH = 99999999;
    private static final String TAG = "PruebaItemActivity";

    @BindView(R.id.rvCashBoxItem)
    RecyclerView rvCashBoxItem;
    @BindView(R.id.itemCash)
    TextView itemCash;
    @BindView(R.id.itemCBCoordinatorLayout)
    CoordinatorLayout itemCBCoordinatorLayout;
    NumberFormat formatCurrency = NumberFormat.getCurrencyInstance();
    private CashBoxManager cashBoxManager;
    private CashBox cashBox;
    private ShareActionProvider shareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.debug(TAG, "on create:");
        setContentView(R.layout.activity_cash_box_item);
        ButterKnife.bind(this);

        //Get data
        Intent intent = getIntent();
        int cashBoxIndex = intent.getIntExtra(CashBoxManagerRecyclerAdapter.INDEX_EXTRA, 0);
        cashBoxManager = intent.getParcelableExtra(CashBoxManagerRecyclerAdapter.CASHBOX_MANAGER_EXTRA);
        cashBox = cashBoxManager.get(cashBoxIndex);

        //Set Toolbar as ActionBar
        setSupportActionBar(findViewById(R.id.toolbarCBItem));
        LogUtil.debug(TAG, "on create: titulo: " + cashBox.getName());
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setTitle(cashBox.getName());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Set up RecyclerView
//        rvCashBoxItem.setNestedScrollingEnabled(true);
        rvCashBoxItem.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvCashBoxItem.setLayoutManager(layoutManager);
        CashBoxItemRecyclerAdapter adapter = new CashBoxItemRecyclerAdapter(cashBox, this);
        rvCashBoxItem.setAdapter(adapter);
        boolean swipeLeftDelete = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("swipeDelete", true);
        (new ItemTouchHelper(new CashBoxSwipeController(adapter, swipeLeftDelete))).attachToRecyclerView(rvCashBoxItem);
        rvCashBoxItem.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        //Set cash to the actual value
        updateCash();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        formatCurrency = NumberFormat.getCurrencyInstance();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Rename the temporary file to the actual store file
        IOCashBoxManager.renameCashBoxManagerTemp(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.debug(TAG, "onDestroy: ");
    }

    @OnClick(R.id.fabCBItem)
    void onFabClicked() {
        showAddDialog();
    }

    public void saveCashBoxManager() {
        try {
            IOCashBoxManager.saveCashBoxManagerTemp(cashBoxManager, this);
        } catch (IOException e) {
            LogUtil.error(TAG, "onStop: error save", e);
            e.printStackTrace();
        }
    }

    public void updateCash() {
        double cash = cashBox.getCash();
        if (Math.abs(cash) > MAX_SHOW_CASH)
            itemCash.setText(R.string.outOfRange);
        else
            itemCash.setText(formatCurrency.format(cash));
    }

    /**
     * Notifies of a change in a CashBox.
     * It updates the cash total and saves the CashBoxManger.
     */
    public void notifyCashBoxChanged() {
        updateCash();
        updateShareIntent();
        saveCashBoxManager();
    }

    private void updateShareIntent() {
        if (shareActionProvider != null)
            shareActionProvider.setShareIntent(Util.getShareIntent(cashBox));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_cash_box_item, menu);

        // Set up ShareActionProvider
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_item_share));
        updateShareIntent();
        return true;
    }

    private void returnResult() {
        Intent intent = new Intent();
        intent.putExtra(CashBoxManagerRecyclerAdapter.CASHBOX_MANAGER_EXTRA, (Parcelable) cashBoxManager);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        returnResult();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // called when clicked the home button
                returnResult();
                return true;
            case R.id.action_item_deleteAll:
                deleteAll();
                return true;
            case R.id.action_item_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAll() {
        if (!cashBox.isEmpty()) {
            List<CashBox.Entry> entryList = cashBox.clear();
            int size = entryList.size();
            rvCashBoxItem.getAdapter().notifyItemRangeRemoved(0, size);
            Snackbar.make(itemCBCoordinatorLayout, getString(R.string.snackbarEntriesDeleted, size), Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    cashBox.addAll(entryList);
                    rvCashBoxItem.getAdapter().notifyItemRangeInserted(0, size);
                    notifyCashBoxChanged();
                })
                .show();
            notifyCashBoxChanged();
        } else
            Toast.makeText(this, R.string.noEntriesDelete, Toast.LENGTH_SHORT)
                    .show();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setTitle(R.string.newEntry)
                .setView(R.layout.entry_cash_box_item_input)
                .setNegativeButton(R.string.cancelDialog, null)
                .setPositiveButton(R.string.addEntryDialog, null)
                .create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnShowListener((DialogInterface dialog1) -> {
            Button positive = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            TextInputEditText inputInfo = ((AlertDialog) dialog1).findViewById(R.id.inputTextInfo);
            TextInputEditText inputAmount = ((AlertDialog) dialog1).findViewById(R.id.inputTextAmount);
            TextInputLayout layoutAmount = ((AlertDialog) dialog1).findViewById(R.id.inputLayoutAmount);

            Util.showKeyboard(this, inputAmount);
            positive.setOnClickListener((View v) -> {
                try {
                    String input = inputAmount.getText().toString().trim();
                    if (input.isEmpty()) {
                        layoutAmount.setError(CashBoxItemActivity.this.getString(R.string.required));
                        Util.showKeyboard(CashBoxItemActivity.this, inputAmount);
                    } else {
                        double amount = Util.parseDouble(inputAmount.getText().toString());
                        cashBox.add(amount, inputInfo.getText().toString().trim(), Calendar.getInstance());
                        //                    rvCashBoxItem.getAdapter().notifyItemInserted(cashBox.sizeEntries() - 1);
                        rvCashBoxItem.getAdapter().notifyItemInserted(0);
//                        updateCash();
                        dialog1.dismiss();
                        rvCashBoxItem.scrollToPosition(0);
                        //                    cashBoxManager.saveDataTemp(this);
//                        saveCashBoxManager();
                        notifyCashBoxChanged();
                    }
                } catch (NumberFormatException e) {
                    layoutAmount.setError(CashBoxItemActivity.this.getString(R.string.errorMessageAmount));
                    inputAmount.selectAll();
                    Util.showKeyboard(this, inputAmount);
                }
            });
        });
        dialog.show();
    }
}
