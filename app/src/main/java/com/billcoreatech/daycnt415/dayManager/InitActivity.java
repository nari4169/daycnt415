package com.billcoreatech.daycnt415.dayManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.billcoreatech.daycnt415.MainActivity;
import com.billcoreatech.daycnt415.R;
import com.billcoreatech.daycnt415.database.DBHandler;
import com.billcoreatech.daycnt415.databinding.ActivityInitBinding;
import com.billcoreatech.daycnt415.util.Holidays;
import com.billcoreatech.daycnt415.util.LunarCalendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class InitActivity extends AppCompatActivity {

    String TAG = "InitActivity=" ;
    ActivityInitBinding binding ;
    SharedPreferences sharedPreferences ;
    ArrayList<Holidays> holidays ;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd") ;
    DBHandler dbHandler ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInitBinding.inflate(getLayoutInflater());
        View view = binding.getRoot() ;
        setContentView(view);
        holidays = new ArrayList<>() ;

        sharedPreferences = getSharedPreferences("holidayData", MODE_PRIVATE);
        if (!"N".equals(sharedPreferences.getString("INIT", "N"))) {
            Intent intent = new Intent(InitActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        };

        binding.btnInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(InitActivity.this);
                builder.setTitle(getString(R.string.InitOK))
                        .setMessage(getString(R.string.msgInitOK))
                        .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                binding.baseProgressBar.setVisibility(View.VISIBLE);
                                Calendar cal = Calendar.getInstance();
                                int year = cal.get(Calendar.YEAR);
                                dbHandler = DBHandler.open(getApplicationContext());
                                for(int iYear = year; iYear < year + 30 ; iYear++) {
                                    holidays.clear();
                                    holidays = LunarCalendar.holidayArray(String.valueOf(iYear));
                                    for (int iMonth = 1 ; iMonth < 13 ; iMonth++) {
                                        cal.set(Calendar.YEAR, iYear);
                                        cal.set(Calendar.MONTH, iMonth);
                                        for(int iDay = 0; iDay <= cal.getActualMaximum(Calendar.DAY_OF_MONTH) ; iDay++) {
                                            cal.set(Calendar.DAY_OF_MONTH, iDay);
                                            String todayMsg = getDayMsg(sdf.format(cal.getTime()));
                                            int dayOfweek = cal.get(Calendar.DAY_OF_WEEK) ;
                                            String isHoliday = "N" ;
                                            if (!"".equals(todayMsg)) {
                                                isHoliday = "Y" ;
                                            } else if (dayOfweek == Calendar.SATURDAY || dayOfweek == Calendar.SUNDAY) {
                                                isHoliday = "Y" ;
                                            }
                                            dbHandler.deleteDayinfo(sdf.format(cal.getTime())) ;
                                            long id = dbHandler.insertDayinfo(sdf.format(cal.getTime()), todayMsg, String.valueOf(dayOfweek), isHoliday);
                                            Log.i(TAG, id + "=" + sdf.format(cal.getTime()) + "," + todayMsg + "," + String.valueOf(dayOfweek) + "," +  isHoliday) ;
                                        }
                                    }
                                }
                                dbHandler.close();
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("INIT", "Y") ;
                                editor.commit() ;

                                binding.baseProgressBar.setVisibility(View.GONE);

                                Intent intent = new Intent(InitActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });
    }

    private String getDayMsg(String pDate) {
        String sMsg = "" ;
        for(int i=0 ; i < holidays.size() ; i++) {
            if (pDate.equals(holidays.get(i).getYear() + holidays.get(i).getDate())) {
                sMsg = holidays.get(i).getName() ;
                break ;
            }
        }
        return sMsg ;
    }
}