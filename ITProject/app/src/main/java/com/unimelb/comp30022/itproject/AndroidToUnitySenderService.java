package com.unimelb.comp30022.itproject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kiptenai on 14/09/2017.
 * class for handling messages to Unity Application
 * The class first initiates the game session
 * Authorize the current user, fetch the game sessionInformation of
 * the information added to the intent on creation, set the game as started and
 * add a listener to the database for any updates in the game sesssion
 * updates the current game state and generates a local version of the game
 * states with all relative positions determined
 * passes the data to the unity plugin that draws the posisions
 */

@SuppressWarnings("MissingPermission")
public class AndroidToUnitySenderService extends Service {
    public static String SERVICE_TAG = "Android To Unity Sender";
    //service handler executes periodically
    private final String LOG_TAG = AndroidToUnitySenderService.class.getName();
    private final int ACCURACY_TOLERANCE_FACTOR = 2;
    private final String FILTER_GAME_SESSION_ITU = "com.unimelb.comp30022.ITProject.sendintent.IntentToUnity";
    private final String FILTER_GAME_SESSIONID_RTA = "com.unimelb.comp30022.ITProject.sendintent.GameSessionIdToAndroidToUnitySender";
    private final String FILTER_LOCATION = "com.unimelb.comp30022.ITProject.sendintent.LatLngFromLocationService";
    private final String FILTER_GAME_SESSION_ATR = "com.unimelb.comp30022.ITProject.sendintent.GameSessionToRunningGameActivity";
    private final String FILTER_CAPTURING_SIGNAL = "com.unimelb.comp30022.ITProject.sendintent.CapturingSignal";
    private final String KEY_LOCATION_DATA = "location";
    private final String KEY_AZIMUTH_DATA = "azimuth";
    private final String KEY_GAMESESSIONID_DATA = "gameSessionId";
    private final String KEY_GAMESESSION_DATA = "gameSession";
    private final String KEY_IS_CAPTURING = "capturing";
    private final Integer AR_FREQUENCY = 50;
    private final Integer LATENCY = 500;
    private final int UPDATING_GAME_STARTED = 0;
    private final int UPDATING_LOCATION = 1;
    private final int UPDATING_CAPTURE = 2;
    private final int FOOTSTEP_UPDATE_FREQUENCY = 5000;
    private final Handler handler = new Handler();
    private Bundle locationAndAzimuthInputs = new Bundle();
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference userDbReference;
    private DatabaseReference gameSessionDbReference;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser fbuser;
    private BroadcastReceiver currentLocationReciever;
    private BroadcastReceiver captureButtonReciever;
    private boolean gameInitializing = true;
    private boolean caputringBtnPressed;
    private boolean gameRunning = true;

    private String gameSessionId;
    private GameSession publicGameSession;
    private GameSession myGameSession;
    private LatLng currentLocation;
    private LatLng prevLocation;
    private Location recentLocation;
    private Float recentBearing;
    private Float currentBearing;
    private Float prevBearing;
    private User currentUserInfo;
    private String userId;
    private Player currentPlayer;
    private double captureDistance = GameSession.EASY_CAPTURE_DISTANCE;
    private int numberOfFootsteps = GameSession.NUMBER_OF_FOOTSTEPS;
    private ArrayList<Player> targetList = new ArrayList<Player>();
    private ArrayList<String> capturedList = new ArrayList<String>();
    private ArrayList<String> myCapturedList = new ArrayList<String>();
    private int capturedNumber = 0;
    private Gson gson = new Gson();
    private JsonParser parser = new JsonParser();
    private Type gameSessionType = new TypeToken<GameSession>() {
    }.getType();
    private Type playerArrayListType = new TypeToken<ArrayList<Player>>() {
    }.getType();
    private Type locationType = new TypeToken<Location>() {
    }.getType();
    private Type playerType = new TypeToken<Player>() {
    }.getType();

    //thread that handles passing information into the unityActivity that hosts the AR modules
    private Runnable sendData = new Runnable() {
        public void run() {
            Intent senderIntent = new Intent();
            //Intent Flags
            senderIntent.setFlags(Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_NO_ANIMATION |
                    Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            //direct intent with string targeted by reciever and specitfy data format
            //---> post this information and update the database once after the latency is passed
            if (currentLocation != null && myGameSession != null) {
                myGameSession.updatePlayerLocation(currentPlayer, currentLocation);
                myGameSession.updateRelativeLocations(currentLocation);
                senderIntent.setAction(FILTER_GAME_SESSION_ITU).putExtra(Intent.EXTRA_TEXT, gson.toJson(myGameSession));
                Log.d(LOG_TAG, "Sending to unity: "+gson.toJson(myGameSession));
                //Log.d(LOG_TAG, "------------------ ");
                for (Player player : myGameSession.allPlayerArrayLists()) {
                    if (player.getDisplayName() != null && player.getCoordinateLocation() != null && !player.getDisplayName().equals(currentPlayer.getDisplayName())) {
                        Log.d(LOG_TAG, player.getDisplayName() + " distance " + String.valueOf(GameSession.distanceBetweenTwoPlayers(myGameSession.getPlayerDetails(currentPlayer.getDisplayName()), player)));
                    }
                }

                sendBroadcast(senderIntent);            //send the current game state to the AR fiewfinder
            } else {
                Log.d(LOG_TAG, "Not sending information");
            }

            handler.removeCallbacks(this);
            handler.postDelayed(this, AR_FREQUENCY);
        }
    };

    private Runnable updateFootsteps = new Runnable() {
        public void run() {
            if (currentLocation != null && myGameSession != null) {
                myGameSession.updatePlayerLocation(currentPlayer, currentLocation);
                myGameSession.updatePaths(numberOfFootsteps);
                myGameSession.updateRelativeLocations(currentLocation);
                Log.d(LOG_TAG, "Updating footprints");

            } else {
                Log.d(LOG_TAG, "Not updating footprints");
            }


            handler.removeCallbacks(this);
            handler.postDelayed(this, FOOTSTEP_UPDATE_FREQUENCY);
        }
    };

    @Override
    public void onStart(Intent intent, int startid) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            gameSessionId = intent.getStringExtra(FILTER_GAME_SESSIONID_RTA);
            Context context = getApplicationContext();
            FirebaseApp.initializeApp(context);
            firebaseAuth = FirebaseAuth.getInstance();
            firebaseDatabase = FirebaseDatabase.getInstance();
            userDbReference = firebaseDatabase.getReference("users");
            gameSessionDbReference = firebaseDatabase.getReference("gameSessions");
            fbuser = firebaseAuth.getCurrentUser();
            userId = fbuser.getUid();
            //update data from calling activity
            gameSessionId = intent.getStringExtra(FILTER_GAME_SESSIONID_RTA);
            Log.d(LOG_TAG, gameSessionId + " started launching Location Service");
            //start location updates from location Service

            Intent locationService = new Intent(AndroidToUnitySenderService.this, LocationService.class);
            locationService.setFlags(Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_NO_ANIMATION |
                    Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            startService(locationService);
            handler.removeCallbacks(sendData);
            handler.postDelayed(sendData, AR_FREQUENCY);
            handler.removeCallbacks(updateFootsteps);
            handler.postDelayed(sendData, FOOTSTEP_UPDATE_FREQUENCY);
            //initiate the game session
            //Authorize the current user, fetch the game sessionInformation of
            //the information added to the intent on creation, set the game as started and
            //add a listener to the database for any updates in the game sesssion
            //starts a broadcastListener that recieves location updates and changes the currenc location.
            setCurrentUserInfo();//->asynchronously gets the gameServerSessionObj--> starts a databaseListener for additional changes
            //runs the update every while
        } else {
            Log.d(LOG_TAG, "Intent Launched without game session information");
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
        //gameRunning = false;
        if (captureButtonReciever != null && currentLocationReciever != null) {
            unregisterReceiver(captureButtonReciever);
            unregisterReceiver(currentLocationReciever);
        }
        if (ServiceTools.isServiceRunning(getApplicationContext(), LocationService.class)) {
            Intent intent = new Intent(AndroidToUnitySenderService.this, LocationService.class);
            stopService(intent);
        }
    }

    private void receiveCaptureButtonSignal() {
        if (captureButtonReciever == null) {
            captureButtonReciever = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String input = intent.getStringExtra(KEY_IS_CAPTURING);
                    if (input.equals("true")) {
                        if (myGameSession != null) {
                            captureUpdate();
                        }
                    }
                }
            };
        }
        registerReceiver(captureButtonReciever, new IntentFilter(FILTER_CAPTURING_SIGNAL));
    }

    private void receiveLocationBroadcasts() {
        if (currentLocationReciever == null) {
            currentLocationReciever = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    locationAndAzimuthInputs = intent.getExtras();
                    String locationExtra = locationAndAzimuthInputs.getString(KEY_LOCATION_DATA);
                    String bearingExtra = locationAndAzimuthInputs.getString(KEY_AZIMUTH_DATA);
                    if (locationExtra != null) {
                        recentLocation = gson.fromJson(locationExtra, locationType);
                        if (recentLocation != null) {
                            currentLocation = new LatLng(recentLocation.getLatitude(), recentLocation.getLongitude(), recentLocation.getAccuracy());
                            if (prevLocation == null || !prevLocation.equals(currentLocation)) {
                                prevLocation = currentLocation;
                                //update player location on server for shared model
                                myGameSession.updatePlayerLocation(currentPlayer, currentLocation);
                                publicGameSession.updatePlayerLocation(currentPlayer, currentLocation);
                                myGameSession.getPlayerDetails(currentPlayer.getDisplayName()).setLastPing(System.currentTimeMillis());
                                publicGameSession.getPlayerDetails(currentPlayer.getDisplayName()).setLastPing(System.currentTimeMillis());
                                updateServerGameSession(publicGameSession, UPDATING_LOCATION);
                                Log.d(LOG_TAG,"receiving location "+ currentLocation.toString());

                            }
                        }
                    }
                    if (bearingExtra != null) {
                        recentBearing = Float.valueOf(bearingExtra);
                        currentBearing = roundToOneDecimal(recentBearing);
                        if (prevBearing == null || !prevBearing.equals(currentBearing)) {
                            //new direction turned
                            prevBearing = currentBearing;
                            myGameSession.setBearing(currentBearing);
                            Log.d(LOG_TAG,"receiving bearing "+ String.valueOf(currentBearing));
                        }
                    }

                    //update session for player's game model
                    //pass new game information to the activity that called it(running game)
                    Log.d(LOG_TAG,"Broadcasting gamesession to runningGame"+ gson.toJson(myGameSession));
                    Intent i = new Intent(FILTER_GAME_SESSION_ATR);
                    i.putExtra(KEY_GAMESESSION_DATA, gson.toJson(myGameSession));
                    sendBroadcast(i);
                    Log.d(LOG_TAG,"sending information to all activities" + gson.toJson(myGameSession));
                }
            };
        }
        registerReceiver(currentLocationReciever, new IntentFilter(FILTER_LOCATION));
    }
    /***
     * Fetches the user information and calls the GetGameSessionServer info to fech the game information
     *
     */

    public void setCurrentUserInfo() {
        /**
         * get AuthListener to fetch logged in user
         * */
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                fbuser = firebaseAuth.getCurrentUser();
                if (fbuser != null) {
                    Log.d(LOG_TAG, "Retrieved firebaseUser");
                } else {
                    Log.d(LOG_TAG, "failure to retrieve firebaseUser");
                }
            }
        };

        /**
         * Fetch current user details
         * */
        userDbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserInfo = dataSnapshot.child(userId).getValue(User.class);
                    Log.d(LOG_TAG, currentUserInfo.getEmail());
                    getServerGameSessionObj(gameSessionId);
                    Log.d(LOG_TAG, "User Exists. Fetching game session Info");

                } else {
                    Log.d(LOG_TAG, "User does not exist");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "Failed to read User info");
            }
        });

    }

    /**
     * Fetch game sesion object if it already exists on the server
     */
    private void getServerGameSessionObj(final String gameSessionId) {
        Query gameSessionIdQuery = gameSessionDbReference.orderByChild("sessionId").equalTo(gameSessionId);
        gameSessionIdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    //assign fetched value
                    publicGameSession = dataSnapshot.child(gameSessionId).getValue(GameSession.class);
                } else {
                    //return null value
                    Log.d(LOG_TAG, "Game Session does not exist");
                }
                if (gameInitializing) {
                    currentPlayer = publicGameSession.getPlayerDetails(currentUserInfo.getEmail());
                    publicGameSession.setGameStarted(true);
                    publicGameSession.getPlayerDetails(currentPlayer.getDisplayName()).setLastPing(System.currentTimeMillis());
                    myGameSession = gson.fromJson(gson.toJson(publicGameSession), gameSessionType);
                    Log.d(LOG_TAG, "Fetched fetchedServerObject " + gson.toJson(myGameSession));
                    receiveLocationBroadcasts();
                    receiveCaptureButtonSignal();
                    updateServerGameSession(publicGameSession, UPDATING_GAME_STARTED);
                    Log.d(LOG_TAG, " Game started and will begin receiving location updates");//remove

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "Game Session - Read Error");
            }
        });
    }

    /***
     * updates a game session information for a specific value from local device to server
     * */
    private void updateServerGameSession(final GameSession gameSession, final int updateField) {
        if (gameSession == null) {
            return;
        }
        final String key = gameSession.getSessionId();
        Query gameSessionIdQuery = gameSessionDbReference.orderByChild("sessionId").equalTo(key);

        gameSessionIdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    //update values
                    //gameSessionDbReference.child(gameSessionId).setValue(gameSession);
                    Map<String, Object> pingUpdate = new HashMap<String, Object>();
                    pingUpdate.put("lastPing", System.currentTimeMillis());
                    int myTeamId = myGameSession.getTeamIndex(currentPlayer);
                    int myPlayerId = myGameSession.getPlayerIndexInTeam(currentPlayer);
                    //update ping value to recent ping
                    gameSessionDbReference
                            .child(gameSessionId)
                            .child("teamArrayList")
                            .child(String.valueOf(myTeamId))
                            .child("playerArrayList")
                            .child(String.valueOf(myPlayerId))
                            .updateChildren(pingUpdate);
                    if (updateField == UPDATING_GAME_STARTED) {
                        Map<String, Object> startedUpdate = new HashMap<String, Object>();
                        startedUpdate.put("gameStarted", true);
                        gameSessionDbReference.child(gameSessionId).updateChildren(startedUpdate);
                    }
                    if (updateField == UPDATING_LOCATION) {
                        Map<String, Object> locationUpdate = new HashMap<String, Object>();
                        locationUpdate.put("absLocation", currentLocation);
                        gameSessionDbReference
                                .child(gameSessionId)
                                .child("teamArrayList")
                                .child(String.valueOf(myTeamId))
                                .child("playerArrayList")
                                .child(String.valueOf(myPlayerId))
                                .updateChildren(locationUpdate);
                    }
                    if (updateField == UPDATING_CAPTURE) {
                        if (myCapturedList.size() > 0) {
                            Map<String, Object> capturingPlayerUpdate = new HashMap<String, Object>();
                            myCapturedList = myGameSession.getTeamArrayList().get(myTeamId).getPlayerArrayList().get(myPlayerId).getPlayerCapturedList();
                            capturingPlayerUpdate.put("capturedList", myCapturedList);
                            //update capturing
                            gameSessionDbReference
                                    .child(gameSessionId)
                                    .child("teamArrayList")
                                    .child(String.valueOf(myTeamId))
                                    .child("playerArrayList")
                                    .child(String.valueOf(myPlayerId))
                                    .updateChildren(capturingPlayerUpdate);

                            String recentCapture = myCapturedList.get(myCapturedList.size() - 1);
                            int capturedTeamId = myGameSession.getTeamIndex(new Player(recentCapture));
                            int capturedPlayerId = myGameSession.getPlayerIndexInTeam(new Player(recentCapture));
                            Map<String, Object> capturedPlayerUpdate = new HashMap<String, Object>();
                            capturedPlayerUpdate.put("hasBeencaptured", true);
                            capturedPlayerUpdate.put("isCapturing", true);
                            gameSessionDbReference
                                    .child(gameSessionId)
                                    .child("teamArrayList")
                                    .child(String.valueOf(capturedTeamId))
                                    .child("playerArrayList")
                                    .child(String.valueOf(capturedPlayerId))
                                    .updateChildren(capturedPlayerUpdate);
                        }
                    }
                }
                if (gameInitializing) {
                    Log.d(LOG_TAG, " updating for the first time and Listening to future updates");
                    listenToServerForGameSessionChanges();
                    gameInitializing = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "Game Session - Update Error");
                gameInitializing = false;
            }

        });
    }

    public void listenToServerForGameSessionChanges() {
        Query gameSessionIdQuery = gameSessionDbReference.orderByChild("sessionId").equalTo(gameSessionId);
        gameSessionIdQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                publicGameSession = dataSnapshot.getValue(GameSession.class);

                if (currentLocation != null && myGameSession != null) {
                    //Log.d(LOG_TAG, " Recieved update from server");
                    publicGameSession.refreshActivePlayers(System.currentTimeMillis(), 30000);
                    myGameSession = gson.fromJson(gson.toJson(publicGameSession), gameSessionType);
                    myGameSession.updatePlayerLocation(currentPlayer, currentLocation);
                    myGameSession.setBearing(currentBearing);
                    myGameSession.updateRelativeLocations(currentLocation);
                    ArrayList<Player> newCaptures = GameSession.determineIndividualsCapturedFromUpdate(myGameSession, publicGameSession);
                    if (newCaptures != null && newCaptures.size() > 0) {
                        for (Player player : newCaptures) {
                            displayCapturedMessge(player.getDisplayName());
                        }
                    }
                    Intent intent = new Intent(FILTER_GAME_SESSION_ATR);
                    intent.putExtra(KEY_GAMESESSION_DATA, gson.toJson(myGameSession));
                    sendBroadcast(intent);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public void captureUpdate() {
        currentPlayer = myGameSession.getPlayerDetails(currentPlayer.getDisplayName());
        if (currentPlayer != null && currentPlayer.getCapturing() == true) {
            ArrayList<Player> potentialCaptures, finalCaptureList;
            //if i'm a capturer
            double dist;
            potentialCaptures = myGameSession.getPlayersWithinDistance(currentPlayer, captureDistance);
            finalCaptureList = gson.fromJson(gson.toJson(potentialCaptures), playerArrayListType);
            Player closestPlayer = null;
            double closestPlayerDistance = Double.MAX_VALUE;
            if (finalCaptureList.size() == 0) {
                return;
            }
            for (Player player : potentialCaptures) {
                //determine who i can capture -> not capturing from both teams,
                if (player.getCapturing() == true) {
                    finalCaptureList.remove(player);
                } else if (player.getCapturing() == false) {
                    Log.d(LOG_TAG, "Player" + player.getDisplayName() + " is available for capture");
                    //from players that aren't capturing determine the closest one
                    if (currentPlayer.getAbsLocation() != null && player.getAbsLocation() != null) {
                        dist = GameSession.distanceBetweenTwoPlayers(currentPlayer, player);
                        if (dist < captureDistance) {
                            if (dist < closestPlayerDistance) {
                                closestPlayerDistance = dist;
                                closestPlayer = finalCaptureList.get(finalCaptureList.indexOf(player));
                            }
                        }
                    }
                }
            }
            //has found the closest player
            if (closestPlayer != null) {
                publicGameSession.capturePlayer(currentPlayer, closestPlayer);
                updateServerGameSession(publicGameSession, UPDATING_CAPTURE);
                myGameSession = gson.fromJson(gson.toJson(publicGameSession), gameSessionType);
                myCapturedList.add(closestPlayer.getDisplayName());
                Toast.makeText(AndroidToUnitySenderService.this, R.string.player + currentPlayer.getDisplayName() + R.string.has_captured + closestPlayer.getDisplayName(), Toast.LENGTH_LONG).show();
            }
        }
    }
    public void displayCapturedMessge(String capturedName) {
        Toast.makeText(AndroidToUnitySenderService.this, R.string.player + capturedName + R.string.has_been_captured, Toast.LENGTH_SHORT).show();
    }
    public void displayPoorSignalMessage() {
        Toast.makeText(AndroidToUnitySenderService.this, R.string.poor_gps_signal, Toast.LENGTH_SHORT).show();
    }

    public float roundToOneDecimal(float f) {
        return (float) (Math.round(f * 100) / 100);
    }


}
