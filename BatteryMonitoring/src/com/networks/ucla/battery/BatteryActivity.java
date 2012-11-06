package com.networks.ucla.battery;

import java.util.LinkedHashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class BatteryActivity extends Activity {
	private TextView mTextView;
	private ToggleButton mToogleButton;
	private int mFirstLevel = 0;
	private int mSecondLevel = 0;
	FaceDetectionTask mTask;
	private static int mCount = 0;
	LinkedHashMap<Integer, Long> mBatteryLevelMap = new LinkedHashMap<Integer, Long>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
        mTextView = (TextView) findViewById(R.id.output);
//        mToogleButton = (ToggleButton) findViewById(R.id.toogle);
//        mToogleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                	
//                } else {
//                    // The toggle is disabled
//                }
//            }
//        });   

        registerReceiver(mBroadrxr, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        mTask = new FaceDetectionTask();
    }
    
	private class myView extends View {

		private int imageWidth, imageHeight;
		private int numberOfFace = 40;
		private FaceDetector myFaceDetect;
		private FaceDetector.Face[] myFace;
		float myEyesDistance;
		int numberOfFaceDetected;

		Bitmap myBitmap;

		public myView(Context context) {
			super(context);
			BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
			BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;
			myBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.test_5, BitmapFactoryOptionsbfo);
			imageWidth = myBitmap.getWidth();
			imageHeight = myBitmap.getHeight();
			myFace = new FaceDetector.Face[numberOfFace];
			myFaceDetect = new FaceDetector(imageWidth, imageHeight,
					numberOfFace);
			numberOfFaceDetected = myFaceDetect.findFaces(myBitmap, myFace);

		}

		@Override
		protected void onDraw(Canvas canvas) {
			int canvasWidth = canvas.getWidth();
			int canvasHeight = canvas.getHeight();
			Bitmap map = Bitmap.createScaledBitmap(myBitmap, 
					canvasWidth, canvasHeight, true);
			canvas.drawBitmap(map, 0, 0, null);
			
			float scaleW = ((float) canvasWidth)/ ((float) imageWidth);
			float scaleH = ((float) canvasHeight) /((float) imageHeight);
			
			int canvasCenterX = canvasWidth/2;
			int canvasCenterY = canvasHeight/2;
			
			int imageCenterX = imageWidth/2;
			int imageCenterY = imageHeight/2;
			
			Paint myPaint = new Paint();
			myPaint.setColor(Color.GREEN);
			myPaint.setStyle(Paint.Style.STROKE);
			myPaint.setStrokeWidth(3);

			for (int i = 0; i < numberOfFaceDetected; i++) {
				Face face = myFace[i];
				PointF myMidPoint = new PointF();
				face.getMidPoint(myMidPoint);
				/* Translate the midpoint to new scale. */
				myMidPoint.x = canvasCenterX + (scaleW * (myMidPoint.x - imageCenterX));
				myMidPoint.y = canvasCenterY + (scaleH * (myMidPoint.y - imageCenterY));
				myEyesDistance = face.eyesDistance() * scaleW;
				
				canvas.drawRect((int) (myMidPoint.x - myEyesDistance),
						(int) (myMidPoint.y - myEyesDistance),
						(int) (myMidPoint.x + myEyesDistance),
						(int) (myMidPoint.y + myEyesDistance), myPaint);
			}
		}
	}
	
	 private class FaceDetectionTask extends AsyncTask<String, Void, Integer> {
		 private View mView;
		@Override
		protected Integer doInBackground(String... params) {
			mView = new myView(getBaseContext());
	        mCount++;
			return mCount;
		}
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			setContentView(mView);
	    	Toast.makeText(getBaseContext(), "competed turn: " + mCount, Toast.LENGTH_SHORT);
	    	Log.e("BatteryActivity", mCount + "");
			if (mCount < 50) {
				mTask = new FaceDetectionTask();
				mTask.execute("");
			}
			else {
				setContentView(R.layout.activity_battery);
		        mTextView = (TextView) findViewById(R.id.output);
		    	 for (Integer i : mBatteryLevelMap.keySet()) {
		    		 long time = mBatteryLevelMap.get(i);
		    		 mTextView.append("\nLevel="+i+"\t"+time);
		    	 }	
			}
		}
	}

	private BroadcastReceiver mBroadrxr = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
//			      int temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
//			      int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
//			      int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
			      int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			      long time = System.currentTimeMillis();
			      
			      if (!mBatteryLevelMap.containsKey(level)) {
			    	  if (mFirstLevel == 0) {
			    		  mFirstLevel = level;
			    	  }
			    	  else if (mSecondLevel == 0) {
			    		  mSecondLevel = level;
			    	  }
			    	  mBatteryLevelMap.put(level, time);
			    	  TextView tv = (TextView) findViewById(R.id.output);
			    	  if (tv != null) {
			    		  tv.append("\nLevel="+level+"\t"+time);
			    	  }
			    	  
			    	  if (mSecondLevel - 1 == level) {
			    		  mTask.execute("");
			    	  }
			      }
			}
		}
	};
}
