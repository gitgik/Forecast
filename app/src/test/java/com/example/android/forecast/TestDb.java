package com.example.android.forecast;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.forecast.data.ForecastContract.LocationEntry;
import com.example.android.forecast.data.ForecastDbHelper;


/**
 * Created by nerd on 22/09/2016.
 */

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
     mContext.deleteDatabase(ForecastDbHelper.DATABASE_NAME);
     SQLiteDatabase db = new ForecastDbHelper(
             this.mContext).getWritableDatabase();
     assertEquals(true, db.isOpen());
     db.close();
    }

    public void testInsertAndReadDb() {
        // Test data to be inserted into tables
        String testName = "Mountain View";
        String testLocationSetting = "99705";
        double testLatitude = 64.772;
        double testLongitude = 147.355;

        ForecastDbHelper dbHelper = new ForecastDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, with column names as keys
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_CITY_NAME, testName);
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        values.put(LocationEntry.COLUMN_LATITUDE, testLatitude);
        values.put(LocationEntry.COLUMN_LONGITUDE, testLongitude);

        // Insert data into the database
        long locationRowId;
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, values);

        // Verify the row exists.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);


        String [] columns = {
                LocationEntry._ID,
                LocationEntry.COLUMN_LOCATION_SETTING,
                LocationEntry.COLUMN_CITY_NAME,
                LocationEntry.COLUMN_LATITUDE,
                LocationEntry.COLUMN_LONGITUDE
        };

        // The primary interface to the query results.
        // Enables traversal over the records in a DB
        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME,
                columns,
                null, // columns for the "where" clause
                null, // values for the "where" clause
                null, // column to group by
                null, // Columns to filter by row groups
                null  // set order
        );

        if (cursor.moveToFirst()) { // move cursor to first row
            // Get the value of each column using their column index
            int locationIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING);
            String location = cursor.getString(locationIndex);
            Log.v(LOG_TAG, "Location index: " + location);

            int nameIndex = cursor.getColumnIndex(LocationEntry.COLUMN_CITY_NAME);
            String name = cursor.getString(nameIndex);
            Log.v(LOG_TAG, "Name index: " + name);

            int latIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LATITUDE);
            String latitude = cursor.getString(latIndex);

            int longIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LONGITUDE);
            String longitude = cursor.getString(longIndex);

            // Assert retrieved location data equals test data
            assertEquals(testName, name);
            assertEquals(testLocationSetting, locationIndex);
            assertEquals(testLatitude, latitude);
            assertEquals(testLongitude, longitude);

        } else {
            fail("No values returned");
        }
    }
}
