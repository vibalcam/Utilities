package com.utilities.vibal.utilities.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity;

/**
 * Implementation of App Widget functionality.
 */
public class CashBoxWidget extends AppWidgetProvider {
    public static final int ADD_REQUEST = 1;
    public static final int DETAILS_REQUEST = 2;

    @Override
    public void onUpdate(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, @NonNull int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.cash_box_manager_widget);

            // OnClick for button
            Intent buttonIntent = new Intent(context, CashBoxManagerActivity.class);
            buttonIntent.putExtra(CashBoxManagerActivity.EXTRA_ACTION, CashBoxManagerActivity.ACTION_ADD_CASHBOX);
            views.setOnClickPendingIntent(R.id.addButtonCBMWidget,
                    PendingIntent.getActivity(context, ADD_REQUEST, buttonIntent, 0));

            // Set-up ListView
            Intent serviceIntent = new Intent(context, CashBoxWidgetService.class);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
            views.setRemoteAdapter(R.id.listCBMWidget, serviceIntent);
            views.setEmptyView(R.id.listCBMWidget, R.id.listEmptyCBMWidget);

            // OnClick for ListView
            Intent listIntent = new Intent(context, CashBoxManagerActivity.class);
            listIntent.putExtra(CashBoxManagerActivity.EXTRA_ACTION, CashBoxManagerActivity.ACTION_DETAILS);
            views.setPendingIntentTemplate(R.id.listCBMWidget,
                    PendingIntent.getActivity(context, DETAILS_REQUEST, listIntent, 0));


            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    //todo update al salir de app

//    @Override
//    public void onReceive(Context context, Intent intent) {
//        if(ACTION_DETAILS.equals(intent.getAction())) {
//            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
//            AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetId, R.id.listCBMWidget);
//        } else
//            super.onReceive(context, intent);
//    }
}

