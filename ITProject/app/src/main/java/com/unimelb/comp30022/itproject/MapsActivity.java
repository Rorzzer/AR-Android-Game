package com.unimelb.comp30022.itproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
        /*LocationListener*/ {

    private static final String GEO_FIRE_DB = "https://itproject-43222.firebaseio.com/";
    private static final String GEO_FIRE_REF = GEO_FIRE_DB + "/GeoFireData";
    private static final int QUERY_DISTANCE = 1;


    private GoogleMap mMap;
    private Map<String, Marker> markers;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    FusedLocationProviderClient mFusedLocationClient;
    GeoLocation QueryCenter = new GeoLocation(-37.7988847, 144.964109);



    //GeoFire database connection
    DatabaseReference GeoRef = FirebaseDatabase.getInstance().getReferenceFromUrl(GEO_FIRE_REF);
    GeoFire geoFire = new GeoFire(GeoRef);

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    //Upon map activity creation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {    //compare SDK requirements
            checkLocationPermission();                                      //Then check if location permission has been granted
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //hashmap for key location pairs
        markers = new HashMap<String, Marker>();

        //GeoQuery of nearby games
        GeoQuery geoQuery = geoFire.queryAtLocation(QueryCenter, QUERY_DISTANCE);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)));
                markers.put(key, marker);
            }

            @Override
            public void onKeyExited(String key) {
                Marker marker = markers.get(key);
                marker.remove();
                markers.remove(key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

                Marker marker = markers.get(key);
                if(key == FirebaseAuth.getInstance().getCurrentUser().getUid()){
                    marker.remove();
                }

                if (marker != null && key != FirebaseAuth.getInstance().getCurrentUser().getUid()) {
                    marker.setPosition(new LatLng(location.latitude, location.longitude));
                }

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //Creation of map and map type
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialisation of google play services, required for user location
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //Checks OS build against manifest
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {              //Checks manifest permissions
                buildGoogleApiClient();                                  //build APi client, method below
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    //Builder method for google api client
    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    //Upon connection to location services
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);             //Use to set required interval for requests
        mLocationRequest.setFastestInterval(1000);      //Same as above, change depending on timer?
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    //Requesting location permissions
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }

    //on pause method, removes user from geofirebase
    @Override
    public void onPause() {
        super.onPause();
       // geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    //OnDestroy//
    @Override
    public void onDestroy(){
        super.onDestroy();
      //  geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    LocationCallback mLocationCallback = new LocationCallback() {    //Call back loop
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                //Setting users location in geofire database, using UserData as key
               // geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()));
                mLastLocation = location;

                QueryCenter = new GeoLocation(location.getLatitude(), location.getLongitude());

                //move map camera
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
               // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));


            }
        }
    };


}