package com.easygis.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * MapView for android layout.<br>
 * <p></p>
 * @author jiangzhen
 *
 */
public class MapView extends ViewGroup {

	public MapView(Context context) {
		super(context);
	}

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
	}

	
	
	public EMap getMap() {
		return mEMap;
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
		public double getScale() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Bounds getBounds() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public double[] getCenter() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int addLayer(Layer layer) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int addLayer(Layer layer, int index) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Layer getLayer(int index) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void removeLayer(int index) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeLayer(Layer layer) {
			// TODO Auto-generated method stub
			
		}
		
	};
}
