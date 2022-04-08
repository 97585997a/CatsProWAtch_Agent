package com.example.kafkademo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.legacy.content.WakefulBroadcastReceiver;

public class GcmBroadcastReceiver  extends BroadcastReceiver
{
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent)
    {

        try {
            Intent myService = new Intent(context, MyService.class);
            context.stopService(myService);
        } catch (Exception e) {

        }
        Intent in = new Intent(context, MyService.class);
        context.startService(in);
        setAlarm(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setAlarm(Context context)
    {
        AlarmManager am1 =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, GcmBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 1, i, PendingIntent.FLAG_ONE_SHOT);
        assert am1 != null;
       am1.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis()/1000L + 60L) *1000L, pi); //Next alarm in 15s
    }
}