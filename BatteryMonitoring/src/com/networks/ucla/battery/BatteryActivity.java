package com.networks.ucla.battery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.TextView;

public class BatteryActivity extends Activity {
	private TextView mTextView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
        mTextView = (TextView) findViewById(R.id.output);
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
