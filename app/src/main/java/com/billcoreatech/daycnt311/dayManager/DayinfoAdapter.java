package com.billcoreatech.daycnt311.dayManager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.billcoreatech.daycnt311.databinding.DayinfoitemBinding;

import java.util.ArrayList;

public class DayinfoAdapter extends BaseAdapter {
    ArrayList<DayinfoBean> dayInfoList = new ArrayList<>();
    LayoutInflater inflater ;
    DayinfoitemBinding binding ;

    public DayinfoAdapter(Context context, ArrayList<DayinfoBean> odata) {
        this.dayInfoList = odata ;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
    }

    @Override
    public int getCount() {
        return dayInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return dayInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        binding = DayinfoitemBinding.inflate(inflater) ;
        CustomViewHolder holder ;
        if (convertView == null) {
            convertView = binding.getRoot();
            holder = new CustomViewHolder();
            holder.txtMdate = binding.txtMdate ;
            holder.txtMsg = binding.txtMsg ;
            convertView.setTag(holder);
        } else {
            holder = (CustomViewHolder) convertView.getTag();
        }
        holder.txtMdate.setText(dayInfoList.get(position).getMdate());
        holder.txtMsg.setText(dayInfoList.get(position).getMsg()) ;
        return convertView;
    }

    private class CustomViewHolder {
        TextView txtMdate ;
        TextView txtMsg ;
    }
}
