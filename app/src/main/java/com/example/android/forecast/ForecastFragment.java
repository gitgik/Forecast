package com.example.android.forecast;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.forecast.data.ForecastContract;
import com.example.android.forecast.data.ForecastContract.LocationEntry;
import com.example.android.forecast.data.ForecastContract.WeatherEntry;

import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor>{

    private String mLocation;
    private int mPosition;
    private static final String LOCATION_KEY = "location";
    private static final String POSITION_KEY = "position";

    // The loader ID for this Loader
    private static final int FORECAST_LOADER = 0;
    // Specify the columns to show
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a
            // table name , since the content provider joins the location
            // & weather tables in the background
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_WEATHER_ID,
            LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to FORECAST_COLUMNS.
    // If FORECAST_COLUMNS changes, these must also change
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_CONDITION_ID = 5;
    public static final int COL_LOCATION_SETTING = 6;


    private ForecastAdapter forecastAdapter;


    @Override
    public void onActivityCreated(Bundle saveDInstanceState) {
        super.onActivityCreated(saveDInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // allow this fragment to handle menu events
        setHasOptionsMenu(true);
    }

    private void updateWeather () {
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        weatherTask.execute(Utility.getPreferredLocation(getActivity()));
    }

    @Override
    public void onStart () {
        super.onStart();
        // updateWeather();
    }

    @Override
    public void onResume() {
        // Check if our location has changed to update the weather
        super.onResume();
        if (mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            // restart our loader
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null && savedInstanceState.containsKey(LOCATION_KEY)) {
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY)) {
            mPosition = savedInstanceState.getInt(POSITION_KEY);
        }

        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);
        // Get a reference to list view and attach the adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);

        // Create our custom adapter.
        // Unlike simple cursor adapter: No need to define db columns it should be mapping
        // The simple cursor adapter takes raw data and populates the ListView it is attached to.
        forecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        listView.setAdapter(forecastAdapter);
        // Listen for clicks on the item
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ForecastAdapter adapter = (ForecastAdapter) adapterView.getAdapter();
                Cursor cursor = adapter.getCursor();
                if (null != cursor && cursor.moveToPosition(position)) {
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .putExtra(DetailActivityFragment.DATE_KEY, cursor.getString(COL_WEATHER_DATE));
                    startActivity(intent);
                }
                mPosition = position;

            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new loader needs to be created.

        // Get current date
        String startDate = ForecastContract.getDbDateString(new Date());

        // Sort order: Ascending, by date.
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";
        mLocation = Utility.getPreferredLocation(getActivity());

        Uri weatherLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(mLocation, startDate);

        Log.v("*** Forecast Fragment: ", "URI: " + weatherLocationUri.toString());

        return new CursorLoader(
                getActivity(),
                weatherLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Use the data from the cursor that the loader just loaded.
        forecastAdapter.swapCursor(data);

        if (!mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        } else if (mPosition != ListView.INVALID_POSITION) {

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Put in null to clear the data
        forecastAdapter.swapCursor(null);
    }
}
