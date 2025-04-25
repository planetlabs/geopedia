package com.sinergise.geopedia.pro.client.ui.widgets.style;

import com.google.gwt.user.client.ui.Anchor;
import com.sinergise.geopedia.core.style.model.LineType;

public class LineStylePicker extends AbstractLinearSelector<Integer> {
	
	@Override
	protected void renderItemAnchor(Anchor anchor, Integer item) {
		anchor.setHTML("<b></b>");
		anchor.setStyleName(LineType.names[item]);		
	}
	
	public LineStylePicker() {
		super(new Integer[]{0,1,2,3,4});
		addStyleName("lineStylePicker");
		setValue(items[0]);
	}
	
}
