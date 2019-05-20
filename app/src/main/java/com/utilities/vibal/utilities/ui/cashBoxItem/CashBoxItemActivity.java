package com.utilities.vibal.utilities.ui.cashBoxItem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.io.IOCashBoxManager;
import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.models.CashBoxManager;
import com.utilities.vibal.utilities.ui.cashBoxManager.CashBoxManagerRecyclerAdapter;
import com.utilities.vibal.utilities.ui.swipeController.CashBoxSwipeController;
import com.utilities.vibal.utilities.util.Util;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CashBoxItemActivity extends AppCompatActivity {
    private static final double MAX_SHOW_CASH = 99999999;

    @BindView(R.id.toolbarCBItem)
    Toolbar toolbarCBItem;
    @BindView(R.id.rvCashBoxItem)
    RecyclerView rvCashBoxItem;
    @BindView(R.id.itemCash)
    TextView itemCash;
    @BindView(R.id.itemCBCoordinatorLayout)
    CoordinatorLayout itemCBCoordinatorLayout;

    private CashBoxManager cashBoxManager;
    private CashBox cashBox;
    private ShareActionProvider shareActionProvider;

    private static final String TAG = "PruebaItemActivity";

    NumberFormat formatCurrency = NumberFormat.getCurrencyInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "on create:");
        setContentView(R.layout.activity_cash_box_item);
        ButterKnife.bind(this);

        //Get data
        Intent intent = getIntent();
        int cashBoxIndex = intent.getIntExtra(CashBoxManagerRecyclerAdapter.STRING_EXTRA, 0);
        cashBoxManager = (CashBoxManager) intent.getSerializableExtra(CashBoxManagerRecyclerAdapter.CASHBOX_MANAGER_EXTRA);
//        cashBoxManager = CashBoxManager.loadData(this);
        cashBox = cashBoxManager.get(cashBoxIndex);

        //Set Toolbar as ActionBar
        setSupportActionBar(toolbarCBItem);
        Log.d(TAG, "on create: titulo: " + cashBox.getName());
        getSupportActionBar().setTitle(cashBox.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set up RecyclerView
//        rvCashBoxItem.setNestedScrollingEnabled(true);
        rvCashBoxItem.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(rvCashBoxItem.getContext());
        rvCashBoxItem.setLayoutManager(layoutManager);
        CashBoxItemRecyclerAdapter adapter = new CashBoxItemRecyclerAdapter(cashBox, this);
        rvCashBoxItem.setAdapter(adapter);
        (new ItemTouchHelper(new CashBoxSwipeController(adapter))).attachToRecyclerView(rvCashBoxItem);
        rvCashBoxItem.addItemDecoration(new DividerItemDecoration(rvCashBoxItem.getContext(),layoutManager.getOrientation()));

        //Set cash to the actual value
        updateCash();

        //Set up Fab
        FloatingActionButton fab = findViewById(R.id.fabCBItem);
        fab.setOnClickListener((View view) -> showAddDialog());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        formatCurrency = NumberFormat.getCurrencyInstance();
    }

    @Override
    protected void onStop() {
        super.onStop();

//        // Save data
//        saveCashBoxManager();
        // Rename the temporary file to the actual store file
        IOCashBoxManager.renameCashBoxManagerTemp(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    public RecyclerView getRecyclerView() {
        return rvCashBoxItem;
    }

//    private Context getContext() {
//        return this;
//    }

    public void saveCashBoxManager() {
        try {
            IOCashBoxManager.saveCashBoxManagerTemp(cashBoxManager,this);
        } catch (IOException e) {
            Log.e(TAG, "onStop: error save", e);
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
        if(shareActionProvider!=null)
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
        intent.putExtra(CashBoxManagerRecyclerAdapter.CASHBOX_MANAGER_EXTRA,cashBoxManager);
        setResult(RESULT_OK,intent);
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAll() {
        if (!cashBox.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.confirmDeleteAllDialog)
                    .setMessage("Are you sure you want to delete all entries? This action CANNOT be undone")
                    .setNegativeButton(R.string.cancelDialog, null)
                    .setPositiveButton(R.string.confirmDeleteDialogConfirm, (DialogInterface dialog, int which) -> {
                        List<CashBox.Entry> entryList = cashBox.clear();
                        int size = entryList.size();
                        rvCashBoxItem.getAdapter().notifyItemRangeRemoved(0, size);
//                        updateCash();
                        Snackbar.make(itemCBCoordinatorLayout, getString(R.string.snackbarEntriesDeleted, size), Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, v -> {
                                    cashBox.addAll(entryList);
                                    rvCashBoxItem.getAdapter().notifyItemRangeInserted(0, size);
                                    notifyCashBoxChanged();
//                                    updateCash();
//                                    saveCashBoxManager();
                                })
                                .show();
//                        saveCashBoxManager();
                        notifyCashBoxChanged();
                    }).show();
        } else
            Toast.makeText(this, "No entries to delete", Toast.LENGTH_SHORT)
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
                    if(input.isEmpty()) {
                        layoutAmount.setError("*Required");
                        Util.showKeyboard(this, inputAmount);
                    } else {
                        double amount = Double.parseDouble(inputAmount.getText().toString());
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
                    layoutAmount.setError("Not a valid number");
                    inputAmount.selectAll();
                    Util.showKeyboard(this, inputAmount);
                }
            });
        });
        dialog.show();
    }
}
