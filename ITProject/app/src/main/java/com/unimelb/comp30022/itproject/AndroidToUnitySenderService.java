package com.unimelb.comp30022.itproject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
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
    private final String FILTER_GAME_SESSION_ITU = "com.unimelb.comp30022.ITProject.sendintent.IntentToUnity";
    private final String FILTER_GAME_SESSIONID_RTA = "com.unimelb.comp30022.ITProject.sendintent.GameSessionIdToAndroidToUnitySender";
    private final String FILTER_LOCATION = "com.unimelb.comp30022.ITProject.sendintent.LatLngFromLocationService";
    private final String FILTER_GAME_SESSION_ATR = "com.unimelb.comp30022.ITProject.sendintent.GameSessionToRunningGameActivity";
    private final String FILTER_CAPTURING_SIGNAL = "com.unimelb.comp30022.ITProject.sendintent.CapturingSignal";
    private final String KEY_LOCATION_DATA = "location";
    private final String KEY_GAMESESSIONID_DATA = "gameSessionId";
    private final String KEY_GAMESESSION_DATA = "gameSession";
    private final String KEY_IS_CAPTURING = "capturing";
    private final double MIN_CAPTURE_DIST = 4.0;
    private final int FASTEST_LOCATION_UPDATE_INTERVAL = 500;//ms
    private final int UPDATE_INTERVAL = 1000;
    private final Integer LATENCY = 500;
    private final Handler handler = new Handler();
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
    private User currentUserInfo;
    private String userId;
    private Player currentPlayer;
    private double captureDistance = MIN_CAPTURE_DIST;
    private ArrayList<Player> targetList = new ArrayList<Player>();
    private ArrayList<String> capturedList = new ArrayList<String>();
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
            if (currentLocation != null && myGameSession != null && currentPlayer != null) {
                for (Player player : myGameSession.getAllPlayerInformation()) {
                    //Log.d(LOG_TAG,"distances are "+ GameSession.distanceBetweenTwoPlayers(currentPlayer,player));
                }
            }
            if (currentLocation != null && myGameSession != null) {
                myGameSession.updateRelativeLocations(currentLocation);
                int bearing = 0;
                Log.d(LOG_TAG, "to unity" + gson.toJson(myGameSession));
                senderIntent.setAction(FILTER_GAME_SESSION_ITU).putExtra(Intent.EXTRA_TEXT,
                        gson.toJson(myGameSession));
                sendBroadcast(senderIntent);            //send the current game state to the AR fiewfinder
            }
            if (gameRunning) {
                handler.removeCallbacks(this);
                handler.postDelayed(this, LATENCY);
            }

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
            //initiate the game session
            //Authorize the current user, fetch the game sessionInformation of
            //the information added to the intent on creation, set the game as started and
            //add a listener to the database for any updates in the game sesssion
            //starts a broadcastListener that recieves location updates and changes the currenc location.
            setCurrentUserInfo();//->asynchronously gets the gameServerSessionObj--> starts a databaseListener for additional changes
            //runs the update every while
            handler.removeCallbacks(sendData);
            handler.postDelayed(sendData, LATENCY);

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
        unregisterReceiver(captureButtonReciever);
        unregisterReceiver(currentLocationReciever);
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
                    String input = intent.getStringExtra(KEY_LOCATION_DATA);
                    Log.d(LOG_TAG, "new loction is " + input);
                    Location recentLocation = gson.fromJson(input, locationType);
                    if (recentLocation != null) {
                        LatLng absLocation = new LatLng(recentLocation.getLatitude(), recentLocation.getLongitude(), recentLocation.getAccuracy());
                        currentLocation = absLocation;
                        if (!recentLocation.equals(currentLocation)) {
                            //update player location on server for shared model
                            publicGameSession.updatePlayerLocation(new Player(currentUserInfo.getEmail()), absLocation);
                            updateServerGameSession(publicGameSession);
                            //update session for player's game model
                            myGameSession = gson.fromJson(gson.toJson(publicGameSession), gameSessionType);
                            myGameSession.setBearing(recentLocation.getBearing());
                            myGameSession.updateRelativeLocations(absLocation);
                            //pass new game information to the activity that called it(running game)
                            Intent i = new Intent(FILTER_GAME_SESSION_ATR);
                            i.putExtra(KEY_GAMESESSION_DATA, gson.toJson(myGameSession));
                            sendBroadcast(i);
                        }
                    }
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
        GameSession fetchedGameSession = null;
        Query gameSessionIdQuery = gameSessionDbReference.orderByChild("sessionId").equalTo(gameSessionId);
        gameSessionIdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    //assign fetched value
                    publicGameSession = dataSnapshot.child(gameSessionId).getValue(GameSession.class);
                    currentPlayer = publicGameSession.getPlayerDetails(currentUserInfo.getEmail());
                } else {
                    //return null value
                    Log.d(LOG_TAG, "Game Session does not exist");
                }
                if (gameInitializing) {
                    publicGameSession.setGameStarted(true);
                    myGameSession = gson.fromJson(gson.toJson(publicGameSession), gameSessionType);
                    updateServerGameSession(publicGameSession);
                    Log.d(LOG_TAG, " Game started and will begin receiving location updates");//remove
                    receiveLocationBroadcasts();
                    receiveCaptureButtonSignal();
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
    private void updateServerGameSession(final GameSession gameSession) {
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
                    gameSessionDbReference.child(gameSessionId).setValue(gameSession);
                    Log.d(LOG_TAG, " Successfully Updated Game Session");
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
                    Log.d(LOG_TAG, " Recieved update from server");
                    getCapturedIndividualsFromServer(myGameSession, publicGameSession);
                    myGameSession = gson.fromJson(gson.toJson(publicGameSession), gameSessionType);
                    myGameSession.updateRelativeLocations(currentLocation);
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
        ArrayList<Player> potentialCaptures, finalCaptureList;
        //if i'm a capturer
        double minPlayerDistance = captureDistance;
        double dist;
        //Log.d(LOG_TAG, "Running Capture");
        if (currentPlayer != null & currentPlayer.getCapturing() == true) {
            potentialCaptures = myGameSession.getPlayersWithinDistance(currentPlayer, captureDistance);
            finalCaptureList = gson.fromJson(gson.toJson(potentialCaptures), playerArrayListType);

            Player closestPlayer = null;
            if (finalCaptureList.size() == 0) {
                return;
            }
            for (Player player : potentialCaptures) {
                //determine who i can capture -> not capturing from both teams,
                if (player.getCapturing() == true) {
                    finalCaptureList.remove(player);
                } else {
                    //players that aren't capturing
                    if (currentPlayer.getAbsLocation() != null && player.getAbsLocation() != null) {
                        dist = GameSession.distanceBetweenTwoPlayers(currentPlayer, player);
                        if (dist < minPlayerDistance) {
                            minPlayerDistance = dist;
                            closestPlayer = finalCaptureList.get(finalCaptureList.indexOf(player));
                        }
                    }
                }
            }
            //catch the closest player
            if (closestPlayer != null && finalCaptureList.size() > 0) {
                if (!currentPlayer.getCapturedList().contains(closestPlayer.getDisplayName())) {
                    myGameSession.capturePlayer(currentPlayer, closestPlayer);
                    publicGameSession.capturePlayer(currentPlayer, closestPlayer);
                    Log.d(LOG_TAG, "player " + currentPlayer.getDisplayName() + " has captured " + closestPlayer.getDisplayName());
                    for (String i : currentPlayer.getCapturedList()) {
                        Log.d(LOG_TAG, currentPlayer.getDisplayName() + "has in his list " + i);
                    }
                    String capturestate = closestPlayer.getCapturing() ? "capturing" : "notCapturing";
                    Log.d(LOG_TAG, closestPlayer.getDisplayName() + " has his capture status " + capturestate);
                    capturedList.add(closestPlayer.getDisplayName());
                    checkCapturedNumberChange();
                    updateServerGameSession(publicGameSession);
                }

            }
        }
        //runs at a frequency determined by the capture handler
        //every iteration,(as determined by the capture handler
        // runs every 200 ms, can capture 5 people a second
    }

    public void checkCapturedNumberChange() {
        int count = 0;
        for (Player player : publicGameSession.getAllPlayerInformation()) {
            if (player.getHasBeenCaptured()) {
                count++;
            }

        }
        if (count > capturedNumber) {
            String newlyCaptured = getCapturedPlayer();
            displayCapturedMessge(newlyCaptured);
            capturedNumber++;
        }
    }

    public void displayCapturedMessge(String capturedName) {
        Toast.makeText(AndroidToUnitySenderService.this, "Player " + capturedName + "has been Captured", Toast.LENGTH_SHORT).show();
    }

    public String getCapturedPlayer() {
        return capturedList.get(capturedList.size() - 1);
    }

    public void getCapturedIndividualsFromServer(GameSession myGameSession, GameSession publicGameSession) {
        ArrayList<Player> mySession = myGameSession.getAllPlayerInformation();
        ArrayList<Player> publicSession = publicGameSession.getAllPlayerInformation();
        for (Player player : mySession) {
            if (publicSession.get(publicSession.indexOf(player)).getCapturing() != mySession.get(mySession.indexOf(player)).getCapturing()) {
                capturedNumber++;
                capturedList.add(player.getDisplayName());
                displayCapturedMessge(player.getDisplayName());
            }
        }
    }





}
