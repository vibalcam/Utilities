package com.utilities.vibal.utilities.ui.cashBoxManager;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.util.LogUtil;
import com.utilities.vibal.utilities.widget.CashBoxWidgetProvider;

public class CashBoxManagerActivity extends AppCompatActivity {
    //todo use action
    public static final String EXTRA_CASHBOX_ID = "com.utilities.vibal.utilities.cashBoxId";
    public static final String EXTRA_ACTION = "com.utilities.vibal.utilities.ui.cashBoxManager.action";
    public static final int NO_ACTION = 0;
    public static final int ACTION_ADD_CASHBOX = 1;
    public static final int ACTION_DETAILS = 2;
    public static final String GROUP_KEY_CASHBOX = "com.utilities.vibal.utilities.CASHBOX";
    private static final String TAG = "PruebaCBMActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cash_box_manager_activity);

        //Cancel reminder notifications if any
        NotificationManagerCompat.from(this).cancelAll();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, CashBoxManagerFragment.newInstance())
                    .commitNow();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //update app widget
        //todo update app widget
        Intent intent = new Intent(this, CashBoxWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplicationContext())
                .getAppWidgetIds(new ComponentName(getApplicationContext(), CashBoxWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        sendBroadcast(intent);
        LogUtil.debug(TAG,"Updated Widgets");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}
