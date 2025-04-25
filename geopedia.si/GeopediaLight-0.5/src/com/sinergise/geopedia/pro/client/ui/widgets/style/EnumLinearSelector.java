package com.sinergise.geopedia.pro.client.ui.widgets.style;

import com.google.gwt.user.client.ui.Anchor;

public class EnumLinearSelector<E extends Enum<E>> extends AbstractLinearSelector<Enum<E>> {

	public EnumLinearSelector(Enum<E>[] values) {
		super(values);
		setValue(values[0]);
	}

	@Override
	protected void renderItemAnchor(Anchor anchor, Enum<E> item) {
		anchor.setHTML("<b></b>");
		anchor.setStyleName(item.name().toLowerCase());			
	}

}
