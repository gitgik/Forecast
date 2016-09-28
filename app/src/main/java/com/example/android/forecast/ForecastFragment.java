package com.example.android.forecast;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }


    public ArrayAdapter<String> forecastAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // allow this fragment to handle menu events
        setHasOptionsMenu(true);
    }


    private void updateWeather () {
        FetchWeatherTask fetch = new FetchWeatherTask(getActivity());

    }

    @Override
    public void onStart () {
        super .onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);

        // Empty array list
        List<String> weekForecast = new ArrayList<String>();

        // Array adapter take raw data and populate the ListView it is attached to.
        forecastAdapter = new ArrayAdapter<String>(
                getActivity(), // the current context
                R.layout.list_item_forecast, // Id of list-item layout (list_item_forecast.xml)
                R.id.list_item_forecast_textview, // Id of text view
                weekForecast);



        // Get a reference to list view and attach the adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);

        // Listen for clicks on the item
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = forecastAdapter.getItem(position);
                // show the toast
                Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
    }
}
