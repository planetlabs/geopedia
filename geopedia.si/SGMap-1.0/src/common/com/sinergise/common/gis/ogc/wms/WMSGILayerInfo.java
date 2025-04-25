/**
 * 
 */
package com.sinergise.common.gis.ogc.wms;

import static com.sinergise.common.util.lang.TypeUtil.boxB;

import java.io.Serializable;

import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.style.ComponentStyle;
import com.sinergise.common.gis.map.model.style.StyleComponent;

public class WMSGILayerInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	protected String  name;
	protected String  style;
	protected Boolean rSymbols;
	protected Boolean rText;
	protected Boolean rLines;
	protected Boolean rFills;
	
	public WMSGILayerInfo() {
		
	}
	public WMSGILayerInfo(Layer ln, ComponentStyle ls) {
		setName(ln.getLocalID());
		StyleComponent cs = null;
		cs = ls.getComponent(ComponentStyle.COMP_FILL);
		if (cs != null) {
			setrFills(boxB(cs.isOn()));
		}
		cs = ls.getComponent(ComponentStyle.COMP_LINE);
		if (cs != null) {
			setrLines(boxB(cs.isOn()));
		}
		cs = ls.getComponent(ComponentStyle.COMP_SYMBOL);
		if (cs != null) {
			setrSymbols(boxB(cs.isOn()));
		}
		cs = ls.getComponent(ComponentStyle.COMP_TEXT);
		if (cs != null) {
			setrText(boxB(cs.isOn()));
		}
	}

	public String genStyleName(String defaultStyle) {
		StringBuilder sb = new StringBuilder();
		
		if (rSymbols != null) {
			sb.append(rSymbols.booleanValue() ? "S_" : "!S_");
		}
		
		if (rText != null) {
			sb.append(rText.booleanValue() ? "T_" : "!T_");
		}
		
		if (rLines != null) {
			sb.append(rLines.booleanValue() ? "L_" : "!L_");
		}
		
		if (rFills != null) {
			sb.append(rFills.booleanValue() ? "F_" : "!F_");
		}
		
		if (sb.length() < 1)
			return defaultStyle;
		
		return sb.deleteCharAt(sb.length()-1).toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public Boolean getrSymbols() {
		return rSymbols;
	}

	public void setrSymbols(Boolean rSymbols) {
		this.rSymbols = rSymbols;
	}

	public Boolean getrText() {
		return rText;
	}

	public void setrText(Boolean rText) {
		this.rText = rText;
	}

	public Boolean getrLines() {
		return rLines;
	}

	public void setrLines(Boolean rLines) {
		this.rLines = rLines;
	}

	public Boolean getrFills() {
		return rFills;
	}

	public void setrFills(Boolean rFills) {
		this.rFills = rFills;
	}
	
}