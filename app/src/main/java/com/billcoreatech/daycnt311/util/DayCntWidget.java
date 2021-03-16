package com.billcoreatech.daycnt311.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import com.billcoreatech.daycnt311.R;
import com.billcoreatech.daycnt311.database.DBHandler;

import java.util.Calendar;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link DayCntWidgetConfigureActivity DayCntWidgetConfigureActivity}
 */
public class DayCntWidget extends AppWidgetProvider {

    private static int WIDGET_UPDATE_INTERVAL = 60000; // 1분 주기 갱신
    private static PendingIntent mSender;
    private static AlarmManager mManager;
    static PendingIntent pendingIntent ;
    static String TAG = "DayCntWidget---" ;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = null;
        super.onReceive(context, intent);
        action = intent.getAction() ;
        SharedPreferences option = context.getSharedPreferences("option", context.MODE_PRIVATE);
        WIDGET_UPDATE_INTERVAL = option.getInt("term", 60000) * 60000;
        Log.i(TAG, "onReceive -------------" + WIDGET_UPDATE_INTERVAL) ;
        if (action == null) {

        } else if(action.equals("android.appwidget.action.APPWIDGET_UPDATE"))
        {
            Log.i(TAG, "android.appwidget.action.APPWIDGET_UPDATE");
            long nexttime = SystemClock.elapsedRealtime() + WIDGET_UPDATE_INTERVAL;
            removePreviousAlarm();
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            mManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            mManager.set(AlarmManager.ELAPSED_REALTIME, nexttime, pendingIntent);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.day_cnt_widget);
            onDispDayTerm(context, views) ;

        } else if(action.equals("android.appwidget.action.APPWIDGET_DISABLED"))
        {
            Log.w(TAG, "android.appwidget.action.APPWIDGET_DISABLED");
            removePreviousAlarm();
        }
    }

    private void removePreviousAlarm() {

        if(mManager != null && mSender != null)
        {
            mSender.cancel();
            mManager.cancel(mSender);
            mManager = null ;
            mSender = null ;
        }

    }

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
        String isHoliday = dbHandler.getIsHoliday(mDate);
        dbHandler.close();

        SharedPreferences option = context.getSharedPreferences("option", context.MODE_PRIVATE);
        String sTime = option.getString("startTime", "18:00");
        String eTime = option.getString("closeTime", "24:00");
        if ("N".equals(isHoliday)) {
            sTime = option.getString("closeTime", "24:00");
            eTime = option.getString("startTime", "18:00");
        }
        views.setTextViewText(R.id.txtDayToDay1, StringUtil.getDispDay(bfDay) + " " + sTime + " ~ " + StringUtil.getDispDay(afDay) + " " + eTime);
        double b = StringUtil.getTimeTerm(context, afDay, bfDay);
        double j = StringUtil.getTodayTerm1(context, bfDay);
        views.setTextViewText(R.id.txtHourTerm1, String.valueOf(Math.round(j / 60)) + "/" + String.valueOf(Math.round(b / 60)));
        views.setTextViewText(R.id.txtRate1, String.format("%.2f", j / b * 100));
        views.setProgressBar(R.id.progressBar1, 100, (int) Math.round(j / b * 100), false);
        Log.i(TAG, "rate=" + String.format("%.2f", j / b * 100)) ;

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

        /*SharedPreferences option = context.getSharedPreferences("option", context.MODE_PRIVATE);
        WIDGET_UPDATE_INTERVAL = option.getInt("term", 60000) * 60000;

        Log.i(TAG, "WIDGET_UPDATE_INTERVAL=" + WIDGET_UPDATE_INTERVAL) ;

        Intent intent = new Intent(context, DayCntWidget.class);
        intent.putExtra("mode","time");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0, intent, 0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), WIDGET_UPDATE_INTERVAL, pendingIntent);*/
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
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, DayCntWidget.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,0);
        alarmManager.cancel(pendingIntent);//알람 해제
        pendingIntent.cancel(); //인텐트 해제
    }

}