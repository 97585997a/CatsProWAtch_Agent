package com.example.kafkademo;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.BATTERY_SERVICE;

public class Utils {
    public static Context _con = null;
    public static UsageStatsManager mUsageStatsManager;
    public static PackageManager mPm;
    public static ArrayList<UsageStats> mPackageStats = new ArrayList<>();
    public static String AndroidID = "";
    public static MqttAndroidClient client;
    public static IMqttToken token;
    public static ArrayMap<String, String> mAppLabelMap = new ArrayMap<>();
    public static UsageTimeComparator mUsageTimeComparator = new UsageTimeComparator();

    public static class UsageTimeComparator implements Comparator<UsageStats> {
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            return (int) (b.getTotalTimeInForeground() - a.getTotalTimeInForeground());
        }
    }

    // @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void UsageStatsAdapter() {
        mPackageStats.clear();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_YEAR, 0);

        final List<UsageStats> stats =
                mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                        cal.getTimeInMillis(), System.currentTimeMillis());
        if (stats == null) {
            return;
        }

        ArrayMap<String, UsageStats> map = new ArrayMap<>();
        final int statCount = stats.size();
        for (int i = 0; i < statCount; i++) {
            final UsageStats pkgStats = stats.get(i);

            try {
                ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                String label = appInfo.loadLabel(mPm).toString();
                mAppLabelMap.put(pkgStats.getPackageName(), label);
                UsageStats existingStats =
                        map.get(pkgStats.getPackageName());
                if (existingStats == null) {
                    map.put(pkgStats.getPackageName(), pkgStats);
                } else {
                    existingStats.add(pkgStats);
                }

            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        mPackageStats.addAll(map.values());
        int Persentage = 0;
        BatteryManager bm = (BatteryManager) _con.getSystemService(BATTERY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            Persentage = percentage;
        }
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) _con.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        JSONArray arrForA = new JSONArray();
        Collections.sort(mPackageStats, mUsageTimeComparator);


        for (int i = 0; i < mPackageStats.size(); i++) {
            UsageStats pkgStats = mPackageStats.get(i);
            if (pkgStats != null) {
                try {
                    ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                    String label = appInfo.loadLabel(mPm).toString();
                    JSONObject itemA = new JSONObject();
                    itemA.put("device_type", "android");
                    itemA.put("applicationName", label);
                    itemA.put("uuid", Build.ID);
                    itemA.put("user_name", Build.USER);
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    itemA.put("FirstTimeStamp", df.format(pkgStats.getFirstTimeStamp()));
                    itemA.put("LastTimeStamp", df.format(pkgStats.getLastTimeStamp()));
                    itemA.put("LastTimeUsed", df.format(pkgStats.getLastTimeUsed()));
                    itemA.put("TotalTimeInForeground", timeconvert(pkgStats.getTotalTimeInForeground() / 1000));
                    itemA.put("runningTime", timeconvert(pkgStats.getTotalTimeInForeground() / 1000));

//                    String date = df.format(Calendar.getInstance().getTime());
//                    itemA.put("currentTime",date);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                        itemA.put("LastTimeForegroundServiceUsed", df.format(pkgStats.getLastTimeForegroundServiceUsed()));
                        itemA.put("LastTimeVisible", df.format(pkgStats.getLastTimeVisible()));
                        itemA.put("TotalTimeForegroundServiceUsed", timeconvert(pkgStats.getTotalTimeForegroundServiceUsed() / 1000));
                        itemA.put("TotalTimeVisible", timeconvert(pkgStats.getTotalTimeVisible() / 1000));
                    } else {
                        itemA.put("LastTimeForegroundServiceUsed", "00:00:00");
                        itemA.put("LastTimeVisible", "00:00:00");
                        itemA.put("TotalTimeForegroundServiceUsed", "00:00:00");
                        itemA.put("TotalTimeVisible", "00:00:00");
                    }

                    arrForA.put(itemA);
                } catch (Exception e) {
                    String a = e.toString();
                    String b = a;
                }
            }
        }

        JSONObject item = new JSONObject();
        try {
//            item.put("uuid", AndroidID);
//            item.put("device_type", "android");
//            item.put("batteryStatus", Persentage + "");
//            item.put("freeMemory", getSize(memoryInfo.availMem));
//            item.put("ramUsed", getSize(memoryInfo.totalMem - memoryInfo.availMem));
            item.put("appList", arrForA);
        } catch (Exception ex) {
            String a = ex.toString();
            String b = a;
        }

        MqttMessage message = new MqttMessage();
        message.setPayload(item.toString().getBytes());
        Log.wtf("devicestatus", item.toString());

        try {
            client.publish("m_devicestatus", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getHardwareAndSoftwareInfo();

//        Log.wtf("json obj", item + "");
//        String url = "http://182.76.238.200:8088/appdata/api/v1/saveAppUsedTimeAndDeviceStatus";
//        ARServiceHelper helper = new ARServiceHelper(_con, url, item, null);
//
//        helper.Post_Standard_Request(new ARServiceHelper.ARServiceCallback() {
//            @Override
//            public void onResponse(String response) {
//
//            }
//
//            @Override
//            public void onError(VolleyError error) {
//
//            }
//        });


    }

    public static String getSize(long size) {
        String s = "";
        double kb = size / 1024;
        double mb = kb / 1024;
        double gb = mb / 1024;
        double tb = gb / 1024;
        if (size < 1024L) {
            s = size + " Bytes";
        } else if (size >= 1024 && size < (1024L * 1024)) {
            s = String.format("%.2f", kb) + " KB";
        } else if (size >= (1024L * 1024) && size < (1024L * 1024 * 1024)) {
            s = String.format("%.2f", mb) + " MB";
        } else if (size >= (1024L * 1024 * 1024) && size < (1024L * 1024 * 1024 * 1024)) {
            s = String.format("%.2f", gb) + " GB";
        } else if (size >= (1024L * 1024 * 1024 * 1024)) {
            s = String.format("%.2f", tb) + " TB";
        }
        return s;
    }


    public static void getLastLocation() {


        if (ActivityCompat.checkSelfPermission(_con, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(_con, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        JSONObject obj = new JSONObject();
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {

                        try {
                            Geocoder geocoder = new Geocoder(_con, Locale.getDefault());

                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            // Log.wtf("address", addresses.toString());
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String date = df.format(Calendar.getInstance().getTime());

                            try {
                                obj.put("uuid", Build.ID);
                                obj.put("time", date);
                                obj.put("user_name", Build.USER);
                                obj.put("position_source", "google");
                                obj.put("position_source_desc", "gps");
                                obj.put("accuracy", "100");
                                obj.put("time_utc", date);
                                obj.put("device_type", "android");
                                obj.put("provider", "google");
                                obj.put("zipcode", addresses.get(0).getPostalCode());
                                obj.put("countrycode", addresses.get(0).getCountryCode());
                                obj.put("country", addresses.get(0).getCountryName());
                                obj.put("city", addresses.get(0).getLocality());
                                obj.put("street", addresses.get(0).getSubLocality());
                                obj.put("longitude", addresses.get(0).getLongitude());
                                obj.put("latitude", addresses.get(0).getLatitude());
                                obj.put("address", addresses.get(0).getAddressLine(0));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }
                }

            }
        };
        LocationServices.getFusedLocationProviderClient(_con).requestLocationUpdates(mLocationRequest, mLocationCallback, null);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                MqttMessage message = new MqttMessage();
                message.setPayload(obj.toString().getBytes());
                Log.wtf("location", obj.toString());

                try {
                    client.publish("m_location", message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //  sendLocation();
            }
        }, 5000);


    }


//    public static void sendLocation() {
//
//        String url = "http://182.76.238.200:8088/appdata/api/v1/saveDeviceLocation";
//        ARServiceHelper helper = new ARServiceHelper(_con, url, obj,null);
//
//        helper.Post_Standard_Request(new ARServiceHelper.ARServiceCallback() {
//            @Override
//            public void onResponse(String response) {
//                Log.wtf("address", response.toString());
//            }
//
//            @Override
//            public void onError(VolleyError error) {
//
//            }
//        });
//
//    }

    public static void getHardwareAndSoftwareInfo() {
        int Persentage = 0;
        BatteryManager bm = (BatteryManager) _con.getSystemService(BATTERY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            Persentage = percentage;
        }
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) _con.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        JSONObject obj = new JSONObject();

        try {
            obj.put("device_type", "android");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = df.format(Calendar.getInstance().getTime());
            obj.put("updated_date", date);
            obj.put("uuid", Build.ID);
            obj.put("model", Build.MODEL);
            obj.put("manufacturer", Build.BRAND);
            obj.put("userName", Build.USER);
            //  obj.put("uuid", AndroidID);
            //  obj.put("device_type", "android");
            obj.put("batteryStatus", Persentage + "");
            obj.put("freeMemory", getSize(memoryInfo.availMem));
            obj.put("ramUsed", getSize(memoryInfo.totalMem - memoryInfo.availMem));

            int androidVersion = Build.VERSION.SDK_INT;
            switch (androidVersion) {
                case 18:
                    obj.put("androidVersion", "18, Jelly Bean");
                    break;
                case 19:
                    obj.put("androidVersion", "19, KitKat");
                    break;
                case 21:
                    obj.put("androidVersion", "21, Lollipop");
                    break;
                case 22:
                    obj.put("androidVersion", "22, Lollipop");
                    break;
                case 23:
                    obj.put("androidVersion", "23, Marshmallow");
                    break;
                case 24:
                    obj.put("androidVersion", "24, Nougat");
                    break;
                case 25:
                    obj.put("androidVersion", "25, Nougat");
                    break;
                case 27:
                    obj.put("androidVersion", "27, Oreo");
                    break;
                case 28:
                    obj.put("androidVersion", "28, Pie");
                    break;
                case 29:
                    obj.put("androidVersion", "29, Android 10");
                    break;
                case 30:
                    obj.put("androidVersion", "30, Android 11");
                    break;
                case 31:
                    obj.put("androidVersion", "31, Android 12");
                    break;
            }

            Log.wtf("device data", obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MqttMessage message = new MqttMessage();
        message.setPayload(obj.toString().getBytes());

        try {
            Utils.client.publish("m_deviceinfo", message);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static String timeconvert(long s) {
        long sec = s % 60;
        long min = (s / 60) % 60;
        long hours = (s / 60) / 60;

        String strSec = (sec < 10) ? "0" + Long.toString(sec) : Long.toString(sec);
        String strmin = (min < 10) ? "0" + Long.toString(min) : Long.toString(min);
        String strHours = (hours < 10) ? "0" + Long.toString(hours) : Long.toString(hours);

        return (strHours + ":" + strmin + ":" + strSec);
    }

}
