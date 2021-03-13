package com.billcoreatech.daycnt311.util;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.billcoreatech.daycnt311.R;
import com.billcoreatech.daycnt311.database.DBHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GridAdapter extends BaseAdapter {

    String TAG = "GridAdapter" ;
    private List<String> list;
    private Calendar mCal;
    private LayoutInflater inflater;
    TextView tvItemGridView ;
    TextView tv1 ;
    private int nListCnt = 0;
    SimpleDateFormat sdf ;
    DBHandler dbHandler ;

    /**
     * 생성자
     *
     * @param context
     * @param list
     */
    public GridAdapter(Context context, List<String> list) {
        this.list = list;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sdf = new SimpleDateFormat("yyyyMMdd") ;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public String getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateReceiptsList(ArrayList<String> _oData) {
        list = _oData;
        nListCnt = list.size(); // 배열 사이즈 다시 확인
        this.notifyDataSetChanged(); // 그냥 여기서 하자
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.itemcalendar, parent, false);
            tvItemGridView = convertView.findViewById(R.id.tv_item_gridview);
            tv1 = convertView.findViewById(R.id.tv1) ;
        }
        if (getItem(position).length() > 3) {
            tvItemGridView.setText("" + getItem(position).substring(6, 8));
        } else {
            tvItemGridView.setText("" + getItem(position));
        }

        //해당 날짜 텍스트 컬러,배경 변경
        mCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd") ;
        try {
            long now = System.currentTimeMillis();
            Date toDay = new Date(now);
            String sToday = sdf.format(toDay) ;
            mCal.setTime(sdf.parse(getItem(position)));
            dbHandler = DBHandler.open(context) ;
            Cursor rs = dbHandler.getTodayMsg(getItem(position));
            if (rs.moveToNext()) {
                tv1.setText(rs.getString(rs.getColumnIndex("msg")));
                if ("Y".equals(rs.getString(rs.getColumnIndex("isholiday")))) {
                    tvItemGridView.setTextColor(context.getColor(R.color.softred));
                }
            }
            dbHandler.close();
            Integer weekOfDay = mCal.get(Calendar.DAY_OF_WEEK);
            if (weekOfDay == Calendar.SUNDAY) {
                tvItemGridView.setTextColor(context.getColor(R.color.softred));
            }
            if (weekOfDay == Calendar.SATURDAY) {
                tvItemGridView.setTextColor(context.getColor(R.color.softblue));
            }
            if (sToday.equals(getItem(position))) { //오늘 day 텍스트 컬러 변경
                tvItemGridView.setBackground(ContextCompat.getDrawable(context, R.drawable.background_text_gray));
                tvItemGridView.setTextColor(context.getColor(R.color.white));
            } else {
                tvItemGridView.setBackground(ContextCompat.getDrawable(context, R.drawable.backgroud_border_100));
            }

        } catch (Exception e) {
            if ("일".equals(getItem(position))) {
                tvItemGridView.setTextColor(ContextCompat.getColor(context, R.color.white));
                tvItemGridView.setBackgroundColor(context.getColor(R.color.softred));
                tvItemGridView.setBackground(ContextCompat.getDrawable(context, R.drawable.backgroud_border_200));
            } else if ("토".equals(getItem(position))) {
                tvItemGridView.setTextColor(ContextCompat.getColor(context, R.color.white));
                tvItemGridView.setBackgroundColor(context.getColor(R.color.softred));
                tvItemGridView.setBackground(ContextCompat.getDrawable(context, R.drawable.backgroud_border_200));
            } else if (!"".equals(getItem(position))){
                tvItemGridView.setTextColor(ContextCompat.getColor(context, R.color.white));
                tvItemGridView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                tvItemGridView.setBackground(ContextCompat.getDrawable(context, R.drawable.backgroud_border_200));
            }
        }

        tv1.setFocusable(true);

        return convertView;
    }
}
