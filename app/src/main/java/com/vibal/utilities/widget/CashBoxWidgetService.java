package com.vibal.utilities.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vibal.utilities.R;
import com.vibal.utilities.models.InfoWithCash;
import com.vibal.utilities.persistence.db.UtilitiesDatabase;
import com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity;

import java.text.NumberFormat;
import java.util.List;

public class CashBoxWidgetService extends RemoteViewsService {
    @NonNull
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CashBoxWidgetItemFactory(getApplicationContext());
    }

    class CashBoxWidgetItemFactory implements RemoteViewsFactory {
        private final Context context;
        private UtilitiesDatabase database;
        private List<InfoWithCash> cashBoxInfos;
        private NumberFormat currencyFormat;

        CashBoxWidgetItemFactory(Context context) {
            this.context = context;
            currencyFormat = NumberFormat.getCurrencyInstance();
        }

        @Override
        public void onCreate() {
            // on create widget
            database = UtilitiesDatabase.getInstance(getApplicationContext());
        }

        @Override
        public void onDataSetChanged() {
            // refresh data
            cashBoxInfos = database.cashBoxLocalDao().getAllCashBoxInfoForWidget();
            currencyFormat = NumberFormat.getCurrencyInstance();
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            return cashBoxInfos == null ? 0 : cashBoxInfos.size();
        }

        @Nullable
        @Override
        public RemoteViews getViewAt(int position) {
            if (cashBoxInfos == null || cashBoxInfos.size() == 0)
                return null;

            // Set up layout
            InfoWithCash cashBoxInfo = cashBoxInfos.get(position);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.cash_box_manager_widget_item);
            views.setTextViewText(R.id.nameCBMWidgetItem, cashBoxInfo.getCashBoxInfo().getName());
            views.setTextViewText(R.id.amountCBMWidgetItem, currencyFormat.format(cashBoxInfo.getCash()));
            if (cashBoxInfo.getCash() < 0)
                views.setTextColor(R.id.amountCBMWidgetItem, context.getColor(R.color.colorNegativeNumber));
            else
                views.setTextColor(R.id.amountCBMWidgetItem, context.getColor(R.color.colorNeutralNumber));

            // Intent for OnClick
            Intent fillIntent = new Intent();
            fillIntent.putExtra(CashBoxManagerActivity.EXTRA_CASHBOX_ID, cashBoxInfo.getCashBoxInfo().getId());
            views.setOnClickFillInIntent(R.id.listItem_cbmWidget, fillIntent);
            return views;
        }

        @Nullable
        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
