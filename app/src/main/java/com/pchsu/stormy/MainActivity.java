package com.pchsu.stormy;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    public static String TAG = "Stormy";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String apiKey = "253549159cc5fdca81e296a5fe409e60";
        double latitude = 37.8267;
        double longitute = -122.423;
        String forecastURL = "https://api.forecast.io/forecast/" + apiKey +
                             "/" + latitude  + "," + longitute;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                                .url(forecastURL)
                                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            if (response.isSuccessful()){
                Log.v(TAG, response.body().string());
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception caught: ", e);
        }
    }
}
