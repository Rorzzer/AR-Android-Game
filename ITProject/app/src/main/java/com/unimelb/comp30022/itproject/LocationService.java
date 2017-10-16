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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class LocationService extends Service implements
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, SensorEventListener {
    private static String TAG = LocationService.class.getName();
    private final String FILTER_LOCATION = "com.unimelb.comp30022.ITProject.sendintent.LatLngFromLocationService";
    private final String KEY_LOCATION_DATA = "location";
    private final String KEY_GAMESESSIONID_DATA = "gameSessionId";
    private final String KEY_AZIMUTH_DATA = "azimuth";
    private final int FASTEST_LOCATION_UPDATE_INTERVAL = 500;//ms
    private final int UPDATE_INTERVAL = 1000;
    private final Integer LATENCY = 500;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor magnetometerSensor;
    private float[] magnetometerReadings = new float[4];
    private float[] accelerometerReadings = new float[4];
    private boolean magnetometerReceived = false;
    private boolean accelerometerReceived = false;
    private float cOrientation = 0f;
    private Float currentBearing;
    private Float prevBearing;
    private float[] orientationArray = new float[4];
    private float[] rotationMatrix = new float[9];
    private float radAzimuth;
    private float degAzimuth;
    private Bundle locationAndAzimuthOutputs = new Bundle();
    private FusedLocationProviderApi fusedLocationProviderClient = LocationServices.FusedLocationApi;
    private Gson gson = new Gson();
    private Type locationtype = new TypeToken<Location>() {
    }.getType();


    @Override
    public void onCreate() {
        super.onCreate();
        setLocationUpdateSettings();
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "location service started");
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
        sensorManager.unregisterListener(this, magnetometerSensor);
        sensorManager.unregisterListener(this, accelerometerSensor);

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
        locationAndAzimuthOutputs.putString(KEY_LOCATION_DATA, gson.toJson(location, locationtype));
        intent.putExtras(locationAndAzimuthOutputs);
        sendBroadcast(intent);
        Log.d(TAG, "location updated" + location.toString());
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == accelerometerSensor) {
            accelerometerReceived = true;
            System.arraycopy(sensorEvent.values, 0, accelerometerReadings, 0, sensorEvent.values.length);
        }
        if (sensorEvent.sensor == magnetometerSensor) {
            magnetometerReceived = true;
            System.arraycopy(sensorEvent.values, 0, magnetometerReadings, 0, sensorEvent.values.length);
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
                //new direction turned
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

    private void setLocationUpdateSettings() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(FASTEST_LOCATION_UPDATE_INTERVAL);
    }


}
