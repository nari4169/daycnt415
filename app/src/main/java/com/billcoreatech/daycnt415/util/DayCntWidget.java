package com.billcoreatech.daycnt415.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.billcoreatech.daycnt415.R;
import com.billcoreatech.daycnt415.database.DBHandler;

import java.text.SimpleDateFormat;
import java.util.Date;

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
        SharedPreferences.Editor editor = option.edit();
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

            if (!option.getBoolean("isBill", false)) {
                KakaoToast.makeToast(context, context.getString(R.string.msgAdView), Toast.LENGTH_LONG).show();
            }
            long oldDate = option.getLong("billTimeStamp", System.currentTimeMillis());
            try {
                /**
                 * 29일 경과 하면 다시 구매하도록 광고를 보여야 함.
                 */
                long termDate = (System.currentTimeMillis() - oldDate) * 1000 / 60 / 60 / 24;
                if (termDate > 29) {
                    editor.putBoolean("isBIll", false);
                    editor.commit() ;
                }
            } catch (Exception e) {

            }
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

        Log.i(TAG, "bfDay=" + bfDay + " " + sTime + " afDay=" + afDay + " " + eTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm") ;
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd") ;
        try {
            Date endTime = sdf.parse(afDay + " " + eTime);
            long endTimeValue = endTime.getTime() ;
            long now = System.currentTimeMillis() ;
            dbHandler = DBHandler.open(context) ;
            String ckDay = sdf1.format(now) ;
            /**
             * 끝나는 날 : 끝나는 시간이 지나갔는지 확인하고 지나갔으면 평일/휴일을 변경해 주어야 함.
             */
            Log.i(TAG, "bfDay = 날자가 지나갔나 ??? " + endTimeValue + " " + now) ;
            if (endTimeValue < now && afDay.equals(ckDay)) {
                mDate = dbHandler.getTomorrow(mDate);
                bfDay = dbHandler.getBfDay(mDate) ;
                afDay = dbHandler.getAfDay(mDate) ;
                isHoliday = dbHandler.getIsHoliday(mDate) ;
                sTime = option.getString("startTime", "18:00");
                eTime = option.getString("closeTime", "24:00");
                if ("N".equals(isHoliday)) {
                    sTime = option.getString("closeTime", "24:00");
                    eTime = option.getString("startTime", "18:00");
                }
            }
            dbHandler.close();

        } catch (Exception e) {

        }

        int progress = option.getInt("transparent", 100);
        switch (Math.floorDiv(progress, 10)) {
            case 10: views.setInt(R.id.layout1, "setBackgroundResource", R.drawable.backgroud_border_10);
                views.setInt(R.id.layout2, "setBackgroundResource", R.drawable.backgroud_border_10);
                views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.black));
                views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.black));
                views.setTextColor(R.id.txtRate1, context.getColor(R.color.black));
                views.setTextColor(R.id.textView12, context.getColor(R.color.black));
                views.setTextColor(R.id.textView13, context.getColor(R.color.black));
            break ;
            case 9: views.setInt(R.id.layout1, "setBackgroundResource", R.drawable.backgroud_border_9);
                views.setInt(R.id.layout2, "setBackgroundResource", R.drawable.backgroud_border_9);
                views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.black));
                views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.black));
                views.setTextColor(R.id.txtRate1, context.getColor(R.color.black));
                views.setTextColor(R.id.textView12, context.getColor(R.color.black));
                views.setTextColor(R.id.textView13, context.getColor(R.color.black));
                break ;
            case 8: views.setInt(R.id.layout1, "setBackgroundResource", R.drawable.backgroud_border_8);
                views.setInt(R.id.layout2, "setBackgroundResource", R.drawable.backgroud_border_8);
                views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.black));
                views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.black));
                views.setTextColor(R.id.txtRate1, context.getColor(R.color.black));
                views.setTextColor(R.id.textView12, context.getColor(R.color.black));
                views.setTextColor(R.id.textView13, context.getColor(R.color.black));
                break ;
            case 7: views.setInt(R.id.layout1, "setBackgroundResource", R.drawable.backgroud_border_7);
                views.setInt(R.id.layout2, "setBackgroundResource", R.drawable.backgroud_border_7);
                views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.black));
                views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.black));
                views.setTextColor(R.id.txtRate1, context.getColor(R.color.black));
                views.setTextColor(R.id.textView12, context.getColor(R.color.black));
                views.setTextColor(R.id.textView13, context.getColor(R.color.black));
                break ;
            case 6: views.setInt(R.id.layout1, "setBackgroundResource", R.drawable.backgroud_border_6);
                views.setInt(R.id.layout2, "setBackgroundResource", R.drawable.backgroud_border_6);
                views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.black));
                views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.black));
                views.setTextColor(R.id.txtRate1, context.getColor(R.color.black));
                views.setTextColor(R.id.textView12, context.getColor(R.color.black));
                views.setTextColor(R.id.textView13, context.getColor(R.color.black));
                break ;
            case 5: views.setInt(R.id.layout1, "setBackgroundResource", R.drawable.backgroud_border_5);
                views.setInt(R.id.layout2, "setBackgroundResource", R.drawable.backgroud_border_5);
                views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.softblue));
                views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.softblue));
                views.setTextColor(R.id.txtRate1, context.getColor(R.color.softblue));
                views.setTextColor(R.id.textView12, context.getColor(R.color.softblue));
                views.setTextColor(R.id.textView13, context.getColor(R.color.softblue));
                break ;
            case 4: views.setInt(R.id.layout1, "setBackgroundResource", R.drawable.backgroud_border_4);
                views.setInt(R.id.layout2, "setBackgroundResource", R.drawable.backgroud_border_4);
                views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.softblue));
                views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.softblue));
                views.setTextColor(R.id.txtRate1, context.getColor(R.color.softblue));
                views.setTextColor(R.id.textView12, context.getColor(R.color.softblue));
                views.setTextColor(R.id.textView13, context.getColor(R.color.softblue));
                break ;
            case 3: views.setInt(R.id.layout1, "setBackgroundResource", R.drawable.backgroud_border_3);
                views.setInt(R.id.layout2, "setBackgroundResource", R.drawable.backgroud_border_3);
                views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.softblue));
                views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.softblue));
                views.setTextColor(R.id.txtRate1, context.getColor(R.color.softblue));
                views.setTextColor(R.id.textView12, context.getColor(R.color.softblue));
                views.setTextColor(R.id.textView13, context.getColor(R.color.softblue));
                break ;
            case 2: views.setInt(R.id.layout1, "setBackgroundResource", R.drawable.backgroud_border_2);
                views.setInt(R.id.layout2, "setBackgroundResource", R.drawable.backgroud_border_2);
                views.setInt(R.id.txtDayToDay1, "setTextColor", R.color.white);
                views.setInt(R.id.txtHourTerm1, "setTextColor", R.color.white);
                views.setInt(R.id.txtRate1, "setTextColor", R.color.white);
                views.setTextColor(R.id.textView12, context.getColor(R.color.white));
                views.setTextColor(R.id.textView13, context.getColor(R.color.white));
                break ;
            case 1: views.setInt(R.id.layout1, "setBackgroundResource", R.drawable.backgroud_border_1);
                views.setInt(R.id.layout2, "setBackgroundResource", R.drawable.backgroud_border_1);
                views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.white));
                views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.white));
                views.setTextColor(R.id.txtRate1, context.getColor(R.color.white));
                views.setTextColor(R.id.textView12, context.getColor(R.color.white));
                views.setTextColor(R.id.textView13, context.getColor(R.color.white));
                break ;
            case 0: views.setInt(R.id.layout1, "setBackgroundResource", R.drawable.backgroud_border_0);
                views.setInt(R.id.layout2, "setBackgroundResource", R.drawable.backgroud_border_0);
                views.setTextColor(R.id.txtDayToDay1, context.getColor(R.color.white));
                views.setTextColor(R.id.txtHourTerm1, context.getColor(R.color.white));
                views.setTextColor(R.id.txtRate1, context.getColor(R.color.white));
                views.setTextColor(R.id.textView12, context.getColor(R.color.white));
                views.setTextColor(R.id.textView13, context.getColor(R.color.white));
                break ;
        }
        views.setTextViewText(R.id.txtDayToDay1, StringUtil.getDispDay(bfDay) + " " + sTime + " ~ " + StringUtil.getDispDay(afDay) + " " + eTime);
        double b = StringUtil.getTimeTerm(context, afDay, eTime, bfDay, sTime);
        double j = StringUtil.getTodayTerm1(context, bfDay, sTime);
        views.setTextViewText(R.id.txtHourTerm1, String.valueOf(Math.round(j / 60)) + "/" + String.valueOf(Math.round(b / 60)));
        views.setTextViewText(R.id.txtRate1, String.format("%.2f", j / b * 100));
        views.setProgressBar(R.id.progressBar1, 100, (int) Math.round(j / b * 100), false);
        Log.i(TAG, "rate=" + String.format("%.2f", j / b * 100)) ;

    }

    public static Drawable GetDrawable(int drawableResId, int color, Context context) {
        Drawable drawable =  context.getResources().getDrawable(drawableResId);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return drawable;
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