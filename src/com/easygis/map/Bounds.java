package com.easygis.map;

/**
 * Definition of bounds.
 * @author jiangzhen
 *
 */
public final class Bounds {
	
	public double left;
	public double top;
	public double right;
	public double bottom;

	public Bounds() {
	}

	public Bounds(double left, double top, double right, double bottom) {
		super();
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	
}
