package com.example.kafkademo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.android.volley.VolleyError;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderProperty;
import com.rudderstack.android.sdk.core.RudderTraits;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME;
import static android.content.ContentValues.TAG;

import androidx.lifecycle.Observer;

public class MainActivity extends Activity {
    protected static final int REQUEST_ENABLE = 0;
    DevicePolicyManager devicePolicyManager;
    ComponentName adminComponent;

    public boolean isGPS;
    public static View view;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressLint("InvalidWakeLockTag")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils._con = this;


        Intent in = new Intent(this, MyServiceAction.class);
        this.startService(in);
        GcmBroadcastReceiverAction alarm2 = new GcmBroadcastReceiverAction();
        alarm2.setAlarm(this);

        Intent in1 = new Intent(this, MyService.class);
        this.startService(in1);
        GcmBroadcastReceiver alarm = new GcmBroadcastReceiver();
        alarm.setAlarm(this);

        requestPermission();

        view = getWindow().getDecorView().getRootView();
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        myEdit.putString("androidid", Build.ID);
        myEdit.commit();

        if (permissiontodetectapp(MainActivity.this)) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));

        } else {

        }
        askPermission();

        new GpsUtils(MainActivity.this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;
            }
        });
        SharedPreferences shared = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String channel = (shared.getString("key", ""));
        Log.wtf("key", channel);

        adminComponent = new ComponentName(MainActivity.this, Darclass.class);
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (!devicePolicyManager.isAdminActive(adminComponent)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            startActivityForResult(intent, REQUEST_ENABLE);

        }

        findViewById(R.id.click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, MQTTConnect.class);
                MainActivity.this.startActivity(myIntent);
            }
        });


//        String clientId = MqttClient.generateClientId();
//        Utils.client = new MqttAndroidClient(this, "tcp://10.0.3.99:1883", clientId);
//        try {
//            Utils.token = Utils.client.connect();
//            Utils.token.setActionCallback(new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    // Toast.makeText(MainActivity.this,"connected!!",Toast.LENGTH_LONG).show();
//                    try {
//                        Utils.client.subscribe("m_location", 0);
//                        Utils.client.subscribe("m_devicestatus", 0);
//                        Utils.client.subscribe("m_deviceinfo", 0);
//                        if (channel.equalsIgnoreCase("1")) {
//
//                        } else {
//                            Log.wtf("Calling", "calling");
//                            getHardwareAndSoftwareInfo();
//
//                        }
//                    } catch (MqttException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    // Toast.makeText(MainActivity.this,"connection failed!!",Toast.LENGTH_LONG).show();
//                }
//            });
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
    }


    public boolean permissiontodetectapp(Context context) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            return ((AppOpsManager) context.getSystemService(APP_OPS_SERVICE)).checkOpNoThrow("android:get_usage_stats", applicationInfo.uid, applicationInfo.packageName) != 0;
        } catch (PackageManager.NameNotFoundException unused) {
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_ENABLE == requestCode) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void askPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {

        if (requestCode == 100) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


            } else {

                Toast.makeText(MainActivity.this, "Please provide the required permission", Toast.LENGTH_SHORT).show();
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getHardwareAndSoftwareInfo() {

        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("key", "1");
        myEdit.commit();
        JSONObject obj = new JSONObject();

        try {
            obj.put("androidId", Build.ID);
            obj.put("model", Build.MODEL);
            obj.put("manufacturer", Build.MANUFACTURER);
            obj.put("brand", Build.BRAND);
            obj.put("androidVersion", Build.VERSION.SDK_INT + "");
            obj.put("userName", Build.USER);

            System.out.println(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MqttMessage message = new MqttMessage();
        message.setPayload(obj.toString().getBytes());
       // Log.wtf("location", obj.toString());

        try {
            Utils.client.publish("m_deviceinfo", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        String url = "http://182.76.238.200:8088/appdata/api/v1/saveDeviceInfo";
//        ARServiceHelper helper = new ARServiceHelper(MainActivity.this, url, obj, null);
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

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + MainActivity.this.getPackageName())
                );
                startActivityForResult(intent, 232);
            } else {

            }
        }
    }

}