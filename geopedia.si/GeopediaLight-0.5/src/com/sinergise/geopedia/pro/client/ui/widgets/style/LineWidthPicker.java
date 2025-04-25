package com.sinergise.geopedia.pro.client.ui.widgets.style;

import com.google.gwt.user.client.ui.Anchor;

public class LineWidthPicker extends AbstractLinearSelector<Long>{

	public LineWidthPicker() {
		super(new Long[] {1l,3l,6l,10l});
		addStyleName("lineWidthPicker");
		setValue(items[0]);
	}

	@Override
	protected void renderItemAnchor(Anchor anchor, Long item) {
		anchor.setHTML("<b></b>");
		anchor.setStyleName("width"+item);
	}

	
	@Override
	protected void onAfterItemSelected(Long item) {
		
		if (selectedItem == null) {
			Long selected = null;
			Long diff = Long.MAX_VALUE;
			for (Long val:items) {
				if (selected == null) {
					selected = val;
				} else {
					Long newDiff = Math.abs(selected-val);
					if (newDiff<diff) {
						selected=val;
						diff = newDiff;
					}
				}
			}
			setValue(selected);
		}
	}
}
