package com.unimelb.comp30022.itproject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

public class AndroidToUnitySender extends Service {
    //service handler executes periodically
    private final String LOG_TAG = AndroidToUnitySender.class.getName();
    private final String FILTER = "com.unimelb.comp30022.ITProject.sendintent.IntentToUnity";
    private final Integer LATENCY = 500;
    private final Handler handler = new Handler();
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference userDbReference;
    private DatabaseReference gameSessionDbReference;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser fbuser;

    private boolean gameInitializing = true;
    private String gameSessionId;
    private GameSession publicGameSession;
    private GameSession myGameSession;
    private User currentUserInfo;
    private String userId;
    private GameSession gameSession;
    private HashMap<String, String> gameSessionHashmap;
    private Gson gson = new Gson();
    private DataGenerator generator = new DataGenerator();
    private Type gameSessionType = new TypeToken<GameSession>(){}.getType();
    private Runnable sendData = new Runnable() {
        public void run() {
            Intent senderIntent = new Intent();
            //Intent Flags
            senderIntent.setFlags(Intent.FLAG_FROM_BACKGROUND| Intent.FLAG_ACTIVITY_NO_ANIMATION|
            Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            //direct intent with string targeted by reciever and specitfy data format
            //fake data
            senderIntent.setAction(FILTER).putExtra(Intent.EXTRA_TEXT, gson.toJson(generator.generateRandomGameSession(new LatLng(-37.795298, 144.961263)),gameSessionType));
            //real data
            //
            //--->fetch current user postition through gps
            //---> post this informatoin and update the database once after the latency is passed
            //publicGameSession.setLocation(fetchDeviceLocation());
            Log.d(LOG_TAG, "Fetching the device location");
            Log.d(LOG_TAG, "updating the database and sending the information over to firebase");
            //updateServerGameSession(gameSession);
            //senderIntent.setAction(FILTER).putExtra(Intent.EXTRA_TEXT, gson.toJson(publicGameSession,gameSessionType));
            //
            Log.d(LOG_TAG, this.getClass().getSimpleName());
            sendBroadcast(senderIntent);
            handler.removeCallbacks(this);
            handler.postDelayed(this,LATENCY);
        }
    };
    @Override
    public void onStart(Intent intent, int startid){

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
            gameSession = new GameSession();
            //update data from calling activity
            gameSessionHashmap = (HashMap<String, String>) intent.getExtras().get(ServiceTools.PASSING_INFO_LABEL);
            gameSessionId = gameSessionHashmap.get(ServiceTools.GAME_SESSION_KEY);
            Log.d(LOG_TAG, gameSessionId + " started");
            //initiate the game session
            //Authorize the current user, fetch the game sessionInformation of
            //the information added to the intent on creation, set the game as started and
            //add a listener to the database for any updates in the game sesssion
            setCurrentUserInfo();
            getServerGameSessionObj(gameSessionId);
            handler.removeCallbacks(sendData);
            handler.postDelayed(sendData, LATENCY);
        } else {
            Log.d(LOG_TAG, "Intent Launched game session information");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
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


    private LatLng fetchDeviceLocation() {

        return new LatLng(45.0, 122.0);
    }

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

}
