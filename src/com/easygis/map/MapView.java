package com.easygis.map;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.easygis.util.CoordinatorTranslation;
import com.easygis.util.EGISLog;

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
			// Remove delayed callback for double tap
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
			if (event.getEventTime() - mLastTouchUpTime < ViewConfiguration
					.getDoubleTapTimeout()) {
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

	int downX = 0;
	int downY = 0;
	int lastX = 0;
	int lastY = 0;
	int offsetX = 0;
	int offsetY = 0;

	private void onTouchDown(MotionEvent event) {
		if (mCheckForLongPress == null) {
			mCheckForLongPress = new CheckForLongPress();
		}
		downX = (int) event.getX();
		downY = (int) event.getY();
		lastX = (int) event.getX();
		lastY = (int) event.getY();
		postDelayed(mCheckForLongPress, ViewConfiguration.getLongPressTimeout());
	}

	private void onTouchMove(MotionEvent event) {
		if (mOPMode == OPMode.DRAG) {
			offsetX = (int) event.getX() - downX;
			offsetY = (int) event.getY() - downY;

			int deltaX = (int) event.getX() - lastX;
			int deltaY = (int) event.getY() - lastY;
			mEMap.translate(deltaX, deltaY, CoordinationUnit.PIXEL);
			lastX = (int) event.getX();
			lastY = (int) event.getY();
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
			// Post delayed for double tap.
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
			// TODO fliing
		} else if (mOPMode == OPMode.DOUBLE_TAPS) {
			if (mPerformDoubleClick == null) {
				mPerformDoubleClick = new PerformDoubleClick();
			}
			if (!post(mPerformDoubleClick)) {
				performDoubleClick();
				updateMode(OPMode.NONE);
			}
			
			doDoubleTapZoomIn((int)event.getX(), (int)event.getY());
		}
		downX = 0;
		downY = 0;
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
	
	//04-09 18:12:54.189: I/EGIS(5756): new bounds:[ 3404810.987934119,-1.718059797359984E7 -  2.3442319330718026E7,2856910.369184077]

	
	private void doDoubleTapZoomIn(int x, int y) {
		if (mEMap.mZoom == mEMap.mMapInfo.mSupportedLevels.length - 1) {
			EGISLog.i("max level reached");
			return;
		}
		EGISLog.e(mEMap.mBounds.toString());
		CoordinatorTranslation translation = mEMap.getTranslation();
		double[] worldPx= translation.translateMetersToPixels(mEMap.mBounds.left, mEMap.mBounds.top, mEMap.mZoom);
		double[] meters = translation.trasnlatePixelsToMeters((int)(worldPx[0] + x), (int)(worldPx[1] + y), mEMap.mZoom);
		EGISLog.e(meters[0] +"   "+ meters[1]);
		double resoultion = translation.resolution(mEMap.mZoom + 1);
		int viewWidth = getWidth() / 2;
		int viewHeight = getHeight() / 2;
		Bounds bounds = new Bounds();
		bounds.left = meters[0] - viewWidth * resoultion;
		bounds.top = meters[1] - viewHeight * resoultion;
		
		bounds.right = meters[0] + viewWidth * resoultion;
		bounds.bottom = meters[1] + viewHeight * resoultion;
		EGISLog.e(bounds.toString());
		mEMap.updateBounds(bounds, getMap().mZoom + 1);
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
		public void updateScaleAtLatLng(float scale, double lat, double lng) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void updateScaleAtMeters(float scale, double mx, double my) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void updateBounds(Bounds bounds, int level) {
			this.mZoom = level;
			this.mResolution = mTranslation.resolution(level);
			this.mBounds = bounds;
			//TODO update resolution
			//TODO update scale
			
			int count = getChildCount();
			for (int i = 0; i < count; i++) {
				((Layer)getChildAt(i)).updateBounds(bounds, level);
			}
		}
		
		

		@Override
		public void updateBounds(Bounds bounds) {
			updateBounds(bounds, mZoom);
		}

		@Override
		public void centerAt(double lat, double lng) {
			// TODO Auto-generated method stub
		}
		

		@Override
		public void centerAtMeters(double lat, double lng) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void centerAt(double lat, double lng, int level) {
			// TODO Auto-generated method stub

		}

		@Override
		public void translate(double offsetX, double offsetY,
				CoordinationUnit unit) {
			EGISLog.i("trasnlateX:" +offsetX +" translateY:"+offsetY);
			double mx = 0;
			double my = 0;
			
			switch (unit) {
			case PIXEL:
				mx = this.mResolution * offsetX;
				my = this.mResolution * offsetY;
				break;
			case LAT_LNG:
				break;
			case METER:
				mx = offsetX;
				my = offsetY;
				break;
			}
			
			EGISLog.i("olad bounds:" +this.mBounds);
			if (this.mBounds.right - mx > this.mMapInfo.mFullExtent.right){
				double offsetM =  this.mBounds.right  - this.mMapInfo.mFullExtent.right;
				this.mBounds.right = this.mMapInfo.mFullExtent.right;
				this.mBounds.left -= offsetM;
			} else if (this.mBounds.left - mx  <  this.mMapInfo.mFullExtent.left){
				double offsetM =  this.mBounds.left  - this.mMapInfo.mFullExtent.left;
				this.mBounds.left = this.mMapInfo.mFullExtent.left;
				this.mBounds.right -= offsetM;
			} else 	{
				this.mBounds.left -= mx;
				this.mBounds.right -= mx;
			}
			
			if (this.mBounds.bottom - my >= this.mMapInfo.mFullExtent.bottom) {
				
				double offsetM = this.mBounds.bottom - this.mMapInfo.mFullExtent.bottom;
				this.mBounds.top -=  offsetM;
				this.mBounds.bottom = this.mMapInfo.mFullExtent.bottom;
				
			} else if( this.mBounds.top - my <= this.mMapInfo.mFullExtent.top) {
				double offsetM = this.mBounds.top - this.mMapInfo.mFullExtent.top;
				this.mBounds.top =  this.mMapInfo.mFullExtent.top;
				this.mBounds.bottom -= offsetM;
			} else {
				this.mBounds.top -= my;
				this.mBounds.bottom -= my;
			}
		
			
			EGISLog.i("new bounds:" +this.mBounds);
			Bounds newBounds = new Bounds(mBounds);
			updateBounds(newBounds);
		}
		
		

		@Override
		public void addLayer(Layer layer) {
			layer.updateBounds(mBounds);
			addView(layer, new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT));
		}

		@Override
		public void addLayer(Layer layer, int index) {
			layer.updateBounds(mBounds);
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
