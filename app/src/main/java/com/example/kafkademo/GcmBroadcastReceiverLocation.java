package com.example.kafkademo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class GcmBroadcastReceiverLocation extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent in = new Intent(context, MyServiceLocation.class);
        context.startService(in);
        setAlarm1(context);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setAlarm1(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, GcmBroadcastReceiverLocation.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        assert am != null;
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis() / 1000L + 20L) * 1000L, pi); //Next alarm in 15s
    }
}