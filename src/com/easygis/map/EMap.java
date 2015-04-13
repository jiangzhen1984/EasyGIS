package com.easygis.map;

import com.easygis.util.CoordinatorTranslation;

/**
 * Map class definition.<br>
 * <ul>This class just define map attributes: scale bounds level resolution etc.</ul>
 * <ul>Also can use this object to update map data, like update center or level or scale by {@link MapOperation}.</ul>
 * 
 * @see MapOperation
 * @see MapView
 * @author jiangzhen
 *
 */
public abstract class EMap implements MapOperation {
	
	protected  MapInfo mMapInfo;
	
	protected Bounds mBounds;
	
	protected double mScale;
	
	protected int mZoom;
	
	protected double mResolution;
	
	protected CoordinatorTranslation mTranslation;
	
	/**
	 * Load initial map configuration information
	 * @param loader
	 * @return
	 */
	public boolean initMapInfo(MapInfoLoader loader) {
		if (loader == null) {
			throw new NullPointerException(" loader is null");
		}
		mMapInfo = loader.load();
		if (mMapInfo == null) {
			return false;
		}
		
		this.mBounds = mMapInfo.mInitialExtent;
		this.mZoom = mMapInfo.mInitialZoom;
		this.mResolution = mMapInfo.mInitialResolution;
		this.mTranslation = new CoordinatorTranslation((int)mMapInfo.mTileWidth);
		return true;
	}
	
	
	/**
	 * Load initial map configuration information
	 * @param mapInfo
	 * @return
	 */
	public boolean initMapInfo(MapInfo mapInfo) {
		mMapInfo = mapInfo;
		if (mMapInfo == null) {
			return false;
		}
		this.mBounds = mMapInfo.mInitialExtent;
		this.mZoom = mMapInfo.mInitialZoom;
		this.mResolution = mMapInfo.mInitialResolution;
		this.mTranslation = new CoordinatorTranslation((int)mMapInfo.mTileWidth);
		return true;
	}
	
	/**
	 * Get current map translation utility
	 * @return
	 */
	public CoordinatorTranslation getTranslation() {
		return this.mTranslation;
	}
	
	/**
	 * Get map configuration information
	 * @return
	 */
	public MapInfo getMapInfo() {
		return this.mMapInfo;
	}


	/**
	 * Get current map scale
	 * @return current scale
	 */
	public double getScale() {
		return mScale;
	}
	
	
	/**
	 * Get current map bounds
	 * @return bounds of map
	 */
	public Bounds getBounds() {
		return mBounds;
	}
	
	
	/**
	 * Get current center of map
	 * @return array[x,y]
	 */
	public double[] getCenter() {
		return new double[]{(mBounds.right - mBounds.left) / 2, (mBounds.bottom - mBounds.top) / 2};
	}
	
	
	public int getZoom() {
		return mZoom;
	}
	
	/**
	 * Add new layer to current map.<br>
	 *  Layer index from start 0.
	 * @param layer
	 */
	public abstract void addLayer(Layer layer);
	
	/**
	 *  Add new layer to current map.<br>
	 *  Layer index from start 0. If index grater than maximal index, will throw Exception.
	 * @param layer  map layer
	 * @param index given index
	 */
	public abstract void addLayer(Layer layer, int index);
	
	/**
	 * Get layer according to index.
	 * @param index index of layer
	 * @return null if no layer at specific index
	 */
	public abstract Layer getLayer(int index);
	
	/**
	 * Remove layer according to index.
	 * @param index index of removed layer
	 */
	public abstract void removeLayer(int index);
	
	
	/**
	 * Remove layer according to layer object.
	 * @param layer
	 */
	public abstract void removeLayer(Layer layer);
	
}
