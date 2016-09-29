package com.example.android.forecast;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
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
    private static boolean DEBUG = true;

    private final Context mContext;

    public FetchWeatherTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {

        if (params.length == 0) {
            // Nothing to look at here
            return null;
        }
        String locationParam = params[0];
        String weatherForecast = getWeatherForecastData(locationParam);

        try {
            getWeatherDataFromJson(weatherForecast, locationParam);
            return null;
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    private void getWeatherDataFromJson(String forecastJsonString, String locationSetting) throws
    JSONException {
        // These are the names of the JSON objects that need to be extracted.

        // Location information
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";
        final String OWM_COORD_LAT = "lat";
        final String OWM_COORD_LONG = "lon";

        // Weather information. Each day's forecast info is an element of the "list" array
        final String OWM_LIST = "list";

        final String OWM_DATETIME = "dt";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        // All temperatures are children of the "temp" object
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        JSONObject forecastJson = new JSONObject(forecastJsonString);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        String cityName = cityJson.getString(OWM_CITY_NAME);
        JSONObject coordJSON = cityJson.getJSONObject(OWM_COORD);
        double cityLatitude = coordJSON.getDouble(OWM_COORD_LAT);
        double cityLongitude = coordJSON.getDouble(OWM_COORD_LONG);

        /** Insert the location into the database. */
        long locationID = insertLocationIntoDB(
                locationSetting, cityName, cityLatitude, cityLongitude
        );

        // Get and insert the new weather information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

        for(int i = 0; i < weatherArray.length(); i++) {
            //  These are the values that will be collected

            long dateTime;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            String description;
            int weatherId;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            dateTime = dayForecast.getLong(OWM_DATETIME);

            pressure = dayForecast.getDouble(OWM_PRESSURE);
            humidity = dayForecast.getInt(OWM_HUMIDITY);
            windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = temperatureObject.getDouble(OWM_MAX);
            low = temperatureObject.getDouble(OWM_MIN);

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationID);
            weatherValues.put(WeatherEntry.COLUMN_DATETEXT,
                    ForecastContract.getDbDateString(new Date(dateTime * 1000L)));
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            cVVector.add(weatherValues);
        }

        /** Insert weather data into database */
        insertWeatherIntoDatabase(cVVector);
    }

    private String getWeatherForecastData (String postalCode) {
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
                    .appendQueryParameter(QUERY_PARAM, postalCode)
                    .appendQueryParameter(FORMAT, "json")
                    .appendQueryParameter(UNITS, "metric")
                    .appendQueryParameter(DAYS, "7")
                    .appendQueryParameter(API_KEY, apiKey).build();

            URL url = new URL(buildUri.toString());

            Log.d("*******"+ LOG_TAG, buildUri.toString());
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

        return forecastJsonString;
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
    private long insertLocationIntoDB(String locationSetting, String cityName, double lat, double lon) {

        long rowId = 0;

        Log.v(LOG_TAG, "Inserting " + cityName + ", with coord: "
                + lat + ", " + lon);

        //Check if the location exists in the db
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                new String[]{LocationEntry._ID},
                LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting}, null
        );

        if (cursor != null && cursor.moveToFirst()) {

            Log.v(LOG_TAG, "Found city in the db");

            int locationIdIndex = cursor.getColumnIndex(LocationEntry._ID);
            rowId = cursor.getLong(locationIdIndex);

        } else {

            Log.v(LOG_TAG, "Didn't find it in the db. Inserting now...");

            ContentValues locationValues = new ContentValues();
            locationValues.put(LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(LocationEntry.COLUMN_LATITUDE, lat);
            locationValues.put(LocationEntry.COLUMN_LONGITUDE, lon);

            Uri uri = mContext.getContentResolver().insert(
                    LocationEntry.CONTENT_URI,
                    locationValues
            );
            rowId =  ContentUris.parseId(uri);

        }

        if (cursor != null) {
            cursor.close();
        }

        return rowId;

    }

    private void insertWeatherIntoDatabase (Vector<ContentValues> vector) {
        if (vector.size() > 0) {
            ContentValues[] contentValuesArray = new ContentValues[vector.size()];
            vector.toArray(contentValuesArray);

            int rowsInserted = mContext.getContentResolver().bulkInsert(
                    WeatherEntry.CONTENT_URI, contentValuesArray);

            if (DEBUG) {
                Cursor weatherCursor = mContext.getContentResolver().query(
                        WeatherEntry.CONTENT_URI,
                        null, null, null, null
                );

                if (weatherCursor.moveToFirst()) {
                    ContentValues resultValues = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(weatherCursor, resultValues);

                    Log.v(LOG_TAG, "QUERY SUCCESSFUL: ********************");
                    for (String key : resultValues.keySet()) {
                        Log.v(LOG_TAG, key + ": " + resultValues.getAsString(key));
                    }
                } else {
                    Log.v(LOG_TAG, "QUERY FAILED!!!!!!!!!!!!!!!!!!!!!!!! :-(");
                }
            }
        }
    }
}