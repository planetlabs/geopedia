package com.sinergise.gwt.gis.map.ui.overlays.highlight;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gwt.user.client.DOM;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.gis.app.ApplicationContext;
import com.sinergise.common.gis.feature.FeaturesSource;
import com.sinergise.common.gis.map.model.layer.system.SelectionSetLayer;
import com.sinergise.common.gis.map.render.RenderInfo;
import com.sinergise.common.util.naming.Identifier;
import com.sinergise.gwt.gis.map.ui.OverlayComponent;
import com.sinergise.gwt.util.html.CSS;


public class HighlightOverlay extends OverlayComponent<HighlightRenderInfo> {
	public static class OverlayListEntry {
		public final Identifier dataSourceId;
		public final OverlayComponent<?> overlay;
		public OverlayListEntry(Identifier dataSourceId, OverlayComponent<?> overlay) {
			this.overlay=overlay;
			this.dataSourceId=dataSourceId;
			System.out.println("Adding highlight "+dataSourceId);
		}
	}
	SelectionSetLayer selectionSet;
	ArrayList<OverlayListEntry> overlays=new ArrayList<OverlayListEntry>();
	
	public HighlightOverlay(SelectionSetLayer layer) {
		super();
		this.selectionSet=layer;
	}
	
	@Override
	public HighlightRenderInfo prepareToRender(DisplayCoordinateAdapter dca, boolean trans, boolean quick) {
		if (!hasAnythingToRender(dca)) {
			return new HighlightRenderInfo(0);
		}
		Set<Identifier> sourceIDs = new HashSet<Identifier>(); 
		sourceIDs.addAll(selectionSet.getDataSourceIDs());
		for (OverlayListEntry o : overlays) {
			sourceIDs.add(o.dataSourceId);
		}
		HighlightRenderInfo info = new HighlightRenderInfo(sourceIDs.size());
		int idx=0;
		
		for (Iterator<Identifier> it = sourceIDs.iterator(); it.hasNext();) {
			Identifier sourceID = it.next();
			OverlayComponent<?> ovr = getOverlay(dca.worldCRS, sourceID);
			if (ovr!=null) {
				info.infos[idx] = ovr.prepareToRender(dca, trans, quick);
				info.overlays[idx] = ovr;
			}
			idx++;
		}
		return info;
	}

	private boolean hasAnythingToRender(DisplayCoordinateAdapter dca) {
		return selectionSet != null && selectionSet.hasAnythingToRender() && selectionSet.hasAnythingToRender(dca);
	}
	public OverlayComponent<?> getOverlay(CRS mapCRS, Identifier dataSourceID) {
		for (OverlayListEntry en : overlays) {
			if (en.dataSourceId.equals(dataSourceID)) return en.overlay;
		}
		FeaturesSource ds=ApplicationContext.getInstance().findDataSource(dataSourceID.getLocalID());
		if (ds==null) {
			System.out.println("HighlightOverlay::getOverlay neither overlay nor FeaturesSource found for dataSourceID: "+dataSourceID);
			return null;
		}
		return createOverlay(mapCRS, ds);
	}	
	
	public OverlayComponent<?> createOverlay(CRS mapCRS, FeaturesSource dataSource) {
		OverlayComponent<?> ret=HighlightOverlaysFactory.INSTANCE.createComponent(mapCRS, dataSource, selectionSet);
		if (ret!=null) {
			CSS.display(ret.getElement(), CSS.DISP_NONE);
			CSS.position(ret.getElement(), CSS.POS_ABSOLUTE);
			CSS.leftTop(ret.getElement(), 0, 0);
			DOM.appendChild(getElement(), ret.getElement());
		}
		overlays.add(new OverlayListEntry(dataSource.getQualifiedID(), ret));
		return ret;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void reposition(HighlightRenderInfo cri) {
		for (int i = 0; i < cri.infos.length; i++) {
			reposition(cri.infos[i], (OverlayComponent)cri.overlays[i]);
		}
	}

	private static <T extends RenderInfo> void reposition(T ri, OverlayComponent<T> ovr) {
		if (ri!=null && ri.hasAnything && ovr!=null) {
			CSS.display(ovr.getElement(), CSS.DISP_BLOCK);
			CSS.setVisible(ovr.getElement(), true);
			ovr.reposition(ri);
		} else if (ovr != null) {
			CSS.display(ovr.getElement(), CSS.DISP_NONE);
		}
	}
}
