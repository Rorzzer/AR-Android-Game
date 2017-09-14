package com.unimelb.comp30022.itproject;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

/**
 * Created by Kiptenai on 14/09/2017.
 * class for handling messages to Unity Application
 */

public class AndroidToUnitySender extends Service {
    //service handler executes periodically
    private final Handler handler = new Handler();
    private int intentID;
    private Runnable sendData = new Runnable() {
        public void run() {
            intentID++;
            Intent senderIntent = new Intent();
            //Intent Flags
            senderIntent.setFlags(Intent.FLAG_FROM_BACKGROUND| Intent.FLAG_ACTIVITY_NO_ANIMATION|
            Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            //direct intent with string targeted by reciever and specitfy data format
            senderIntent.setAction("com.ITProject.sendintent.IntentToUnity").putExtra(Intent.EXTRA_TEXT, "Intent "+ intentID);
            sendBroadcast(senderIntent);
            handler.removeCallbacks(this);
            handler.postDelayed(this,1000);
        }
    };
    @Override
    public void onStart(Intent intent, int startid){
        intentID =0;
        handler.removeCallbacks(sendData);
        handler.postDelayed(sendData,1000);
    }
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }
}
