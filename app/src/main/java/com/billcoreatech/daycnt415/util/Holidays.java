package com.billcoreatech.daycnt415.util;

public class Holidays implements Comparable<Holidays> {

    // ArrayList의 type이 Comparable을 implements한 경우에만 sort 메소드의 정렬 기능을 사용할 수 있다
    private String year; // 연도
    private String date; // 월일
    private String name; // 휴일 명칭

    public Holidays() {
    }

    public Holidays(String year, String date, String name) {
        this.year = year;
        this.date = date;
        this.name = name;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Holidays o) {
        return this.date.compareTo(o.date);
    }
}
