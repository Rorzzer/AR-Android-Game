package com.unimelb.comp30022.itproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.unimelb.comp30022.itproject.arcamera.UnityPlayerActivity;

import java.lang.reflect.Type;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RunningGameActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    public static int PERMISSION_CODE = 99;
    private static String TAG = RunningGameActivity.class.getName();
    //intent filter information for communicating between activity and service
    private final String FILTER_GAME_SESSIONID_RTA = "com.unimelb.comp30022.ITProject.sendintent.GameSessionIdToAndroidToUnitySender";
    private final String FILTER_LOCATION = "com.unimelb.comp30022.ITProject.sendintent.LatLngFromLocationService";
    private final String FILTER_GAME_SESSION_ATR = "com.unimelb.comp30022.ITProject.sendintent.GameSessionToRunningGameActivity";
    private final String FILTER_CAPTURING_SIGNAL = "com.unimelb.comp30022.ITProject.sendintent.CapturingSignal";
    private final String KEY_LOCATION_DATA = "location";
    private final String KEY_GAMESESSIONID_DATA = "gameSessionId";
    private final String KEY_IS_CAPTURING = "capturing";
    private final String KEY_GAMESESSION_DATA = "gameSession";
    private final int FASTEST_LOCATION_UPDATE_INTERVAL = 500;//ms
    private final int UPDATE_INTERVAL = 1000;
    private final int CAPTURING_LATENCY = 2000;

    private final Integer LATENCY = 500;
    private final Handler mHideHandler = new Handler();
    private final Handler handler = new Handler();
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    TextView textView;
    private BroadcastReceiver currentGameStateReciever;
    private GameSession currentGameState;
    private boolean hasFineLocationPermission;
    private Boolean canFetchLocations;
    private boolean caputringBtnPressed;
    private String gameSessionId;
    private Gson gson = new Gson();
    private Type gameSessionType = new TypeToken<GameSession>() {
    }.getType();
    private Runnable capturingButtonListener = new Runnable() {
        public void run() {
            Intent senderIntent = new Intent();
            //while the individual does not press "capture" the broadcaster does not send out a capture signal
            if (caputringBtnPressed) {
                Log.d(TAG, "capturing button being pressed");
                Intent intent = new Intent(FILTER_CAPTURING_SIGNAL);
                intent.putExtra(KEY_IS_CAPTURING, "true");
                sendBroadcast(intent);
            } else {
                Intent intent = new Intent(FILTER_CAPTURING_SIGNAL);
                intent.putExtra(KEY_IS_CAPTURING, "false");
                sendBroadcast(intent);
            }
            handler.removeCallbacks(this);
            handler.postDelayed(this, CAPTURING_LATENCY);
        }
    };
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_running_game);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //run the request location information to launch the game session
        if (getIntent().hasExtra(KEY_GAMESESSIONID_DATA)) {
            gameSessionId = getIntent().getStringExtra(KEY_GAMESESSIONID_DATA);
        } else {
            Log.d(TAG, "GameSession id was not recived by the Activity");
            finish();
        }

        //Verify that location settings are enabled before start game
        if (!hasGooglePlay()) {
            Toast.makeText(RunningGameActivity.this, R.string.google_play_unavailable, Toast.LENGTH_SHORT).show();
            finish();
        } else {

            if (hasFineLocationPermission) {
                Log.d(TAG, "Has Google play installed ");
                ServiceTools serviceTools = new ServiceTools();
                boolean isServiceRunning;
                textView = findViewById(R.id.fullscreen_content);
                Intent intent = new Intent(RunningGameActivity.this, AndroidToUnitySenderService.class);
                intent.putExtra(FILTER_GAME_SESSIONID_RTA, gameSessionId);
                startService(intent);
                isServiceRunning = ServiceTools.isServiceRunning(RunningGameActivity.this, AndroidToUnitySenderService.class);
                caputringBtnPressed = true;
                handler.removeCallbacks(capturingButtonListener);
                handler.postDelayed(capturingButtonListener, CAPTURING_LATENCY);
                receiveMyGameSessionBroadcasts();
                launchCurrentGame();
       //       
            } else {
                shouldRequestPermissions();
            }

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentGameStateReciever != null) {
            unregisterReceiver(currentGameStateReciever);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }
    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    //verify that the location and connectivity are enabled
    private boolean getCurrentPermissions() {
        hasFineLocationPermission = (PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION));
        return hasFineLocationPermission;
    }

    public void launchCurrentGame() {
        ServiceTools serviceTools = new ServiceTools();
        boolean isServiceRunning;
        textView = findViewById(R.id.fullscreen_content);
        Intent intent = new Intent(RunningGameActivity.this, AndroidToUnitySenderService.class);
        intent.putExtra(FILTER_GAME_SESSIONID_RTA, gameSessionId);
        startService(intent);
        isServiceRunning = ServiceTools.isServiceRunning(RunningGameActivity.this, AndroidToUnitySenderService.class);
        Intent ar = new Intent(RunningGameActivity.this, UnityPlayerActivity.class);
        startActivity(ar);
    }


    //whether the applications should request for permisssions
    public boolean shouldRequestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            //Post snackbar to explain permission request
            //request for permissions
            Log.d(TAG, "should show rationale for request");

            canFetchLocations = false;
            locationRequestRationaleSnackbar("Location permission is needed for functionality",
                    "Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(RunningGameActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    PERMISSION_CODE);
                        }
                    });
            return true;
        } else {
            //
            Log.d(TAG, "shouldn't show rationale  ");

            ActivityCompat.requestPermissions(RunningGameActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_CODE);

        }

        canFetchLocations = true;
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length <= 0) {
                //failed permissions request
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                canFetchLocations = true;
            } else {
                //notify why permissions are being requested
                locationRequestRationaleSnackbar("Location permission is needed for functionality",
                        "Ok", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Request permission
                                ActivityCompat.requestPermissions(RunningGameActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSION_CODE);
                            }
                        });
            }

        }
    }
    private boolean hasGooglePlay() {
        int availability = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (availability == ConnectionResult.SUCCESS) {
            return true;
        } else {
            GooglePlayServicesUtil.showErrorDialogFragment(availability, this, 0);
            return false;
        }

    }





    private void locationRequestRationaleSnackbar(String mainText, String actionText, View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                mainText,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(actionText, listener).show();
    }

    private void receiveMyGameSessionBroadcasts() {
        if (currentGameStateReciever == null) {
            currentGameStateReciever = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String input = intent.getStringExtra(FILTER_GAME_SESSION_ATR);
                    if (input != null) {
                        currentGameState = gson.fromJson(input, gameSessionType);
                        Log.d(TAG, input);
                    }

                }
            };
        }
        registerReceiver(currentGameStateReciever, new IntentFilter(KEY_GAMESESSION_DATA));
    }



}
