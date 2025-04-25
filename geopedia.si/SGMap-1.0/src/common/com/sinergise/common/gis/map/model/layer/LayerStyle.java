/**
 * 
 */
package com.sinergise.common.gis.map.model.layer;

import com.sinergise.common.gis.map.model.style.ComponentStyle;
import com.sinergise.common.gis.map.model.style.StyleComponent;

public class LayerStyle extends ComponentStyle {
	
	class LayerStyleComponent extends StyleComponent {
		Boolean dfv;
		public LayerStyleComponent(String componentName, Boolean defaultValue) {
			super(componentName);
			this.dfv = defaultValue;
			
			if (this.dfv != null) {
				setOn(this.dfv.booleanValue());
			}
		}
		
		@Override
		public void reset() {
		}
	}
	
	public LayerStyle() {
	}
	
	public LayerStyle(Boolean text, Boolean fill, Boolean line, Boolean symbol) {
		if (text != null) addComponent(new LayerStyleComponent(COMP_TEXT,   text));
		if (fill != null) addComponent(new LayerStyleComponent(COMP_FILL,   fill));
		if (line != null) addComponent(new LayerStyleComponent(COMP_LINE,   line));
		if (symbol != null) addComponent(new LayerStyleComponent(COMP_SYMBOL, symbol));
	}
	
	public LayerStyle(Boolean text, Boolean fill, Boolean line, Boolean symbol, Boolean cluster) {
		this(text, fill, line, symbol);
		if (cluster != null) addComponent(new LayerStyleComponent(COMP_CLUSTER, cluster));
	}
}