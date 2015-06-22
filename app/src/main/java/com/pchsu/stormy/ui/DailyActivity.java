package com.pchsu.stormy.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;

import com.pchsu.stormy.R;
import com.pchsu.stormy.adapter.DayAdapter;
import com.pchsu.stormy.weather.Day;

import java.util.Arrays;

public class DailyActivity extends ListActivity {

    private Day[] mDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily);

        Intent intent = getIntent();

        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        mDays = Arrays.copyOf(parcelables,parcelables.length, Day[].class);

        DayAdapter adapter = new DayAdapter(this, mDays);
        setListAdapter(adapter);

        TextView locationLabel = (TextView) this.findViewById(R.id.locationLabel_Daily);
        String location_str = intent.getExtras().getString(MainActivity.LOCATION_STR);

        if(location_str != null) {
            locationLabel.setText(location_str);
        }

    }
}