package com.sinergise.gwt.gis.map.ui.attributes;

import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.gwt.core.GenericWidget;
import com.sinergise.generics.gwt.widgetprocessors.TableValueBinderWidgetProcessor;

public class PropertyGenTableProcessor extends TableValueBinderWidgetProcessor {
	@Override
	public Widget bind(Widget widget, int idx, GenericObjectProperty property, GenericWidget gw) {
		EntityObject eo = (EntityObject)tableData.get(idx);
		if (MetaAttributes.isTrue(property.getAttributes(), MetaAttributes.RENDER_AS_ANCHOR)) {
			return GenSummaryTableBuilder.renderAsLink(property, eo);
		}
		//add tooltip if set
		Widget w = super.bind(widget, idx, property, gw);
		GenSummaryTableBuilder.setTooltip(property, eo, w);
		return w;
	}
}