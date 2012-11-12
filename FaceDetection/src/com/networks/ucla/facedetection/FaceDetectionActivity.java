
package com.networks.ucla.facedetection;

import java.util.LinkedHashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class FaceDetectionActivity extends Activity {
	FaceDetectionTask mTask;
	private static int mCount = 0;
	LinkedHashMap<Integer, Long> mBatteryLevelMap = new LinkedHashMap<Integer, Long>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Broadcast that we started. */
        Intent broadcastIntent = new Intent(); 
        broadcastIntent.setAction("com.networks.ucla.FACEDETECT_START"); 
        sendBroadcast(broadcastIntent); 
        
        mTask = new FaceDetectionTask();
        mTask.execute("");
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTask.cancel(true);
		
		/* Broadcast that we stop. */
        Intent broadcastIntent = new Intent(); 
        broadcastIntent.setAction("com.networks.ucla.FACEDETECT_STOP"); 
        sendBroadcast(broadcastIntent); 
        
		mCount = 0;
	}

	private class myView extends View {
		private int imageWidth, imageHeight;
		private int numberOfFace = 70;
		private FaceDetector myFaceDetect;
		private FaceDetector.Face[] myFace;
		float myEyesDistance;
		int numberOfFaceDetected;

		Bitmap myBitmap;

		public myView(Context context, int resId) {
			super(context);
			BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
			BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;
			myBitmap = BitmapFactory.decodeResource(getResources(),
					resId, BitmapFactoryOptionsbfo);
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
			int a = mCount % 3;
			int resId = R.drawable.test_1;
			switch (a) {
				case 1:
					resId = R.drawable.test_2;
					break;
				case 2:
					resId = R.drawable.test_3;
					break;
			}
			mView = new myView(getBaseContext(), resId);
	        mCount++;
			return mCount;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			setContentView(mView);
	    	Toast.makeText(FaceDetectionActivity.this, 
	    			"completed turn: " + mCount, Toast.LENGTH_SHORT).show();
	    	Log.e("BatteryActivity", mCount + "");
			if (mCount < 5000) {
				mTask = new FaceDetectionTask();
				mTask.execute("");
			}
		}
	}
}
