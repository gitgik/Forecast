package com.example.android.forecast.data;

import android.provider.BaseColumns;

/**
 * Created by nerd on 22/09/2016.
 */

public class ForecastContract {
    /**
     * Inner class that defines the table contents of forecast table
     */
    public static final class WeatherEntry implements BaseColumns {

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
    }

    /**
     * Inner class that defines the table contents of the location table
     */
    public static  final class LocationEntry implements BaseColumns {
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
}

