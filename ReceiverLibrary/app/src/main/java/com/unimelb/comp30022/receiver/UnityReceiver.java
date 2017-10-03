package com.unimelb.comp30022.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UnityReceiver extends BroadcastReceiver {
    private static UnityReceiver instance;
    public static String text = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        String sentIntent = intent.getStringExtra(Intent.EXTRA_TEXT);
        if(sentIntent != null) {
            text = sentIntent;
        }

    }

    public static void createInstance() {
        if(instance == null) {
            instance = new UnityReceiver();
        }

    }
}
