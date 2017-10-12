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
import java.util.HashMap;

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
    private final String FILTER_LOCATION_RTA = "com.unimelb.comp30022.ITProject.sendintent.LatLngToAndroidToUnitySender";
    private final String FILTER_GAME_SESSION_ATR = "com.unimelb.comp30022.ITProject.sendintent.GameSessionToRunningGameActivity";
    private final String KEY_LOCATION_DATA = "location";
    private final String KEY_GAMESESSIONID_DATA = "gameSessionId";
    private final String KEY_GAMESESSION_DATA = "gameSession";
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
    private boolean gameInitializing = true;
    private String gameSessionId;
    private GameSession publicGameSession;
    private GameSession myGameSession;
    private LatLng currentLocation = new LatLng();
    private User currentUserInfo;
    private String userId;
    private Player currentPlayer;
    private HashMap<String, String> gameSessionHashmap;
    private Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    private Type gameSessionType = new TypeToken<GameSession>() {
    }.getType();
    private Type locationType = new TypeToken<Location>() {
    }.getType();
    private Runnable sendData = new Runnable() {
        public void run() {
            Intent senderIntent = new Intent();
            //Intent Flags
            senderIntent.setFlags(Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_NO_ANIMATION |
                    Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            //direct intent with string targeted by reciever and specitfy data format
            //---> post this information and update the database once after the latency is passed
            senderIntent.setAction(FILTER_GAME_SESSION_ITU).putExtra(Intent.EXTRA_TEXT, gson.toJson(myGameSession, gameSessionType));
            Log.d(LOG_TAG, gson.toJson(publicGameSession, gameSessionType));
            Log.d(LOG_TAG, this.getClass().getSimpleName());
            sendBroadcast(senderIntent);
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
            //gameSessionId = intent.getStringExtra(ServiceTools.PASSING_INFO_LABEL);
            Context context = getApplicationContext();
            FirebaseApp.initializeApp(context);
            firebaseAuth = FirebaseAuth.getInstance();
            firebaseDatabase = FirebaseDatabase.getInstance();
            userDbReference = firebaseDatabase.getReference("users");
            gameSessionDbReference = firebaseDatabase.getReference("gameSessions");
            fbuser = firebaseAuth.getCurrentUser();
            userId = fbuser.getUid();

            //update data from calling activity
            gameSessionHashmap = (HashMap<String, String>) intent.getExtras().get(FILTER_GAME_SESSIONID_RTA);
            gameSessionId = gameSessionHashmap.get(KEY_GAMESESSIONID_DATA);
            Log.d(LOG_TAG, gameSessionId + " started");
            //initiate the game session
            //Authorize the current user, fetch the game sessionInformation of
            //the information added to the intent on creation, set the game as started and
            //add a listener to the database for any updates in the game sesssion

            setCurrentUserInfo();//->asynchronously gets the gameServerSessionObj--> starts a databaseListener for additional changes
            handler.removeCallbacks(sendData);
            handler.postDelayed(sendData, LATENCY);
        } else {
            Log.d(LOG_TAG, "Intent Launched game session information");
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
    }

    private void startBroadCastReciever() {
        if (currentLocationReciever == null) {
            currentLocationReciever = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String input = intent.getStringExtra(KEY_LOCATION_DATA);
                    Location recentLocation = gson.fromJson(input, locationType);
                    LatLng absLocation = new LatLng(recentLocation.getLatitude(), recentLocation.getLongitude());
                    if (!recentLocation.equals(currentLocation)) {
                        //update player location
                        Log.d(LOG_TAG, "recieved new location through the broadcaster: " + recentLocation.toString());
                        publicGameSession.updatePlayerLocation(new Player(currentUserInfo.getEmail()), absLocation);
                        publicGameSession.setBearing(recentLocation.getBearing());
                        myGameSession = gson.fromJson(gson.toJson(publicGameSession, gameSessionType), gameSessionType);
                        myGameSession.updateRelativeLocations(absLocation);
                        updateServerGameSession(publicGameSession);
                        Intent i = new Intent(FILTER_GAME_SESSION_ATR);
                        i.putExtra(KEY_GAMESESSION_DATA, gson.toJson(publicGameSession, gameSessionType));
                        sendBroadcast(i);
                        Log.d(LOG_TAG, "updated player location");
                        Log.d(LOG_TAG, "location: " + absLocation.toString() + " bearing :" + recentLocation.getBearing());
                        Log.d(LOG_TAG, gson.toJson(publicGameSession, gameSessionType));
                    }

                }
            };
        }
        registerReceiver(currentLocationReciever, new IntentFilter(FILTER_LOCATION_RTA));

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
                    getServerGameSessionObj(gameSessionId);
                    Log.d(LOG_TAG, currentUserInfo.getEmail());
                    currentPlayer = new Player(currentUserInfo.getEmail());
                    getServerGameSessionObj(gameSessionId);
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
    private GameSession getServerGameSessionObj(final String gameSessionId) {
        GameSession fetchedGameSession = null;
        Query gameSessionIdQuery = gameSessionDbReference.orderByChild("sessionId").equalTo(gameSessionId);
        gameSessionIdQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    //assign fetched value
                    publicGameSession = dataSnapshot.child(gameSessionId).getValue(GameSession.class);
                    Log.d(LOG_TAG, " Successfully Fetched Game Session");
                } else {
                    //return null value
                    Log.d(LOG_TAG, "Game Session does not exist");
                }
                if (gameInitializing) {
                    publicGameSession.setGameStarted(true);
                    updateServerGameSession(publicGameSession);
                    Log.d(LOG_TAG, "Broadcast reciever started");//remove
                    startBroadCastReciever();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "Game Session - Read Error");
            }
        });

        return publicGameSession;
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
                Intent intent = new Intent(FILTER_GAME_SESSION_ATR);
                intent.putExtra(KEY_GAMESESSION_DATA, gson.toJson(publicGameSession, gameSessionType));
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
                    Log.d(LOG_TAG, " Successfully Updated Game Session");
                } else {
                    //create new value
                    Log.d(LOG_TAG, " Successfully Updated Game Session");
                }
                gameSessionDbReference.child(gameSessionId).setValue(gameSession);
                if (gameInitializing) {
                    listenToServerForGameSessionChanges();
                    gameInitializing = false;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "Game Session - Update Error");
            }
        });
    }






}
