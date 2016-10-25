package com.example.android.forecast;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.example.android.forecast.sync.ForecastSyncAdapter;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILACTIVITYFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;
    private String mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocation = Utility.getPreferredLocation(this);
        Uri contentUri = getIntent() != null ? getIntent().getData() : null;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

//        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large
            // screen layouts
            // If this view is present, then the activity will be in two-pane mode
            mTwoPane = true;


            if (savedInstanceState == null) {
                DetailActivityFragment fragment = new DetailActivityFragment();
                if (contentUri != null ) {
                    Bundle args = new Bundle();
                    // In two-pane mode, show the detail view in this activity by
                    // adding a detail fragment using a fragment transaction
                    args.putParcelable("URI", contentUri);
                    fragment.setArguments(args);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, fragment, DETAILACTIVITYFRAGMENT_TAG)
                        .commit();
            }

        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        ForecastFragment forecastFragment = ((ForecastFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment));
        forecastFragment.setUseTodayLayout(!mTwoPane);

        // Make sure we have an account created to allow sync
        ForecastSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap() {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String location = sharedPrefs.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default)
        );

        Uri geoLocationUri = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();

        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        mapIntent.setData(geoLocationUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // start the activity with this intent
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Log.d(LOG_TAG, "" + getPackageManager().toString());
            Log.d(LOG_TAG, "MAP ERROR: Could not call " + location);
        }

    }

    @Override
    public void onItemSelected(String date) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a fragment
            // transaction

            getSupportFragmentManager().beginTransaction().replace(
                    R.id.weather_detail_container,
                    DetailActivityFragment.newInstance(date)).commit();

        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.DATE_KEY, date);
            startActivity(intent);
        }
    }
}
