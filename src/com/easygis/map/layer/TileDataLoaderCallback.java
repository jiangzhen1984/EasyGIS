package com.easygis.map.layer;

import com.easygis.map.Tile;

/**
 * 
 * @author 28851274
 *
 */
public interface TileDataLoaderCallback {

	/**
	 * 
	 * @param row
	 * @param col
	 * @param zoom
	 * @param tile
	 */
	public void tileLoadedNotification(int row, int col, int zoom, Tile tile);
}
