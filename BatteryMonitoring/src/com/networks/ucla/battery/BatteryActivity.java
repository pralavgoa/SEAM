package com.networks.ucla.battery;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class BatteryActivity extends Activity {
	public static String TAG = BatteryActivity.class.getSimpleName();
	LinkedHashMap<Integer, Long> mBatteryLevelMap = new LinkedHashMap<Integer, Long>();
	private WifiManager mWifiManager;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        /* Find out about our Connection. */
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null || !info.isConnectedOrConnecting()) {
        	Toast.makeText(BatteryActivity.this, 
	    			"No Network Connection", Toast.LENGTH_SHORT).show();
        	Log.i(TAG, "No Network connection");
        } 
        else {
            int netType = info.getType();
            if (netType == ConnectivityManager.TYPE_WIFI) {
				mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				int rssi = mWifiManager.getConnectionInfo().getRssi();
				int signalStrength = WifiManager.calculateSignalLevel(rssi, 100);
            	Toast.makeText(BatteryActivity.this, 
    	    			"Connected to Wifi, Signal Strength: " + signalStrength, Toast.LENGTH_SHORT).show();
				Log.i(TAG, "Wifi connection");
            }
            else if (netType == ConnectivityManager.TYPE_MOBILE) {
            	Log.i(TAG, "GPRS/3G connection"); 
            	//Need to get differentiate between 3G/GPRS
            } 
        }
        
        /* Register to monitor battery. */
        registerReceiver(mBroadrxr, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        
        /* Register to monitor face detection. */
        IntentFilter intentFilter = new IntentFilter(); 
        intentFilter.addAction("com.networks.ucla.FACEDETECT_START");
        intentFilter.addAction("com.networks.ucla.FACEDETECT_STOP");
        registerReceiver(mFaceDetection, intentFilter); 
    }
    
    private BroadcastReceiver mFaceDetection = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			logToSDCard(action + ";" + System.currentTimeMillis() + "\n");
		}
    };
    
    
	private BroadcastReceiver mBroadrxr = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				Toast.makeText(BatteryActivity.this, 
		    			"Battery changed", Toast.LENGTH_SHORT).show();
			      int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			      long time = System.currentTimeMillis();
			      if (!mBatteryLevelMap.containsKey(level)) {
			    	  /* Add to the Battery Map. */
			    	  mBatteryLevelMap.put(level, time);
			    	  /* Get Wifi Signal Strength. */
			    	  mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			    	  int rssi = mWifiManager.getConnectionInfo().getRssi();
			    	  int wifiStrength = WifiManager.calculateSignalLevel(rssi, 100);
			    	  logToSDCard(level + ";" + time + ";" + wifiStrength +"\n");
			      }
			}
		}
	};
	
	public void logToSDCard(String stringData)
	{
		String filename = "batterylog.txt";
		File file = new File(Environment.getExternalStorageDirectory(), filename);
		try {
			FileWriter writer = new FileWriter(file, true);
			writer.append(stringData);
			writer.flush();
			writer.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
