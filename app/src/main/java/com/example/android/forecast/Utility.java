package com.example.android.forecast;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.forecast.data.ForecastContract;
import com.example.android.forecast.sync.ForecastSyncAdapter;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by nerd on 28/09/2016.
 */

public class Utility {
    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    public static String formatTemperature(Context context, double temperature, boolean isMetric) {
        double temp;
        if ( !isMetric ) {
            temp = 9 * temperature / 5 + 32;
        } else {
            temp = temperature;
        }
        // append the degrees symbol for the temperatures
        return context.getString(R.string.format_temperature, temp);
    }

    static String getFormattedWind(Context context, float windSpeed, float degrees) {
        int windFormat;
        if (isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        // From wind direction in degrees, determine compass direction as a string (e.g NW)
        // You know what's fun, writing really long if/else statements with tons of possible
        // conditions.  Seriously, try it!
        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 || degrees < 22.5) {
            direction = "NW";
        }
        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    static String formatDate(long dateInMillis) {
//        Date date = ForecastContract.getDateFromDb(dateString);
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }

    static String getFormattedMonthDay(Context context, long dateInMillis) {
        android.text.format.Time time = new android.text.format.Time();
        time.setToNow();
//        SimpleDateFormat dateFormat = new SimpleDateFormat(ForecastContract.DATE_FORMAT);
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthFormat.format(dateInMillis);
        return  monthDayString;
//        try {
//            Date inputDate = dateFormat.parse(dateString);
//            SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
//            String monthDayString = monthDayFormat.format(inputDate);
//            return monthDayString;
//        } catch (ParseException e) {
//            e.printStackTrace();
//            return null;
//        }
    }

    public static String getFriendlyDayString (Context context, long dateInMillis, boolean displayLongToday) {
        // For today: "Today, September 29
        // For tomorrow: "Tomorrow", ...etc
        android.text.format.Time time = new android.text.format.Time();
        time.setToNow();

        long currentTime = System.currentTimeMillis();
        int julianDay = android.text.format.Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = android.text.format.Time.getJulianDay(currentTime, time.gmtoff);

        if (displayLongToday && julianDay == currentJulianDay) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId, today, getFormattedMonthDay(context, dateInMillis)));
        } else if (julianDay < currentJulianDay + 7) {
            // If less than a week in the future, just return the day name
            return getDayName(context, dateInMillis);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE MM dd");
            return dateFormat.format(dateInMillis);
        }

//        Date todayDate = new Date();
//        String todayString = ForecastContract.getDbDateString(todayDate);
//        Date inputDate = ForecastContract.getDateFromDb(dateString);
//
//        if (todayString.equals(dateString)) {
//            String today = context.getString(R.string.today);
//            int formatId = R.string.format_full_friendly_date;
//
//            return String.format(
//                    context.getString(
//                            formatId, today, getFormattedMonthDay(context, dateString)));
//        } else {
//            Calendar cal = Calendar.getInstance();
//            cal.setTime(todayDate);
//            cal.add(Calendar.DATE, 7);
//            String weekFutureString = ForecastContract.getDbDateString(cal.getTime());
//
//            if (dateString.compareTo(weekFutureString) <  0) {
//                return getDayName(context, dateString);
//            } else {
//                SimpleDateFormat shortenedDayFormat = new SimpleDateFormat("EEE MMM dd");
//                return  shortenedDayFormat.format(inputDate);
//            }
//        }
    }



    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return
     */
    public static String getDayName(Context context, long dateInMillis) {
        android.text.format.Time t = new android.text.format.Time();
        t.setToNow();
        int julianDay = android.text.format.Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = android.text.format.Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if (julianDay == currentJulianDay + 1) {
            return context.getString(R.string.tomorrow);
        } else {
            android.text.format.Time time = new android.text.format.Time();
            time.setToNow();
            // other days of the week
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }


//        SimpleDateFormat dbDateFormat = new SimpleDateFormat(ForecastContract.DATE_FORMAT);
//        try {
//            Date inputDate = dbDateFormat.parse(dateStr);
//            Date todayDate = new Date();
//            // If the date is today, return the localized version of "Today" instead of the actual
//            // day name.
//            if (ForecastContract.getDbDateString(todayDate).equals(dateStr)) {
//                return context.getString(R.string.today);
//            } else {
//                // If the date is set for tomorrow, the format is "Tomorrow".
//                Calendar cal = Calendar.getInstance();
//                cal.setTime(todayDate);
//                cal.add(Calendar.DATE, 1);
//                Date tomorrowDate = cal.getTime();
//                if (ForecastContract.getDbDateString(tomorrowDate).equals(
//                        dateStr)) {
//                    return context.getString(R.string.tomorrow);
//                } else {
//                    // Otherwise, the format is just the day of the week (e.g "Wednesday".
//                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
//                    return dayFormat.format(inputDate);
//                }
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//            // It couldn't process the date correctly.
//            return "";
//        }
    }

    /**
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding image. -1 if no relation is found.
     */
    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_rain;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }

    public static String getArtUrlForWeatherCondition(Context context, int weatherId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String formatArtUrl = prefs.getString(
                context.getString(R.string.pref_art_pack_key),
                context.getString(R.string.pref_art_pack_sunshine));

        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return String.format(Locale.US, formatArtUrl, "storm");
        } else if (weatherId >= 300 && weatherId <= 321) {
            return String.format(Locale.US, formatArtUrl, "light_rain");
        } else if (weatherId >= 500 && weatherId <= 504) {
            return String.format(Locale.US, formatArtUrl, "rain");
        } else if (weatherId == 511) {
            return String.format(Locale.US, formatArtUrl, "snow");
        } else if (weatherId >= 520 && weatherId <= 531) {
            return String.format(Locale.US, formatArtUrl, "rain");
        } else if (weatherId >= 600 && weatherId <= 622) {
            return String.format(Locale.US, formatArtUrl, "snow");
        } else if (weatherId >= 701 && weatherId <= 761) {
            return String.format(Locale.US, formatArtUrl, "fog");
        } else if (weatherId == 761 || weatherId == 781) {
            return String.format(Locale.US, formatArtUrl, "storm");
        } else if (weatherId == 800) {
            return String.format(Locale.US, formatArtUrl, "clear");
        } else if (weatherId == 801) {
            return String.format(Locale.US, formatArtUrl, "light_clouds");
        } else if (weatherId >= 802 && weatherId <= 804) {
            return String.format(Locale.US, formatArtUrl, "clouds");
        }
        return null;
    }


    /**
     * Check whether network is available for fetching weather data
     * @param context
     * @return isConnected
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }


    /**
     * Gets the location status
     * @param c Context used to get the SharedPreferences
     * @return the location status integer type
     */
    @SuppressWarnings("ResourceType")
    public static @ForecastSyncAdapter.LocationStatus
    int getLocationStatus(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_location_status_key),
                ForecastSyncAdapter.LOCATION_STATUS_UNKNOWN);
    }

    /**
     * Resets the location status
     * @param c Context used to get the  shared preferences
     */
    public static void resetLocationStatus(Context c) {
        Log.d("UTILITY", "RESETTING LOCATION STATUS");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(
                R.string.pref_location_status_key),
                ForecastSyncAdapter.LOCATION_STATUS_UNKNOWN);
        spe.apply(); // use this rather than commit() since it will be called from the UI thread
    }
}
