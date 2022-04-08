package com.example.kafkademo;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class MyServiceAction extends Service {
    DevicePolicyManager devicePolicyManager;
    FusedLocationProviderClient fusedLocationProviderClient;
    JSONObject obj;

    @Override
    public void onCreate() {
        Utils._con = this;

        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
        wakeLock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences shared = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String channel = (shared.getString("androidid", ""));
        Utils.AndroidID = channel;
       // callApi();
        String clientId = MqttClient.generateClientId();
        Utils.client = new MqttAndroidClient(this, "tcp://10.0.3.99:1883",clientId);
        try {
            Utils.token = Utils.client.connect();
            Utils.token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Toast.makeText(MainActivity.this,"connected!!",Toast.LENGTH_LONG).show();
                    try {
                        Utils.client.subscribe("m_location",0);
                        Utils.client.subscribe("m_devicestatus",0);
                        Utils.client.subscribe("m_deviceinfo", 0);

                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Toast.makeText(MainActivity.this,"connection failed!!",Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }



        Utils.client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
              if(topic.equalsIgnoreCase("m_action"))
              {
                  if (new String(message.getPayload()).equalsIgnoreCase("lock")) {
                      devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                      devicePolicyManager.lockNow();
                  } else if (new String(message.getPayload()).equalsIgnoreCase("reset")) {
                      devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                      // devicePolicyManager.wipeData(0);
                  }
              }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        Utils.getLastLocation();

        return START_STICKY;
    }


    public void callApi() {

        String url = "http://182.76.238.200:8088/appdata/api/v1/actionLockReset/" + Utils.AndroidID;
        ARServiceHelper helper = new ARServiceHelper(MyServiceAction.this, url, null, null);

        helper.Get_Simple_Request(new ARServiceHelper.ARServiceCallback() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getString("message").equalsIgnoreCase("lock")) {
                        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                        devicePolicyManager.lockNow();
                    } else if (json.getString("message").equalsIgnoreCase("reset")) {
                        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                       // devicePolicyManager.wipeData(0);
                    }
                    Log.wtf("action",json.getString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(VolleyError error) {
                Log.wtf("error", error);
            }
        });

        Utils.getLastLocation();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding
        return null;
    }

    @Override
    public void onDestroy() {
        Utils.client.unregisterResources();
        Utils.client.close();
        super.onDestroy();
    }
}