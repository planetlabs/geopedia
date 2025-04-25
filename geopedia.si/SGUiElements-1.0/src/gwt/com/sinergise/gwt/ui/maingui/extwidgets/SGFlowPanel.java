package com.sinergise.gwt.ui.maingui.extwidgets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.controls.CanEnsureSelfVisibility;
import com.sinergise.common.ui.controls.SourcesVisibilityChangeEvents;
import com.sinergise.common.ui.controls.VisibilityChangeListener;
import com.sinergise.gwt.ui.controls.EnsureVisibilityUtil;

public class SGFlowPanel extends FlowPanel implements RequiresResize, ProvidesResize, CanEnsureSelfVisibility, SourcesVisibilityChangeEvents {

	@Override
	public void onResize() {
		for (Widget child : getChildren()) {
			if (child instanceof RequiresResize) {
				((RequiresResize)child).onResize();
			}
		}
	}

	public SGFlowPanel() {}

	public SGFlowPanel(Widget... widgets) {
		addWidgets(widgets);
	}

	public SGFlowPanel(String style) {
		setStyleName(style);
	}

	public void addWidgets(Widget... widgets) {
		for (Widget w : widgets) {
			add(w);
		}
	}

	@Override
	public void ensureVisible() {
		EnsureVisibilityUtil.ensureVisibility(this);
	}

	@Override
	public boolean isDeepVisible() {
		return EnsureVisibilityUtil.isDeepVisible(this);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible == isVisible() && !isAttached()) {
			return;
		}
		super.setVisible(visible);
		notifyVisibilityChange(visible); //check if still attached, as resetWidget could be called after detach
	}

	@Override
	protected void onUnload() {
		notifyVisibilityChange(false);
		super.onUnload();
	}

	protected List<VisibilityChangeListener> visibilityListeners = new ArrayList<VisibilityChangeListener>();

	protected void notifyVisibilityChange(boolean visible) {
		for (VisibilityChangeListener l : visibilityListeners) {
			l.visibilityChanged(visible);
		}
	}

	@Override
	public void addVisibilityChangeListener(VisibilityChangeListener listener) {
		visibilityListeners.add(listener);
	}

	@Override
	public void removeVisibilityChangeListener(VisibilityChangeListener listener) {
		visibilityListeners.remove(listener);
	}
}
