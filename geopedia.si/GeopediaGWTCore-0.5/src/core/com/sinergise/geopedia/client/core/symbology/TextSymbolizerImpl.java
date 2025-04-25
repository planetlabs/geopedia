package com.sinergise.geopedia.client.core.symbology;

import java.awt.Color;

import com.google.gwt.core.client.JavaScriptObject;
import com.sinergise.geopedia.core.symbology.PointSymbolizer;
import com.sinergise.geopedia.core.symbology.SymbolizerFont;
import com.sinergise.geopedia.core.symbology.TextSymbolizer;

public class TextSymbolizerImpl extends SymbolizerImpl implements TextSymbolizer {

	

	public static TextSymbolizerImpl create() {
		TextSymbolizerImpl tsi =  JavaScriptObject.createObject().cast();
		tsi.init();
		return tsi;
	}
	
	protected final void init() {
		setClassId(ID_TEXTSYMBOLIZER);
		setColor("fill",PointSymbolizer.DEFAULT_FILL);
		setDisplacementX(PointSymbolizer.DEFAULT_DISPLACEMENTX);
		setDisplacementY(PointSymbolizer.DEFAULT_DISPLACEMENTY);
		setOpacity(PointSymbolizer.DEFAULT_OPACITY);
		setFont(SymbolizerFontImpl.create());
	}
	
	protected TextSymbolizerImpl() {
	}
	
	@Override
	public final native String getLabel() /*-{
		return this.label;
	}-*/;
	
	public final native void setLabe(String label) /*-{
		this.label=label;
	}-*/;


	@Override
	public final native SymbolizerFont getFont() /*-{
		return this.font;
	}-*/;
	
	public final native void setFont(SymbolizerFont font) /*-{
		this.font=font;
	}-*/;


	@Override
	public final Color getFill() {
		return fromRawColorARGB(getFillNative());
	}
	
	private final native double getFillNative() /*-{
		return this.fill;
	}-*/;

}
