package com.utilities.vibal.utilities.activities;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.adapters.CashBoxItemRecyclerAdapter;
import com.utilities.vibal.utilities.adapters.CashBoxManagerRecyclerAdapter;
import com.utilities.vibal.utilities.adapters.CashBoxSwipeController;
import com.utilities.vibal.utilities.io.IOCashBoxManager;
import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.models.CashBoxManager;
import com.utilities.vibal.utilities.util.Util;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CashBoxItemActivity extends AppCompatActivity {

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
    private static final double MAX_SHOW_CASH = 99999999;

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
        getSupportActionBar().setTitle(cashBox.getName().toUpperCase());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set up RecyclerView
        rvCashBoxItem.setHasFixedSize(true);
        rvCashBoxItem.setLayoutManager(new LinearLayoutManager(this));
        CashBoxItemRecyclerAdapter adapter = new CashBoxItemRecyclerAdapter(cashBox, this);
        rvCashBoxItem.setAdapter(adapter);
        (new ItemTouchHelper(new CashBoxSwipeController(adapter))).attachToRecyclerView(rvCashBoxItem);

        //Set cash to the actual value
        updateCash();

        //Set up Fab
        FloatingActionButton fab = findViewById(R.id.fabCBItem);
        fab.setOnClickListener((View view) -> showAddDialog());
    }

    // Cuando se ponga el widget
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//    }

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
            itemCash.setText(getString(R.string.amountMoney, cash));
    }

    /**
     * Notifies of a change in a CashBox.
     * It updates the cash total and saves the CashBoxManger.
     */
    public void notifyCashBoxChanged() {
        updateCash();
        saveCashBoxManager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_cash_box_item, menu);

        //Set up ShareActionProvider
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share));
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

    //Have to complete it
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // called when clicked the home button
                returnResult();
                return true;
            case R.id.action_deleteAll:
                deleteAll();
                return true;
            case R.id.action_share:
                updateShareIntent();
                return true;
            case R.id.action_help:
                //Util.showHelp(this, R.string.cashBoxItem_helpTitle, R.string.cashBoxItem_help).show();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateShareIntent() {
        if(shareActionProvider!=null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, cashBox.toString())
                    .setType("text/plain");
            shareActionProvider.setShareIntent(shareIntent);
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
                                    //                    cashBoxManager.saveDataTemp(this);
                                })
                                .show();
                        //        cashBoxManager.saveDataTemp(this);
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
                    Log.d(TAG, "showAddDialog: cause" + (inputInfo.getText() == null) + (inputInfo.getText().toString().isEmpty()));
                    String input = inputAmount.getText().toString();
                    if(input.trim().isEmpty()) {
                        layoutAmount.setError("You must enter an amount");
                        inputAmount.setText("");
                    } else {
                        double amount = Double.parseDouble(inputAmount.getText().toString());
                        cashBox.add(amount, inputInfo.getText().toString(), Calendar.getInstance());
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
                    inputAmount.setText("");
                }
            });
        });
        dialog.show();
    }
}
