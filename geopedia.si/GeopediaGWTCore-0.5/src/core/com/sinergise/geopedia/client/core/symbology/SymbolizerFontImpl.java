package com.sinergise.geopedia.client.core.symbology;

import com.google.gwt.core.client.JavaScriptObject;
import com.sinergise.geopedia.core.symbology.SymbolizerFont;

public class SymbolizerFontImpl extends JavaScriptObject implements SymbolizerFont {

	public static SymbolizerFontImpl create() {
		SymbolizerFontImpl sfi =  JavaScriptObject.createObject().cast();
		sfi.init();
		return sfi;
	}
	
	
	protected final void init() {
		setFontWeight(SymbolizerFont.DEFAULT_FONTWEIGHT);
		setFontStyle(SymbolizerFont.DEFAULT_FONTSTYLE);
		setFontSize(SymbolizerFont.DEFAULT_FONTSIZE);
		setFontFamily(SymbolizerFont.DEFAULT_FONTFAMILY);
	}
	
	
	protected SymbolizerFontImpl() {
	}
	
	@Override
	public final native String getFontFamily()  /*-{
		return this.fontFamily;
	}-*/;
	
	public final native void setFontFamily(String fontFamily) /*-{
		this.fontFamily=fontFamily;
	}-*/;


	public final native void setFontSize(double fontSize) /*-{
		this.fontSize=fontSize;
	}-*/;

	
	@Override
	public final native double getFontSize()  /*-{
		return this.fontSize;
	}-*/;

	@Override
	public final FontStyle getFontStyle() {
		String nativeFT = getFontStyleNative();
		for (FontStyle ft:FontStyle.values()) {
			if (ft.name().equalsIgnoreCase(nativeFT)) {
				return ft;
			}
		}
		return FontStyle.NORMAL;
	}

	public final void setFontStyle(FontStyle fs) {
		setStringNative("fontStyle", fs.name());
	}
	
	private final native String getFontStyleNative() /*-{
		return this.fontStyle;
	}-*/;

	
	@Override
	public final FontWeight getFontWeight() {
		String nativeFT = getFontWeightNative();
		for (FontWeight ft:FontWeight.values()) {
			if (ft.name().equalsIgnoreCase(nativeFT)) {
				return ft;
			}
		}
		return FontWeight.NORMAL;
	}

	public final void setFontWeight(FontWeight fw) {
		setStringNative("fontWeight", fw.name());
	}
	
	private final native String getFontWeightNative() /*-{
		return this.fontWeight;
	}-*/;
	
	private final native void setStringNative(String attributeName, String value) /*-{
		this[attributeName]=value;
	}-*/;

}
