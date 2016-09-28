package com.example.android.forecast;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.forecast.data.ForecastContract;
import com.example.android.forecast.data.ForecastContract.LocationEntry;
import com.example.android.forecast.data.ForecastContract.WeatherEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Vector;

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

        if (params.length == 0) {
            // Nothing to look at here
            return null;
        }
        String locationQuery = params[0];

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
                    .appendQueryParameter(QUERY_PARAM, locationQuery)
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
        final String WIND_DIRECTION = "deg";

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

            long locationId = addLocationIntoDB(locationQuery, cityName, cityLatitude, cityLongitude);


            Vector<ContentValues> contentValVector =
                    new Vector<>(weatherArray.length());

            for (int i = 0; i < weatherArray.length(); i++) {
                long dateTime;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;

                double high;
                double low;

                String description;
                int weatherId;

                // Get the json object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                dateTime = dayForecast.getLong(DATETIME);
                pressure = dayForecast.getDouble(PRESSURE);
                humidity = dayForecast.getInt(HUMIDITY);
                windSpeed = dayForecast.getDouble(WINDSPEED);
                windDirection = dayForecast.getDouble(WIND_DIRECTION);

                // Description is in a child array "weather" with one element only
                // weather[0]
                JSONObject weatherObject = dayForecast.getJSONArray(WEATHER)
                        .getJSONObject(0);
                description = weatherObject.getString(DESCRIPTION);
                weatherId = weatherObject.getInt(WEATHER_ID);

                // Temperatures are in a child object "temp"
                JSONObject temperatureObject = dayForecast.getJSONObject(TEMPERATURE);
                high = temperatureObject.getDouble(MAX);
                low = temperatureObject.getDouble(MIN);

                ContentValues weatherValues = new ContentValues();

                weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
                weatherValues.put(
                        WeatherEntry.COLUMN_DATETEXT,
                        ForecastContract.getDbDateString(new Date(dateTime * 1000L)));
                weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
                weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
                weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
                weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
                weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                contentValVector.add(weatherValues);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Helper method to handle the insertion of a new location
     *  in the weather database
     *
     * @param locationSetting the location string to request the updates from the service
     * @param cityName the city name eg. Nairobi
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row id of the location
     */
    private long addLocationIntoDB (String locationSetting, String cityName, double lat, double lon) {
        Log.v(LOG_TAG, "Inserting " + cityName + ", with coord: "
                + lat + ", " + lon);
        //Check if the location exists in the db
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                new String[]{LocationEntry._ID},
                LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting}, null
        );

        if (cursor.moveToFirst()) {
            Log.v(LOG_TAG, "Found city in the db");
            int locationIdIndex = cursor.getColumnIndex(LocationEntry._ID);
            return cursor.getLong(locationIdIndex);
        } else {
            Log.v(LOG_TAG, "Didn't find it in the db. Inserting now...");

            ContentValues locationValues = new ContentValues();
            locationValues.put(
                    LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(
                    LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(
                    LocationEntry.COLUMN_LATITUDE, lat);
            locationValues.put(
                    LocationEntry.COLUMN_LONGITUDE, lon);

            Uri locationInsertUri = mContext.getContentResolver().insert(
                    LocationEntry.CONTENT_URI,
                    locationValues
            );
            return ContentUris.parseId(locationInsertUri);
        }
    }
}