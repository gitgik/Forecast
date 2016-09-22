package com.example.android.forecast.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.forecast.data.ForecastContract.LocationEntry;
import com.example.android.forecast.data.ForecastContract.WeatherEntry;

/**
 * Created by nerd on 22/09/2016.
 */

public class ForecastDbHelper extends SQLiteOpenHelper {
    // if you change the db schema. you must increment the version
    private static final int DATABASE_VERSION  = 1;
    public static final String DATABASE_NAME = "forecast.db";

    public ForecastDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a table to hold locations.
        final String SQL_CREATE_FORECAST_TABLE = "CREATE TABLE " +
                WeatherEntry.TABLE_NAME + "(" +
                WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
                WeatherEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " +

                WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " +

                WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
                WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, " +

                " FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                LocationEntry.TABLE_NAME + "(" + LocationEntry._ID + "), " +

                // Create a unique constraint with replace strategy
                // To ensure that we have one weather entry per day.
                "UNIQUE (" + WeatherEntry.COLUMN_DATETEXT + ", " +
                WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
