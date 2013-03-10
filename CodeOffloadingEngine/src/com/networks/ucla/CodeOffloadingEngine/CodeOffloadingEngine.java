package com.networks.ucla.CodeOffloadingEngine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import ucla.nrl.seam.CodeOffloadDecider;
import ucla.nrl.seam.Location;
import ucla.nrl.seam.PhoneRuntimeLevels;
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
import android.widget.Toast;

public class CodeOffloadingEngine extends Activity {
	public static String TAG = CodeOffloadingEngine.class.getSimpleName();
	public static final String KEY_APP_NAME = "app_name";
	public static final String KEY_FUNCTION_NAME = "function_name";
	public static final String KEY_DATA_SIZE = "data_size";
	public static final String KEY_SERVER_URL = "server_url";
	public static final String KEY_DECISION = "decision";
	public static final String KEY_START_TIME = "start_time";
	public static final String KEY_END_TIME = "end_time";
	
	private String mAppName, mFunctionName, mServerUrl;
	private int mDataSize;
	private long mStartTime, mEndTime;
	//final String SERVER_APP_URL;
	//final String SERVER_APP_URL = "http://ec2-50-19-179-123.compute-1.amazonaws.com:8080/SEAM/FaceDetector";
	private Queue<Integer> mWifiLevelQueue;
	private Context mContext;
	private int mCurrentBatteryLevel;
	private CodeOffloadDecider mCodeOffloadDecider;
	
	public enum OffloadStatus {
		NO_OFFLOAD,
		OFFLOAD,
		OFFLOAD_COMPLETE
	}
	
	private OffloadStatus mStatusOffload;

	private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mCurrentBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);	
		}
	};
	
	private BroadcastReceiver mOffloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e(TAG, "offload response");
			Bundle extras = intent.getExtras(); 
			if (extras!=null) {
				Log.e(TAG, "Inside if");
				/* Get State information from FDApp's intent.*/ 
				setStatus(OffloadStatus.NO_OFFLOAD);
				String app_name = extras.getString(KEY_APP_NAME);
				String function_name = extras.getString(KEY_FUNCTION_NAME);
				String server_url = extras.getString(KEY_SERVER_URL);
				int data_size = extras.getInt(KEY_DATA_SIZE);
				
				/* set State of the engine. */
				setState(app_name, function_name, server_url, data_size, 0, 0);
				
				
				/* Find if code is offloadable or not. */
				boolean is_offload = isOffloadingBeneficial(server_url,
						app_name, function_name, data_size, mStartTime);
				
				/* send the decision back to the app. */
				Intent offloadDecisionIntent = new Intent();
				Log.e(TAG, "Sending Decision");
		        offloadDecisionIntent.setAction(getString(R.string.intent_decision));
		        offloadDecisionIntent.putExtra(KEY_DECISION, is_offload);
		        sendBroadcast(offloadDecisionIntent);
		        Log.e(TAG, "sent offload decision");
		        
			}
			else {
				Log.e(TAG, "Inside else");
			}
		}
		
		public boolean isOffloadingBeneficial(String server_url, String app_name, 
				String function_name, int data_size,long start_time) {

			/* Get current System time. */
			long unixTime = System.currentTimeMillis();

			/* Get Current WiFi level. */
			int currentWiFiLevel = getCurrentWiFiValue();

			/* Get Current Battery Level. */
			int currentBatteryLevel = getCurrentBatteryValue();
			Integer[] wifi = mWifiLevelQueue.toArray(
					new Integer[mWifiLevelQueue.size()]);
			
			/* call Pralav's method. */
			PhoneRuntimeLevels runTimeLevels = new PhoneRuntimeLevels(unixTime,
					currentBatteryLevel, currentWiFiLevel, data_size);
			Location location = new Location(0, 0, "myLocation");
			return mCodeOffloadDecider.isOffloadingBeneficial(
					app_name, function_name, runTimeLevels, wifi, location);

		}
	};
	
	/* Record history of app's function. */
	private BroadcastReceiver mOffloadLogReceiver = new BroadcastReceiver()	{
		@Override
		public void onReceive(Context context, Intent intent) {
			int currentWiFi = getCurrentWiFiValue();
			int currentBattery = getCurrentBatteryValue();
			Bundle extras = intent.getExtras(); 
			String data = (Integer.valueOf(currentWiFi)).toString() + "," 
					+ (Integer.valueOf(currentBattery)).toString();
			data = mAppName + "," + mFunctionName + "," + mDataSize + "," + data + ",";
			if(extras!=null) {
				set_start_time(intent.getLongExtra(KEY_START_TIME, 0));
				set_end_time(intent.getLongExtra(KEY_END_TIME, 0));
				data = data + mStartTime + "," + mEndTime;
				Log.e(TAG, data);
			}
			//logToSDCard(data);
			PhoneRuntimeLevels runTimeLevels = new PhoneRuntimeLevels(mEndTime,
					currentBattery, currentWiFi, mDataSize);
			mCodeOffloadDecider.notifyAppFunctionExecutionStop(
					mAppName, mFunctionName, runTimeLevels);
		}
	};

	private DoOffload mDoOffloadReceiver = new DoOffload();

	public void onCreate(Bundle savedInstanceState)	{
        super.onCreate(savedInstanceState);
		Log.e(TAG, "Inside onCreate");
		mWifiLevelQueue = new LinkedList<Integer>();
		mStatusOffload = OffloadStatus.NO_OFFLOAD;
		mCurrentBatteryLevel = 0;
		
		/* Register Receivers. */
		registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		registerReceiver(mOffloadReceiver, new IntentFilter(getString(R.string.intent_should_offload)));
		registerReceiver(mOffloadLogReceiver, new IntentFilter(getString(R.string.intent_log_to_memory)));
		registerReceiver(mDoOffloadReceiver,new IntentFilter(getString(R.string.intent_perform_offload)));
		

		/* Start counting WiFi values. */
		Timer schedule = new Timer();
		Log.e(TAG, "schedule wifi");
		schedule.schedule(new RecordWifi(),0 , 60000);
		
		/* create new CodeOffloadDecider instance. */
		mCodeOffloadDecider = CodeOffloadDecider.getInstance();
	}
	
	//Helper Functions
	private void setStatus(OffloadStatus value) {
		mStatusOffload = value;
	}
	
	private OffloadStatus getStatus() {
		return mStatusOffload;
	}
	
	private void set_start_time(long value)	{
		mStartTime = value;
	}
	
	private void set_end_time(long value) {
		mEndTime = value;
	}
	
	private int getCurrentWiFiValue() {
		if (!mWifiLevelQueue.isEmpty()) {
			return ((Integer)mWifiLevelQueue.peek()).intValue();
		}
		return -1;
	}
	
	private int getCurrentBatteryValue() {
		return mCurrentBatteryLevel;
	}
	
	private void setState(String name, String func_name, 
			String server, int size, long start, long end) {
		mAppName =name;
		mFunctionName = func_name;
		mServerUrl = server;
		mDataSize = size;
		mStartTime = start;
		mEndTime = end;
	}
	
	/* Record history of app's function. */
	public static void logToSDCard(String stringData) {
		String filename = "batterylog.txt";
		File file = new File(Environment.getExternalStorageDirectory(), filename);
		try {
			Log.e("Pranay","Storing data to log file");
			FileWriter writer = new FileWriter(file, true);
			writer.append(stringData+"\n");
			writer.flush();
			writer.close();
			Log.e("Pranay","Successfully stored data to log file");
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* Check if code can be offloaded or not. */
	int count_wifi = 0;
	//Record WiFi level every minute
	class RecordWifi extends TimerTask {
		public void run() {
			Log.e("checkWiFi","adding wifi level with count : "+count_wifi);
			addWiFiLevelToQueue();
			Log.e("checkWiFi","added wifi level with count : " + count_wifi++);
		}
		//Find current WiFi level, if there's a WiFi connection and add to fixed-size queue
		void addWiFiLevelToQueue() {
			Log.e("checkWiFi","check connection");
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cm.getActiveNetworkInfo();
			Log.e("checkWiFi","after check connection");

			if (info == null || !info.isConnectedOrConnecting()) {
				Log.e("checkWiFi","got no connection");

				Toast.makeText(mContext, "No Network Connection", Toast.LENGTH_SHORT).show();
				Log.i("checkWiFi", "No Network connection");
				
				//Add WiFi level to queue
				if(mWifiLevelQueue.size() == 15)
					mWifiLevelQueue.remove();
				mWifiLevelQueue.add(Integer.valueOf(0));
        
			} 
			else {         
				int netType = info.getType();
				if (netType == ConnectivityManager.TYPE_WIFI) {
					Log.e("checkWiFi","got connection");
					WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
					try {
						int rssi = wifiManager.getConnectionInfo().getRssi();
						int signalStrength = WifiManager.calculateSignalLevel(rssi, 100);
						//Toast.makeText(BatteryActivity.this, 
						//		"Connected to Wifi, Signal Strength: " + signalStrength, Toast.LENGTH_SHORT).show();
						Log.i("checkWiFi", "Wifi connection");

						//Add WiFi level to Queue        				
						if(mWifiLevelQueue.size() == 15) {
							mWifiLevelQueue.remove();
						}
						mWifiLevelQueue.add(Integer.valueOf(signalStrength));
					}
					catch(java.lang.ArithmeticException exc) {
						Log.e("addWifiLevelToQueue","Android API doing divide by zero again");
					}
				}          
				else if (netType == ConnectivityManager.TYPE_MOBILE) {            
					Log.i("checkWiFi", "GPRS/3G connection");
					//Add WiFi level to Queue        				
					if(mWifiLevelQueue.size() == 15) {
						mWifiLevelQueue.remove();
					}
					mWifiLevelQueue.add(Integer.valueOf(0));
					//Need to get differentiate between 3G/GPRS
				} 
			}		
		}
	}
	
	class DoOffload extends BroadcastReceiver implements Runnable {
		Thread mDoOffloadThread;
		@Override
		public void onReceive(Context context, Intent intent) {
			setStatus(OffloadStatus.OFFLOAD);
			Log.e(TAG, "Perform Offload - sending image to server: " + mServerUrl);
			Log.e(TAG, mAppName);

			//create new thread to offload data to server
			mDoOffloadThread = new Thread(this, "Offload-to-Server Thread");
			Log.e(TAG, "Starting Offloading in New Thread");
			mDoOffloadThread.start();

		}
		//Offload data to thread
		public void run() {	
			HttpResponse response;
			HttpEntity resEntity;
			String responseAsString = "";
			HttpClient httpclient = new DefaultHttpClient();
			try {
				HttpPost httppost = new HttpPost(mServerUrl);

				FileBody bin = new FileBody(new File(Environment.getExternalStorageDirectory(),
				        "test_1.jpg"));
            
				//HttpGet httpget = new HttpGet(server_url);
            
				StringBody comment = new StringBody("An image file to be uploaded");

				MultipartEntity reqEntity = new MultipartEntity();
				reqEntity.addPart("bin", bin);
				reqEntity.addPart("comment", comment);
				
				httppost.setEntity(reqEntity);

				Log.e(mAppName,"executing request " + httppost.getRequestLine());
				
				//response = httpclient.execute(httpget);

				response = httpclient.execute(httppost);
				resEntity = response.getEntity();

				Log.e(mAppName,"----------------------------------------");
				Log.e(mAppName, response.getStatusLine().toString());
				if (resEntity != null) {
					Log.e(mAppName, "Response content length: " + resEntity.getContentLength());
					InputStream respInputStream = resEntity.getContent();
					StringWriter writer = new StringWriter();
					IOUtils.copy(respInputStream, writer, "UTF-8");
					responseAsString = writer.toString();
					//Log.e(mAppName, responseAsString);
				}
				setStatus(OffloadStatus.OFFLOAD_COMPLETE);
				
				//--EntityUtils.consume(resEntity);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
        
			finally {
				try { 
					httpclient.getConnectionManager().shutdown(); 
				} 
				catch (Exception ignore) {}
			}
			Intent responseIntent = new Intent();
			responseIntent.setAction(getString(R.string.intent_offload_complete));
			responseIntent.putExtra("response", responseAsString);
			sendBroadcast(responseIntent);
		}
	}
	public void onDestroy()	{
		super.onDestroy();
		unregisterReceiver(mDoOffloadReceiver);
		unregisterReceiver(mOffloadLogReceiver);
		unregisterReceiver(mOffloadReceiver);
		unregisterReceiver(mBatteryReceiver);
	}
}
