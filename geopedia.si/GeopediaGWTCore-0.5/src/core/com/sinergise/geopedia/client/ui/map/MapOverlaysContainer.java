package com.sinergise.geopedia.client.ui.map;

import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.gis.map.render.RenderInfo;
import com.sinergise.gwt.gis.map.ui.AbsoluteDeckPanel;
import com.sinergise.gwt.gis.map.ui.IOverlaysHolder;
import com.sinergise.gwt.gis.map.ui.OverlayComponent;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.util.html.CSS;

public class MapOverlaysContainer extends AbsoluteDeckPanel.FocusableDeckPanel implements IOverlaysHolder {
	private RenderInfo[] specs;
	
	public MapOverlaysContainer() {
		CSS.position(getElement(), CSS.POS_RELATIVE);
		CSS.overflow(getElement(), CSS.OVR_HIDDEN);
		setStyleName(StyleConsts.MAPOVERLAYS);
	}
	
	public void positionTiles(DisplayCoordinateAdapter dca, boolean quick) {
		boolean trans = false;
		int size = getWidgetCount();
		if (specs == null || specs.length < size) specs = new RenderInfo[size];
		
		for (int i = 0; i < size; i++) {
			OverlayComponent ovr = (OverlayComponent)getChildWidget(i);
			RenderInfo spec = ovr.prepareToRender(dca, trans, quick);
			spec.dca = dca;
			if (!trans) trans = spec.hasAnything;
			specs[i] = spec;
		}
		
		boolean hasOpaq = false;
		for (int i = size - 1; i >= 0; i--) {
			OverlayComponent ovr = (OverlayComponent) getChildWidget(i);
			if (hasOpaq || !specs[i].hasAnything) ovr.setVisible(false);
			else {
				ovr.setVisible(true);
				if (!specs[i].isTransparent) hasOpaq = true;
			}
			ovr.addStyleName("tableOverlays"+i);
		}
		
		for (int i = 0; i < size; i++) {
			OverlayComponent ovr = (OverlayComponent) getChildWidget(i);
			if (ovr.isVisible()) ovr.reposition(specs[i]);
		}
	
	}
	
	public void insertOverlay(OverlayComponent overlay, int zIndex, boolean end) {
		
		overlay.zIndex = zIndex;
		int cCount = getWidgetCount();
		if (cCount < 1) {
			// Empty, just do the adding
			insert(overlay, 0);
			return;
		}
		
		for (int i = 0; i < cCount; i++) {
			OverlayComponent cmp = (OverlayComponent) getChildWidget(i);
			if ((end && cmp.zIndex > zIndex) || (!end && cmp.zIndex >= zIndex)) {
				insert(overlay, i);
				return;
			}
		}
		
		// Not found, add to the end
		insert(overlay, cCount);
	}
	
	public void insert(Widget w, int beforeIndex) {
		super.insertChildWidget(w, beforeIndex);		
	}
	
	public void insertOverlay(OverlayComponent overlay, int zIndex) {
		insertOverlay(overlay, zIndex, true);
	}
	
	// Enable/disable an group of overlays by zIndex.
	public void setOverlayGroupEnabled(int zIndex, boolean showGroup) {
		int cnt = getWidgetCount();
		for (int i = 0; i < cnt; i++) {
			OverlayComponent cmp = (OverlayComponent) getChildWidget(i);
			if (cmp.zIndex == zIndex) cmp.setVisible(showGroup);
		}
	}
	
	public void removeOverlay(OverlayComponent ovr) {
		removeChildWidget(ovr);
	}
}