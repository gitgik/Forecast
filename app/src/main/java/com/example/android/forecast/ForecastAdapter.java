package com.example.android.forecast;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.forecast.data.ForecastContract;

/**
 * Created by nerd on 29/09/2016.
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    /**
     * Cache the child views for a forecast list item
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highView;
        public final TextView lowView;

        public ViewHolder(View view) {
            super(view);
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int dateColumnIndex = mCursor.getColumnIndex(ForecastContract.WeatherEntry.COLUMN_DATETEXT);
            mClickHandler.onClick(mCursor.getLong(dateColumnIndex), this);
        }


    }

    public static interface ForecastAdapterOnClickHandler {
        void onClick(Long date, ViewHolder viewHolder);
    }

    public final String TAG = ForecastAdapter.class.getSimpleName();

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;
    // Flag to determine if we want to use a separate view for "today"
    private boolean mUseTodayLayout = true;

    private Cursor mCursor;
    private final Context mContext;
    private ForecastAdapterOnClickHandler mClickHandler;
    private final View mEmptyView;

    public ForecastAdapter (Context context, ForecastAdapterOnClickHandler clickHandler, View emptyView) {
        mContext = context;
        mClickHandler = clickHandler;
        mEmptyView = emptyView;
    }


    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_TODAY: {
                    layoutId = R.layout.list_item_base_forecast_today;
                    break;
                }
                case VIEW_TYPE_FUTURE_DAY: {
                    layoutId = R.layout.list_item_forecast;
                    break;
                }
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new ViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        mCursor.moveToPosition(position);
        int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int defaultImage;

        switch (getItemViewType(position)) {
            case VIEW_TYPE_TODAY:
                defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
                Glide.with(mContext)
                        .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                        .error(defaultImage)
                        .crossFade()
                        .into(viewHolder.iconView);
                break;
            default:
                defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);
                Glide.with(mContext)
                        .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                        .error(defaultImage)
                        .crossFade()
                        .into(viewHolder.iconView);
        }

        String dateInMillis = mCursor.getString(ForecastFragment.COL_WEATHER_DATE);
        viewHolder.dateView.setText(Utility.getFriendlyDayString(mContext, dateInMillis));

        // Read weather forecast from cursor
        String description = mCursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.descriptionView.setText(description);
        viewHolder.descriptionView.setContentDescription(
                mContext.getString(R.string.a11y_forecast,description));


        //Read user preference for temperature units
        boolean isMetric = Utility.isMetric(mContext);

        // Read the temperature from the cursor.
        double high = mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        String highTemp = Utility.formatTemperature(mContext, high, isMetric);
        viewHolder.highView.setText(highTemp);
        viewHolder.highView.setContentDescription(mContext.getString(
                R.string.a11y_high_temp, description));

        double low = mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        String lowTemp = Utility.formatTemperature(mContext, low, isMetric);
        viewHolder.lowView.setText(lowTemp);
        viewHolder.lowView.setContentDescription(mContext.getString(R.string.a11y_low_temp, lowTemp));


    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        // Set visibility of empty view depending on item count
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

//    public Cursor getCursor() { return mCursor; }

//    @Override
//    public int getViewTypeCount() {
//        // There are two different layouts
//        return 2;
//    }


//    @Override
//    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//        // Choose the layout type
//        int viewType = getItemViewType(cursor.getPosition());
//        int layoutId = -1;
//
//        switch (viewType) {
//
//            case VIEW_TYPE_TODAY:
//                layoutId = R.layout.list_item_forecast_today;
//                break;
//
//            case VIEW_TYPE_FUTURE_DAY:
//                layoutId = R.layout.list_item_forecast;
//                break;
//        }
//
//        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
//        ViewHolder viewHolder = new ViewHolder(view);
//        view.setTag(viewHolder);
//
//        return view;
//    }

//    @Override
//    public void bindView(View view, Context context, Cursor cursor) {
//        // View holder already contains references to the relevant view,
//        // Set the appropriate values through the viewHolder reference instead of costly
//        // findViewById calls
//
//        // Read from the tag to get back the viewHolder object
//        ViewHolder viewHolder = (ViewHolder) view.getTag();
//
//        // Read weather icon ID from cursor
//        int weatherId;
//        int defaultImage;
//        // Use image that corresponds to the weather code from the API.
//        int viewType= getItemViewType(cursor.getPosition());
//        switch (viewType) {
//            case VIEW_TYPE_TODAY: {
//                // Get weather icon
//                weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
//                defaultImage = Utility.getArtResourceForWeatherCondition(weatherId);
//                Glide.with(context)
//                        .load(Utility.getArtUrlForWeatherCondition(context, weatherId))
//                        .error(defaultImage)
//                        .crossFade()
//                        .into(viewHolder.iconView);
//                break;
//            }
//            default: {
//                // get weather icon
//                weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
//                defaultImage = Utility.getIconResourceForWeatherCondition(weatherId);
//                Glide.with(context)
//                        .load(Utility.getArtUrlForWeatherCondition(context, weatherId))
//                        .error(defaultImage)
//                        .crossFade()
//                        .into(viewHolder.iconView);
//                break;
//            }
//        }
//
//        // Read date from cursor
//        String dateString = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
//
//        // Find TextView and set formatted date on it
//        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateString));
//
//        // Read weather forecast from cursor
//        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
//        viewHolder.descriptionView.setText(description);
//
//        //Read user preference for temperature units
//        boolean isMetric = Utility.isMetric(context);
//
//        float high = cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP);
//        viewHolder.highView.setText(Utility.formatTemperature(context, high, isMetric));
//
//        float low = cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP);
//        viewHolder.lowView.setText(Utility.formatTemperature(context, low, isMetric));
//
//    }
}
