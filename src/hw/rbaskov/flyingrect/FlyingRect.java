package hw.rbaskov.flyingrect;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class FlyingRect extends Activity {

	private GestureDetector gestureDetector;
	private FlyingRectView flyingRectView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		flyingRectView = (FlyingRectView) findViewById(R.id.flyingRectView);

		if (savedInstanceState != null) {
			flyingRectView.restartRect(savedInstanceState);
		}
		gestureDetector = new GestureDetector(this, gestureListener);
		Log.i(">>>>> onCreate", "onCreate");
				
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.i(">>>>> onSaveInstanceState", "onSaveInstanceState");
		super.onSaveInstanceState(outState);
		flyingRectView.onSaveInstanceState(outState);
	}

	
	@Override
	public void onPause() {
		Log.i(">>>>> onPause", "onPause");
		super.onPause();
		flyingRectView.stopFly();
	}

	@Override
	protected void onDestroy() {
		Log.i(">>>>> onDestroy", "onDestroy");
		super.onDestroy();
	}
	
	@Override
	protected void onStop() {
		Log.i(">>>>> onStop", "onStop");
		super.onStop();
	}
	
	@Override
	protected void onStart() {
		Log.i(">>>>> onStart", "onStart");
		super.onStart();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction();

		if (action == MotionEvent.ACTION_DOWN
				|| action == MotionEvent.ACTION_MOVE) {
			flyingRectView.deriveThrowAngle(event);
		}

		return gestureDetector.onTouchEvent(event);
	}

	SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			flyingRectView.throwRect(e);
			return true;
		}

		public void onLongPress(MotionEvent e) {
			finish();
		};
	};
}
