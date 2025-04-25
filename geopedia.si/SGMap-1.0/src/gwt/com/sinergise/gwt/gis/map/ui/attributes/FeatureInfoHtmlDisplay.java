package com.sinergise.gwt.gis.map.ui.attributes;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sinergise.common.ui.controls.CanEnsureSelfVisibility;
import com.sinergise.gwt.ui.controls.EnsureVisibilityUtil;


public class FeatureInfoHtmlDisplay extends Composite implements CanEnsureSelfVisibility {
	ScrollPanel scroll;
	
	public FeatureInfoHtmlDisplay(HTML content) {
		initWidget(scroll = new ScrollPanel(content));
	}
	
	@Override
	public void ensureVisible() {
		EnsureVisibilityUtil.ensureVisibility(this);
	}

	@Override
	public boolean isDeepVisible() {
		return EnsureVisibilityUtil.isDeepVisible(this);
	}

	public void setHtml(HTML html) {
		scroll.setWidget(html);
	}
}
