package com.testcase;

import java.text.DecimalFormat;

import junit.framework.TestCase;

import com.easygis.map.Bounds;
import com.easygis.util.CoordinatorTranslation;

public class CoordinatorTranslationTest extends TestCase {

	private CoordinatorTranslation ct;
	protected void setUp() throws Exception {
		super.setUp();
		ct = new CoordinatorTranslation();
	}

	

	public void testTranslateLatLonToMeters() {
	
	}
	

	public void testTranslateMetersToLatLon() {
		double x = -20037508.3427892D;
		double y = -20037508.3427892D;
		double[] latlng = ct.translateMetersToLatLon(x, y);
		assertTrue((int)latlng[0] == -85);
		assertTrue((int)Math.rint(latlng[1]) == -180);
		
		double[] latlng1 = ct.translateMetersToLatLon(0, 0);
		assertTrue((int)latlng1[0] == 0);
		assertTrue((int)latlng1[1] == 0);

	}
	

	public void testTrasnlatePixelsToMeters() {
		double[] m1 = ct.trasnlatePixelsToMeters(128, 1, 0);
		DecimalFormat df = new DecimalFormat("#.0000000");
		assertTrue(m1[0] == 0);
		assertTrue(df.format(m1[1]).equals(df.format(-20037508.3427892D + ct.resolution(0))));
		
	}

	public void testTranslateMetersToPixels() {
		double x = -20037508.3427892D + ct.resolution(0) * 3;
		double y = -20037508.3427892D + ct.resolution(0) * 3;
		double[]  pix = ct.translateMetersToPixels(x, y, 0);
		assertTrue((int)pix[0] == 3);
		assertTrue((int)pix[1] == 3);
		
	}

	public void testTranslatePixelsToTile() {
	}

	public void testTranslateMetersToTile() {
	}

	public void testTranslateTileBounds() {
		Bounds bounds = ct.translateTileBounds(0, 0, 0);
		double[] latlon = ct.translateMetersToLatLon(bounds.left, bounds.top);
		assertTrue((int)latlon[0] == -85);
		assertTrue((int)latlon[1] == -180);
		
		
		Bounds bound1s = ct.translateTileBounds(0, 1, 1);
		double[] latlon1 = ct.translateMetersToLatLon(bound1s.left, bound1s.top);
		assertTrue((int)latlon1[0] == 0);
		assertTrue((int)latlon1[1] == -180);
		
		Bounds bound2s = ct.translateTileBounds(1, 0, 1);
		double[] latlon2 = ct.translateMetersToLatLon(bound2s.left, bound2s.top);
		assertTrue((int)latlon2[0] == -85);
		assertTrue((int)latlon2[1] == 0);
	}

	public void testTransalteTileToLatLonBounds() {
		Bounds bounds = ct.transalteTileToLatLonBounds(0, 0, 0);
		assertTrue((int)bounds.left == -85);
		assertTrue((int)bounds.right == 85);
		assertTrue((int)bounds.top == -180);
		assertTrue((int)bounds.bottom == 180);
		
		Bounds bounds1 = ct.transalteTileToLatLonBounds(0, 0, 1);
		assertTrue((int)bounds1.left == -85);
		assertTrue((int)bounds1.right == 0);
		assertTrue((int)bounds1.top == -180);
		assertTrue((int)bounds1.bottom == 0);
		
		Bounds bounds2 = ct.transalteTileToLatLonBounds(0, 1, 1);
		assertTrue((int)bounds2.left == 0);
		assertTrue((int)bounds2.right == 85);
		assertTrue((int)bounds2.top == -180);
		assertTrue((int)bounds2.bottom == 0);
		
		
		Bounds bounds3 = ct.transalteTileToLatLonBounds(1, 0, 1);
		assertTrue((int)bounds3.left == -85);
		assertTrue((int)bounds3.right == 0);
		assertTrue((int)bounds3.top == 0);
		assertTrue((int)bounds3.bottom == 180);
		
	}

	public void testResolution() {
		double d1 = ct.resolution(0);
		double d2 = ct.resolution(1);
		double d3 = ct.resolution(2);
		double d4 = ct.resolution(3);
		double d5 = ct.resolution(4);
		
		 DecimalFormat df = new DecimalFormat("#.0000000");
		
		assertTrue(df.format(d1) .equals("156543.0339280"));
		assertTrue(df.format(d2) .equals(df.format(78271.5169639994D)));
		assertTrue(df.format(d3) .equals(df.format(39135.75848200009D)));
		assertTrue(df.format(d4) .equals(df.format(19567.87924099992)));
		assertTrue(df.format(d5) .equals(df.format(9783.93962049996)));
		
	}

}
