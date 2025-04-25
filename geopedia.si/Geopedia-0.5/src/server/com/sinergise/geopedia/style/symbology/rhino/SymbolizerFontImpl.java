package com.sinergise.geopedia.style.symbology.rhino;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSGetter;
import org.mozilla.javascript.annotations.JSSetter;

import com.sinergise.geopedia.core.symbology.SymbolizerFont;

public class SymbolizerFontImpl extends ScriptableObject implements SymbolizerFont {

	private static final long serialVersionUID = -1437970329985897391L;
	private String fontFamily = DEFAULT_FONTFAMILY;
	private double fontSize = SymbolizerFont.DEFAULT_FONTSIZE;
	private FontStyle fontStyle = SymbolizerFont.DEFAULT_FONTSTYLE;
	private FontWeight fontWeight = SymbolizerFont.DEFAULT_FONTWEIGHT;
	
	@Override
	@JSGetter
	public String getFontFamily() {
		return fontFamily;
	}
	
	@JSSetter
	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}

	@Override
	@JSGetter
	public double getFontSize() {
		return fontSize;
	}
	
	@JSSetter
	public void setFontSize(double fontSize) {
		this.fontSize = fontSize;
	}

	@Override
	@JSGetter
	public FontStyle getFontStyle() {
		return fontStyle;
	}
	
	@JSSetter
	public void setFontStyle(String fontStyleString) {
		for (FontStyle fs:FontStyle.values()) {
			if (fs.name().equalsIgnoreCase(fontStyleString)) {
				fontStyle=fs;
			}
		}
	}

	@Override
	@JSGetter
	public FontWeight getFontWeight() {
		return fontWeight;
	}
	
	@JSSetter
	public void setFontWeight(String fontWeightString) {
		for (FontWeight fw:FontWeight.values()) {
			if (fw.name().equalsIgnoreCase(fontWeightString)) {
				fontWeight=fw;
				break;
			}
		}
	}
	

	@Override
	public String getClassName() {
		return "SymbolizerFont";
	}

}
