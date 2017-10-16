package com.unimelb.comp30022.itproject;

/**
 * Created by Kiptenai on 10/10/2017.
 * Class that handles the fetching of gps coordinates and bearing
 * passes the informatin out using as a broadcast using the variable
 * it was passed via intent when it is launched
 *
 * Edited by Connor Mclean, replaced deprecated fusedlocationapi as of 3/10, with more
 * modern FusedLocationProviderClientAPI.
 */

import android.*;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class LocationService extends Service implements
         GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static String TAG = LocationService.class.getName();
    private final String FILTER_LOCATION = "com.unimelb.comp30022.ITProject.sendintent.LatLngFromLocationService";
    private final String KEY_LOCATION_DATA = "location";
    private final String KEY_GAMESESSIONID_DATA = "gameSessionId";
    private final int FASTEST_LOCATION_UPDATE_INTERVAL = 500;//ms
    private final int UPDATE_INTERVAL = 1000;
    private final Integer LATENCY = 500;
    private GoogleApiClient mgoogleApiClient;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private Gson gson = new Gson();
    private Type locationtype = new TypeToken<Location>() {
    }.getType();


    //Builds google API client
    @Override
    public void onCreate() {
        super.onCreate();

        Toast toast = Toast.makeText(getApplicationContext(), "onCreate", Toast.LENGTH_SHORT);
        toast.show();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Log.d(TAG, "Started Location Service");
        setLocationUpdateSettings();
        mgoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast toast = Toast.makeText(getApplicationContext(), "omStartCommand", Toast.LENGTH_SHORT);
        toast.show();
        Log.d(TAG, "location service started");
        if (mgoogleApiClient != null) {
            mgoogleApiClient.connect();
        }

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mgoogleApiClient != null) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mgoogleApiClient.disconnect();
        }
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connected to Google services");
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection Failed");
    }

    /*
    @Override
    public void onLocationChanged(Location location) {
        Intent intent = new Intent(FILTER_LOCATION);
        intent.putExtra(KEY_LOCATION_DATA, gson.toJson(location, locationtype));
        sendBroadcast(intent);
        Log.d(TAG, "location updated" + location.toString());
    }*/

    //verify that the location and connectivity are enabled
    private boolean checkCurrentPermissions(Context context) {
        return (PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private void setLocationUpdateSettings() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(FASTEST_LOCATION_UPDATE_INTERVAL);
    }

    private LocationCallback mLocationCallback = new LocationCallback() {    //Call back loop
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {

                Intent intent = new Intent(FILTER_LOCATION);
                intent.putExtra(KEY_LOCATION_DATA, gson.toJson(location, locationtype));
                sendBroadcast(intent);
                Toast toast = Toast.makeText(getApplicationContext(), "Location change", Toast.LENGTH_SHORT);
                toast.show();
                Log.d(TAG, "location updated" + location.toString());

            }
        }
    };


}
