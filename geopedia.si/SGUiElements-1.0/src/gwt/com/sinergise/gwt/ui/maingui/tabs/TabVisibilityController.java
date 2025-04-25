package com.sinergise.gwt.ui.maingui.tabs;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.sinergise.common.ui.controls.DeepVisibilityChangeListener;
import com.sinergise.common.ui.controls.VisibilityChangeListener;
import com.sinergise.gwt.ui.controls.EnsureVisibilityUtil;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;


public abstract class TabVisibilityController <T extends SGFlowPanel> {
	
	public interface TabVisibilityChangeListener <T extends SGFlowPanel> {
		void tabVisibilityChanged(T tab, boolean visible);
	}
	
	
	private final List<TabVisibilityChangeListener<? super T>> tabVisibilityListeners = new ArrayList<TabVisibilityChangeListener<? super T>>();
	
	public void addTabVisibilityListener(TabVisibilityChangeListener<? super T> listener) {
		tabVisibilityListeners.add(listener);
	}
	
	public void removeTabVisibilityListener(TabVisibilityChangeListener<? super T> listener) {
		tabVisibilityListeners.remove(listener);
	}
	
	protected void registerDeepVisibilityChangeListener(final T tab) {
		if (!tab.isAttached()) {
			
			//call again when attached if not yet attached
			tab.addAttachHandler(new AttachEvent.Handler() {
				@Override
				public void onAttachOrDetach(AttachEvent event) {
					if (event.isAttached()) {
						registerDeepVisibilityChangeListener(tab);
					}
				}
			});
		}
			
		//register listener
		final VisibilityChangeListener vcl = EnsureVisibilityUtil.registerDeepVisibilityChangeListener(tab, new DeepVisibilityChangeListener() {
			@Override
			public void deepVisibilityChanged(boolean newVisible) {
				fireDocumentTabVisiblityChanged(tab, newVisible);
			}
		});
		
		//unregister when detaching
		tab.addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (!event.isAttached()) {
					EnsureVisibilityUtil.deregisterVisibilityChangeListener(tab, vcl);
				}
			}
		});
	}
	
	protected void fireDocumentTabVisiblityChanged(T tab, boolean visible) {
		for (TabVisibilityChangeListener<? super T> l : tabVisibilityListeners) {
			l.tabVisibilityChanged(tab, visible);
		}
	}

}
