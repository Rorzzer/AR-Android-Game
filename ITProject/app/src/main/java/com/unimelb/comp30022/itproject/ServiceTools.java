package com.unimelb.comp30022.itproject;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Kiptenai on 19/09/2017.
 */

public class ServiceTools {

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
        Intent i = new Intent(context, serviceClass);
        Set set = list.entrySet();
        Iterator iterator = set.iterator();
        if (iterator.hasNext()) {
            Map.Entry ent = (Map.Entry) iterator.next();
            i.putExtra(ent.getKey().toString(), ent.getKey().toString());
        }
        context.startService(i);
        return false;
    }

    public static boolean stopRunningService(Context context, Class<?> serviceClass){
        context.stopService(new Intent(context,serviceClass));
        return false;
    }

}
