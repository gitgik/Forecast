package com.example.android.forecast;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.example.android.forecast.data.ForecastContract;
import com.example.android.forecast.sync.ForecastSyncAdapter;

/**
 * Created by nerd on 05/10/2016.
 */

public class SettingsFragment extends PreferenceFragment {

    public SettingsFragment() {
        super();
    }
    // Track whether onSharedPreferenceChanged is called in the binding step
    // or called on an actual preference change later
    // We are not using preference change initially to populate the summary
    boolean mBindingPreferences = false;

    private SharedPreferences.OnSharedPreferenceChangeListener mListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals(getString(R.string.pref_location_key))) {
                        Preference locationPref = findPreference(key);
                        locationPref.setSummary(sharedPreferences.getString(key, getString(R.string.pref_location_default)));
                    }

                    if (key.equals(getString(R.string.pref_units_key))) {
                        Preference tempUnitsPref = findPreference(key);
                        tempUnitsPref.setSummary(sharedPreferences.getString(key, getString(R.string.pref_units_metric)));
                    }

                    if (!mBindingPreferences) {
//                        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
//                        String location = sharedPreferences.getString(key, getString(R.string.pref_location_default));
//                        weatherTask.execute(location);
                        ForecastSyncAdapter.syncImmediately(getActivity());
                    } else {
                        // Notify code that weather may be impacted
                        getActivity().getContentResolver().notifyChange(ForecastContract.WeatherEntry.CONTENT_URI, null);
                    }

                }
            };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_general);

        bindPreferencesSummaryToValue();
    }

    private void bindPreferencesSummaryToValue() {
        mBindingPreferences = true;

        // Update the location preferences
        // Update can also be done with a listener
        findPreference(getString(R.string.pref_location_key))
                .setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default)));

        // Update the temp units preference
        findPreference(getString(R.string.pref_units_key))
                .setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getString(getString(R.string.pref_units_key), getString(R.string.pref_units_metric)));

        mBindingPreferences = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(mListener);
    }

}
