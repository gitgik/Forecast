package com.example.android.forecast;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by nerd on 27/09/2016.
 */

public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    Context mContext;
    public FetchWeatherTask(Context context) {
       mContext = context;
    }
    @Override
    protected Void doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will store the raw JSON response as a string.
        String forecastJsonString = null;
        String apiKey = "de07b800a0f30d675a6ceb7d9b30ce11";

        try {
            // Construct URL for the OpenWeatherMap API
            final String QUERY_PARAM = "q";
            final String FORECAST_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String FORMAT = "mode";
            final String UNITS = "units";
            final String DAYS = "cnt";
            final String API_KEY = "APPID";

            Uri buildUri = Uri.parse(FORECAST_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, params[0])
                    .appendQueryParameter(FORMAT, "json")
                    .appendQueryParameter(UNITS, "metric")
                    .appendQueryParameter(DAYS, "14")
                    .appendQueryParameter(API_KEY, apiKey).build();

            URL url = new URL(buildUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null) {
                // add new line for easier debugging
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return  null;
            }
            forecastJsonString = buffer.toString();
            // log the json string
            Log.v(LOG_TAG, "JSON: " + forecastJsonString);

        } catch (IOException e) {
            Log.e(LOG_TAG, "NETWORK ERROR: ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try
                {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "CLOSING STREAM", e);
                }
            }
        }


            // return getWeatherFromJson(forecastJsonString, 7);

        // Location information
        final String CITY = "city";
        final String CITY_NAME = "name";
        final String COORDINATES = "coord";

        // Location coordinate
        final String LATITUDE = "lat";
        final String LONGITUDE = "lon";

        // Weather information Each forecast is an element
        // of the "list" object
        final String LIST = "list";
        final String DATETIME = "dt";
        final String PRESSURE = "pressure";
        final String HUMIDITY = "humidity";
        final String WINDSPEED = "speed";
        final String WINDE_DIRECTION = "deg";

        // All temp are childen of the "temp" object
        final String TEMPERATURE = "temp";
        final String MAX = "max";
        final String MIN = "min";

        final String WEATHER = "weather";
        final String DESCRIPTION = "main";
        final String WEATHER_ID = "id";

        try {
            JSONObject forecastJson = new JSONObject(forecastJsonString);
            JSONArray weatherArray = forecastJson.getJSONArray(LIST);

            JSONObject cityJson = forecastJson.getJSONObject(CITY);
            String cityName = cityJson.getString(CITY_NAME);

            JSONObject cityCoord = cityJson.getJSONObject(COORDINATES);
            double cityLatitude = cityCoord.getDouble(LATITUDE);
            double cityLongitude = cityCoord.getDouble(LONGITUDE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}