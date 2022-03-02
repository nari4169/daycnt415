package com.billcoreatech.daycnt415.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.billcoreatech.daycnt415.R;
import com.billcoreatech.daycnt415.databinding.AppsFinishViewBinding;
import com.google.android.gms.ads.AdRequest;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StringUtil {

    static String TAG = "StringUtil" ;
    static AppsFinishViewBinding binding ;

    public static void showSnackbarAd(Activity context, AdRequest adRequest, final int mainTextStringId, final int actionStringId,
                                      boolean isBill,
                                      View.OnClickListener listener) {
        Snackbar snackbar = Snackbar.make(
                context.findViewById(android.R.id.content),
                context.getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(context.getString(actionStringId), listener);
        binding = AppsFinishViewBinding.inflate(((Activity) context).getLayoutInflater());
        binding.adView.loadAd(adRequest);
        if (isBill) {
            binding.adView.setVisibility(View.GONE);
        }
        View views = binding.getRoot();
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView() ;
        snackbarLayout.setPadding(0, 0, 0 ,0);
        snackbarLayout.addView(views);
        snackbar.setBackgroundTint(context.getColor(R.color.softblue));
        snackbar.show();
    }

    public static String getToday() {
        long now = System.currentTimeMillis() ;
        Date date = new Date(now) ;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        return sdf.format(date) ;
    }

    public static long getTodayTerm1(Context context, String sD2, String sTime) {

        long sec = 0 ;
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd HHmm", Locale.KOREA);
        long now = System.currentTimeMillis() ;
        Date date = new Date(now) ;
        try {
            Date d1 = f.parse(f.format(date));
            Date d2 = f.parse(sD2 + " " + sTime.replaceAll(":",""));
            long diff = d1.getTime() - d2.getTime();
            sec = diff / 1000 / 60;
        } catch (Exception e) {

        }
        return sec  ;
    }

    public static long getTimeTerm(Context context, String sD1, String eTime, String sD2, String sTime) {

        long sec = 0 ;
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd HHmm", Locale.KOREA);
        try {
            Date d1 = f.parse(sD1 + " " + eTime.replaceAll(":",""));
            Date d2 = f.parse(sD2 + " " + sTime.replaceAll(":",""));
            long diff = d1.getTime() - d2.getTime();
            sec = diff / 1000 / 60;
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

    public static String getDate(long ltime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date nDate = new Date(ltime);
        return sdf.format(nDate);
    }
}
