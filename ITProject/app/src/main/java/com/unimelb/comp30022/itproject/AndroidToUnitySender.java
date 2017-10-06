package com.unimelb.comp30022.itproject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by Kiptenai on 14/09/2017.
 * class for handling messages to Unity Application
 */

public class AndroidToUnitySender extends Service {
    //service handler executes periodically
    private final String LOG_TAG = AndroidToUnitySender.class.getName();
    private final String FILTER = "com.unimelb.comp30022.ITProject.sendintent.IntentToUnity";
    private final Integer LATENCY = 100;
    private final Handler handler = new Handler();
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference userDbReference;
    private DatabaseReference gameSessionDbReference;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser fbuser;
    private String gameSessionId;
    private GameSession publicGameSession;
    private GameSession myGameSession;
    private User currentUserInfo;
    private String userId;
    private GameSession gameSession;
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
            senderIntent.setAction(FILTER).putExtra(Intent.EXTRA_TEXT, gson.toJson(generator.generateRandomGameSession(new LatLng(-37.795298, 144.961263)),gameSessionType));
            Log.d(LOG_TAG, this.getClass().getSimpleName());
            sendBroadcast(senderIntent);
            handler.removeCallbacks(this);
            handler.postDelayed(this,LATENCY);
        }
    };
    @Override
    public void onStart(Intent intent, int startid){
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
        //gameSessionId = getIntent().getStringExtra("gameSessionId");
        Log.d(LOG_TAG, gameSessionId);
        Type playerType = new TypeToken<Player>(){}.getType();
        Log.d("JSONobject", gson.toJson(generator.generateRandomPlayer(new LatLng(-37.795298, 144.961263)),playerType));

        handler.removeCallbacks(sendData);
        handler.postDelayed(sendData,LATENCY);
    }
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }
}
