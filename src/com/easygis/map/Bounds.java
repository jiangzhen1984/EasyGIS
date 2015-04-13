package com.easygis.map;

import java.text.DecimalFormat;

/**
 * Definition of bounds.
 * 
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
	
	
	public Bounds(Bounds copy) {
		this.left = copy.left;
		this.top = copy.top;
		this.right = copy.right;
		this.bottom = copy.bottom;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(bottom);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(left);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(right);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(top);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bounds other = (Bounds) obj;
		if (Double.doubleToLongBits(bottom) != Double
				.doubleToLongBits(other.bottom))
			return false;
		if (Double.doubleToLongBits(left) != Double
				.doubleToLongBits(other.left))
			return false;
		if (Double.doubleToLongBits(right) != Double
				.doubleToLongBits(other.right))
			return false;
		if (Double.doubleToLongBits(top) != Double.doubleToLongBits(other.top))
			return false;
		return true;
	}

	public String toString() {
		return "[ " + this.left + "," + this.top + " -  " + this.right + ","
				+ this.bottom + "]";
	}
}
