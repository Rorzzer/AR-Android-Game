package com.unimelb.comp30022.itproject;

/**
 * Created by Kiptenai on 10/10/2017.
 * Class that handles the fetching of gps coordinates and bearing
 * passes the informatin out using as a broadcast using the variable
 * it was passed via intent when it is launched
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class LocationService extends Service implements
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static String TAG = LocationService.class.getName();
    private final String FILTER_LOCATION = "com.unimelb.comp30022.ITProject.sendintent.LatLngFromLocationService";
    private final String KEY_LOCATION_DATA = "location";
    private final String KEY_GAMESESSIONID_DATA = "gameSessionId";
    private final int FASTEST_LOCATION_UPDATE_INTERVAL = 500;//ms
    private final int UPDATE_INTERVAL = 1000;
    private final Integer LATENCY = 500;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private Gson gson = new Gson();
    private Type locationtype = new TypeToken<Location>() {
    }.getType();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "location service started");
        setLocationUpdateSettings();
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connected to Google services");
        if (checkCurrentPermissions(getApplicationContext())) {
            PendingResult<Status> pending =
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } else {
            Log.d(TAG, "LocationPermissions not granted ");

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection Failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Intent intent = new Intent(FILTER_LOCATION);
        intent.putExtra(KEY_LOCATION_DATA, gson.toJson(location, locationtype));
        sendBroadcast(intent);
        Log.d(TAG, "location updated" + location.toString());
    }

    //verify that the location and connectivity are enabled
    private boolean checkCurrentPermissions(Context context) {
        return (PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private void setLocationUpdateSettings() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(FASTEST_LOCATION_UPDATE_INTERVAL);
    }


}
