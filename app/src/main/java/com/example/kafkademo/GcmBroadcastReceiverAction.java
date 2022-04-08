package com.example.kafkademo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Calendar;

public class GcmBroadcastReceiverAction extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Intent myService = new Intent(context, MyServiceAction.class);
            context.stopService(myService);
        } catch (Exception e) {

        }

        Intent in = new Intent(context, MyServiceAction.class);
        context.startService(in);
        setAlarm(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setAlarm(Context context) {
        Calendar updateTime = Calendar.getInstance();

        updateTime.set(Calendar.MINUTE, 5);
        AlarmManager am1 = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, GcmBroadcastReceiverAction.class);
        PendingIntent pi1 = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
        assert am1 != null;
        am1.cancel(pi1);
        am1.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis() / 1000L + 300L) * 1000L, pi1);

//        Intent i1 = new Intent(context, GcmBroadcastReceiver.class);
//        PendingIntent pi11 = PendingIntent.getBroadcast(context, 1, i1, PendingIntent.FLAG_ONE_SHOT);
//        assert am1 != null;
//        am1.cancel(pi11);
//        am1.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis() / 1000L + 60L) * 1000L, pi11); //Next alarm in 15s
//86400

    }


}