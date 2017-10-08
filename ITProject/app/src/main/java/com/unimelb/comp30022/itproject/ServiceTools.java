package com.unimelb.comp30022.itproject;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by Kiptenai on 19/09/2017.
 */

public class ServiceTools {

    public static String PASSING_INFO_LABEL = "gameInformation";
    public static String GAME_SESSION_KEY = "gameSession";
    public static boolean isServiceRunning(Context context, Class<?> serviceClass){

        final ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        //iterate throough service list and compare service names
        for(ActivityManager.RunningServiceInfo runningServiceInfo: manager.getRunningServices(Integer.MAX_VALUE)){
            if(runningServiceInfo.service.getClassName().equals(serviceClass.getName())){
                //log result of running servvice
                Log.d(ServiceTools.class.getName(),serviceClass.getSimpleName());
                return true;
            }
        }
        return false;
    }

    public static boolean startNewService(Context context, Class<?> serviceClass,
                                          HashMap<String, String> list) {
        Intent intent = new Intent(context, serviceClass);
        intent.putExtra(PASSING_INFO_LABEL, list);
        context.startService(intent);
        return false;
    }

    public static boolean stopRunningService(Context context, Class<?> serviceClass){
        context.stopService(new Intent(context,serviceClass));
        return false;
    }

}
