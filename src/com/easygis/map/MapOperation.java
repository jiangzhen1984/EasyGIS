package com.easygis.map;

/**
 * <ul>Define map operation API.</ul>
 * 
 * @see EMap
 * 
 * @author jiangzhen
 *
 */
public interface MapOperation {
	
	/**
	 * Zooms in one level from current map resolution with animation.<br>
	 * If current level is maximal level, then ignore this.
	 */
	public void zoomIn();
	
	
	/**
	 * Zooms out one level from current map resolution with animation.
	 * <br>
	 * If current level is minimal level, then ignore this.
	 */
	public void zoomOut();
	
	
	/**
	 * update  scale base on current scale by parameters.<br>
	 * Will automatically change level if possible. 
	 * @param scale
	 */
	public void updateScale(float scale);
	
	
	/**
	 * Update scale at specific point
	 * @param scale
	 * @param lat
	 * @param lng
	 */
	public void updateScaleAtLatLng(float scale, double lat, double lng);
	
	/**
	 * Update scale at specific point
	 * @param scale
	 * @param mx
	 * @param my
	 */
	public void updateScaleAtMeters(float scale, double mx, double my);
	
	
	/**
	 * Update current map bounds with current zoom
	 * @param bounds
	 */
	public void updateBounds(Bounds bounds);
	
	
	
	/**
	 * Update current map bounds
	 * @param bounds
	 * @param level
	 */
	public void updateBounds(Bounds bounds, int level);
	
	
	/**
	 * Set current map center point with current zoom level.
	 * @param lat latitude
	 * @param lng longitude
	 */
	public void centerAt(double lat, double lng);
	
	
	/**
	 * Set current map center point with current zoom level.<br>
	 * @param lat latitude
	 * @param lng longitude
	 * @param level
	 */
	public void centerAt(double lat, double lng, int level);
	
	/**
	 * Set current map center point with current zoom level.
	 * @param mx x of meters
	 * @param my y of meters
	 */
	public void centerAtMeters(double mx, double my);
	
	/**
	 * Set current map center point with current zoom level.
	 * @param mx x of meters
	 * @param my y of meters
	 * @param zoom level
	 */
	public void centerAtMeters(double mx, double my, int level);
	
	
	
	/**
	 * Translate offset according coordination unit.<br>
	 * If offset out of maximal bounds, will ignore.
	 * @param offsetX   offset of X
	 * @param offsetY   offset of Y
	 */
	public void translate(double offsetX, double offsetY, CoordinationUnit unit);

}
