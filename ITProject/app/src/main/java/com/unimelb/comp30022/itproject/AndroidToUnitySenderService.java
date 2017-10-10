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
import com.google.gson.GsonBuilder;
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
    private final String KEY_LOCATION_DATA = "location";
    private final String KEY_GAMESESSIONID_DATA = "gameSessionId";
    private final String KEY_GAMESESSION_DATA = "gameSession";
    private final double MIN_CAPTURE_DIST = 3;
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
    private Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    private Type gameSessionType = new TypeToken<GameSession>() {
    }.getType();
    private Type locationType = new TypeToken<Location>() {
    }.getType();
    private Type gameStateType = new TypeToken<GameState>() {
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
                GameState snapshot = new GameState(myGameSession.getAllPlayerInformation());
                Log.d(LOG_TAG, gson.toJson(myGameSession.getAllPlayerInformation()));
                senderIntent.setAction(FILTER_GAME_SESSION_ITU).putExtra(Intent.EXTRA_TEXT,
                        gson.toJson(snapshot, gameStateType));
                sendBroadcast(senderIntent);            //send the current game state to the AR fiewfinder
                //Log.d(LOG_TAG,"Passing to unity"+ gson.toJson( gson.toJson(snapshot, gameStateType)));
            }

            // ==========>captureUpdate(); // can capture 2 players per second
            handler.removeCallbacks(this);
            handler.postDelayed(this, LATENCY);
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
        if (ServiceTools.isServiceRunning(getApplicationContext(), LocationService.class)) {
            Intent intent = new Intent(AndroidToUnitySenderService.this, LocationService.class);
            stopService(intent);
        }
    }

    private void recieveLocationBroadcasts() {
        if (currentLocationReciever == null) {
            currentLocationReciever = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String input = intent.getStringExtra(KEY_LOCATION_DATA);
                    Location recentLocation = gson.fromJson(input, locationType);
                    LatLng absLocation = new LatLng(recentLocation.getLatitude(), recentLocation.getLongitude());
                    currentLocation = absLocation;
                    Log.d(LOG_TAG, "Reciever caught location");
                    if (!recentLocation.equals(currentLocation)) {
                        Log.d(LOG_TAG, "reciever location new");
                        //update player location on server for shared model
                        publicGameSession.updatePlayerLocation(new Player(currentUserInfo.getEmail()), absLocation);
                        publicGameSession.setBearing(recentLocation.getBearing());
                        updateServerGameSession(publicGameSession);
                        //update session for player's game model
                        myGameSession = gson.fromJson(gson.toJson(publicGameSession, gameSessionType), gameSessionType);
                        myGameSession.updateRelativeLocations(absLocation);
                        //pass new game information to the activity that called it(running game)
                        Intent i = new Intent(FILTER_GAME_SESSION_ATR);
                        i.putExtra(KEY_GAMESESSION_DATA, gson.toJson(myGameSession, gameSessionType));
                        sendBroadcast(i);
                        Log.d(LOG_TAG, "Broadcasted updated Game state to Running Game");
                    }
                    Log.d(LOG_TAG, "Discarded reciever location");


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
                    Log.d(LOG_TAG, " Successfully Fetched Game Session");
                    Log.d(LOG_TAG, gson.toJson(publicGameSession, gameSessionType));

                } else {
                    //return null value
                    Log.d(LOG_TAG, "Game Session does not exist");
                }
                if (gameInitializing) {
                    publicGameSession.setGameStarted(true);
                    updateServerGameSession(publicGameSession);
                    Log.d(LOG_TAG, " Game started is true");//remove
                    recieveLocationBroadcasts();
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
                } else {
                    //create new value
                    Log.d(LOG_TAG, " Successfully Updated Game Session");
                }
                if (gameInitializing) {
                    Log.d(LOG_TAG, " Listening to future updates");
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
                myGameSession = gson.fromJson(gson.toJson(publicGameSession, gameSessionType), gameSessionType);
                if (currentLocation != null) {
                    myGameSession.updateRelativeLocations(currentLocation);
                    Intent intent = new Intent(FILTER_GAME_SESSION_ATR);
                    intent.putExtra(KEY_GAMESESSION_DATA, gson.toJson(myGameSession, gameSessionType));
                    sendBroadcast(intent);
                }
                Log.d(LOG_TAG, "recieved an update from the server");

                // ------>captureUpdate();
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
        double minPlayerDistance = Double.MAX_VALUE;
        double dist;

        if (currentPlayer != null & currentPlayer.getCapturing()) {
            potentialCaptures = new ArrayList<Player>(publicGameSession.getPlayersWithinDistance(currentPlayer, captureDistance));
            finalCaptureList = new ArrayList<Player>(potentialCaptures);
            Player closestPlayer = null;
            for (Player player : potentialCaptures) {
                //determine who i can capture -> not capturing from both teams,
                if (!player.getCapturing()) {
                    finalCaptureList.remove(player);
                }
                dist = GameSession.distanceBetweenTwoPlayers(currentPlayer, player);
                if (dist < minPlayerDistance) {
                    minPlayerDistance = dist;
                    closestPlayer = finalCaptureList.get(finalCaptureList.indexOf(player));
                }
                if (dist > captureDistance) {
                    finalCaptureList.remove(player);
                }
            }

            /**
             if(capturingButtonPressed == "true"){
             //get closest player and capture them
             if(closestPlayer != null){
             publicGameSession.capturePlayer(currentPlayer,closestPlayer);
             updateServerGameSession(publicGameSession);
             }

            }

             **/
        }

        //set their is being captured to true
        //every iteration,(as determined by the capture handler
        //reduce their health points. if the health points are zero, set them as captured
        checkCapturedNumberChange(); // runs every 200 ms, can capture 5 people a second
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
        }
    }

    public void displayCapturedMessge(String capturedName) {
        Toast.makeText(AndroidToUnitySenderService.this, "Player " + capturedName, Toast.LENGTH_SHORT).show();
    }

    public String getCapturedPlayer() {
        return capturedList.get(capturedList.size() - 1);
    }






}
