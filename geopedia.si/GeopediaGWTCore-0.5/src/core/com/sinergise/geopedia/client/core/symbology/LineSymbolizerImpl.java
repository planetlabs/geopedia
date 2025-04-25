package com.sinergise.geopedia.client.core.symbology;

import java.awt.Color;

import com.google.gwt.core.client.JavaScriptObject;
import com.sinergise.geopedia.core.symbology.LineSymbolizer;

public class LineSymbolizerImpl extends SymbolizerImpl implements LineSymbolizer {

	public static LineSymbolizerImpl create() {
		LineSymbolizerImpl lsi =  JavaScriptObject.createObject().cast();		
		lsi.init();
		return lsi;
	}

	
	protected LineSymbolizerImpl() {
	}
	
	protected final void init() {
		setClassId(ID_LINESYMBOLIZER);
		setStroke(LineSymbolizer.DEFAULT_STROKE);
		setDisplacementX(LineSymbolizer.DEFAULT_DISPLACEMENTX);
		setDisplacementY(LineSymbolizer.DEFAULT_DISPLACEMENTY);
		setOpacity(LineSymbolizer.DEFAULT_OPACITY);
		setStrokeWidth(LineSymbolizer.DEFAULT_STROKEWIDTH);
		setLineType(LineSymbolizer.DEFAULT_LINETYPE);
	}
	
	
	public final void setStroke(Color stroke) {
		setColor("stroke", stroke);
	}
	@Override
	public final Color getStroke() {
		return fromRawColorARGB(getStrokeNative());
	}
	
	private final native double getStrokeNative() /*-{
		return this.stroke;
	}-*/;

	@Override
	public final native double getStrokeWidth() /*-{
		return this.strokeWidth;
	}-*/;
	
	
	public final native void setStrokeWidth(double strokeWidth) /*-{
		this.strokeWidth=strokeWidth;
	}-*/;

	
	
	public final void setLineType(LineType lt) {
		setStringNative("lineType", lt.name());
	}
	@Override
	public final LineType getLineType() {
		String nativeLT = getNativeLineType();
		for (LineType lt:LineType.values()) {
			if (lt.name().equalsIgnoreCase(nativeLT)) {
				return lt;
			}
		}
		return LineType.SOLID;
	}
	
	private final native String getNativeLineType() /*-{
		return this.lineType;
	}-*/;


}
