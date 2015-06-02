package com.pchsu.stormy.weather;

public class Day {
    private long mTime;
    private String mSummary;
    private double mTempeMax;
    private double mTempeMin;
    private String mIcon;
    private String mTimezone;

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

    public double getTempeMax() {
        return mTempeMax;
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
}
