package com.example.kafkademo;

import android.Manifest;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Process;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

public class MyService extends Service {
    DevicePolicyManager devicePolicyManager;
    FusedLocationProviderClient fusedLocationProviderClient;
    JSONObject obj;

    @Override
    public void onCreate() {
        Utils._con = this;


        Utils.mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        Utils.mPm = getPackageManager();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
        wakeLock.acquire();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences shared = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String channel = (shared.getString("androidid", ""));
        Utils.AndroidID = channel;
        callApi();

        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void callApi() {
        Utils.UsageStatsAdapter();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding
        return null;
    }


}