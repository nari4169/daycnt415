package com.billcoreatech.daycnt311.dayManager;

public class DayinfoBean {
    int id ;
    String mDate ;
    String msg ;
    String dayOfWeek ;
    String isHoliday ;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setMdate(String mDate) {
        this.mDate = mDate;
    }

    public String getMdate() {
        return mDate;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setIsHoliday(String isHoliday) {
        this.isHoliday = isHoliday;
    }

    public String getIsHoliday() {
        return isHoliday;
    }
}
