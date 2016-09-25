package com.example.android.forecast.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by nerd on 25/09/2016.
 */

public class WeatherProvider extends ContentProvider {

    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private static final UriMatcher uriMatcher = buildUriMatcher();
    private static UriMatcher buildUriMatcher () {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ForecastContract.CONTENT_AUTHORITY;

        // for each uri type, create a corresponding code
        matcher.addURI(authority, ForecastContract.WEATHER_PATH, WEATHER);
        // Use * since the location and date are stored as Strings
        // Technically the date will always be numeric
        matcher.addURI(authority, ForecastContract.WEATHER_PATH + "/*", WEATHER_WITH_LOCATION);
        matcher.addURI(authority, ForecastContract.WEATHER_PATH + "/*/*", WEATHER_WITH_LOCATION_AND_DATE);

        matcher.addURI(authority, ForecastContract.LOCATION_PATH, LOCATION);
        // Use # since the id is a long type
        matcher.addURI(authority, ForecastContract.LOCATION_PATH + "/#", LOCATION_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
