package com.example.kafkademo;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.MailTo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;

import java.security.spec.ECField;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Activity to display package usage statistics.
 */
public class ApplicationUsesTime extends Activity {
    private static final String TAG = "UsageStatsActivity";
    private static final boolean localLOGV = false;
    private UsageStatsManager mUsageStatsManager;
    private LayoutInflater mInflater;
    private UsageStatsAdapter mAdapter;
    private PackageManager mPm;

    public static class AppNameComparator implements Comparator<UsageStats> {
        private Map<String, String> mAppLabelList;

        AppNameComparator(Map<String, String> appList) {
            mAppLabelList = appList;
        }

        @Override
        public final int compare(UsageStats a, UsageStats b) {
            String alabel = mAppLabelList.get(a.getPackageName());
            String blabel = mAppLabelList.get(b.getPackageName());
            return alabel.compareTo(blabel);
        }
    }

    public static class LastTimeUsedComparator implements Comparator<UsageStats> {
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            // return by descending order
            return (int) (b.getLastTimeUsed() - a.getLastTimeUsed());
        }
    }

    public static class UsageTimeComparator implements Comparator<UsageStats> {
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            return (int) (b.getTotalTimeInForeground() - a.getTotalTimeInForeground());
        }
    }

    // View Holder used when displaying views
    static class AppViewHolder {
        TextView pkgName;
        TextView lastTimeUsed;
        TextView usageTime;
    }

    class UsageStatsAdapter extends BaseAdapter {
        private static final int _DISPLAY_ORDER_USAGE_TIME = 0;
        private static final int _DISPLAY_ORDER_LAST_TIME_USED = 1;
        private static final int _DISPLAY_ORDER_APP_NAME = 2;

        private int mDisplayOrder = _DISPLAY_ORDER_USAGE_TIME;
        private LastTimeUsedComparator mLastTimeUsedComparator = new LastTimeUsedComparator();
        private AppNameComparator mAppLabelComparator;
        private final ArrayMap<String, String> mAppLabelMap = new ArrayMap<>();
        private final ArrayList<UsageStats> mPackageStats = new ArrayList<>();
        private UsageTimeComparator mUsageTimeComparator = new UsageTimeComparator();

        UsageStatsAdapter() {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -1);

            final List<UsageStats> stats =
                    mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                            cal.getTimeInMillis(), System.currentTimeMillis());
            //  Log.wtf("stats", stats.toString());
            if (stats == null) {
                return;
            }

            ArrayMap<String, UsageStats> map = new ArrayMap<>();
            final int statCount = stats.size();
            for (int i = 0; i < statCount; i++) {
                final android.app.usage.UsageStats pkgStats = stats.get(i);

                try {
                    ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                    String label = appInfo.loadLabel(mPm).toString();
                    // Log.wtf("label", label);
                    mAppLabelMap.put(pkgStats.getPackageName(), label);
                    UsageStats existingStats =
                            map.get(pkgStats.getPackageName());
                    if (existingStats == null) {
                        map.put(pkgStats.getPackageName(), pkgStats);
                    } else {
                        existingStats.add(pkgStats);
                    }

                } catch (NameNotFoundException e) {
                    // This package may be gone.
                }
            }
            mPackageStats.addAll(map.values());
            int Persentage = 0;
            BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                Persentage = percentage;
            }
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(memoryInfo);
            JSONArray arrForA = new JSONArray();
            Collections.sort(mPackageStats, mUsageTimeComparator);


            for (int i = 0; i < 15; i++) {
                UsageStats pkgStats = mPackageStats.get(i);
                if (pkgStats != null) {
                    try {

                        ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                        String label = appInfo.loadLabel(mPm).toString();
                        JSONObject itemA = new JSONObject();

                        itemA.put("applicationName", label);
                        itemA.put("lastUsedTime", DateUtils.formatSameDayTime(pkgStats.getLastTimeUsed(),
                                System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM));

                        itemA.put("runningTime", DateUtils.formatElapsedTime(pkgStats.getTotalTimeInForeground() / 1000));


                        arrForA.put(itemA);
                    } catch (Exception e) {

                    }
                }
            }
            JSONObject item = new JSONObject();
            try {
                item.put("androidId", Utils.AndroidID);
                item.put("batteryStatus", Persentage + "");
                item.put("freeMemory", getSize(memoryInfo.availMem));
                item.put("ramUsed", getSize(memoryInfo.totalMem-memoryInfo.availMem));
                item.put("appList", arrForA);
            } catch (Exception ex) {

            }

            Log.wtf("json obj",item+"");
            String url = "http://182.76.238.200:8088/appdata/api/v1/saveAppUsedTimeAndDeviceStatus";
            ARServiceHelper helper = new ARServiceHelper(ApplicationUsesTime.this, url, item, null);

            helper.Post_Standard_Request(new ARServiceHelper.ARServiceCallback() {
                @Override
                public void onResponse(String response) {

                }

                @Override
                public void onError(VolleyError error) {

                }
            });


            mAppLabelComparator = new AppNameComparator(mAppLabelMap);
            sortList();
        }

        @Override
        public int getCount() {
            return mPackageStats.size() >= 15 ? 15 : mPackageStats.size();
        }

        @Override
        public Object getItem(int position) {
            return mPackageStats.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            AppViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.usage_state_item, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new AppViewHolder();
                holder.pkgName = (TextView) convertView.findViewById(R.id.package_name);
                holder.lastTimeUsed = (TextView) convertView.findViewById(R.id.last_time_used);
                holder.usageTime = (TextView) convertView.findViewById(R.id.usage_time);
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (AppViewHolder) convertView.getTag();
            }

            UsageStats pkgStats = mPackageStats.get(position);
            //  Log.wtf("lasttimeuse", "" + pkgStats.getLastTimeUsed());
            if (pkgStats != null) {
                String label = mAppLabelMap.get(pkgStats.getPackageName());
                holder.pkgName.setText(label);
                holder.lastTimeUsed.setText(DateUtils.formatSameDayTime(pkgStats.getLastTimeUsed(),
                        System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM));
                holder.usageTime.setText(
                        DateUtils.formatElapsedTime(pkgStats.getTotalTimeInForeground() / 1000));
            } else {
                Log.w(TAG, "No usage stats info for package:" + position);
            }
            return convertView;
        }

        void sortList(int sortOrder) {
            if (mDisplayOrder == sortOrder) {
                // do nothing
                return;
            }
            mDisplayOrder = sortOrder;
            sortList();
        }

        private void sortList() {
            if (mDisplayOrder == _DISPLAY_ORDER_USAGE_TIME) {
                if (localLOGV) Log.i(TAG, "Sorting by usage time");
                Collections.sort(mPackageStats, mUsageTimeComparator);
            } else if (mDisplayOrder == _DISPLAY_ORDER_LAST_TIME_USED) {
                if (localLOGV) Log.i(TAG, "Sorting by last time used");
                Collections.sort(mPackageStats, mLastTimeUsedComparator);
            } else if (mDisplayOrder == _DISPLAY_ORDER_APP_NAME) {
                if (localLOGV) Log.i(TAG, "Sorting by application name");
                Collections.sort(mPackageStats, mAppLabelComparator);
            }
            notifyDataSetChanged();
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_application_uses_time);


        mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPm = getPackageManager();

        ListView listView = (ListView) findViewById(R.id.pkg_list);
        mAdapter = new UsageStatsAdapter();
        listView.setAdapter(mAdapter);


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

}