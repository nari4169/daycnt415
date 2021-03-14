package com.billcoreatech.daycnt311;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import com.billcoreatech.daycnt311.databinding.ActivitySettingBinding;
import com.billcoreatech.daycnt311.util.DayCntWidget;

import java.util.Calendar;

public class SettingActivity extends AppCompatActivity {

    ActivitySettingBinding binding ;
    DayCntWidget dayCntWidget ;
    SharedPreferences option ;
    SharedPreferences.Editor editor ;
    int hour = 0 ;
    int min = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        option = getSharedPreferences("option", MODE_PRIVATE);
        editor = option.edit() ;
        binding.edStartTime.setText(option.getString("startTime", "18:00"));
        binding.edCloseTime.setText(option.getString("closeTime", "24:00"));
        binding.editTermLength.setText(String.valueOf(option.getInt("term", 1))) ;
        binding.btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("startTime", binding.edStartTime.getText().toString());
                editor.putString("closeTime", binding.edCloseTime.getText().toString());
                editor.putInt("term", Integer.parseInt(binding.editTermLength.getText().toString().replaceAll("[^0-9]", "")));
                editor.commit();

                int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), DayCntWidget.class));
                DayCntWidget myWidget = new DayCntWidget();
                myWidget.onUpdate(SettingActivity.this, AppWidgetManager.getInstance(SettingActivity.this),ids);

                finish();
            }
        });

        binding.textView8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance() ;
                hour = cal.get(Calendar.HOUR_OF_DAY);
                min = cal.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(SettingActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        binding.edStartTime.setText(pad(hourOfDay) + ":" + pad(minute)) ;
                    }
                },hour, min, true);
            }
        });

        binding.textView9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance() ;
                hour = cal.get(Calendar.HOUR_OF_DAY);
                min = cal.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(SettingActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        binding.edCloseTime.setText(pad(hourOfDay) + ":" + pad(minute)) ;
                    }
                },hour, min, true);
            }
        });
    }

    private String pad(int pValue) {
        return pValue < 10 ? "0" + pValue : String.valueOf(pValue) ;
    }
}