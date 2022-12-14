package com.billcoreatech.daycnt415.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBHandler extends RuntimeException {

    DBHelper helper ;
    SQLiteDatabase db ;
    String tableName = "dayinfo" ;

    String TAG = "dayinfo" ;

    public DBHandler(Context context) {
        helper = new DBHelper(context) ;
        db = helper.getWritableDatabase() ;
    }

    public static DBHandler open (Context ctx) throws SQLException {
        DBHandler handler = new DBHandler(ctx) ;
        return handler ;
    }

    public void close() {
        helper.close();
    }

    public Cursor selectAll() {
        StringBuffer sb = new StringBuffer();
        sb.append("select * from " + tableName + " ");
        sb.append("order by mdate desc   ");
        Cursor rs = db.rawQuery(sb.toString(), null) ;
        return rs ;
    }

    public Cursor getTodayMsg(String mDate) {
        String strMsg = "" ;
        StringBuffer sb = new StringBuffer();
        sb.append(" select * from " + tableName + " " );
        sb.append(" where mdate <= '" + mDate.replaceAll("-","") + "' ");
        sb.append(" order by mdate desc            ");
        Cursor rs = db.rawQuery(sb.toString(), null) ;
        return rs ;
    }

    @SuppressLint("Range")
    public String getIsHoliday(String mDate) {
        String isHoliday = "N";
        StringBuffer sb = new StringBuffer();
        sb.append(" select * from " + tableName + " " );
        sb.append(" where mdate = '" + mDate.replaceAll("-","") + "' ");
        Cursor rs = db.rawQuery(sb.toString(), null) ;
        if (rs.moveToNext()) {
            isHoliday = rs.getString(rs.getColumnIndex("isholiday"));
        }
        return isHoliday ;
    }

    @SuppressLint("Range")
    public String getBfDay(String mDate) {
        String isHoliday = "M";
        String bfDay = "";
        StringBuffer sb = new StringBuffer();
        sb.append(" select * from " + tableName + " " );
        sb.append(" where mdate <= '" + mDate.replaceAll("-","") + "' ");
        sb.append(" order by mdate desc            ");
        Cursor rs = db.rawQuery(sb.toString(), null) ;
        while (rs.moveToNext()) {
            if ("M".equals(isHoliday)) {
                isHoliday = rs.getString(rs.getColumnIndex("isholiday"));
            }
            //Log.i(TAG, "bfDay=" + bfDay + " " + rs.getString(rs.getColumnIndex("mdate")) + " isHoliday=" + isHoliday) ;
            if (!isHoliday.equals(rs.getString(rs.getColumnIndex("isholiday")))) {
                bfDay = rs.getString(rs.getColumnIndex("mdate")) ;
                break ;
            }

        }
        //Log.i(TAG, "Result bfDay =" + bfDay) ;
        return bfDay ;
    }

    @SuppressLint("Range")
    public String getAfDay(String mDate) {
        String isHoliday = "M";
        String afDay = "";
        StringBuffer sb = new StringBuffer();
        sb.append(" select * from " + tableName + " " );
        sb.append(" where mdate >= '" + mDate.replaceAll("-","") + "' ");
        sb.append(" order by mdate ");
        Cursor rs = db.rawQuery(sb.toString(), null) ;
        while (rs.moveToNext()) {
            if ("M".equals(isHoliday)) {
                isHoliday = rs.getString(rs.getColumnIndex("isholiday"));
            }
            //Log.i(TAG, "afDay=" + afDay + " " + rs.getString(rs.getColumnIndex("mdate")) + " isHoliday=" + isHoliday) ;
            if (!isHoliday.equals(rs.getString(rs.getColumnIndex("isholiday")))) {
                afDay = rs.getString(rs.getColumnIndex("mdate")) ;
                isHoliday = rs.getString(rs.getColumnIndex("isholiday")) ;
                break ;
            }
        }
        // ????????? ????????? ????????? ?????? ????????? ?????? ???????????? ?????????.
        sb = new StringBuffer();
        sb.append(" select * from " + tableName + " " );
        sb.append(" where mdate <= '" + afDay + "' ");
        sb.append(" order by mdate desc ");
        rs = db.rawQuery(sb.toString(), null) ;
        while (rs.moveToNext()) {
            //Log.i(TAG, "afDay=" + afDay + " " + rs.getString(rs.getColumnIndex("mdate")) + " isHoliday=" + isHoliday) ;
            if (!isHoliday.equals(rs.getString(rs.getColumnIndex("isholiday")))) {
                afDay = rs.getString(rs.getColumnIndex("mdate")) ;
                break ;
            }
        }
        //Log.i(TAG, "Result afDay =" + afDay) ;
        return afDay ;
    }

    public long insertDayinfo(String mDate, String msg, String dayOfweek, String isHoliday) {
        long _id = 0  ;
        ContentValues values = new ContentValues() ;
        values.put("mdate", mDate);
        values.put("msg", msg) ;
        values.put("dayOfweek", dayOfweek);
        values.put("isholiday", isHoliday);
        _id = db.insert(tableName, null, values) ;
        return _id ;
    }

    public long updateDayinfo(String mDate, String msg, String isHoliday) {
        long _id = 0  ;
        ContentValues values = new ContentValues() ;
        values.put("mdate", mDate);
        values.put("msg", msg) ;
        values.put("isholiday", isHoliday);
        _id = db.update(tableName, values, " mdate = '" + mDate + "' " , null) ;
        return _id ;
    }

    public long deleteDayinfo(String mDate) {
        long _id = 0  ;
        _id = db.delete(tableName, " mdate = '" + mDate + "' " , null) ;
        return _id ;
    }

    @SuppressLint("Range")
    public String getTomorrow(String mDate) {

        String afDay = "";
        StringBuffer sb = new StringBuffer();
        sb.append(" select * from " + tableName + " " );
        sb.append(" where mdate > '" + mDate.replaceAll("-","") + "' ");
        sb.append(" order by mdate ");
        Cursor rs = db.rawQuery(sb.toString(), null) ;
        if (rs.moveToNext()) {
            afDay = rs.getString(rs.getColumnIndex("mdate")) ;
        }
        return afDay ;
    }
}
