package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.user.client.ui.Anchor;
import com.sinergise.common.util.web.AnchorData;

public class SGAnchor extends Anchor {
	
	public SGAnchor() { }

	public SGAnchor(AnchorData data) {
		super(data.getLabel(), data.getHref(), data.getTarget());
	}
	
}
