package com.networks.ucla.batterymonitoringtoggle;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	private TextView mTextView;
	private ToggleButton toButton1;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.output);
        toButton1= (ToggleButton) findViewById(R.id.tButton1);
        toButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                	Toast.makeText(getApplicationContext(), "is on", Toast.LENGTH_SHORT).show();
                	for (int i=0; i<Integer.MAX_VALUE; i++) {
                		for (int j=0; j<Integer.MAX_VALUE; j++) {
                			Log.d("TAG", i + " " + j);
                		}
                	}
                } else {
                    // The toggle is disabled
                }
            }
        });   
       	 registerReceiver(mBroadrxr, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }
    
 
    
private BroadcastReceiver mBroadrxr = new BroadcastReceiver() {
	 @Override
	    public void onReceive(Context context, Intent intent) {
		 if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
		      int temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
		      int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		      int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
		      int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
		      long time = System.currentTimeMillis();
		      mTextView.append("\nT = " + temp + "\tLvl = " + level +
		    		  "\tMax = " + scale + "\nV = " + voltage + "\tt = " + time);
		      
		     
		 }
	 }
};




}


