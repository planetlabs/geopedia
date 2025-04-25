package com.sinergise.gwt.gis.map.ui.controls.mapLayersTree;

import java.util.ArrayList;
import java.util.Iterator;

import com.sinergise.common.gis.map.model.layer.Layer;


public class StyleWidgetFactory {
	public interface StyleWidgetProvider {
		boolean canHandle(Layer layer);
		LayerStyleWidget createFor(Layer layer);
	}
	private static ArrayList<StyleWidgetProvider> providers=new ArrayList<StyleWidgetProvider>();

	static {
		providers.add(new ComponentStyleWidgets());
	}
	
	public static final LayerStyleWidget createFor(Layer layer) {
		for (Iterator<StyleWidgetProvider> ot = providers.iterator(); ot.hasNext();) {
			StyleWidgetProvider swp = ot.next();
			if (swp.canHandle(layer)) {
				LayerStyleWidget ret=swp.createFor(layer);
				if (ret!=null) return ret;
			}
		}
		return null;
	}
	
	public static final boolean register(StyleWidgetProvider widgetProvider) {
		if (providers.contains(widgetProvider)) return false;
		providers.add(0, widgetProvider);
		return true;
	}
	
	public static final boolean deregister(StyleWidgetProvider widgetProvider) {
		return providers.remove(widgetProvider);
	}
}
