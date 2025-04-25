package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.controls.CanEnsureSelfVisibility;
import com.sinergise.gwt.ui.controls.EnsureVisibilityUtil;
import com.sinergise.gwt.ui.resources.Theme;

public class SGHeaderPanel extends HeaderPanel implements CanEnsureSelfVisibility {
	
	//TODO: implement resizing also when changing height of browser. This is done by using RootLayoutPanel and SGFlowPanel that have ProvidesResize
	//TODO: check for case when SGHeaderPanel is used in this sample: SGHeaderPanel -> SGTabLayoutPanel -> SGHeaderPanel (height of content Widget is not calculated)
	
	public SGHeaderPanel() {
		setStyleName(Theme.getTheme().layoutBundle().tabLayout().sgHeaderPanel());
		setHeight("100%");
	}
	
	public SGHeaderPanel(Widget header, Widget content) {
		this();
		setHeaderWidget(header);
		setContentWidget(content);
	}
	
	public SGHeaderPanel(Widget header, Widget content, Widget footer) {
		this(header, content);
		setFooterWidget(footer);
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		onResize();
	}
	
	@Override
	public void onResize() {
		super.onResize();
	}
	
	@Override
	public boolean isDeepVisible() {
		return EnsureVisibilityUtil.isDeepVisible(this);
	}
	
	@Override
	public void ensureVisible() {
		EnsureVisibilityUtil.ensureVisibility(this);
	}
	
	@Override
	public void setHeaderWidget(Widget w) {
		if (w != null && w.getElement() != null) {
			w.getElement().getStyle().setPosition(Position.RELATIVE);
		}
		super.setHeaderWidget(w);
	}
}
