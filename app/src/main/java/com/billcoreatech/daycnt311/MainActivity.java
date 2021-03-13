package com.billcoreatech.daycnt311;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.billcoreatech.daycnt311.database.DBHandler;
import com.billcoreatech.daycnt311.databinding.ActivityMainBinding;
import com.billcoreatech.daycnt311.databinding.DayinfoitemBinding;
import com.billcoreatech.daycnt311.databinding.PopupWindowBinding;
import com.billcoreatech.daycnt311.dayManager.DayinfoBean;
import com.billcoreatech.daycnt311.util.DayCntWidget;
import com.billcoreatech.daycnt311.util.GridAdapter;
import com.billcoreatech.daycnt311.util.Holidays;
import com.billcoreatech.daycnt311.util.StringUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding ;
    PopupWindowBinding popupBinding ;
    DayinfoitemBinding dayInfoBinding;
    String TAG = "MainActivity" ;
    SharedPreferences option ;
    StringUtil strUtil ;
    GridAdapter gridAdapter ;
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    ArrayList<DayinfoBean> dayinfoLists ;
    ArrayList<Holidays> holidays ;
    DBHandler dbHandler ;
    ArrayList<String> dayList;
    SimpleDateFormat curYearFormat ;
    SimpleDateFormat curMonthFormat ;
    private GestureDetectorCompat detector;
    Date pDate ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater()) ;
        View view = binding.getRoot() ;
        setContentView(view);
        option = getSharedPreferences("option", MODE_PRIVATE);
        dayinfoLists = new ArrayList<>();
        holidays = new ArrayList<>() ;
        curYearFormat = new SimpleDateFormat("yyyy") ;
        curMonthFormat = new SimpleDateFormat("MM") ;
        detector = new GestureDetectorCompat(this, new MyGestureListener());
        strUtil = new StringUtil();

        binding.gridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return false;
            }
        });

        binding.btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        binding.txtYearMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onButtonShowPopupWindowClick(v, pDate);
            }
        });

        binding.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dayInfoBinding = DayinfoitemBinding.inflate(getLayoutInflater());
                View dayInfoView = dayInfoBinding.getRoot();
                dbHandler = DBHandler.open(getApplicationContext());
                Cursor rs = dbHandler.getTodayMsg(dayList.get(position));
                String msg = "" ;
                String isHoliday = "N" ;
                if (rs.moveToNext()) {
                    msg = rs.getString(rs.getColumnIndex("msg"));
                    isHoliday = rs.getString(rs.getColumnIndex("isholiday"));
                }
                dbHandler.close();
                dayInfoBinding.txtMsg.setText(msg);
                if ("Y".equals(isHoliday)) {
                    dayInfoBinding.checkBox.setChecked(true);
                } else {
                    dayInfoBinding.checkBox.setChecked(false);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(StringUtil.getDispDayYMD(dayList.get(position)))
                        .setMessage(getString(R.string.msgEnterHoliday))
                        .setView(dayInfoView)
                        .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbHandler = DBHandler.open(getApplicationContext());
                                String isHoliday = "N" ;
                                if (dayInfoBinding.checkBox.isChecked()) {
                                    isHoliday = "Y";
                                }
                                dbHandler.updateDayinfo(
                                    dayList.get(position),
                                    dayInfoBinding.txtMsg.getText().toString(),
                                    isHoliday
                                );
                                dbHandler.close();
                                getDispMonth(pDate);

                                int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), DayCntWidget.class));
                                DayCntWidget myWidget = new DayCntWidget();
                                myWidget.onUpdate(MainActivity.this, AppWidgetManager.getInstance(MainActivity.this),ids);
                            }
                        })
                        .setNegativeButton(getString(R.string.close), null);
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getColor(R.color.softblue));

            }
        });

    }



    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
        {
            super.onBackPressed();
        }
        else
        {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), getString(R.string.msgBackPress), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        String mDate = strUtil.getToday();
        pDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            pDate = sdf.parse(mDate);
            getDispMonth(pDate);
        } catch (ParseException e) {

        }
    }

    public void getDispMonth(Date pDate) {

        String mDate = strUtil.getToday();
        dbHandler = DBHandler.open(getApplicationContext()) ;
        String bfDay = dbHandler.getBfDay(mDate);
        String afDay = dbHandler.getAfDay(mDate);
        dbHandler.close();

        Log.i(TAG, "bfDay=" + bfDay + " afDay=" + afDay);
        String sTime = option.getString("startTime", "18:00");
        String eTime = option.getString("closeTime", "24:00");

        binding.txtDayToDay.setText(strUtil.getDispDay(bfDay) + " " + sTime + " ~ "
                + strUtil.getDispDay(afDay) + " " + eTime) ;
        double b = strUtil.getTimeTerm(getApplicationContext(), afDay, bfDay);
        double j = strUtil.getTodayTerm1(getApplicationContext(), bfDay);
        binding.txtHourTerm.setText(String.valueOf(j) + "/" + String.valueOf(b) + " Hour") ;
        binding.txtRate.setText(String.valueOf(Math.round(j / b * 100)) + "%");
        binding.progressBar.setMax(100);
        binding.progressBar.setProgress((int) Math.round(j / b * 100));

        binding.txtYearMonth.setText(curYearFormat.format(pDate) + "." + curMonthFormat.format(pDate));
        dayList = new ArrayList<String>();
        Calendar mCal = Calendar.getInstance();
        //이번달 1일 무슨요일인지 판단 mCal.set(Year,Month,Day)
        mCal.set(Integer.parseInt(curYearFormat.format(pDate)), Integer.parseInt(curMonthFormat.format(pDate)) - 1, 1);
        int dayNum = mCal.get(Calendar.DAY_OF_WEEK);
        //1일 - 요일 매칭 시키기 위해 공백 add
        for (int i = 1; i < dayNum; i++) {
            dayList.add("");
        }
        setCalendarDate(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH) + 1);

        gridAdapter = new GridAdapter(getApplicationContext(), dayList);
        gridAdapter.updateReceiptsList(dayList);
        binding.gridView.setAdapter(gridAdapter);

    }

    private void setCalendarDate(int year, int month) {
        Calendar mCal = Calendar.getInstance();
        mCal.set(Calendar.YEAR, year) ;
        mCal.set(Calendar.MONTH, month - 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd") ;
        int iNext = 0 ;
        for (int i = 0; i < mCal.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            mCal.set(Calendar.DAY_OF_MONTH, i + 1);
            dayList.add(sdf.format(new Date(mCal.getTimeInMillis())));
            iNext = mCal.get(Calendar.DAY_OF_WEEK) ;
            Log.d(TAG,"week :" + mCal.get(Calendar.DAY_OF_WEEK)) ;
        }
        // 나머지 빈칸도 채우기 위해서
        for (int i = iNext ; i < 7 ; i++) {
            dayList.add("") ;
        }
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {

            float diffY = event2.getY() - event1.getY();
            float diffX = event2.getX() - event1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                }
            } else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                }
            }
            return true;
        }
    }

    private void onSwipeLeft() {
        pDate = StringUtil.addMonth(pDate, 1) ;
        getDispMonth(pDate);
        binding.gridView.setFocusable(true);
    }

    private void onSwipeRight() {
        pDate = StringUtil.addMonth(pDate, -1) ;
        getDispMonth(pDate);
        binding.gridView.setFocusable(true);
    }

    private void onSwipeTop() {
        pDate = StringUtil.addMonth(pDate, 12) ;
        getDispMonth(pDate);
        binding.gridView.setFocusable(true);
    }

    private void onSwipeBottom() {
        pDate = StringUtil.addMonth(pDate, -12) ;
        getDispMonth(pDate);
        binding.gridView.setFocusable(true);
    }

    public void onButtonShowPopupWindowClick(View view, Date ppDate) {

        popupBinding = PopupWindowBinding.inflate(getLayoutInflater()) ;
        View popupView = popupBinding.getRoot() ;

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, 300, 600);

        popupBinding.txtMonth.setText(curYearFormat.format(pDate)) ;
        Calendar mCal = Calendar.getInstance();
        mCal.set(Integer.parseInt(curYearFormat.format(pDate)), Integer.parseInt(curMonthFormat.format(pDate)) - 1, 1);
        switch(mCal.get(Calendar.MONTH)) {
            case 0: onSetColor(popupBinding.txtMonth1) ;
                break ;
            case 1: onSetColor(popupBinding.txtMonth2) ;
                break ;
            case 2: onSetColor(popupBinding.txtMonth3) ;
                break ;
            case 3: onSetColor(popupBinding.txtMonth4) ;
                break ;
            case 4: onSetColor(popupBinding.txtMonth5) ;
                break ;
            case 5: onSetColor(popupBinding.txtMonth6) ;
                break ;
            case 6: onSetColor(popupBinding.txtMonth7) ;
                break ;
            case 7: onSetColor(popupBinding.txtMonth8) ;
                break ;
            case 8: onSetColor(popupBinding.txtMonth9) ;
                break ;
            case 9: onSetColor(popupBinding.txtMonth10) ;
                break ;
            case 10: onSetColor(popupBinding.txtMonth11) ;
                break ;
            case 11: onSetColor(popupBinding.txtMonth12) ;
                break ;
        }
        popupBinding.btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.addMonth(pDate, -12) ;
                popupBinding.txtMonth.setText(curYearFormat.format(pDate)) ;
            }
        });
        popupBinding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.addMonth(pDate, 12) ;
                popupBinding.txtMonth.setText(curYearFormat.format(pDate)) ;
            }
        });
        popupBinding.txtMonth1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 1);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 2);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 3);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 4);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 5);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 6);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 7);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 8);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 9);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 10);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 11);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 12);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.btnToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                pDate = new Date(now);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
    }

    private void onSetColor(TextView txtMonth) {
        txtMonth.setBackgroundColor(getColor(R.color.softblue));
        txtMonth.setTextColor(Color.YELLOW) ;
    }
}