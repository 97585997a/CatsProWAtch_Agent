package com.example.kafkademo;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class InstallApplicationList extends AppCompatActivity {

    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_application_list);



        listView = findViewById(R.id.listview);

        try {
            getallapps();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void getallapps() throws PackageManager.NameNotFoundException {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        // get list of all the apps installed
        List<ResolveInfo> ril = getPackageManager().queryIntentActivities(mainIntent, 0);
        List<String> componentList = new ArrayList<String>();
        String name = null;
        int i = 0;

        // get size of ril and create a list
        String[] apps = new String[ril.size()];
        for (ResolveInfo ri : ril) {
            if (ri.activityInfo != null) {
                // get package
                Resources res = getPackageManager().getResourcesForApplication(ri.activityInfo.applicationInfo);
                // if activity label res is found
                if (ri.activityInfo.labelRes != 0) {
                    name = res.getString(ri.activityInfo.labelRes);
                } else {
                    name = ri.activityInfo.applicationInfo.loadLabel(
                            getPackageManager()).toString();
                }
                apps[i] = name;
                i++;
            }
        }
        // set all the apps name in list view
        listView.setAdapter(new ArrayAdapter<String>(InstallApplicationList.this, android.R.layout.simple_list_item_1, apps));
        // write total count of apps available.

    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}