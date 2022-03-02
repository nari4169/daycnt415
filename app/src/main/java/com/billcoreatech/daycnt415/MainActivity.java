package com.billcoreatech.daycnt415;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
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

import com.billcoreatech.daycnt415.billing.BillingManager;
import com.billcoreatech.daycnt415.database.DBHandler;
import com.billcoreatech.daycnt415.databinding.ActivityMainBinding;
import com.billcoreatech.daycnt415.databinding.DayinfoitemBinding;
import com.billcoreatech.daycnt415.databinding.PopupWindowBinding;
import com.billcoreatech.daycnt415.dayManager.DayinfoBean;
import com.billcoreatech.daycnt415.util.GridAdapter;
import com.billcoreatech.daycnt415.util.Holidays;
import com.billcoreatech.daycnt415.util.StringUtil;
import com.github.anrwatchdog.ANRWatchDog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.appset.AppSet;
import com.google.android.gms.appset.AppSetIdClient;
import com.google.android.gms.appset.AppSetIdInfo;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.review.model.ReviewErrorCode;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int UPDATE_APP_REQUEST = 100;
    ActivityMainBinding binding ;
    PopupWindowBinding popupBinding ;
    DayinfoitemBinding dayInfoBinding;
    String TAG = "MainActivity" ;
    SharedPreferences option ;
    SharedPreferences.Editor editor ;
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
    SimpleDateFormat sdf ;
    AdRequest adRequest ;
    BillingManager billingManager ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater()) ;
        View view = binding.getRoot() ;
        setContentView(view);
        option = getSharedPreferences("option", MODE_PRIVATE);
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        Log.i(TAG, "billTimeStamp=" + sdf.format(new Date(option.getLong("billTimeStamp", System.currentTimeMillis())))) ;

        dayinfoLists = new ArrayList<>();
        holidays = new ArrayList<>() ;
        curYearFormat = new SimpleDateFormat("yyyy") ;
        curMonthFormat = new SimpleDateFormat("MM") ;
        detector = new GestureDetectorCompat(this, new MyGestureListener());
        strUtil = new StringUtil();

        new ANRWatchDog().start();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(MainActivity.this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // This example applies an immediate update. To apply a flexible update
                    // instead, pass in AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Request the update.
                try {
                    doUpdateApps(appUpdateManager, appUpdateInfo);
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "update Error...");
                }
            }
        });

        ReviewManager manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener( task -> {
            if (task.isSuccessful()) {
                ReviewInfo reviewInfo = task.getResult();
                doReviewUpdate(manager, MainActivity.this, reviewInfo);
            } else {
                @ReviewErrorCode
                int reviewErrorCode = task.getException().hashCode();

            }
        }) ;

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
            @SuppressLint("Range")
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
                            }
                        })
                        .setNegativeButton(getString(R.string.close), null);
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getColor(R.color.softblue));

            }
        });

        getIdAndLAT();

    }

    private void doUpdateApps(AppUpdateManager appUpdateManager, AppUpdateInfo appUpdateInfo) throws IntentSender.SendIntentException {
        appUpdateManager.startUpdateFlowForResult(
                // Pass the intent that is returned by 'getAppUpdateInfo()'.
                appUpdateInfo,
                // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                AppUpdateType.IMMEDIATE,
                // The current activity making the update request.
                this,
                // Include a request code to later monitor this update request.
                UPDATE_APP_REQUEST);
    }

    private void doReviewUpdate(ReviewManager manager, MainActivity mainActivity, ReviewInfo reviewInfo) {
        Task<Void> flow = manager.launchReviewFlow(mainActivity, reviewInfo);
        flow.addOnCompleteListener( task -> {
           Log.e(TAG, "result=" + task.isSuccessful()) ;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_APP_REQUEST) {
            if (resultCode != RESULT_OK) {
                Log.e(TAG, "Update flow failed! Result code: " + resultCode);
                Toast.makeText(MainActivity.this, getString(R.string.msgAppUpdateCompletedForRestart), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
//        long tempTime = System.currentTimeMillis();
//        long intervalTime = tempTime - backPressedTime;
//
//        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
//        {
//            super.onBackPressed();
//        }
//        else
//        {
//            backPressedTime = tempTime;
//            KakaoToast.makeToast(getApplicationContext(), getString(R.string.msgBackPress), Toast.LENGTH_LONG).show();
//        }
        // 종료 메시지 처리 방법 변경
        StringUtil.showSnackbarAd(this, adRequest,
                R.string.msgBackPress, R.string.Ok,
                option.getBoolean("isBill", false),
                new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        billingManager = new BillingManager(MainActivity.this);

        String mDate = strUtil.getToday();
        pDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            pDate = sdf.parse(mDate);
            getDispMonth(pDate);
        } catch (ParseException e) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "isBill=" + option.getBoolean("isBill", false)) ;
        if (option.getBoolean("isBill", false)) {
            binding.adView.setVisibility(View.GONE);
        } else {
            binding.adView.setVisibility(View.VISIBLE);
        }

    }

    public void getDispMonth(Date pDate) {

        String mDate = strUtil.getToday();

        dbHandler = DBHandler.open(getApplicationContext()) ;
        String bfDay = dbHandler.getBfDay(mDate);
        String afDay = dbHandler.getAfDay(mDate);
        String isHoliday = dbHandler.getIsHoliday(mDate);
        dbHandler.close();

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
            dbHandler = DBHandler.open(getApplicationContext()) ;
            String ckDay = sdf1.format(now) ;
            /**
             * 끝나는 날 : 끝나는 시간이 지나갔는지 확인하고 지나갔으면 평일/휴일을 변경해 주어야 함.
             */
            Log.i(TAG, "bfDay = 시간이 지나갔나 ??? " + endTimeValue + " " + now + " " + afDay + " " + ckDay) ;
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

        binding.txtDayToDay.setText(strUtil.getDispDay(bfDay) + " " + sTime + " ~ "
                + strUtil.getDispDay(afDay) + " " + eTime) ;
        double b = strUtil.getTimeTerm(getApplicationContext(), afDay, eTime, bfDay, sTime);
        double j = strUtil.getTodayTerm1(getApplicationContext(), bfDay, sTime);
        binding.txtHourTerm.setText(String.valueOf(Math.round(j / 60)) + "/" + String.valueOf(Math.round(b / 60)) + " Hour") ;
        binding.txtRate.setText(String.format("%.2f", j / b * 100) + "%");
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

    private void getIdAndLAT() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                AdvertisingIdClient.Info adInfo = null;
                try {
                    adInfo = AdvertisingIdClient.getAdvertisingIdInfo(MainActivity.this);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                final String GAID = adInfo.getId();
                final boolean limitAdTracking = adInfo.isLimitAdTrackingEnabled();

                Log.e(TAG, GAID + "=" + limitAdTracking);

                AppSetIdClient client = AppSet.getClient(MainActivity.this);
                Task<AppSetIdInfo> task = client.getAppSetIdInfo();

                task.addOnSuccessListener(new OnSuccessListener<AppSetIdInfo>() {
                    @Override
                    public void onSuccess(AppSetIdInfo appSetIdInfo) {
                        int scope = appSetIdInfo.getScope();
                        String id = appSetIdInfo.getId();

                        Log.e(TAG, "" + id + "" + scope);
                    }
                });
            }
        }).start();

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