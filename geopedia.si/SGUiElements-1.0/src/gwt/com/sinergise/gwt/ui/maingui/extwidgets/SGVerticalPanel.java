package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.controls.CanEnsureSelfVisibility;
import com.sinergise.gwt.ui.controls.EnsureVisibilityUtil;

public class SGVerticalPanel extends VerticalPanel implements RequiresResize, ProvidesResize, CanEnsureSelfVisibility {
	public void onResize() {
		for (Widget child : getChildren()) {
			if (child instanceof RequiresResize) {
				((RequiresResize)child).onResize();
			}
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
}
