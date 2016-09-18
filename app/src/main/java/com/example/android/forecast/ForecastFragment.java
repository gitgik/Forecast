package com.example.android.forecast;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.util.LogWriter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
        forecastAdapter = new ArrayAdapter<String>(
                getActivity(), // the current context
                R.layout.list_item_forecast, // Id of list-item layout (list_item_forecast.xml)
                R.id.list_item_forecast_textview, // Id of text view
                weekForecast);

        // Get a reference to list view and attach the adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            FetchWeatherTask fetch = new FetchWeatherTask();
            fetch.execute("Nairobi");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected void onPostExecute(String[] results) {
            if (results != null) {
                forecastAdapter.clear();
                for (String dayForecastString : results) {
                    forecastAdapter.add(dayForecastString);
                }
            }
        }

        /*
         * Converts Unix timestamp to readable date
        */
        private String getReadableDataString (long time) {
            Date date = new Date(time * 1000);
            SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
            return format.format(date).toString();
        }

        /*
         * Format weather's high and low temperature
         */
        private String formatHighLows (double high, double low) {
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String result = roundedHigh + "/" + roundedLow;
            return result;
        }

        /*
         * Format json string to relevant data
         */
        private String [] getWeatherFromJson(String jsonString, int days) throws JSONException {
            // JSON objects that need to be extracted
            final String LIST = "list";
            final String WEATHER = "weather";
            final String TEMPERATURE = "temp";
            final String MAX = "max";
            final String MIN = "min";
            final String DATETIME = "dt";
            final String DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(jsonString);
            JSONArray weatherArray = forecastJson.getJSONArray(LIST);

            String[] resultStrings = new String[days];
            for (int i = 0; i < weatherArray.length(); i++) {
                String day, description, highLow;

                // Get json object data representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                long dateTime = dayForecast.getLong(DATETIME);
                day = getReadableDataString(dateTime);

                // description is in child array "weather"
                JSONObject weatherObject = dayForecast.getJSONArray(WEATHER).getJSONObject(0);
                description = weatherObject.getString(DESCRIPTION);

                // Temperature are in object called "temp"
                JSONObject temperatureObject = dayForecast.getJSONObject(TEMPERATURE);
                double highTemperature = temperatureObject.getDouble(MAX);
                double lowTemperature = temperatureObject.getDouble(MIN);

                highLow = formatHighLows(highTemperature, lowTemperature);

                resultStrings[i] = day + " –– " + description + " –– " + highLow;
            }

            for (String s : resultStrings) {
                Log.v(LOG_TAG, "FORECAST ENTRY: " + s);
            }

            return resultStrings;
        }

        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will store the raw JSON response as a string.
            String forecastJsonString = null;
            String apiKey = "de07b800a0f30d675a6ceb7d9b30ce11";

            try {
                // Construct URL for the OpenWeatherMap API
                final String QUERY_PARAM = "q";
                final String FORECAST_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String FORMAT = "mode";
                final String UNITS = "units";
                final String DAYS = "cnt";
                final String API_KEY = "APPID";

                Uri buildUri = Uri.parse(FORECAST_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT, "json")
                        .appendQueryParameter(UNITS, "metric")
                        .appendQueryParameter(DAYS, "7")
                        .appendQueryParameter(API_KEY, apiKey).build();

                URL url = new URL(buildUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = reader.readLine()) != null) {
                    // add new line for easier debugging
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return  null;
                }
                forecastJsonString = buffer.toString();
                // log the json string
                Log.v(LOG_TAG, "JSON: " + forecastJsonString);

            } catch (IOException e) {
                Log.e(LOG_TAG, "NETWORK ERROR: ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try
                    {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "CLOSING STREAM", e);
                    }
                }
            }

            try {
                return getWeatherFromJson(forecastJsonString, 7);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
