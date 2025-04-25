package com.sinergise.geopedia.pro.client.ui.widgets.style;

import com.google.gwt.user.client.ui.Anchor;
import com.sinergise.geopedia.core.style.model.FillType;

public class FillStylePicker extends AbstractLinearSelector<Integer> {
	
	@Override
	protected void renderItemAnchor(Anchor anchor, Integer item) {
		anchor.setHTML("<b></b>");
		anchor.setStyleName(FillType.names[item]);		
	}
	
	public FillStylePicker() {
		super(FillType.ids);
		addStyleName("fillStylePicker");
		setValue(items[0]);
	}
	
}
