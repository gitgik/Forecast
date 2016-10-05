package com.example.android.forecast.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by nerd on 05/10/2016.
 */

/**
 * A bound service that instantiates the authenticator when started
 */
public class ForecastAuthenticatorService extends Service {

    // Instance field that stores the authenicator object
    private ForecastAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new Authenticator object
        mAuthenticator  = new ForecastAuthenticator(this);

        super.onCreate();
    }


    /**
     * When the system binds to this service to make the RPC call
     * return the authenticator's IBinder
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}

