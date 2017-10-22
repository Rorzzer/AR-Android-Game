package com.unimelb.comp30022.itproject;

/**
 * Created by Kiptenai on 10/10/2017.
 * Class that handles the fetching of gps coordinates and bearing
 * passes the informatin out using as a broadcast using the variable
 * it was passed via intent when it is launched
 *
 * Edited by Connor Mclean, replaced deprecated fusedlocationapi as of 3/10, with more
 * modern FusedLocationProviderClientAPI. Also implemented Callback method as required.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class LocationService extends Service implements
         GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SensorEventListener {
    private static String TAG = LocationService.class.getName();
    private final String FILTER_LOCATION = "com.unimelb.comp30022.ITProject.sendintent.LatLngFromLocationService";
    private final String KEY_LOCATION_DATA = "location";
    private final String KEY_GAMESESSIONID_DATA = "gameSessionId";
    private final String KEY_AZIMUTH_DATA = "azimuth";
    private final int FASTEST_LOCATION_UPDATE_INTERVAL = 500;//ms
    private final int UPDATE_INTERVAL = 1000;
    private final Integer LATENCY = 500;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor magnetometerSensor;
    private float[] magnetometerReadings = new float[4];
    private float[] accelerometerReadings = new float[4];
    private boolean magnetometerReceived = false;
    private boolean accelerometerReceived = false;
    private Float currentBearing;
    private Float prevBearing;
    private float[] orientationArray = new float[4];
    private float[] rotationMatrix = new float[9];
    private float radAzimuth =0;
    private float degAzimuth =0;
    private Bundle locationAndAzimuthOutputs = new Bundle();
    private GoogleApiClient mgoogleApiClient;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private Gson gson = new Gson();
    private Type locationtype = new TypeToken<Location>() {
    }.getType();

    //Location callback function
    private LocationCallback mLocationCallback = new LocationCallback() {    //Call back loop
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                //new location received. Broadcast to listening activity
                Intent intent = new Intent(FILTER_LOCATION);
                locationAndAzimuthOutputs.putString(KEY_LOCATION_DATA, gson.toJson(location, locationtype));
                intent.putExtras(locationAndAzimuthOutputs);
                sendBroadcast(intent);
                Log.d(TAG, "location updated" + location.toString());

            }
        }
    };

    //Builds google API client and initiates accelerometer and magnetometers for orientation readings
    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
        //if sensor initialization did not fail
        if(magnetometerSensor != null && accelerometerSensor != null){
            sensorManager.unregisterListener(this, magnetometerSensor);
            sensorManager.unregisterListener(this, accelerometerSensor);
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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //first magnetometer reading
        if (sensorEvent.sensor == magnetometerSensor) {
            magnetometerReceived = true;
            System.arraycopy(sensorEvent.values, 0, magnetometerReadings, 0, sensorEvent.values.length);
        }
        //accelerometer currently reading
        if (sensorEvent.sensor == accelerometerSensor) {
            accelerometerReceived = true;
            System.arraycopy(sensorEvent.values, 0, accelerometerReadings, 0, sensorEvent.values.length);
        }
        //calculate rotation and reorientation and push to the calling activity
        if (magnetometerReceived && accelerometerReceived) {
            SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReadings, magnetometerReadings);
            SensorManager.getOrientation(rotationMatrix, orientationArray);
            //latest orientation
            radAzimuth = orientationArray[0];
            degAzimuth = (float) (Math.toDegrees(radAzimuth) + 360) % 360;
            currentBearing = (float) (((int) (degAzimuth) / 10) * 10);
            if (prevBearing == null || !prevBearing.equals(currentBearing)) {
                //new direction turned broadcast to the listening activiy
                prevBearing = currentBearing;
                Intent intent = new Intent(FILTER_LOCATION);
                locationAndAzimuthOutputs.putString(KEY_AZIMUTH_DATA, String.valueOf(degAzimuth));
                intent.putExtras(locationAndAzimuthOutputs);
                sendBroadcast(intent);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //verify that the location and connectivity are enabled
    private boolean checkCurrentPermissions(Context context) {
        return (PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION));
    }
    //settings for ratea of location update
    private void setLocationUpdateSettings() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(FASTEST_LOCATION_UPDATE_INTERVAL);
    }
}
