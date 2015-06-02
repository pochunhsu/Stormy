package com.pchsu.stormy.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.pchsu.stormy.R;
import com.pchsu.stormy.weather.Current;
import com.pchsu.stormy.weather.Day;
import com.pchsu.stormy.weather.Forecast;
import com.pchsu.stormy.weather.Hour;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static String TAG = MainActivity.class.getSimpleName();

    private Forecast mForecast;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    //private double mLatitude = 37.3544;
    //private double mLongitude = -121.9692;
    private Location mLocation;
    private String mLocationStr;

    @InjectView(R.id.timeLabel) TextView mTimeLabel;
    @InjectView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @InjectView(R.id.humidityValue) TextView mHumidityValue;
    @InjectView(R.id.precipValue) TextView mPrecipValue;
    @InjectView(R.id.summaryLabel) TextView mSummaryLabel;
    @InjectView(R.id.iconImageView) ImageView mIconImageView;
    @InjectView(R.id.refreshImageView) ImageView mRefreshImageView;
    @InjectView(R.id.progressBar) ProgressBar mProgressBar;
    @InjectView(R.id.locationLabel) TextView mLocationLabel;
    @InjectView(R.id.buttonHourly) Button mButtonHourly;
    @InjectView(R.id.buttonDaily) Button mButtonDaily;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mProgressBar.setVisibility(View.INVISIBLE);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(30 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(10 * 1000); // 1 second, in milliseconds

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getForecast();
            }
        });

        mButtonHourly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HourlyActivity.class);
                startActivity(intent);
            }
        });
        mButtonDaily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, DailyActivity.class);
                startActivity(intent);
            }
        });
        //getForecast(mLatitude, mLongitude);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    private void getForecast() {
        if (mLocation == null) return;

        String apiKey = getString(R.string.forecastio_apikey);

        double latitude = mLocation.getLatitude();
        double longitude = mLocation.getLongitude();

        String forecastURL = "https://api.forecast.io/forecast/" + apiKey +
                "/" + latitude + "," + longitude;
        if (isNetworkAvailable()) {
            toggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastURL)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mForecast = parseForecastDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "IO Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Exception caught: ", e);
                    }
                }
            });
        }else {
            Toast.makeText(this, getString(R.string.network_unavailable_message), Toast.LENGTH_LONG).show();
        }
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }else{
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateDisplay() {
        Current current = mForecast.getCurrent();

        mTemperatureLabel.setText(current.getTemperature() + "");
        mTimeLabel.setText(current.getFormattedTime());
        mHumidityValue.setText(current.getHumidity() + "");
        mPrecipValue.setText(current.getPrecipChance() + "%");
        mSummaryLabel.setText(current.getSummary());
        mLocationLabel.setText(mLocationStr);

        // Drawable drawable = getResources().getDrawable(current.getIconId());
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), current.getIconId(), null);
        mIconImageView.setImageDrawable(drawable);
    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException{
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));

        return forecast;
    }

    private Day[] getDailyForecast(String jsonData) throws JSONException {
        JSONObject jo_forecast = new JSONObject(jsonData);
        String timezone = jo_forecast.getString("timezone");
        JSONObject jo_daily = jo_forecast.getJSONObject("daily");
        JSONArray ja_data = jo_daily.getJSONArray("data");

        Day[] days = new Day[ja_data.length()];

        for(int i =0; i< ja_data.length(); i++){
            JSONObject jo_day = ja_data.getJSONObject(i);
            Day day = new Day();

            day.setSummary(jo_day   .getString("summary"));
            day.setTempeMax(jo_day.getDouble("temperatureMax"));
            day.setIcon(jo_day.getString("icon"));
            day.setTime(jo_day.getLong("time"));
            day.setTimezone(timezone);

            days[i] = day;
        }
        return days;
    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException {
        JSONObject jo_forecast = new JSONObject(jsonData);
        String timezone = jo_forecast.getString("timezone");
        JSONObject jo_hourly = jo_forecast.getJSONObject("hourly");
        JSONArray ja_data = jo_hourly.getJSONArray("data");

        Hour[] hours = new Hour[ja_data.length()];

        for(int i =0; i< ja_data.length(); i++){
            JSONObject jo_hour = ja_data.getJSONObject(i);
            Hour hour = new Hour();

            hour.setSummary(jo_hour.getString("summary"));
            hour.setTemperature(jo_hour.getDouble("temperature"));
            hour.setIcon(jo_hour.getString("icon"));
            hour.setTime(jo_hour.getLong("time"));
            hour.setTimezone(timezone);

            hours[i] = hour;
        }

        return hours;
    }

    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject jo_forecast = new JSONObject(jsonData);
        String timezone = jo_forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone );

        JSONObject jo_currently = jo_forecast.getJSONObject("currently");

        Current current = new Current();
        current.setHumidity(jo_currently.getDouble("humidity"));
        current.setTime(jo_currently.getLong("time"));
        current.setIcon(jo_currently.getString("icon"));
        current.setPrecipChance(jo_currently.getDouble("precipProbability"));
        current.setSummary(jo_currently.getString("summary"));
        current.setTemperature(jo_currently.getDouble("temperature"));
        current.setTimeZone(timezone);

        Log.d(TAG, current.getFormattedTime());

        return current;
    }

    private String getLocationStirng(String JsonDataString) throws JSONException {
        JSONObject json_geo = new JSONObject(JsonDataString);
        JSONArray jsonA_results = json_geo.getJSONArray("results");
        JSONObject json_addr_part;
        JSONArray jsonA_types, jsonA_addr_parts;
        String str_Location = "";

        jsonA_addr_parts = jsonA_results.getJSONObject(0).getJSONArray("address_components");
        for (int i=0; i<jsonA_addr_parts.length(); i++){
            json_addr_part = jsonA_addr_parts.getJSONObject(i);
            jsonA_types = json_addr_part.getJSONArray("types");
            for (int j=0; j<jsonA_types.length(); j++){
                switch (jsonA_types.getString(j)){
                    case "locality":
                    //case "administrative_area_level_1":
                        str_Location += json_addr_part.getString("long_name");
                        str_Location += ", ";
                        break;
                    case "country":
                        str_Location += json_addr_part.getString("long_name");
                        break;
                }
            }
        }

        Log.d(TAG,"@@@ @@@ @@@ " + str_Location);
        return str_Location;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void handleNewLocation() {
        Log.d(TAG, mLocation.toString());

        //String apiKey = getString(R.string.google_api_key);

        double latitude = mLocation.getLatitude();
        double longitude = mLocation.getLongitude();

        String locationURL = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" +
                 + latitude + "," + longitude;
                 //+ "&location_type=APPROXIMATE&result_type=neighborhood&key=" +apiKey;

        if (isNetworkAvailable()) {
            toggleRefresh();  // showing progress

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(locationURL)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh(); // showing refresh button
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh(); // showing refresh button
                        }
                    });
                    try {
                        String jsonDataString = response.body().string();
                        Log.v(TAG, jsonDataString);
                        if (response.isSuccessful()) {
                            mLocationStr = getLocationStirng(jsonDataString);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                               //     updateDisplay();  //  let getForecast to display // currentWeather still null
                                }
                            });
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "IO Exception caught: ", e);
                    } catch (JSONException e){
                        Log.e(TAG, "JSON Exception caught: ", e);
                    }
                }
            });
        }else {
            Toast.makeText(this, getString(R.string.network_unavailable_message), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation();
            getForecast();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        handleNewLocation();
        getForecast();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
       /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }
}
