package com.billcoreatech.daycnt311.util;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import com.billcoreatech.daycnt311.R;
import com.billcoreatech.daycnt311.database.DBHandler;
import com.billcoreatech.daycnt311.databinding.DayCntWidgetBinding;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link DayCntWidgetConfigureActivity DayCntWidgetConfigureActivity}
 */
public class DayCntWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = "" ;
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.day_cnt_widget);

        onDispDayTerm(context, views) ;

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void onDispDayTerm(Context context, RemoteViews views) {

        DBHandler dbHandler ;
        String mDate = StringUtil.getToday();
        dbHandler = DBHandler.open(context) ;
        String bfDay = dbHandler.getBfDay(mDate);
        String afDay = dbHandler.getAfDay(mDate);
        dbHandler.close();

        SharedPreferences option = context.getSharedPreferences("option", context.MODE_PRIVATE);
        String sTime = option.getString("startTime", "18:00");
        String eTime = option.getString("closeTime", "00:00");

        views.setTextViewText(R.id.txtDayToDay1, StringUtil.getDispDay(bfDay) + " " + sTime + " ~ " + StringUtil.getDispDay(afDay) + " " + eTime);
        double b = StringUtil.getTimeTerm(context, afDay, bfDay);
        double j = StringUtil.getTodayTerm1(context, bfDay);
        views.setTextViewText(R.id.txtHourTerm1, String.valueOf(j) + "/" + String.valueOf(b) + " Hour");
        views.setTextViewText(R.id.txtRate1, String.valueOf(Math.round(j / b * 100)) + "%");
        views.setProgressBar(R.id.progressBar1, 100, (int) Math.round(j / b * 100), false);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {

        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}