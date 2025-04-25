/*
 *
 */
package com.sinergise.common.gis.ogc.wms.request;

import java.util.ArrayList;
import java.util.Iterator;

import com.sinergise.common.gis.map.model.style.ComponentStyle;
import com.sinergise.common.gis.map.model.style.NamedStyle;
import com.sinergise.common.gis.map.model.style.Style;
import com.sinergise.common.gis.map.model.style.StyleComponent;


public class StyleEncoderFactory {
	public static interface StyleEncoder {
		String encodeStyle(Style sty);
		
		Style decodeStyle(Style target, String styleString);
		
		boolean canHandle(Style sty);
	}
	
	public static class NamedStyleEncoder implements StyleEncoder {
		@Override
		public boolean canHandle(Style sty) {
			return sty == null || (sty instanceof NamedStyle);
		}
		
		@Override
		public Style decodeStyle(Style target, String styleString) {
			if (target != null && (target instanceof NamedStyle)
				&& ((NamedStyle) target).getName().equals(styleString)) return target;
			return new NamedStyle(styleString);
		}
		
		@Override
		public String encodeStyle(Style stl) {
			if (stl == null) return "";
			return ((NamedStyle) stl).getName();
		}
	}
	
	public static class ComponentStyleEncoder implements StyleEncoder {
		@Override
		public boolean canHandle(Style stl) {
			return stl instanceof ComponentStyle;
		}
		
		@Override
		public Style decodeStyle(Style stl, String styleString) {
			ComponentStyle st = (ComponentStyle) stl;
			st.reset();
			if (styleString == null || styleString.length() < 1) return stl;
			String[] onCmps = styleString.split("_");
			String cmpName;
			boolean on;
			for (int i = 0; i < onCmps.length; i++) {
				cmpName = onCmps[i];
				on = true;
				if (cmpName.charAt(0) == '!') {
					cmpName = cmpName.substring(1);
					on = false;
				}
				StyleComponent sc = st.getComponent(cmpName);
				if (sc==null) throw new IllegalArgumentException("Could not find component "+cmpName+" in the style");
				sc.setOn(on);
			}
			return stl;
		}
		
		@Override
		public String encodeStyle(Style stl) {
			ComponentStyle cs = (ComponentStyle) stl;
			StringBuffer buf = new StringBuffer();
			boolean first = true;
			for (Iterator<?> it = cs.namesIterator(); it.hasNext();) {
				String name = (String) it.next();
				if (first) {
					first = false;
				} else {
					buf.append("_");
				}
				if (!cs.getComponent(name).isOn()) buf.append('!');
				buf.append(name);
			}
			return buf.toString();
		}
	}
	
	private static ArrayList<StyleEncoder> encoders = new ArrayList<StyleEncoder>();
	static {
		registerEncoder(new NamedStyleEncoder());
		registerEncoder(new ComponentStyleEncoder());
	}
	
	public static void registerEncoder(StyleEncoder encoder) {
		encoders.remove(encoder);
		encoders.add(0, encoder);
	}
	
	public static void deregisterEncoder(StyleEncoder encoder) {
		encoders.remove(encoder);
	}
	
	public static StyleEncoder findEncoder(Style stl) {
		for (Iterator<StyleEncoder> it = encoders.iterator(); it.hasNext();) {
			StyleEncoder enc = it.next();
			if (enc.canHandle(stl)) return enc;
		}
		return null;
	}
	
	public static String encodeStyle(Style stl) {
		StyleEncoder enc = findEncoder(stl);
		if (enc == null) return null;
		return enc.encodeStyle(stl);
	}
	
	public static Style decodeStyle(Style stl, String styleString) {
		StyleEncoder enc = findEncoder(stl);
		if (enc == null) {
			stl.reset();
			return stl;
		}
		return enc.decodeStyle(stl, styleString);
	}
}
