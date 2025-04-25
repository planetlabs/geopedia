package com.sinergise.gwt.gis.map.ui.attributes;

import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.feature.CFeatureUtils.PropertyDisplayData;

public interface PropertyWidgetFactory {

	Widget createValueWidget(PropertyDisplayData<?> data);
	
	Widget createLabelWidget(PropertyDisplayData<?> data);

}
