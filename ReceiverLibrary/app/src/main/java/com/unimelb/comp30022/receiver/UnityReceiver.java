package com.unimelb.comp30022.receiver;
/**
 * Created by Kiptenai on 10/10/2017.
 * Handles recieving information on the unity end, running as a plugin
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UnityReceiver extends BroadcastReceiver {
    private static UnityReceiver instance;
    public static String text = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra(Intent.EXTRA_TEXT);
        if(message != null) {
            text = message;
        }

    }

    public static void createInstance() {
        if(instance == null) {
            instance = new UnityReceiver();
        }

    }
}
