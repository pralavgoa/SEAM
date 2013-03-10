package com.networks.ucla.facedetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.view.View;

public class MyView extends View {
	private int imageWidth, imageHeight;
	private int numberOfFace = 70;
	private FaceDetector myFaceDetect;
	private FaceDetector.Face[] myFace;
	float myEyesDistance;
	int numberOfFaceDetected;
	boolean mDontDetectFaces = false;
	Bitmap myBitmap;

	public MyView(Context context, int resId) {
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

	public MyView(Context context, int resId, boolean dontdetect) {
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
		numberOfFaceDetected = 55;
		mDontDetectFaces = true;
	}
	
	protected void onDrawFake(Canvas canvas) {
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
			//Face face = myFace[i];
			PointF myMidPoint = new PointF();
			//face.getMidPoint(myMidPoint);
			/* Translate the midpoint to new scale. */
			myMidPoint.x = canvasCenterX + (scaleW * (i*10 - imageCenterX));
			myMidPoint.y = canvasCenterY + (scaleH * (i*10 - imageCenterY));
			myEyesDistance = 20 * scaleW;
			
			canvas.drawRect((int) (myMidPoint.x - myEyesDistance),
					(int) (myMidPoint.y - myEyesDistance),
					(int) (myMidPoint.x + myEyesDistance),
					(int) (myMidPoint.y + myEyesDistance), myPaint);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mDontDetectFaces) {
			onDrawFake(canvas);
			return;
		}
		
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
