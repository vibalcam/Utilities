package com.utilities.vibal.utilities.ui.cashBoxManager;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.utilities.vibal.utilities.R;

public class CashBoxManagerActivity extends AppCompatActivity {
    public static final String EXTRA_CASHBOX_ID = "com.utilities.vibal.utilities.ui.cashBoxManager.cashBoxIndex";
    public static final String EXTRA_ACTION = "com.utilities.vibal.utilities.ui.cashBoxManager.action";
    public static final int NO_ACTION = 0;
    public static final int ACTION_ADD_CASHBOX = 1;
    public static final int ACTION_DETAILS = 2;
    private static final String TAG = "PruebaCBMActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cash_box_manager_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, CashBoxManagerFragment.newInstance())
                    .commitNow();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}
