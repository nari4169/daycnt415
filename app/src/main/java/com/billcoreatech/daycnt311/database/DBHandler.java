package com.billcoreatech.daycnt311.database;

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
        sb.append("select * from " + tableName);
        sb.append("order by mdate desc   ");
        Cursor rs = db.rawQuery(sb.toString(), null) ;
        return rs ;
    }

    public long insertDayinfo(String mDate, String msg) {
        long _id = 0  ;
        ContentValues values = new ContentValues() ;
        values.put("mdate", mDate);
        values.put("msg", msg) ;
        _id = db.insert(tableName, null, values) ;
        return _id ;
    }

    public long updateDayinfo(int id, String mDate, String msg) {
        long _id = 0  ;
        ContentValues values = new ContentValues() ;
        values.put("mdate", mDate);
        values.put("msg", msg) ;
        _id = db.update(tableName, values, " _id = " + id , null) ;
        return _id ;
    }

    public long deleteDayinfo(String mDate) {
        long _id = 0  ;
        _id = db.delete(tableName, " mdate = '" + mDate + "' " , null) ;
        return _id ;
    }
}
