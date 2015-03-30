package com.easygis.util;

import com.easygis.map.Bounds;

/**
 * #############################################################################
 * ##<br>
 * $Id$<br>
 * 
 * Project: GDAL2Tiles, Google Summer of Code 2007 & 2008<br>
 * Global Map Tiles Classes<br>
 * Purpose: Convert a raster into TMS tiles, create KML SuperOverlay EPSG:4326,<br>
 * generate a simple HTML viewers based on Google Maps and OpenLayers<br>
 * Author: Klokan Petr Pridal, klokan at klokan dot cz<br>
 * Web: http://www.klokan.cz/projects/gdal2tiles/<br>
 * 
 * #############################################################################
 * ##<br>
 * Copyright (c) 2008 Klokan Petr Pridal. All rights reserved.<br>
 * <br>
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * </p>
 * <br>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.<br>
 * <br>
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * </p>
 * #############################################################################
 * #<br>
 * 
 * GlobalMercator (based on EPSG:900913 = EPSG:3785)<br>
 * 
 * &nbsp; &nbsp; &nbsp; &nbsp; Created by Klokan Petr Pridal on 2008-07-03.<br>
 * Class is available under the open-source GDAL license (www.gdal.org).<br>
 * 
 * 
 */
public class CoordinatorTranslation {

	/**
	 * Default tile size
	 */
	private int DEFAULT_TILE_SIZE = 256;

	/**
	 * Default resolution
	 */
	public double DEFAULT_INIT_RESOLUTION = 2 * Math.PI * 6378137
			/ DEFAULT_TILE_SIZE;

	/**
	 * 20037508.342789244
	 */
	private final double ORIGIN_SHIFT = 2 * Math.PI * 6378137 / 2.0F;

	private int mTileSize;

	private double mResoultion;

	public CoordinatorTranslation() {
		mTileSize = DEFAULT_TILE_SIZE;
		mResoultion = DEFAULT_INIT_RESOLUTION;
	}

	public CoordinatorTranslation(int tileSize) {
		mTileSize = tileSize;
		mResoultion = 2 * Math.PI * 6378137 / mTileSize;
	}

	/**
	 * Converts given lat/lon in WGS84 Datum to XY in Spherical Mercator
	 * EPSG:900913
	 * 
	 * @param lat
	 *            latitude
	 * @param lng
	 *            longitude
	 * @return x y of Mercator
	 */
	public double[] translateLatLonToMercator(double lat, double lng) {
		double mx = lng * ORIGIN_SHIFT / 180.0F;
		double my = Math.log(Math.tan((90 + lat) * Math.PI / 360.0))
				/ (Math.PI / 180.0);

		my = my * ORIGIN_SHIFT / 180.0;
		return new double[] { mx, my };
	}

	/**
	 * Converts XY point from Spherical Mercator EPSG:900913 to lat/lon in WGS84
	 * Datum
	 * 
	 * @param mx
	 *            Mercator x
	 * @param my
	 *            Mercator y
	 * @return [latitude, longitude]
	 */
	public double[] translateMercatorToLatLon(double mx, double my) {

		double lon = (mx / ORIGIN_SHIFT) * 180.0;
		double lat = (my / ORIGIN_SHIFT) * 180.0;

		lat = 180
				/ Math.PI
				* (2 * Math.atan(Math.exp(lat * Math.PI / 180.0)) - Math.PI / 2.0);
		return new double[] { lat, lon };
	}

	/**
	 * "Converts pixel coordinates in given zoom level of pyramid to EPSG:900913"
	 * 
	 * @return
	 */
	public double[] trasnlatePixelsToMercator(int px, int py, int zoom) {

		double res = resolution(zoom);
		double mx = px * res - ORIGIN_SHIFT;
		double my = py * res - ORIGIN_SHIFT;
		return new double[] { mx, my };
	}

	/**
	 * "Converts EPSG:900913 to pyramid pixel coordinates in given zoom level"
	 * 
	 * @return
	 */
	public double[] translateMercatorToPixels(int mx, int my, int zoom) {
		double res = resolution(zoom);
		double px = (mx + ORIGIN_SHIFT) / res;
		double py = (my + ORIGIN_SHIFT) / res;
		return new double[] { px, py };
	}

	/**
	 * "Returns a tile covering region in given pixel coordinates"
	 * 
	 * @param px
	 * @param py
	 * @return int[row, col]
	 */
	public int[] translatePixelsToTile(int px, int py) {

		int tx = (int) Math.ceil(px / (float) mTileSize) - 1;
		int ty = (int) Math.ceil(py / (float) mTileSize) - 1;
		return new int[] { tx, ty };
	}

	/**
	 * "Returns tile for given mercator coordinates"
	 * 
	 * @param mx
	 * @param my
	 * @param zoom
	 */
	public int[] translateMercatorToTile(int mx, int my, int zoom) {
		double[] pixels = translateMercatorToPixels(mx, my, zoom);
		return translatePixelsToTile((int) pixels[0], (int) pixels[1]);
	}

	/**
	 * "Returns bounds of the given tile in EPSG:900913 coordinates"
	 * 
	 * @param tx
	 * @param ty
	 * @param zoom
	 * @return
	 */
	public Bounds translateTileBounds(int tx, int ty, int zoom) {
		double[] min = trasnlatePixelsToMercator(tx * mTileSize,
				ty * mTileSize, zoom);
		double[] max = trasnlatePixelsToMercator((tx + 1) * mTileSize, (ty + 1)
				* mTileSize, zoom);
		return new Bounds(min[0], min[1], max[0], max[1]);
	}

	/**
	 * "Returns a tile covering region in given pixel coordinates"
	 * 
	 * @param tx
	 * @param ty
	 * @param zoom
	 * @return
	 */
	public Bounds TileLatLonBounds(int tx, int ty, int zoom) {

		Bounds bounds = translateTileBounds(tx, ty, zoom);
		double[] min = translateMercatorToLatLon(bounds.left, bounds.top);
		double[] max = translateMercatorToLatLon(bounds.right, bounds.bottom);
		return new Bounds(min[0], min[1], max[0], max[1]);
	}

	/**
	 * "Resolution (meters/pixel) for given zoom level (measured at Equator)"
	 * 
	 * @return
	 */
	public double resolution(int zoom) {
		return mResoultion / (2 ^ zoom);
	}
}