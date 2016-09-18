package com.example.android.forecast;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    private ArrayAdapter<String> forecastAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);


        String[] forecastArray = {
                "Today - sunny - 99/67",
                "Tomorrow - cloudy - 75/61",
                "Weds - heavy rain - 71/56"
        };

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));

        // Array adapter take raw data and populate the ListView it is attached to.
        forecastAdapter = new ArrayAdapter <String>(
                getActivity(), // the current context
                R.layout.list_item_forecast, // Id of list-item layout (list_item_forecast.xml)
                R.id.list_item_forecast_textview, // Id of text view
                weekForecast);

        // Get a reference to list view and attach the adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);

        return rootView;
    }
}
