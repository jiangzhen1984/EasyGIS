package com.easygis.map;

public final class MapInfo {

	public double mMinimalScale;

	public double mMaximalScale;

	public Bounds mFullExtent;

	public Bounds mInitialExtent;

	public int mInitialZoom;

	public double mInitialResolution;

	public boolean isSupportTileData;

	public float mTileWidth;

	public float mTileHeight;

	public float mTileDPI;

	public String mTilePath;

	public String mTileExtension;

	public boolean isLocalTilePath;

	public ZoomInfo[] mSupportedLevels;

	public static class ZoomInfo {
		public double mResoultion;
		public double mScale;
		public int mEndCol;
		public int mEndRow;

		public ZoomInfo(double mResoultion, double mScale) {
			this.mResoultion = mResoultion;
			this.mScale = mScale;
		}

		public ZoomInfo(double mResoultion, double mScale,
				int mEndRow, int mEndCol) {
			super();
			this.mResoultion = mResoultion;
			this.mScale = mScale;
			this.mEndCol = mEndCol;
			this.mEndRow = mEndRow;
		}
		
		

	}

}
