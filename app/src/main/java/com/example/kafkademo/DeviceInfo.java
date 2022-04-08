package com.example.kafkademo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceInfo extends AppCompatActivity {
    private TextView textViewSetInformation;
    int Persentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        textViewSetInformation = (TextView) findViewById(R.id.tv_displayInfo);
        String information = getHardwareAndSoftwareInfo();

        textViewSetInformation.setText(information);
        converjson(information);
    }

    private String getHardwareAndSoftwareInfo() {
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            Persentage = percentage;
        }

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
       String url="http://182.76.238.200:8088/appdata/api/v1/saveDeviceInfo";
        ARServiceHelper helper = new ARServiceHelper(DeviceInfo.this, url, obj,null);

        helper.Post_Standard_Request(new ARServiceHelper.ARServiceCallback() {
            @Override
            public void onResponse(String response) {
            }

            @Override
            public void onError(VolleyError error) {

            }
        });
        return getString(R.string.serial) + " " + Build.SERIAL + "\n" +
                getString(R.string.model) + " " + Build.MODEL + "\n" +
                getString(R.string.id) + " " + Build.ID + "\n" +
                getString(R.string.manufacturer) + " " + Build.MANUFACTURER + "\n" +
                getString(R.string.brand) + " " + Build.BRAND + "\n" +
                getString(R.string.type) + " " + Build.TYPE + "\n" +
                getString(R.string.user) + " " + Build.USER + "\n" +
                getString(R.string.sdk) + " " + Build.VERSION.SDK + "\n" +
                getString(R.string.host) + " " + Build.HOST + "\n" +
                "Battery %" + " " + Persentage + "%\n" +
                getString(R.string.versioncode) + " " + Build.VERSION.RELEASE;
    }

    public void converjson(String a) {

    }

}