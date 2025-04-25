package com.sinergise.geopedia.pro.client.ui.widgets.style;

import com.google.gwt.user.client.ui.Anchor;

public class OpacityPicker extends AbstractLinearSelector<Integer>{

	
	public OpacityPicker(Integer[] opacities) {
		super(opacities);
		addStyleName("opacityPicker");		
	}

	public OpacityPicker() {
		this(new Integer[]{0,20,40,60,80,100});
		setValue(items[0]);
	}

	//TODO: change to simple input box or at least add input box
	@Override
	protected void renderItemAnchor(Anchor anchor, Integer item) {
		anchor.setStyleName("opacity"+item);
		anchor.setHTML("<b>"+item+"</b>");
	}
	
	@Override
	protected void onAfterItemSelected(Integer item) {
		if (selectedItem==null) {
			setValue(60);
		}
	}

}
