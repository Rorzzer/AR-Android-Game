package com.unimelb.comp30022.itproject;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by Kiptenai on 14/09/2017.
 * class for handling messages to Unity Application
 */

public class AndroidToUnitySender extends Service {
    //service handler executes periodically
    private final String LOG_TAG = "Running service";
    private final String FILTER = "com.unimelb.comp30022.ITProject.sendintent.IntentToUnity";
    private final Integer LATENCY = 100;
    private final Handler handler = new Handler();
    private GameSession gameSession;
    private Gson gson = new Gson();
    private DataGenerator generator = new DataGenerator();
    private Type gameSessionType = new TypeToken<GameSession>(){}.getType();
    private int intentID;
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
        gameSession = new GameSession();
        handler.removeCallbacks(sendData);
        handler.postDelayed(sendData,LATENCY);
    }
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }
}
