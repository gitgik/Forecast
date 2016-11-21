package com.example.android.forecast;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.forecast.data.ForecastContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class  DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";
    static final String DETAIL_TRANSITION_ANIMATION = "DTA";
    private static final String FORECAST_SHARE_HASHTAG = "ForecastApp";


    public static final String DATE_KEY = "date";
    public static final String LOCATION_KEY = "location";


    private String mForecastString;
    private Uri mUri;
    private boolean mTransitionAnimation;

    private static final int DETAIL_LOADER = 0;
    private String mLocation;

    private static final String[] DETAIL_COLUMNS = {
            // In this case the id needs to be fully qualified with a
            // table name , since the content provider joins the location
            // & weather tables in the background
            ForecastContract.WeatherEntry.TABLE_NAME + "." + ForecastContract.WeatherEntry._ID,
            ForecastContract.WeatherEntry.COLUMN_DATETEXT,
            ForecastContract.WeatherEntry.COLUMN_SHORT_DESC,
            ForecastContract.WeatherEntry.COLUMN_MAX_TEMP,
            ForecastContract.WeatherEntry.COLUMN_MIN_TEMP,
            ForecastContract.WeatherEntry.COLUMN_HUMIDITY,
            ForecastContract.WeatherEntry.COLUMN_PRESSURE,
            ForecastContract.WeatherEntry.COLUMN_WIND_SPEED,
            ForecastContract.WeatherEntry.COLUMN_DEGREES,
            ForecastContract.WeatherEntry.COLUMN_WEATHER_ID,
            // Return the location data from the JOIN
            ForecastContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;

    public ImageView iconView;
    public TextView dateView;
    public TextView descriptionView;
    public TextView highTemperatureView;
    public TextView lowTemperatureView;
    public TextView humidityView;
    public TextView humidityLabelView;
    public TextView windView;
    public TextView windLabelView;
    public TextView pressureView;
    public TextView pressureLabelView;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // allow this fragment to handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri  = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
            mTransitionAnimation = arguments.getBoolean(
                    DetailActivityFragment.DETAIL_TRANSITION_ANIMATION, false);
        }
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        iconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        dateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        descriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        highTemperatureView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        lowTemperatureView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        humidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        windView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        pressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        return rootView;
    }

    private void finishCreatingMenu(Menu menu) {
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareForecastIntent());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getActivity() instanceof DetailActivity) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.menu_detail_fragment, menu);
            finishCreatingMenu(menu);
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastString + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }



    private Intent shareForecastIntent () {
        Intent intent = new Intent(Intent.ACTION_SEND);
        // prevent the activity from being placed into the activity stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,
                mForecastString + FORECAST_SHARE_HASHTAG);

        return intent;
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        // save the location to our bundle
        super.onSaveInstanceState(outState);
        if (null != mLocation) {
            outState.putString(LOCATION_KEY, mLocation);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Intent intent = getActivity().getIntent();
        Bundle arguments = getArguments();
        if (null != mLocation && arguments != null &&
                arguments.containsKey(DetailActivity.DATE_KEY) &&
                !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent  = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = ForecastContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = ForecastContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            // Create and return a cursor loader that will take care
            // of creating a cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        ViewParent vp = getView().getParent();
        if (vp instanceof CardView) {
            ((View)vp).setVisibility(View.INVISIBLE);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            ViewParent vp = getView().getParent();
            if (vp instanceof CardView) {
                ((View)vp).setVisibility(View.VISIBLE);
            }
            // Read weather condition id from the cursor
            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);

            // Use glide to set images
            Glide.with(this).load(Utility.getArtUrlForWeatherCondition(getActivity(), weatherId))
                    .error(Utility.getArtResourceForWeatherCondition(weatherId))
                    .into(iconView);

            // Read the date from cursor and update views for day of week
            long date = data.getLong(COL_WEATHER_DATE);
            Log.v(LOG_TAG, "DATE: ++++++++++++++++>" + Long.toString(date));
            String dateText = Utility.getFullFriendlyDayString(getActivity(), date);
            dateView.setText(dateText);

            // Get description from weather condition ID
            String description = Utility.getStringForWeatherCondition(getActivity(), weatherId);
            descriptionView.setText(description);
            descriptionView.setContentDescription(getString(R.string.a11y_forecast, description));

            // Read high temperature from cursor and update view
            boolean isMetric = Utility.isMetric(getActivity());

            double high = data.getDouble(COL_WEATHER_MAX_TEMP);
            String highText = Utility.formatTemperature(getActivity(), high);
            highTemperatureView.setText(highText);
            highTemperatureView.setContentDescription(getString(R.string.a11y_high_temp, highText));

            double low = data.getDouble(COL_WEATHER_MIN_TEMP);
            String lowText = Utility.formatTemperature(getActivity(), low);
            lowTemperatureView.setText(highText);
            lowTemperatureView.setContentDescription(getString(R.string.a11y_low_temp, lowText));

            // Read humidity from cursor and update view
            float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
            humidityView.setText(getActivity().getString(R.string.format_humidity, humidity));
            humidityView.setContentDescription(getString(R.string.a11y_humidity, humidityView.getText()));

            // Read wind speed and direction from cursor and update view
            float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
            float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
            windView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));
            windView.setContentDescription(getString(R.string.a11y_wind, windView.getText()));

            // Read pressure from cursor and update view
            float pressure = data.getFloat(COL_WEATHER_PRESSURE);
            pressureView.setText(getString(R.string.format_pressure, pressure));
            pressureView.setContentDescription(getString(R.string.a11y_pressure, pressureView.getText()));

            // For the share intent
            mForecastString = String.format("%s - %s - %s/%s",
                    dateText,
                    description,
                    high,
                    low);

        }

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

        // We need to start the enter transition after the data has loaded
        if (mTransitionAnimation) {
            activity.supportStartPostponedEnterTransition();

            if ( null != toolbarView ) {
                activity.setSupportActionBar(toolbarView);

                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            if ( null != toolbarView ) {
                Menu menu = toolbarView.getMenu();
                if ( null != menu ) menu.clear();
                toolbarView.inflateMenu(R.menu.menu_detail_fragment);
                finishCreatingMenu(toolbarView.getMenu());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
