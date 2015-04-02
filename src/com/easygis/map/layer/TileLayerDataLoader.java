package com.easygis.map.layer;

import com.easygis.map.Tile;

public interface TileLayerDataLoader extends LayerDataLoader {

	
	/**
	 * Get tile data by synchronization.<br>
	 * @param row  row of tile
	 * @param col  column of tile
	 * @param zoom  zoom level
	 * @return  tile data
	 */
	public Tile getTile(int row, int col, int zoom);
	
	
	/**
	 * Get tile data asynchronization.<br>
	 * return not null means data already loaded, then won't call callback. Otherwise 
	 * return null and call callback function when data is loaded. 
	 * @param row  row of tile
	 * @param col  column of tile
	 * @param zoom   zoom level
	 * @param callback  callback function
	 * @return  if not null, is cache data. Otherwise cache doesn't hold this tile data.
	 */
	public Tile getTileAsync(int row, int col, int zoom, TileDataLoaderCallback callback);
}
