package com.example.android.forecast.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by nerd on 05/10/2016.
 */

public class ForecastSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static ForecastSyncAdapter sForecastSyncAdapter = null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sForecastSyncAdapter.getSyncAdapterBinder();
    }

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sForecastSyncAdapter == null) {
                sForecastSyncAdapter = new ForecastSyncAdapter(getApplicationContext(), true);
            }
        }
    }
}
