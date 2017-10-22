package com.unimelb.comp30022.itproject;

import android.*;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.fragment.BuildConfig;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.maps.MapsInitializer;
import com.google.firebase.auth.FirebaseAuth;
import com.unimelb.comp30022.itproject.arcamera.UnityPlayerActivity;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

import static android.app.PendingIntent.getActivity;

public class MainActivity extends AppCompatActivity
    implements View.OnClickListener, MapsFragment.OnFragmentInteractionListener {
    public static final String TAG = MainActivity.class.getName();
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final String PUBLIC_CHAT = "PUBLIC_CHAT";

    private Boolean chatOpen = false;
    private Fragment mFrag;
    TextView statusText;
    Button btnCreateLobby ;
    Button btnFindLobby;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser user ;

    /**
     * Standard Activity lifecycle methods
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        // Set up click handlers and view item references
        findViewById(R.id.btnSignInReg).setOnClickListener(this);
        findViewById(R.id.btnUser).setOnClickListener(this);
        findViewById(R.id.btnCreateOrUpdateLobby).setOnClickListener(this);
        findViewById(R.id.btnFindLobby).setOnClickListener(this);
        btnCreateLobby = (Button)findViewById(R.id.btnCreateOrUpdateLobby);
        btnFindLobby = (Button)findViewById(R.id.btnFindLobby);
        statusText = (TextView)findViewById(R.id.tvStatus);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "Signed in: " + user.getUid());
                }
                else{
                    Log.d(TAG, "Currently Signed Out");
                }
            }
        };

        //Launch Location services
    }
    /**
     * When the Activity starts and stops, the app needs to connect and
     * disconnect the AuthListener
     */
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    /**
     * When the Activity resumes, the application should check authentication
     *
     */
    @Override
    public void onResume(){
        super.onResume();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            showGameMenus();
            statusText.setText( user.getEmail());
            checkLocationPermission();
            Intent LocationService = new Intent(MainActivity.this, LocationService.class);
            startService(LocationService);
        }
        else{
            hideGameMenus();
            Intent signIn = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(signIn);
        }




    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignInReg:
                Intent signIn = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(signIn);
                break;

            case R.id.btnUser:
                if (FirebaseAuth.getInstance().getCurrentUser()!= null){
                    Intent userProf = new Intent(getApplicationContext(), UserProfActivity.class);
                    startActivity(userProf);
                }else{
                    updateStatus("You must be signed in to access this feature.");
                }
                break;
            case R.id.btnCreateOrUpdateLobby:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    Intent createLobby = new Intent(getApplicationContext(), CreateLobbyActivity.class);
                    startActivity(createLobby);
                } else {
                    updateStatus("You must be signed in to access this feature.");
                }
                break;
            case R.id.btnFindLobby:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    Intent findLobby = new Intent(getApplicationContext(), FindLobbyActivity.class);
                    startActivity(findLobby);
                } else {
                    updateStatus("You must be signed in to access this feature.");
                }
                break;
        }
    }

    private void updateStatus(String stat) {
        TextView tvStat = (TextView) findViewById(R.id.tvStatus);
        tvStat.setText(stat);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    //Requesting location permissions
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    public void showGameMenus(){
        btnCreateLobby.setVisibility(View.VISIBLE);
        btnFindLobby.setVisibility(View.VISIBLE);
        btnCreateLobby.setVisibility(View.VISIBLE);
        btnFindLobby.setVisibility(View.VISIBLE);
        findViewById(R.id.gameButtonsTitle).setVisibility(View.VISIBLE);
    }
    public void hideGameMenus(){
        findViewById(R.id.gameButtonsTitle).setVisibility(View.GONE);
        btnCreateLobby.setVisibility(View.GONE);
        btnFindLobby.setVisibility(View.GONE);
    }
}

