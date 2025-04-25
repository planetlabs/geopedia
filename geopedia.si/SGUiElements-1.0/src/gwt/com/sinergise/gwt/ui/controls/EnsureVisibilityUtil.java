package com.sinergise.gwt.ui.controls;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.controls.CanEnsureChildVisibility;
import com.sinergise.common.ui.controls.DeepVisibilityChangeListener;
import com.sinergise.common.ui.controls.SourcesVisibilityChangeEvents;
import com.sinergise.common.ui.controls.VisibilityChangeListener;

/**
 * @author tcerovski
 *
 */
public class EnsureVisibilityUtil {
	private EnsureVisibilityUtil() {
		//hide constructor
	}
	
	public static void ensureVisibility(Widget w) {
		Widget parent = getLogicalParentForVisiblity(w);
		if (parent instanceof CanEnsureChildVisibility) {
			((CanEnsureChildVisibility)parent).ensureChildVisible(w);
		}
		if (parent != null) ensureVisibility(parent);
		w.setVisible(true);
	}
	
	public static boolean isDeepVisible(Widget w) {
		if (w == null) {
			return true;
		}
		if (!w.isVisible()) {
			return false;
		}

		Widget parent = getLogicalParentForVisiblity(w);
		if (parent instanceof CanEnsureChildVisibility) { //check logical parent
			if (!((CanEnsureChildVisibility)parent).isChildVisible(w)) {
				return false;
			}
		}
		
		if (parent==null || parent instanceof RootPanel) {
			return true;
		}
		return isDeepVisible(parent);
	}
	
	private static Widget getLogicalParentForVisiblity(Widget w) {
		if (w == null) {
			return null;
		}
		Widget parent = w.getParent();
		while (parent != null && !(parent instanceof CanEnsureChildVisibility) && !(parent instanceof RootPanel)) {
			parent = parent.getParent();
		}
		return parent;
	}
	
	private static Widget getLogicalParentForVisiblityListener(Widget w) {
		if (w == null) return null;
		Widget parent = w.getParent();
		while (parent != null && !(parent instanceof SourcesVisibilityChangeEvents) && !(parent instanceof RootPanel)) {
			parent = parent.getParent();
		}
		return parent;
	}
	
	public static VisibilityChangeListener registerDeepVisibilityChangeListeners(final IsWidget w, final DeepVisibilityChangeListener... listeners) {
		return registerDeepVisibilityChangeListeners(Widget.asWidgetOrNull(w), listeners);
	}
	
	public static VisibilityChangeListener registerDeepVisibilityChangeListener(final Widget w, final DeepVisibilityChangeListener listener) {
		return registerDeepVisibilityChangeListeners(w, listener);
	}
	
	public static VisibilityChangeListener registerDeepVisibilityChangeListeners(final Widget w, final DeepVisibilityChangeListener... listeners) {
		VisibilityChangeListener vcl = new VisibilityChangeListener() {
			Boolean oldDeepVisible = null;
			@Override
			public void visibilityChanged(boolean newVisible) {
				boolean newDeepVisible = newVisible && isDeepVisible(w);
				if (oldDeepVisible == null || newDeepVisible != oldDeepVisible.booleanValue()) {
					if(listeners != null){
						for(DeepVisibilityChangeListener listener : listeners){
							listener.deepVisibilityChanged(newDeepVisible);
						}
					}
					oldDeepVisible = Boolean.valueOf(newDeepVisible);
				}
			}
		};
		
		registerDeepVisibilityChangeListener(w, vcl);
		return vcl;
	}
	
	private static void registerDeepVisibilityChangeListener(Widget w, VisibilityChangeListener listener) {
		if (w == null) return;
		
		if (w instanceof SourcesVisibilityChangeEvents) {
			((SourcesVisibilityChangeEvents)w).addVisibilityChangeListener(listener);
		}
		registerDeepVisibilityChangeListener(getLogicalParentForVisiblityListener(w), listener);
	}
	
	public static void deregisterVisibilityChangeListener(Widget w, VisibilityChangeListener listener) {
		if (w == null) return;
		
		if (w instanceof SourcesVisibilityChangeEvents) {
			((SourcesVisibilityChangeEvents)w).removeVisibilityChangeListener(listener);
		}
		deregisterVisibilityChangeListener(getLogicalParentForVisiblityListener(w), listener);
	}
	
	public static void deregisterVisibilityChangeListener(IsWidget w, VisibilityChangeListener listener) {
		deregisterVisibilityChangeListener(Widget.asWidgetOrNull(w), listener);
	}
	
}
