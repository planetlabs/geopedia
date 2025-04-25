package com.sinergise.gwt.util;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayNumber;


public class UtilGwtImplEdge extends UtilGwtImplOld {
	private JsArrayNumber tmp_d;
	private JsArrayInteger tmp_i;
	
	public UtilGwtImplEdge() {
	}

	@Override
	protected void init() {
		tmp_d = createFloat64(1);
		tmp_i = createInt32(tmp_d);
	}
	private static native JsArrayNumber createFloat64(int size) /*-{
		return new Float64Array(size);
	}-*/;
	
	private static native JsArrayInteger createInt32(JavaScriptObject refArr) /*-{
		return new Int32Array(refArr.buffer);
	}-*/;
	
	private int getLowInt() {
		return tmp_i.get(0);
	}
	
	private void setLowInt(int val) {
		tmp_i.set(0, val);
	}

	private int getHighInt() {
		return tmp_i.get(1);
	}
	
	private void setHighInt(int val) {
		tmp_i.set(1, val);
	}

	private double getDbl() {
		return tmp_d.get(0);
	}

	private void setDbl(double x) {
		tmp_d.set(0, x);
	}
		
	@Override
	public double floorPow2(double x) {
		setDbl(x);
		setLowInt(0);
		setHighInt(getHighInt() & 0xfff00000);
		return getDbl();
	}
	
	@Override
	public double ceilPow2(double x) {
		setDbl(x);
		final int i1 = getHighInt();
		if ((getLowInt() == 0) && ((i1 & 0x000fffff) == 0)) {
			return x;
		}
		setLowInt(0);
		setHighInt(i1 & 0xfff00000);
		return 2 * getDbl();
	}
	
	@Override
	public int hashCode(double x) {
		setDbl(x);
		return getLowInt() ^ getHighInt();
	}
}
