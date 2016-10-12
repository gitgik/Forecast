package com.example.android.forecast;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Created by nerd on 12/10/2016.
 */


public class LocationEditTextPreference extends EditTextPreference {
    private int mMinLength;
    private static final int DEFAULT_MINIMUM_LOCATION_LENGTH = 2;

    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LocationEditTextPreference,
                0, 0);
        try {
            mMinLength = a.getInteger(R.styleable.LocationEditTextPreference_minLength,
                    DEFAULT_MINIMUM_LOCATION_LENGTH);
        } finally {
            // Recycle the typed array to be reused by a later caller
             a.recycle();
        }
    }
}
