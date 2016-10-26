package com.example.android.forecast;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.forecast.data.ForecastContract;
import com.example.android.forecast.data.ForecastContract.LocationEntry;
import com.example.android.forecast.data.ForecastContract.WeatherEntry;
import com.example.android.forecast.sync.ForecastSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private String mLocation;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    private boolean mUseTodayLayout;
    private ForecastAdapter mForecastAdapter;
    private static final String SELECTED_KEY = "selected_position";

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




    /**
     * A callback interface that all activities containing this fragment
     * must implement. This mechanism allows activities to be notified
     * of item selection
     */
    public interface Callback {
        /**
         * Callback for when an item is selected
         */
        public void onItemSelected(Uri dateUri, ForecastAdapter.ViewHolder viewHolder);
    }

    public ForecastFragment () {

    }

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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void updateWeather () {
        ForecastSyncAdapter.syncImmediately(getActivity());
    }

    public void setUseTodayLayout (boolean useTodayLayout)
    {
        mUseTodayLayout = useTodayLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(useTodayLayout);
        }
    }

    @Override
    public void onStart () {
        super.onStart();
        // updateWeather();
    }

    @Override
    public void onResume() {
        // Register the on shared preference listener
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        // Check if our location has changed to update the weather
        super.onResume();
        if (mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            // restart our loader
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    @Override
    public void onPause() {
        // Unregister the listener for shared preferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
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

        // Get a reference to the RecyclerView, and attach this adapter to it.
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_forecast);

        // Set the layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        View emptyView = rootView.findViewById(R.id.recyclerview_forecast_empty);
        // Improve performance if the changes in content do not change the layout size of RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Unlike simple cursor adapter: No need to define db columns it should be mapping
        // The simple cursor adapter takes raw data and populates the RecyclerView it is attached to.
        mForecastAdapter = new ForecastAdapter(getActivity(),
                new ForecastAdapter.ForecastAdapterOnClickHandler() {

            @Override
            public void onClick(Long date, ForecastAdapter.ViewHolder viewHolder) {
                String location = Utility.getPreferredLocation(getActivity());
                ((Callback) getActivity()).onItemSelected(
                        ForecastContract.WeatherEntry.buildWeatherLocationWithDate(location, date), viewHolder);
                mPosition = viewHolder.getAdapterPosition();
            }
        }, emptyView);

//        listView.setEmptyView(emptyView);

//        mForecastAdapter = new ForecastAdapter(getActivity(), new ForecastAdapter.ForecastAdapterOnClickHandler())
        mRecyclerView.setAdapter(mForecastAdapter);


        // Listen for clicks on the item
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//                Cursor cursor = forecastAdapter.getCursor();
//                if (cursor != null && cursor.moveToPosition(position)) {
//                    ((Callback) getActivity()).onItemSelected(cursor.getString(COL_WEATHER_DATE));
//                }
//                // whenever the item is clicked, update it's position
//                mPosition = position;
//            }
//        });

        // If there is an instance state, mine it for information
        // This will make the app feel more fluid when the device is rotated
        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY)) {
            mPosition = savedInstanceState.getInt(POSITION_KEY);
        }

        // Activity on create might call before onCreateView
        // when the adapter is null
        // Therefore, we use mUserTodayLayout value to the adapter here too
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablet rotates, the currently selected item needs to be saved
        // When no item is selected, mPosition will be set to ListView.INVALID_POSITION
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(POSITION_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
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
        Long currentDate = System.currentTimeMillis();

        // Sort order: Ascending, by date.
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";
        mLocation = Utility.getPreferredLocation(getActivity());

        Uri weatherLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(mLocation, currentDate);

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
        mForecastAdapter.swapCursor(data);

        if (!mLocation.equals(Utility.getPreferredLocation(getContext()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
        updateEmptyView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Put in null to clear the data
        mForecastAdapter.swapCursor(null);
    }

    /**
     * Update the empty view based on the location status(SERVER STATUS)
     * and the network state of the phone
     */
    private void updateEmptyView() {
        if (mForecastAdapter.getItemCount() == 0) {
            TextView tv  = (TextView) getView().findViewById(R.id.recyclerview_forecast_empty);
            if (null != tv) {
                // if cursor is empty
                int message = R.string.empty_forecast;
                @ForecastSyncAdapter.LocationStatus int location = Utility.getLocationStatus(getActivity());
                switch (location) {
                    case ForecastSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                        message = R.string.server_down;
                        break;
                    case ForecastSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                        message = R.string.server_error;
                        break;
                    case ForecastSyncAdapter.LOCATION_STATUS_INVALID:
                        message = R.string.invalid_location;
                        break;
                    default:
                        // There is no internet connection
                        if (!Utility.isNetworkAvailable(getActivity())) {
                            message = R.string.no_internet;
                        }
                }
                tv.setText(message);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_location_key))) {
            updateEmptyView();
        }
    }
 }
