package com.billcoreatech.daycnt311.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StringUtil {

    public static String getToday() {
        long now = System.currentTimeMillis() ;
        Date date = new Date(now) ;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        return sdf.format(date) ;
    }

    public static long getTodayTerm1(Context context, String sD2) {
        String TAG = "StringUtil" ;

        SharedPreferences option = context.getSharedPreferences("option", context.MODE_PRIVATE);
        String sTime = option.getString("startTime", "1800");
        String eTime = option.getString("closeTime", "2400");
        Log.i(TAG, "getTimeTerm sTime=" + sTime + " eTime=" + eTime);

        long sec = 0 ;
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd HHmm", Locale.KOREA);
        long now = System.currentTimeMillis() ;
        Date date = new Date(now) ;
        try {
            Date d1 = f.parse(f.format(date));
            Date d2 = f.parse(sD2 + " " + sTime.replaceAll(":",""));
            long diff = d1.getTime() - d2.getTime();
            sec = diff / 1000 / 60 / 60;
        } catch (Exception e) {

        }
        return sec  ;
    }

    public static long getTimeTerm(Context context, String sD1, String sD2) {

        String TAG = "StringUtil" ;

        SharedPreferences option = context.getSharedPreferences("option", context.MODE_PRIVATE);
        String sTime = option.getString("startTime", "1800");
        String eTime = option.getString("closeTime", "2400");
        Log.i(TAG, "getTimeTerm sTime=" + sTime + " eTime=" + eTime);
        long sec = 0 ;
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd HHmm", Locale.KOREA);
        try {
            Date d1 = f.parse(sD1 + " " + eTime.replaceAll(":",""));
            Date d2 = f.parse(sD2 + " " + sTime.replaceAll(":",""));
            long diff = d1.getTime() - d2.getTime();
            sec = diff / 1000 / 60 / 60;
        } catch (Exception e) {

        }
        return sec  ;
    }

    public static String getFriday(String dateString) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        try{
            date = sdf.parse(dateString);
        }catch(ParseException e){
        }
        Calendar cal = Calendar.getInstance(Locale.KOREA);
        cal.setTime(date);
        cal.add(Calendar.DATE, Calendar.FRIDAY - cal.get(Calendar.DAY_OF_WEEK));
        return sdf.format(cal.getTime()) ;
    }

    public static String getSunday(String dateString) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        try{
            date = sdf.parse(dateString);
        }catch(ParseException e){
        }
        Calendar cal = Calendar.getInstance(Locale.KOREA);
        cal.setTime(date);
        cal.add(Calendar.DATE, Calendar.SUNDAY - cal.get(Calendar.DAY_OF_WEEK));
        return sdf.format(cal.getTime()) ;
    }


    public static String getDispDay(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("MM-dd") ;
        Date date = new Date();
        try{
            date = sdf.parse(dateString);
        }catch(ParseException e){
        }

        return sdf1.format(date);
    }

    public static String getDispDayYMD(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd") ;
        Date date = new Date();
        try{
            date = sdf.parse(dateString);
        }catch(ParseException e){
        }

        return sdf1.format(date);
    }

    public static Date addMonth(Date pDate, int iTerm) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(pDate);
        cal.add(Calendar.MONTH, iTerm);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd") ;
        //Log.i(TAG, sdf.format(new Date(cal.getTimeInMillis()))) ;
        return new Date(cal.getTimeInMillis())  ;
    }

    public static Date getDay(Date pDate, int iTerm) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(pDate);
        cal.set(Calendar.MONTH, iTerm - 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd") ;
        //Log.i(TAG, sdf.format(new Date(cal.getTimeInMillis()))) ;
        return new Date(cal.getTimeInMillis())  ;
    }
}
