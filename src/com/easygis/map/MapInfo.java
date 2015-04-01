package com.easygis.map;

public final class MapInfo {
	
	public double mMinimalScale;
	
	public double mMaximalScale;

	public Bounds mFullExtent;
	
	public Bounds mInitialExtent;
	
	public float mTileWidth;
	
	public float mTileHeight;
	
	public float mTileDPI;
	
	public ZoomInfo[] mSupportedLevels;
	
	public class ZoomInfo {
		public double mResoultion;
		public double mScale;
	}
}
