package com.sinergise.geopedia.client.core.symbology;

import java.awt.Color;

import com.google.gwt.core.client.JavaScriptObject;
import com.sinergise.geopedia.core.symbology.FillSymbolizer;

public class FillSymbolizerImpl extends SymbolizerImpl implements FillSymbolizer {

	public static FillSymbolizerImpl create() {
		FillSymbolizerImpl fsi = JavaScriptObject.createObject().cast();
		fsi.init();
		return fsi;
	}
	
	protected FillSymbolizerImpl() {
	}
	
	
	protected final void init() {
		setClassId(ID_FILLSYMBOLIZER);
		setFill(FillSymbolizer.DEFAULT_FILL);
		setColor("fillBackground",FillSymbolizer.DEFAULT_FILLBACKGROUND);
		setDisplacementX(FillSymbolizer.DEFAULT_DISPLACEMENTX);
		setDisplacementY(FillSymbolizer.DEFAULT_DISPLACEMENTY);
		setOpacity(FillSymbolizer.DEFAULT_OPACITY);
		setFillType(FillSymbolizer.DEFAULT_FILLTYPE);
	}
	
	
	public final void setFillType(GPFillType fillType) {
		setStringNative("fillType", fillType.name());
	}
	
	@Override
	public final GPFillType getFillType() {
		String nativeFT = getFillTypeNative();
		for (GPFillType ft:GPFillType.values()) {
			if (ft.name().equalsIgnoreCase(nativeFT)) {
				return ft;
			}
		}
		return GPFillType.SOLID;
	}
	
	private final native String getFillTypeNative() /*-{
		return this.fillType;
	}-*/;

	@Override
	public final Color getFill() {
		return fromRawColorARGB(getFillNative());
	}
	
	public final void setFill(Color fill) {
		setColor("fill", fill);
	}
	
	public final void setFillBackground(Color fill) {
		setColor("fillBackground", fill);
	}
	private final native double getFillNative() /*-{
		return this.fill;
	}-*/;

	@Override
	public final Color getFillBackground() {
		return fromRawColorARGB(getFillBackgroundNative());
	}

	private final native double getFillBackgroundNative() /*-{
		return this.fillBackground;
	}-*/;
	

}
