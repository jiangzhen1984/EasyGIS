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
 *  What is the coordinate extent of Earth in EPSG:900913?<br>
 * <br>
 *       [-20037508.342789244, -20037508.342789244, 20037508.342789244, 20037508.342789244]
 *       Constant 20037508.342789244 comes from the circumference of the Earth in meters,
 *       which is 40 thousand kilometers, the coordinate origin is in the middle of extent.
 *       In fact you can calculate the constant as: 2 * math.pi * 6378137 / 2.0
 *       $ echo 180 85 | gdaltransform -s_srs EPSG:4326 -t_srs EPSG:900913
 *       Polar areas with abs(latitude) bigger then 85.05112878 are clipped off.<br>
 * 	<p>
 *     What are zoom level constants (pixels/meter) for pyramid with EPSG:900913?<br><br>
 * 
 *       whole region is on top of pyramid (zoom=0) covered by 256x256 pixels tile,
 *       every lower zoom level resolution is always divided by two
 *       initialResolution = 20037508.342789244 * 2 / 256 = 156543.03392804062
 *       </p>
 */
public class CoordinatorTranslation {
	
	private static final double  EQUATOR_R = 6378137D;

	/**
	 * Default tile size
	 */
	private int DEFAULT_TILE_SIZE = 256;

	/**
	 * Default resolution
	 */
	public double DEFAULT_INIT_RESOLUTION = 2D * Math.PI * EQUATOR_R
			/ DEFAULT_TILE_SIZE;

	/**
	 * 20037508.342789244
	 */
	private final double ORIGIN_SHIFT = 2D * Math.PI * EQUATOR_R / 2.0D;

	private int mTileSize;

	private double mResoultion;

	public CoordinatorTranslation() {
		mTileSize = DEFAULT_TILE_SIZE;
		mResoultion = DEFAULT_INIT_RESOLUTION;
	}

	public CoordinatorTranslation(int tileSize) {
		mTileSize = tileSize;
		mResoultion = 2D * Math.PI * EQUATOR_R / mTileSize;
	}

	/**
	 * Converts given lat/lon in WGS84 Datum to XY in Spherical Mercator
	 * EPSG:900913
	 * 
	 * @param lat
	 *            latitude
	 * @param lng
	 *            longitude
	 * @return x y of Meters
	 */
	public double[] translateLatLonToMeters(double lat, double lng) {
		double mx = lng * ORIGIN_SHIFT / 180.0D;
		double my = Math.log(Math.tan((90.0D + lat) * Math.PI / 360.0D))
				/ (Math.PI / 180.0D);

		my = my * ORIGIN_SHIFT / 180.0;
		return new double[] { mx, my };
	}

	/**
	 * Converts XY point from Spherical Mercator EPSG:900913 to lat/lon in WGS84
	 * Datum
	 * 
	 * @param mx
	 *            Meters x
	 * @param my
	 *            Meters y
	 * @return [latitude, longitude]
	 */
	public double[] translateMetersToLatLon(double mx, double my) {

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
	public double[] trasnlatePixelsToMeters(int px, int py, int zoom) {

		double res = resolution(zoom);
		double mx = px * res - ORIGIN_SHIFT;
		double my = py * res - ORIGIN_SHIFT;
		return new double[] { mx, my };
	}

	/**
	 * "Converts EPSG:900913 to pyramid pixel coordinates in given zoom level"
	 * 
	 * @return []{px, py} 
	 */
	public double[] translateMetersToPixels(double mx, double my, int zoom) {
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
	 * @return tx ty
	 */
	public int[] translatePixelsToTile(float px, float py) {

		int tx = (int) Math.ceil(px / (float) mTileSize) - 1;
		int ty = (int) Math.ceil(py / (float) mTileSize) - 1;
		return new int[] { tx, ty };
	}

	/**
	 * Returns tile for given Meters coordinates"
	 * 
	 * @param mx
	 * @param my
	 * @param zoom
	 */
	public int[] translateMetersToTile(double mx, double my, int zoom) {
		double[] pixels = translateMetersToPixels(mx, my, zoom);
		return translatePixelsToTile((float) pixels[0], (float) pixels[1]);
	}

	/**
	 * "Returns bounds of the given tile in EPSG:900913 coordinates"
	 * 
	 * @param tRow
	 * @param tCol
	 * @param zoom
	 * @return
	 */
	public Bounds translateTileBounds(int tRow, int tCol, int zoom) {
		double[] min = trasnlatePixelsToMeters(tRow * mTileSize,
				tCol * mTileSize, zoom);
		double[] max = trasnlatePixelsToMeters((tRow + 1) * mTileSize, (tCol + 1)
				* mTileSize, zoom);
		return new Bounds(min[0], min[1], max[0], max[1]);
	}

	/**
	 * "Returns a tile covering region in given pixel coordinates"
	 * 
	 * @param tRow
	 * @param tCol
	 * @param zoom
	 * @return
	 */
	public Bounds transalteTileToLatLonBounds(int tRow, int tCol, int zoom) {

		Bounds bounds = translateTileBounds(tRow, tCol, zoom);
		double[] min = translateMetersToLatLon(bounds.left, bounds.top);
		double[] max = translateMetersToLatLon(bounds.right, bounds.bottom);
		return new Bounds(min[0], min[1], max[0], max[1]);
	}

	/**
	 * "Resolution (meters/pixel) for given zoom level (measured at Equator)"
	 * 
	 * @return
	 */
	public double resolution(int zoom) {
		return mResoultion / Math.pow(2 , zoom);
	}
	
	/**
	 * 
	 * @param zoom
	 * @return
	 */
	public double scale(int zoom) {
		double res = resolution(zoom);
		return res * 96 / 0.0254;
	}
}
