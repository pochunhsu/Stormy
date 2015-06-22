package com.pchsu.stormy.weather;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Day implements Parcelable{
    private long mTime;
    private String mSummary;
    private double mTempeMax;
    private double mTempeMin;
    private String mIcon;
    private String mTimezone;
    private String mLocation;

     public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public int getTempeMax() {
        return (int) Math.round(mTempeMax);
    }

    public void setTempeMax(double tempeMax) {
        mTempeMax = tempeMax;
    }

    public double getTempeMin() {
        return mTempeMin;
    }

    public void setTempeMin(double tempeMin) {
        mTempeMin = tempeMin;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public String getTimezone() {
        return mTimezone;
    }

    public void setTimezone(String timezone) {
        mTimezone = timezone;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public int getIconId(){
        return Forecast.getIconId(mIcon);
    }

    public String getDayOfTheWeek(){
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE");
        formatter.setTimeZone(TimeZone.getTimeZone(mTimezone));
        Date dateTime = new Date(mTime*1000);
        return formatter.format(dateTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mTime);
        dest.writeString(mSummary);
        dest.writeDouble(mTempeMax);
        dest.writeString(mIcon);
        dest.writeString(mTimezone);
    }

    private Day(Parcel in){
        mTime = in.readLong();
        mSummary = in.readString();
        mTempeMax = in.readDouble();
        mIcon = in.readString();
        mTimezone = in.readString();
    }

    public Day(){}

    public static final Creator<Day> CREATOR = new Creator<Day>() {
        @Override
        public Day createFromParcel(Parcel source) {
            return new Day(source);
        }

        @Override
        public Day[] newArray(int size) {
            return new Day[size];
        }
    };

}
