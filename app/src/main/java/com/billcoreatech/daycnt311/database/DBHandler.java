package com.billcoreatech.daycnt311.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
        // 마지막 날짜를 찾아야 하기 떄문애 다시 역순으로 찾아감.
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

}
