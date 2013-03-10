
package com.networks.ucla.facedetection;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;


public class FaceDetectionActivity extends Activity {
	public static final String APP_NAME = "Face Detection";
	public static final String FUNCTION_NAME = "Test";
	public static final String SERVER_URL = 
			"http://ec2-50-19-179-123.compute-1.amazonaws.com:8080/SEAM/FaceDetector";
	public static final String TAG = FaceDetectionActivity.class.getSimpleName();
	
	public static final String KEY_APP_NAME = "app_name";
	public static final String KEY_FUNCTION_NAME = "function_name";
	public static final String KEY_DATA_SIZE = "data_size";
	public static final String KEY_SERVER_URL = "server_url";
	public static final String KEY_DECISION = "decision";
	public static final String KEY_START_TIME = "start_time";
	public static final String KEY_END_TIME = "end_time";
	
	FaceDetectionTask mTask;
	private static int mCount = 0;
	boolean mShouldOffload = false;
	long mStartTime;
	LinkedHashMap<Integer, Long> mBatteryLevelMap = new LinkedHashMap<Integer, Long>();
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        /* Register Receiver. */
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.intent_decision));
        filter.addAction(getString(R.string.intent_offload_complete));
        
        registerReceiver(mOffloadReceiver, filter);
        shouldOffload();
    }
	
    public void shouldOffload() {
        /* 
         * Send app's state to CodeOffloading engine to 
         * determine if offloading is beneficial or not
         */
        Intent offloadIntent = new Intent();
        offloadIntent.setAction(getString(R.string.intent_should_offload));
        offloadIntent.putExtra(KEY_APP_NAME, APP_NAME);
        offloadIntent.putExtra(KEY_FUNCTION_NAME, FUNCTION_NAME);
        offloadIntent.putExtra(KEY_DATA_SIZE, 2000);
        offloadIntent.putExtra(KEY_SERVER_URL, SERVER_URL);
        Log.e(TAG, "Requesting offload decision");
        
    	sendBroadcast(offloadIntent);
    }
    
    @Override
	public void onDestroy() {
		super.onDestroy();
		if (mTask != null) {
		    mTask.cancel(true);
		}
		
		unregisterReceiver(mOffloadReceiver);
		
		/* Broadcast that we stop. */
	    Intent broadcastIntent = new Intent(); 
	    broadcastIntent.setAction(getString(R.string.intent_face_detect_stop)); 
	    sendBroadcast(broadcastIntent); 
	    
		mCount = 0;
	}

	private BroadcastReceiver mOffloadReceiver = new BroadcastReceiver() {
    	@Override
		public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		if (getString(R.string.intent_decision).equals(action)) {
	    		Log.e(TAG, "Received offload decision");
	    		Bundle extras = intent.getExtras(); 
				if (extras!=null) {
					mShouldOffload = extras.getBoolean(KEY_DECISION);
					Log.e(TAG, "decision: " + mShouldOffload + " - starting calculation");
					compute();
				}
    		}
    		else if (getString(R.string.intent_offload_complete).equals(action)) {
    	        /* Send intent to Engine with start and end times to log data. */
    			Log.e(TAG,"Offload Response received for iteration:" + mCount);
    			Intent broadcastLogIntent = new Intent(); 
    			broadcastLogIntent.setAction(getString(R.string.intent_log_to_memory));
    			broadcastLogIntent.putExtra(KEY_START_TIME, mStartTime);
    			broadcastLogIntent.putExtra(KEY_END_TIME, System.currentTimeMillis());
    			sendBroadcast(broadcastLogIntent);
    			/* setup UI with view. */
    			mCount++;
                View view = new MyView(getBaseContext(), R.drawable.test_1, false);
    			setContentView(view);
    	    	Toast.makeText(FaceDetectionActivity.this, 
    	    			"completed turn: " + mCount, Toast.LENGTH_SHORT).show();
    			if (mCount < 50000) {
    				shouldOffload();
    			}
    		}
		}
    };
    
	private void compute()  {
    	mStartTime = System.currentTimeMillis();
        if (!mShouldOffload) {	
        	Log.e(TAG, "Not offloading");
        	mTask = new FaceDetectionTask();
        	mTask.execute("");
        }
        else {
        	Log.e(TAG, "Offloading");
        	
        	/* Send intent to engine to start offloading. */
        	Intent broadcastDoOffloadIntent = new Intent(); 
            broadcastDoOffloadIntent.setAction(getString(R.string.intent_perform_offload));
            sendBroadcast(broadcastDoOffloadIntent);
        	//mTask = new FaceDetectionTask();	//Comment out these lines later
        	//mTask.execute("");				//Only to check if Offloading works properly
        }
    }
    
    private class FaceDetectionTask extends AsyncTask<String, Void, Integer> {
		 private View mView;
		@Override
		protected Integer doInBackground(String... params) {
			//int a = mCount % 3;
			int resId = R.drawable.test_1;
			/*switch (a) {
				case 1:
					resId = R.drawable.test_2;
					break;
				case 2:
					resId = R.drawable.test_3;
					break;
			}*/
			mView = new MyView(getBaseContext(), resId);
			
	        mCount++;
			return mCount;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			setContentView(mView);
			Intent broadcastLogIntent = new Intent(); 
			broadcastLogIntent.setAction(getString(R.string.intent_log_to_memory));
			broadcastLogIntent.putExtra(KEY_START_TIME, mStartTime);
			broadcastLogIntent.putExtra(KEY_END_TIME, System.currentTimeMillis());
			sendBroadcast(broadcastLogIntent);
	    	Toast.makeText(FaceDetectionActivity.this, 
	    			"completed turn: " + mCount, Toast.LENGTH_SHORT).show();
	    	Log.e(TAG, mCount + "");
			if (mCount < 50000) {
				shouldOffload();
			}
		}
	}
}
