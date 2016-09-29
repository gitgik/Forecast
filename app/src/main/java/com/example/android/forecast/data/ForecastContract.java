package com.example.android.forecast.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by nerd on 22/09/2016.
 */

public class ForecastContract {
    // Content URI made from the CONTENT AUTHORITY
    public static final String CONTENT_AUTHORITY = "com.example.android.forecast";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths to be appended to the URI
    public static final String WEATHER_PATH = "weather";
    public static final String LOCATION_PATH = "location";


    /**
     * Inner class that defines the table contents of forecast table
     */
    public static final class WeatherEntry implements BaseColumns {

        public static final Uri CONTENT_URI =  BASE_CONTENT_URI.buildUpon()
                .appendPath(WEATHER_PATH).build();

        // Mime type prefixes that indicates type of data to be returned
        // (DIR or SINGLE ITEM)
        public static final String CONTENT_TYPE_DIR =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + WEATHER_PATH;
        public  static final  String CONTENT_TYPE_ITEM =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + WEATHER_PATH;


        public static final String TABLE_NAME = "forecast";

        // Column is a Foreign Key from the location table
        public static final String COLUMN_LOC_KEY = "location_id";

        // Date in the format yyyy-mm-dd
        public static final String COLUMN_DATETEXT = "date";

        // Forecast id returned by API, to identify icons to be used
        public static final String COLUMN_WEATHER_ID = "weather_id";

        // Short description of the weather forecast from the API
        public static final String COLUMN_SHORT_DESC = "short_desc";

        // Min and Max temperatures
        public static final String COLUMN_MIN_TEMP = "min";
        public static final String COLUMN_MAX_TEMP = "max";

        //Humidity: stored as float (percentage)
        public static final String COLUMN_HUMIDITY = "humidity";

        // Pressure: stored as float (percentage)
        public static  final  String COLUMN_PRESSURE = "pressure";

        // Wind: store as a float (mph)
        public static final String COLUMN_WIND_SPEED = "wind";
        // Degrees as a float
        public static final String COLUMN_DEGREES = "degrees";

        public static Uri buildWeatherUri (long id) {
            Log.v("********* LOG_TAG ", ContentUris.withAppendedId(CONTENT_URI, id).toString() + "");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWeatherLocation (String locationSetting) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

        public static Uri buildWeatherLocationWithStartDate (String locationSetting, String startDate) {
             return CONTENT_URI.buildUpon().appendPath(locationSetting).appendQueryParameter(COLUMN_DATETEXT, startDate).build();
        }

        public static Uri buildWeatherLocationWithDate (String locationSetting, String date) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).appendPath(date).build();
        }

        public static String getLocationSettingFromUri (Uri uri) {
            // get decoded path segments: location setting
            return uri.getPathSegments().get(1);
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getStartDateFromUri (Uri uri) {
            return uri.getQueryParameter(COLUMN_DATETEXT);
        }


    }

    /**
     * Inner class that defines the table contents of the location table
     */
    public static  final class LocationEntry implements BaseColumns {

        public static final Uri CONTENT_URI =  BASE_CONTENT_URI.buildUpon()
                .appendPath(LOCATION_PATH).build();

        public static Uri buildLocationUri (long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // Mime type prefixes that indicates type of data to be returned
        // (DIR or SINGLE ITEM)
        public static final String CONTENT_TYPE_DIR =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + LOCATION_PATH;
        public  static final  String CONTENT_TYPE_ITEM =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + LOCATION_PATH;

        // Table name
        public static final String TABLE_NAME = "location";

        // Location setting string (to be sent to openweathermap as query
        public static final String COLUMN_LOCATION_SETTING = "location_setting";

        // Human readable location string, provided by the API.
        public static final String COLUMN_CITY_NAME = "city_name";

        // Geolocation map data (longitudes and latitudes)
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";

    }

    public static final String DATE_FORMAT = "yyyyMMdd";
    public static String getDbDateString(Date date) {
        // Change returned unix timestamp into a readable date format
        SimpleDateFormat simpleDate = new SimpleDateFormat(DATE_FORMAT);
        return simpleDate.format(date);
    }

    /**
     * Convert date text to a long unix time representation
     * @param dateString the input date string
     * @return the Date Object
     */
    public static Date getDateFromDb(String dateString) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            return dbDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}

