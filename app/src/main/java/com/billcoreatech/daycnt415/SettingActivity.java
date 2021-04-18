package com.billcoreatech.daycnt415;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.SkuDetails;
import com.billcoreatech.daycnt415.billing.BillingManager;
import com.billcoreatech.daycnt415.databinding.ActivitySettingBinding;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SettingActivity extends AppCompatActivity {

    ActivitySettingBinding binding ;
    SharedPreferences option ;
    SharedPreferences.Editor editor ;
    int hour = 0 ;
    int min = 0 ;
    String TAG = "SettingActivity";
    BillingManager billingManager ;
    SimpleDateFormat sdf ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        option = getSharedPreferences("option", MODE_PRIVATE);
        billingManager = new BillingManager(SettingActivity.this);
        Log.i(TAG, "billTimeStamp=" + sdf.format(new Date(option.getLong("billTimeStamp", System.currentTimeMillis())))) ;
        Log.i(TAG, "isBill=" + option.getBoolean("isBill", false)) ;
        if (option.getBoolean("isBill", false)) {
            binding.adView.setVisibility(View.GONE);
        } else {
            binding.adView.setVisibility(View.VISIBLE);
        }
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);
        editor = option.edit() ;
        binding.edStartTime.setText(option.getString("startTime", "18:00"));
        binding.edCloseTime.setText(option.getString("closeTime", "24:00"));
        binding.editTermLength.setText(String.valueOf(option.getInt("term", 1))) ;
        binding.seekTransparent.setMax(100);
        binding.seekTransparent.setProgress(option.getInt("transparent", 100));
        binding.progressTextView.setText(option.getInt("transparent", 100) + "%");
        doSeekProgressDisp(option.getInt("transparent", 100));

        binding.btnAdPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "clicked..." ) ;
                if(billingManager.connectStatus == BillingManager.connectStatusTypes.connected) {
                    Log.i(TAG, "connected ..") ;
                    try {
                        SkuDetails skuDetails = (SkuDetails) billingManager.mSkuDetails.get(0);
                        int iResp = billingManager.purchase(skuDetails);
                        Log.i(TAG, "iResp=" + iResp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        binding.btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("startTime", binding.edStartTime.getText().toString());
                editor.putString("closeTime", binding.edCloseTime.getText().toString());
                editor.putInt("term", Integer.parseInt(binding.editTermLength.getText().toString().replaceAll("[^0-9]", "")));
                editor.putInt("transparent", binding.seekTransparent.getProgress());
                editor.commit();

                finish();
            }
        });

        binding.edStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "edStartTime") ;
                Calendar cal = Calendar.getInstance() ;
                hour = cal.get(Calendar.HOUR_OF_DAY);
                min = cal.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(SettingActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        binding.edStartTime.setText(pad(hourOfDay) + ":" + pad(minute)) ;
                    }
                },hour, min, true);
                timePickerDialog.show();
            }
        });

        binding.edCloseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "edCloseTime") ;
                Calendar cal = Calendar.getInstance() ;
                hour = cal.get(Calendar.HOUR_OF_DAY);
                min = cal.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(SettingActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay == 0 && minute == 0) {
                            hourOfDay = 24 ;
                        }
                        binding.edCloseTime.setText(pad(hourOfDay) + ":" + pad(minute)) ;
                    }
                },hour, min, true);
                timePickerDialog.show();
            }
        });

        binding.seekTransparent.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG, "progress=" + progress) ;
                doSeekProgressDisp(progress) ;
                binding.progressTextView.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void doSeekProgressDisp(int progress) {
        switch (Math.floorDiv(progress, 10)) {
            case 10: binding.transparentTest.setBackgroundColor(getColor(R.color.white100));
                break ;
            case 9: binding.transparentTest.setBackgroundColor(getColor(R.color.white90));
                break ;
            case 8: binding.transparentTest.setBackgroundColor(getColor(R.color.white80));
                break ;
            case 7: binding.transparentTest.setBackgroundColor(getColor(R.color.white70));
                break ;
            case 6: binding.transparentTest.setBackgroundColor(getColor(R.color.white60));
                break ;
            case 5: binding.transparentTest.setBackgroundColor(getColor(R.color.white50));
                break ;
            case 4: binding.transparentTest.setBackgroundColor(getColor(R.color.white40));
                break ;
            case 3: binding.transparentTest.setBackgroundColor(getColor(R.color.white30));
                break ;
            case 2: binding.transparentTest.setBackgroundColor(getColor(R.color.white20));
                break ;
            case 1: binding.transparentTest.setBackgroundColor(getColor(R.color.white10));
                break ;
            case 0: binding.transparentTest.setBackgroundColor(getColor(R.color.white00));
                break ;
        }
    }

    private String pad(int pValue) {
        return pValue < 10 ? "0" + pValue : String.valueOf(pValue) ;
    }
}