package com.sinergise.gwt.gis.map.ui.util;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.feature.HasFeatureRepresentations;
import com.sinergise.common.gis.map.model.layer.system.SelectionSetLayer;
import com.sinergise.common.ui.controls.DeepVisibilityChangeListener;
import com.sinergise.common.ui.controls.VisibilityChangeListener;
import com.sinergise.gwt.ui.controls.EnsureVisibilityUtil;

public class VisibleFeatureHighlightController {
	
	private VisibleFeatureHighlightController() {
		//hide constructor
	}
	
	public static void register(final Widget w, final HasFeatureRepresentations features, final SelectionSetLayer hltLayer) {
		
		if (!w.isAttached()) {
			
			//call again when attached if not yet attached
			w.addAttachHandler(new AttachEvent.Handler() {
				@Override
				public void onAttachOrDetach(AttachEvent event) {
					if (event.isAttached()) {
						register(w, features, hltLayer);
					}
				}
			});
			return;
		}
		
		final VisibilityChangeListener vclHandle = EnsureVisibilityUtil.registerDeepVisibilityChangeListener(w, new DeepVisibilityChangeListener() {
			@Override
			public void deepVisibilityChanged(boolean newVisible) {
				if (newVisible) {
					hltLayer.addCollection(features);
				} else {
					hltLayer.removeCollection(features);
				} 
				
			}
		});

		//unregister listeners when detached
		w.addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (!event.isAttached()) {
					EnsureVisibilityUtil.deregisterVisibilityChangeListener(w, vclHandle);
				}
			}
		});
		
	}
	

}
