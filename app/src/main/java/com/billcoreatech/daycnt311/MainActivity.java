package com.billcoreatech.daycnt311;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.billcoreatech.daycnt311.databinding.ActivityMainBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding ;
    String TAG = "MainActivity" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater()) ;
        View view = binding.getRoot() ;
        setContentView(view);

        binding.txtDayToDay.setText(getSunday(getToday()) + " 18:00 " + getFriday(getToday()) + " 24:00") ;
        double b = getTimeTerm(getFriday(getToday()), getSunday(getToday()));
        double j = getTodayTerm1(getSunday(getToday()));
        binding.txtHourTerm.setText(String.valueOf(j) + "/" + String.valueOf(b) + " Hour") ;
        binding.txtRate.setText(String.valueOf(Math.round(j / b * 100)) + "%");
        binding.progressBar.setMax(100);
        binding.progressBar.setProgress((int) Math.round(j / b * 100));

    }

    public String getToday () {
        long now = System.currentTimeMillis() ;
        Date date = new Date(now) ;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        return sdf.format(date) ;
    }

    public long getTodayTerm1(String sD2) {
        long sec = 0 ;
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA);
        long now = System.currentTimeMillis() ;
        Date date = new Date(now) ;
        try {
            Date d1 = f.parse(f.format(date));
            Date d2 = f.parse(sD2 + " 18:00");
            long diff = d1.getTime() - d2.getTime();
            sec = diff / 1000 / 60 / 60;
        } catch (Exception e) {

        }
        return sec  ;
    }

    public long getTimeTerm(String sD1, String sD2) {
        long sec = 0 ;
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA);
        try {
            Date d1 = f.parse(sD1 + " 24:00");
            Date d2 = f.parse(sD2 + " 18:00");
            long diff = d1.getTime() - d2.getTime();
            sec = diff / 1000 / 60 / 60;
        } catch (Exception e) {

        }
        return sec  ;
    }

    public String getFriday(String dateString) {

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

    public String getSunday(String dateString) {

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


}