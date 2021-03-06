package com.unimelb.comp30022.itproject;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by Kiptenai on 19/09/2017.
 * provides a simple methods determine if the service is running
 */

public class ServiceTools {

    /***
     * determines whether an instance of the service is running
     * @param context the currrent application context
     * @param serviceClass the service to be checked
     * */
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



}
