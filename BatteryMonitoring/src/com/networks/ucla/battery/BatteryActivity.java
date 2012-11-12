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
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

public class BatteryActivity extends Activity {
	LinkedHashMap<Integer, Long> mBatteryLevelMap = new LinkedHashMap<Integer, Long>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
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
			      // wifi signal
			      // 3g signal			      
			      if (!mBatteryLevelMap.containsKey(level)) {
			    	  logToSDCard(level + ";" + time + "\n");
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
