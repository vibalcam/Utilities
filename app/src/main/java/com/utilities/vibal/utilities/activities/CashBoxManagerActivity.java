package com.utilities.vibal.utilities.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.adapters.CashBoxManagerRecyclerAdapter;
import com.utilities.vibal.utilities.adapters.CashBoxSwipeController;
import com.utilities.vibal.utilities.io.IOCashBoxManager;
import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.models.CashBoxManager;
import com.utilities.vibal.utilities.util.Util;

import java.io.IOException;
import java.util.Calendar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CashBoxManagerActivity extends AppCompatActivity {
    private static final String TAG = "PruebaManagerActivity";

    @BindView(R.id.rvCashBoxManager)
    RecyclerView rvCashBoxManager;
    @BindView(R.id.toolbarCBManager)
    Toolbar toolbarCBManager;
    @BindView(R.id.lyCBM)
    CoordinatorLayout lyCBM;

    private CashBoxManager cashBoxManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_box_manager);
        ButterKnife.bind(this);

        //Set Toolbar as ActionBar
        setSupportActionBar(toolbarCBManager);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initialize data
        cashBoxManager = IOCashBoxManager.loadCashBoxManager(getContext());
        CashBox.PLACE_HOLDER_AMOUNT = getString(R.string.amountMoney);

        //Set up RecyclerView
        rvCashBoxManager.setHasFixedSize(true);
        rvCashBoxManager.setLayoutManager(new LinearLayoutManager(this));
        CashBoxManagerRecyclerAdapter adapter = new CashBoxManagerRecyclerAdapter(cashBoxManager, this);
        rvCashBoxManager.setAdapter(adapter);
        (new ItemTouchHelper(new CashBoxSwipeController(adapter))).attachToRecyclerView(rvCashBoxManager);

        //Set up fab
        FloatingActionButton fab = findViewById(R.id.fabCBManager);
        fab.setOnClickListener((View view) -> showAddDialog());

        Log.d(TAG, "onCreate: ");
    }

    // Cuando se ponga el widget
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        // Reload the data
////        cashBoxManager = IOCashBoxManager.loadCashBoxManager(getContext());
//        Log.d(TAG, "onRestart: ");
//    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop: ");
//        // Save data
//        saveCashBoxManager();
        // Rename the temporary file to the actual store file
//        File originalFile = getFileStreamPath(CashBoxManager.FILENAME_TEMP);
//        File newFile = new File(originalFile.getParent(), CashBoxManager.FILENAME);
//        Log.d(TAG, "onStop: existe temp:" + originalFile.exists() + "\t" + Arrays.toString(newFile.getParentFile().list()));
//        if (!IOCashBoxManager.renameCashBoxManagerTemp(getContext()))
//            Log.d(TAG, "onStop: fallo al renameTo");
//        Log.d(TAG, "onStop: existe temp:" + originalFile.exists() + "\t" + Arrays.toString(newFile.getParentFile().list()));

        IOCashBoxManager.renameCashBoxManagerTemp(getContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: " + cashBoxManager.toString());
        if(requestCode==CashBoxManagerRecyclerAdapter.REQUEST_CODE_ITEM && resultCode==RESULT_OK)
            cashBoxManager = (CashBoxManager) data.getSerializableExtra(CashBoxManagerRecyclerAdapter.CASHBOX_MANAGER_EXTRA);
        Log.d(TAG, "onActivityResult: " + cashBoxManager.toString());
        ((CashBoxManagerRecyclerAdapter) rvCashBoxManager.getAdapter()).updateCashBoxManager(cashBoxManager);
//        rvCashBoxManager.getAdapter().notifyDataSetChanged();

        super.onActivityResult(requestCode, resultCode, data);
    }

    public RecyclerView getRecyclerView() {
        return rvCashBoxManager;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_deleteAll:
                deleteAll();
                return true;
            case R.id.action_help:
                Util.showHelp(this, R.string.cashBoxManager_helpTitle, R.string.cashBoxManager_help).show();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void saveCashBoxManager() {
        try {
            IOCashBoxManager.saveCashBoxManagerTemp(cashBoxManager,getContext());
        } catch (IOException e) {
            Log.e(TAG, "onStop: error save", e);
            e.printStackTrace();
        }
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
                        rvCashBoxManager.getAdapter().notifyItemRangeRemoved(0, size);
//                        cashBoxManager.saveDataTemp(getContext());
                        saveCashBoxManager();
                        Toast.makeText(this, "Deleted all entries", Toast.LENGTH_SHORT).show();
                    }).show();
        } else
            Toast.makeText(this, "No entries to delete", Toast.LENGTH_SHORT).show();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setTitle(R.string.newEntry)
                .setView(R.layout.new_cash_box_input)  //use that view from folder layout
                .setNegativeButton(R.string.cancelDialog, null)
                .setPositiveButton(R.string.createCashBoxDialog, null)
                .create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                TextInputEditText inputTextName = (TextInputEditText) ((AlertDialog) dialog).findViewById(R.id.inputTextName);
                TextInputLayout inputLayoutName = (TextInputLayout) ((AlertDialog) dialog).findViewById(R.id.inputLayoutName);
                TextInputEditText inputTextInitCash = (TextInputEditText) ((AlertDialog) dialog).findViewById(R.id.inputTextInitCash);
                TextInputLayout inputLayoutInitCash = (TextInputLayout) ((AlertDialog) dialog).findViewById(R.id.inputLayoutInitCash);

                Util.showKeyboard(getContext(), inputTextName);
                inputTextName.setMaxLines(CashBox.MAX_LENGTH_NAME);
                inputTextInitCash.setText("0");
                positive.setOnClickListener(v -> {
                    try {
                        CashBox cashBox = new CashBox(inputTextName.getText().toString());
                        String strInitCash = inputTextInitCash.getText().toString();
                        if (!strInitCash.isEmpty() && Double.parseDouble(strInitCash) != 0)
                            cashBox.add(Double.parseDouble(strInitCash), "Initial Amount", Calendar.getInstance());
                        if (cashBoxManager.add(cashBox)) {
                            rvCashBoxManager.getAdapter().notifyItemInserted(cashBoxManager.size() - 1);
                            dialog.dismiss();
//                            cashBoxManager.saveDataTemp(getContext());
                            saveCashBoxManager();
                        } else
                            inputLayoutName.setError(getContext().getString(R.string.nameInUse));
                    } catch (NumberFormatException e) {
                        inputLayoutInitCash.setError("Not a valid number");
                        inputTextInitCash.setText("");
                    } catch (IllegalArgumentException e) {
                        inputLayoutName.setError(e.getMessage());
                        inputTextName.setText(inputTextName.getText().toString().trim());
                    }
                });
            }
        });
        dialog.show();
    }

    private Context getContext() {
        return this;
    }
}
