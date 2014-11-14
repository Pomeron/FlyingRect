package hw.rbaskov.flyingrect;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

public class FlyingRectView extends SurfaceView implements
		SurfaceHolder.Callback {
	private FlyingRectThread flyingRectThread;
	private Activity activity;

	private double framePerSecond;

	private Rect flyingRect;
	private int flyingRectVelocityX;
	private int flyingRectVelocityY;
	private boolean flyingRectOnScreen = true;

	private int flyingRectRadius;
	private int flyingRectSpeed;
	private int screenWidth;
	private int screenHeight;

	private Paint flyingRectPaint;
	private Paint backgroundPaint;
	private Paint textPaint;

	private boolean wasPaused = false;
	private int orientation;
	private long delayUpdate = 0;
	private int statusBarHeight;
	private int titleBarHeight;

	public FlyingRectView(Context context, AttributeSet attrs) {
		super(context, attrs);
		activity = (Activity) context;
		getHolder().addCallback(this);

		flyingRect = new Rect();
		flyingRectPaint = new Paint();
		flyingRectPaint.setColor(Color.MAGENTA);
		backgroundPaint = new Paint();
		textPaint = new Paint();
		orientation = getScreenOrientation();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		int scale;
		screenWidth = w;
		screenHeight = h;
		if (screenWidth > screenHeight) {
			scale = screenHeight;
		} else {
			scale = screenWidth;
		}
		flyingRectRadius = (int) activity.getResources().getDimension(
				R.dimen.flyingRectRadius);
		flyingRectSpeed = 25 * flyingRectRadius;
		System.out.println(">>>>" + flyingRectRadius);
		Log.d("surface", "bla bl bl");
		backgroundPaint.setColor(Color.WHITE);
		textPaint.setTextSize(scale / 20);
		textPaint.setAntiAlias(true);
		getBarsHeights();
		startFly();
	}

	public void startFly() {
		if (!wasPaused)
			flyingRectOnScreen = false;
		framePerSecond = 0.0;

	}

	private void updatePositions(double elapsedTimeMS) {
		double interval = elapsedTimeMS / 1000.0;
		if (delayUpdate > 0 && delayUpdate != 0) {
			delayUpdate -= elapsedTimeMS;
			if (delayUpdate < 0)
				delayUpdate = 0;
			return;
		}
		// if (onPause)
		// return;

		if (flyingRectOnScreen) {
			// update rect position
			flyingRect.left += interval * flyingRectVelocityX;
			flyingRect.right += interval * flyingRectVelocityX;
			flyingRect.top += interval * flyingRectVelocityY;
			flyingRect.bottom += interval * flyingRectVelocityY;

			// check for collisions with left and right walls
			if (flyingRect.right >= screenWidth) {
				flyingRectVelocityX = -flyingRectVelocityX;
				flyingRect.right = screenWidth - 1;
				flyingRect.left = screenWidth - 2 * flyingRectRadius;
			} else if (flyingRect.left <= 0) {
				flyingRectVelocityX = -flyingRectVelocityX;
				flyingRect.right = 2 * flyingRectRadius;
				flyingRect.left = 1;
			}

			// check for collisions with top and bottom walls
			if (flyingRect.top >= screenHeight) {
				flyingRectVelocityY = -flyingRectVelocityY;
				flyingRect.top = screenHeight - 1;
				flyingRect.bottom = screenHeight - 2 * flyingRectRadius;
			}

			else if (flyingRect.bottom <= 0) {
				flyingRectVelocityY = -flyingRectVelocityY;
				flyingRect.top = 2 * flyingRectRadius;
				flyingRect.bottom = 1;
			}
		}
		Log.i(">>>>> updatePositions", "centerX= " + flyingRect.centerX()
				+ " , centerY= " + flyingRect.centerY());
		Log.i(">>>>> updatePositions", "veloX= " + flyingRectVelocityX
				+ " , veloY= " + flyingRectVelocityY);

	}

	public void throwRect(MotionEvent event) {
		if (flyingRectOnScreen)
			return;

		double angle = deriveThrowAngle(event);

		// move the flyingRect
		flyingRect.left = 0;
		flyingRect.right = 2 * flyingRectRadius;
		flyingRect.top = screenHeight / 2 + flyingRectRadius;
		flyingRect.bottom = screenHeight / 2 - flyingRectRadius;
		// get the x component of the total velocity
		flyingRectVelocityX = (int) (flyingRectSpeed * Math.sin(angle));
		// get the y component of the total velocity
		flyingRectVelocityY = (int) (-flyingRectSpeed * Math.cos(angle));
		flyingRectOnScreen = true;

	}

	public double deriveThrowAngle(MotionEvent event) {
		// get the location of the touch in this view
		Point touchPoint = new Point((int) event.getX(), (int) event.getY());

		// compute the touch's distance from center of the screen
		// on the y-axis
		double centerMinusY = (screenHeight / 2 - touchPoint.y);

		double angle = 0; // initialize angle to 0

		// calculate the angle with the horizontal
		if (centerMinusY != 0) // prevent division by 0
			angle = Math.atan((double) touchPoint.x / centerMinusY);

		// if the touch is on the lower half of the screen
		if (touchPoint.y > screenHeight / 2)
			angle += Math.PI;

		return angle;
	}

	public void drawGraphicElements(Canvas canvas) {

		// clear the background
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(),
				backgroundPaint);

		canvas.drawText(
				getResources().getString(R.string.frame_per_seconds,
						framePerSecond), 30, 50, textPaint);

		if (flyingRectOnScreen)
			canvas.drawRect(flyingRect, flyingRectPaint);

	}

	public void stopFly() {
		Log.i(">>>>> stopFly", "centerX= " + flyingRect.centerX()
				+ " , centerX= " + flyingRect.centerY());
		if (flyingRectThread != null)
			flyingRectThread.setRunning(false);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		Log.i(">>>>> surfaceChanged", "centerX= " + flyingRect.centerX()
				+ " , centerX= " + flyingRect.centerY());
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		Log.i(">>>>> surfaceCreated", "centerX= " + flyingRect.centerX()
				+ " , centerX= " + flyingRect.centerY());
		flyingRectThread = new FlyingRectThread(holder);
		flyingRectThread.setRunning(true);
		flyingRectThread.start(); // start the game loop thread
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(">>>>> surfaceDestroyed", "centerX= " + flyingRect.centerX()
				+ " , centerX= " + flyingRect.centerY());
		boolean retry = true;
		flyingRectThread.setRunning(false);

		while (retry) {
			try {
				flyingRectThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}

	}

	// Thread subclass to control the game loop
	private class FlyingRectThread extends Thread {
		private SurfaceHolder surfaceHolder;
		private boolean threadIsRunning = true;

		public FlyingRectThread(SurfaceHolder holder) {
			surfaceHolder = holder;
			setName("FlyingRectThread");
		}

		public void setRunning(boolean running) {
			threadIsRunning = running;
		}

		@Override
		public void run() {
			Canvas canvas = null; // used for drawing
			long previousFrameTime = System.currentTimeMillis();
			while (threadIsRunning) {
				try {
					canvas = surfaceHolder.lockCanvas(null);
					synchronized (surfaceHolder) {
						long currentTime = System.currentTimeMillis();
						double elapsedTimeMS = currentTime - previousFrameTime;
						framePerSecond = 1 / (elapsedTimeMS / 1000);
						updatePositions(elapsedTimeMS);
						drawGraphicElements(canvas);
						previousFrameTime = currentTime;
					}
				} finally {
					if (canvas != null)
						surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		return super.onSaveInstanceState();
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("flyingRectOnScreen", flyingRectOnScreen);

		Log.i(">>>>> onSaveInstanceState View",
				"centerX= " + flyingRect.centerX() + " , centerX= "
						+ flyingRect.centerY());
		outState.putInt("rect.centerX", flyingRect.centerX());
		outState.putInt("rect.centerY", flyingRect.centerY());
		outState.putInt("rect.veloX", flyingRectVelocityX);
		outState.putInt("rect.veloY", flyingRectVelocityY);
		outState.putInt("oldwidth", screenWidth);
		outState.putInt("oldheight", screenHeight);
		outState.putInt("orientation", orientation);
		outState.putInt("rect.radius", flyingRectRadius);
		outState.putInt("statusBarHeight", statusBarHeight);
		outState.putInt("titleBarHeight", titleBarHeight);

	}

	public void restartRect(Bundle savedInstanceState) {
		flyingRectOnScreen = savedInstanceState
				.getBoolean("flyingRectOnScreen");
		if (!flyingRectOnScreen) {
			return;
		}
		System.out.println(">>>> restartRect");
		int[] oldCenterCoords = new int[] {
				savedInstanceState.getInt("rect.centerX"),
				savedInstanceState.getInt("rect.centerY") };
		screenHeight = savedInstanceState.getInt("oldheight");
		screenWidth = savedInstanceState.getInt("oldwidth");
		statusBarHeight = savedInstanceState.getInt("statusBarHeight");
		titleBarHeight = savedInstanceState.getInt("titleBarHeight");
		flyingRectRadius = savedInstanceState.getInt("rect.radius");
		int[] centerCoords = getNewCoords(oldCenterCoords,
				savedInstanceState.getInt("orientation"),
				getScreenOrientation());
		System.out.println(centerCoords[0] + "," + centerCoords[1] + "old:"
				+ oldCenterCoords[0] + "," + oldCenterCoords[1]);
		flyingRect.right = centerCoords[0] + flyingRectRadius;
		flyingRect.left = centerCoords[0] - flyingRectRadius;
		flyingRect.top = centerCoords[1] + flyingRectRadius;
		flyingRect.bottom = centerCoords[1] - flyingRectRadius;
		flyingRectVelocityX = centerCoords[2]
				* savedInstanceState.getInt("rect.veloY");
		flyingRectVelocityY = centerCoords[3]
				* savedInstanceState.getInt("rect.veloX");
		wasPaused = true;
		delayUpdate = 1500;

	}

	private int[] getNewCoords(int[] oldCenterCoords, int oldScreenOrientation,
			int screenOrientation) {

		int newX = 0, newY = 0, veloXSign = 1, veloYSign = 1;
		if ((oldScreenOrientation == Surface.ROTATION_90 && screenOrientation == Surface.ROTATION_0)
				|| (oldScreenOrientation == Surface.ROTATION_0 && screenOrientation == Surface.ROTATION_270)) {

			newX = screenHeight - oldCenterCoords[1];
			newY = oldCenterCoords[0];
			;// - (titleBarHeight + statusBarHeight);
			veloXSign = -1;
		}

		if ((oldScreenOrientation == Surface.ROTATION_0 && screenOrientation == Surface.ROTATION_90)
				|| (oldScreenOrientation == Surface.ROTATION_270 && screenOrientation == Surface.ROTATION_0)) {

			newX = +oldCenterCoords[1];// titleBarHeight + statusBarHeight +
										// oldCenterCoords[1];
			newY = screenWidth - oldCenterCoords[0];
			// - (titleBarHeight + statusBarHeight);
			veloYSign = -1;
		}

		return new int[] { newX, newY, veloXSign, veloYSign };
	}

	private int getScreenOrientation() {

		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();

		return rotation;
	}

	public void getBarsHeights() {
		Rect rectgle = new Rect();
		Window window = activity.getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
		statusBarHeight = rectgle.top;
		int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT)
				.getTop();
		titleBarHeight = contentViewTop - statusBarHeight;

		Log.i("*** Jorgesys :: ", "StatusBar Height= " + statusBarHeight
				+ " , TitleBar Height = " + titleBarHeight
				+ " , Screen Height = " + screenHeight);
	}

}
