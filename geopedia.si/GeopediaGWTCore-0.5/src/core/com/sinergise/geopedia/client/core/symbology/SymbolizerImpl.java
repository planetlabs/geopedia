package com.sinergise.geopedia.client.core.symbology;

import java.awt.Color;

import com.google.gwt.core.client.JavaScriptObject;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.geopedia.core.symbology.Symbolizer;

public abstract class SymbolizerImpl extends JavaScriptObject implements Symbolizer {

	protected  SymbolizerImpl() {
	}
	
	protected final void setColor(String attributeName, Color color) {
		setDoubleNative(attributeName, color.getRGB());
	}

	protected final native void setDoubleNative(String attributeName, double value) /*-{
		this[attributeName]=value;
	}-*/;
	
	
	protected final native void setStringNative(String attributeName, String value) /*-{
		this[attributeName]=value;
	}-*/;
 


	public static final int ID_POINTSYMBOLIZER=1;
	public static final int ID_LINESYMBOLIZER=2;
	public static final int ID_FILLSYMBOLIZER=3;
	public static final int ID_TEXTSYMBOLIZER=4;
	

	protected final native void setClassId(int classId) /*-{
		this.classId=classId;
	}-*/;

	public final native int getClassId() /*-{
		return this.classId;
	}-*/;


	
	protected static Color fromRawColorARGB(double rawColor) {
		int rgba = (int) (new Double(rawColor)).longValue();
		return  new Color(rgba, true);
	}
	
	private final native double getDisplacementX() /*-{
		return this.displacementX;
	}-*/;
	
	
	public final native void setDisplacementX(double displacementX) /*-{
		this.displacementX=displacementX;
	}-*/;

	private final native double getDisplacementY() /*-{
		return this.displacementY;
	}-*/;
	
	public final native void setDisplacementY(double displacementY) /*-{
		this.displacementY=displacementY;
	}-*/;
	
	
	
	@Override
	public final Position2D getDisplacement() {
		return new Position2D(getDisplacementX(), getDisplacementY());
	}

	
	public final native void setOpacity(double opacity) /*-{
		this.opacity = opacity;
	}-*/;

	@Override
	public final native double getOpacity() /*-{
		return this.opacity;
	}-*/;
}
