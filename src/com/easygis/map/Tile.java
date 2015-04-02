package com.easygis.map;

public final class Tile {

	public Bounds mBounds;

	public int mRow;

	public int mCol;

	public int mZoom;

	public double mResolution;

	public double mScale;

	public Object mTileData;
	
	/**
	 * Mark mTileData is dirty or recycled
	 */
	public boolean isDirty;
}
