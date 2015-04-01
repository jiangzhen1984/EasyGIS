package com.easygis.map;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

/**
 * MapView for android layout.<br>
 * <p>
 * </p>
 * 
 * @author jiangzhen
 * 
 */
public class MapView extends ViewGroup implements OnTouchListener {
	
	private static final String TAG = "MapView";

	private VelocityTracker mVelocityTracker;

	private PerformClick mPerformClick;

	private PerformLongClick mPerformLongClick;
	
	private PerformDoubleClick mPerformDoubleClick;

	private CheckForLongPress mCheckForLongPress;
	
	private long mLastTouchUpTime = 0;

	private OPMode mOPMode = OPMode.NONE;

	public MapView(Context context) {
		super(context);
		initMapView();
	}

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initMapView();
	}

	public MapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initMapView();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed) {
			int count = getChildCount();
			for (int i = 0; i < count; i++) {
				getChildAt(i).layout(l + getPaddingLeft(), t + getPaddingTop(),
						r - getPaddingRight(), b - getPaddingBottom());
			}
		}
	}

	@Override
	public void setOnTouchListener(OnTouchListener l) {
		if (l != this) {
			throw new RuntimeException("Do not support this operation!");
		}
		super.setOnTouchListener(l);
	}

	private void initMapView() {
		setOnTouchListener(this);
	}

	@Override
	public boolean performClick() {
		Log.d(TAG, "clicked==============");
		return super.performClick();
	}

	@Override
	public boolean performLongClick() {
		Log.d(TAG, "long clicked==============");
		return true;
	}
	
	public boolean performDoubleClick() {
		Log.d(TAG, "double clicked==============");
		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		initVelocityTrackerIfNotExists();
		
		mVelocityTracker.addMovement(ev);

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_UP:
			recycleVelocityTracker();
			break;

		}
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int mask = event.getActionMasked();
		switch (mask & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			//Remove delayed callback for double tap
			removeCallbacks(mPerformClick);
			updateMode(OPMode.TAP);
			onTouchDown(event);
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			updateMode(OPMode.SCALE);
			break;
		case MotionEvent.ACTION_MOVE:
			if (event.getPointerCount() < 2) {
				updateMode(OPMode.DRAG);
			}
			onTouchMove(event);
			break;
		case MotionEvent.ACTION_UP:
			if (event.getEventTime() - mLastTouchUpTime < ViewConfiguration.getDoubleTapTimeout()) {
				updateMode(OPMode.DOUBLE_TAPS);
			}
			mLastTouchUpTime = event.getEventTime();
			onTouchUp(event);
			break;
		case MotionEvent.ACTION_POINTER_UP:
			if (event.getPointerCount() < 2) {
				updateMode(OPMode.DRAG);
			}
			break;
		}
		return true;
	}

	private void onTouchDown(MotionEvent event) {
		if (mCheckForLongPress == null) {
			mCheckForLongPress = new CheckForLongPress();
		}
		postDelayed(mCheckForLongPress, ViewConfiguration.getLongPressTimeout());
	}
	
	
	private void onTouchMove(MotionEvent event) {
		if (mOPMode == OPMode.DRAG) {
			Log.d(TAG, "draging2==============");
		} else if (mOPMode == OPMode.SCALE) {
			Log.d(TAG, "scaling==============");
		}
		
		removeCallbacks(mCheckForLongPress);
	}

	private void onTouchUp(MotionEvent event) {
		if (mOPMode == OPMode.TAP) {
			if (mPerformClick == null) {
				mPerformClick = new PerformClick();
			}
			//Post delayed for double tap.
			postDelayed(mPerformClick, ViewConfiguration.getDoubleTapTimeout());
		} else if (mOPMode == OPMode.LONG_PRESS) {
			if (mPerformLongClick == null) {
				mPerformLongClick = new PerformLongClick();
			}
			if (!post(mPerformLongClick)) {
				performLongClick();
				updateMode(OPMode.NONE);
			}
		} else if (mOPMode == OPMode.DRAG) {
			//TODO fliing
		} else if (mOPMode == OPMode.DOUBLE_TAPS) {
			if (mPerformDoubleClick == null) {
				mPerformDoubleClick = new PerformDoubleClick();
			}
			if (!post(mPerformDoubleClick)) {
				performDoubleClick();
				updateMode(OPMode.NONE);
			}
		}
	}
	
	
	
	private synchronized void updateMode(OPMode mode) {
		this.mOPMode = mode;
	}

	private void initVelocityTrackerIfNotExists() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
	}

	private void recycleVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	public EMap getMap() {
		return mEMap;
	}

	class CheckForLongPress implements Runnable {

		public void run() {
			updateMode(OPMode.LONG_PRESS);
		}

	}

	class PerformClick implements Runnable {

		@Override
		public void run() {
			performClick();
			updateMode(OPMode.NONE);
		}

	}

	class PerformLongClick implements Runnable {

		@Override
		public void run() {
			performLongClick();
			updateMode(OPMode.NONE);
		}

	}
	
	class PerformDoubleClick implements Runnable {

		@Override
		public void run() {
			performDoubleClick();
			updateMode(OPMode.NONE);
		}

	}

	private EMap mEMap = new EMap() {

		@Override
		public void zoomIn() {
			// TODO Auto-generated method stub

		}

		@Override
		public void zoomOut() {
			// TODO Auto-generated method stub

		}

		@Override
		public void updateScale(float scale) {
			// TODO Auto-generated method stub

		}

		@Override
		public void updateBounds(Bounds bounds, int level) {
			// TODO Auto-generated method stub
		}

		@Override
		public void centerAt(double lat, double lng) {
			// TODO Auto-generated method stub

		}

		@Override
		public void centerAt(double lat, double lng, int level) {
			// TODO Auto-generated method stub

		}

		@Override
		public void translate(double offsetX, double offsetY,
				CoordinationUnit unit) {
			// TODO Auto-generated method stub

		}

		@Override
		public void addLayer(Layer layer) {
			addView(layer, new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT));
		}

		@Override
		public void addLayer(Layer layer, int index) {
			addView(layer, index, new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT));
		}

		@Override
		public Layer getLayer(int index) {
			return (Layer) getChildAt(index);
		}

		@Override
		public void removeLayer(int index) {
			removeViewAt(index);
		}

		@Override
		public void removeLayer(Layer layer) {
			removeView(layer);
		}

	};

	enum OPMode {
		NONE, DRAG, SCALE, SCROLLING, TAP, LONG_PRESS, DOUBLE_TAPS
	}
}
