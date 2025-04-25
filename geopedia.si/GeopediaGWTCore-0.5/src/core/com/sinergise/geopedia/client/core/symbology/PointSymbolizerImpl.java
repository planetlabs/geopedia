package com.sinergise.geopedia.client.core.symbology;

import java.awt.Color;

import com.google.gwt.core.client.JavaScriptObject;
import com.sinergise.geopedia.core.symbology.PointSymbolizer;

public class PointSymbolizerImpl extends SymbolizerImpl implements PointSymbolizer {

	public static PointSymbolizerImpl create() {
		PointSymbolizerImpl psi =  JavaScriptObject.createObject().cast();
		psi.init();		
		return psi;
	}
	
	protected final void init() {
		setClassId(ID_POINTSYMBOLIZER);
		setFill(PointSymbolizer.DEFAULT_FILL);
		setDisplacementX(PointSymbolizer.DEFAULT_DISPLACEMENTX);
		setDisplacementY(PointSymbolizer.DEFAULT_DISPLACEMENTY);
		setOpacity(PointSymbolizer.DEFAULT_OPACITY);
		setSize(PointSymbolizer.DEFAULT_SIZE);
		setSymbolId(PointSymbolizer.DEFAULT_SYMBOLID);
	}
	
	protected PointSymbolizerImpl() {
	}
	
	
	public native final void setSize(double size) /*-{
		this.size=size;
	}-*/;

	@Override
	public native final double getSize() /*-{
		return this.size;
	}-*/;

	
	public final void setFill(Color fill) {
		setColor("fill", fill);
	}
	@Override
	public final Color getFill() {
		return fromRawColorARGB(getFillNative());
	}
	
	private final native double getFillNative() /*-{
		return this.fill;
	}-*/;

	
	public native final void setSymbolId(int symbolId) /*-{
		this.symbolId=symbolId;
	}-*/;

	@Override
	public native final int getSymbolId() /*-{
		return this.symbolId;
	}-*/;

}
