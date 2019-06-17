package com.utilities.vibal.utilities.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.io.IOCashBoxManager;
import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.models.CashBoxManager;
import com.utilities.vibal.utilities.ui.cashBoxItem.CashBoxItemActivity;

import java.text.NumberFormat;

public class CashBoxWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CashBoxWidgetItemFactory(getApplicationContext());
    }

    class CashBoxWidgetItemFactory implements RemoteViewsFactory {
        private Context context;
        private CashBoxManager cashBoxManager;
        private NumberFormat currencyFormat;

        CashBoxWidgetItemFactory(Context context) {
            this.context = context;
            cashBoxManager = new CashBoxManager();
            currencyFormat = NumberFormat.getCurrencyInstance();
        }

        @Override
        public void onCreate() {}

        @Override
        public void onDataSetChanged() {
            cashBoxManager = IOCashBoxManager.loadCashBoxManager(context);
            currencyFormat = NumberFormat.getCurrencyInstance();
        }

        @Override
        public void onDestroy() {}

        @Override
        public int getCount() {
//            return cashBoxManager.size();TODO
            return 0;
        }
        @Override
        public RemoteViews getViewAt(int position) {
            // Set up layout
//            CashBox cashBox = cashBoxManager.get(position);TODO
            CashBox cashBox = new CashBox("Juan");
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.item_cbm_widget);
            views.setTextViewText(R.id.nameCBMWidgetItem,cashBox.getName());
            views.setTextViewText(R.id.amountCBMWidgetItem,currencyFormat.format(cashBox.getCash()));
            if(cashBox.getCash()<0)
                views.setTextColor(R.id.amountCBMWidgetItem,context.getColor(R.color.colorNegativeNumber));
            else
                views.setTextColor(R.id.amountCBMWidgetItem,context.getColor(R.color.colorPositiveNumber));

            // Intent for OnClick
            Intent fillIntent = new Intent();
            fillIntent.putExtra(CashBoxItemActivity.EXTRA_INDEX,position);
            fillIntent.putExtra(CashBoxItemActivity.EXTRA_CASHBOX_MANAGER, (Parcelable) cashBoxManager);
            views.setOnClickFillInIntent(R.id.listItem_cbmWidget,fillIntent);
            return views;
        }

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
