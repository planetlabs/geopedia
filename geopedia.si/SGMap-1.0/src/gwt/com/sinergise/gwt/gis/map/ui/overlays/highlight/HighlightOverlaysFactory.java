/*
 *
 */
package com.sinergise.gwt.gis.map.ui.overlays.highlight;

import java.util.HashSet;
import java.util.Set;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.gis.feature.FeaturesSource;
import com.sinergise.common.gis.map.model.layer.system.SelectionSetLayer;
import com.sinergise.gwt.gis.map.ui.OverlayComponent;
import com.sinergise.gwt.gis.map.ui.OverlayProvider;
import com.sinergise.gwt.gis.map.ui.OverlaysFactory;
import com.sinergise.gwt.gis.map.ui.OverlaysFactory.FactoryListener;


public class HighlightOverlaysFactory {
	public static final HighlightOverlaysFactory INSTANCE = new HighlightOverlaysFactory();
	private HashSet<HighlightOverlayProvider> providers = new HashSet<HighlightOverlayProvider>();

	public HighlightOverlaysFactory() {
		Set<OverlayProvider> registered = OverlaysFactory.INSTANCE.getProviders();
		for (OverlayProvider ovp : registered) {
			if (ovp instanceof HighlightOverlayProvider) {
				registerProvider((HighlightOverlayProvider)ovp);
			}
		}

		OverlaysFactory.INSTANCE.addFactoryListener(new FactoryListener() {
			@Override
			public void providerRegistered(String sourceType, Object provider) {
				if (provider instanceof HighlightOverlayProvider) {
					registerProvider((HighlightOverlayProvider)provider);
				}
			}

			@Override
			public void providerDeregistered(String sourceType, Object provider) {
				if (provider instanceof HighlightOverlayProvider) {
					deregisterProvider((HighlightOverlayProvider)provider);
				}
			}
		});
	}

	public void registerProvider(HighlightOverlayProvider sourceProvider) {
		providers.add(sourceProvider);
	}

	public void deregisterProvider(HighlightOverlayProvider sourceProvider) {
		providers.remove(sourceProvider);
	}

	public HighlightOverlayProvider findProvider(FeaturesSource dataSource) {
		for (HighlightOverlayProvider hop : providers) {
			if (hop.canHandle(dataSource)) {
				return hop;
			}
		}
		return null;
	}

	public OverlayComponent<?> createComponent(CRS mapCRS, FeaturesSource dataSource, SelectionSetLayer lyr) {
		HighlightOverlayProvider hop = findProvider(dataSource);
		if (hop == null) {
			return null;
		}
		return hop.createHighlightOverlay(mapCRS, dataSource, lyr);
	}
}
