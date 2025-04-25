package com.sinergise.generics.gwt.widgets;

import java.util.List;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.gwt.core.GenericWidget;

public class BasicWidget extends GenericWidget{

	private Grid grid;
	
	
	
	public BasicWidget () {
		grid = new Grid();
		initWidget(grid);
	}
	
	@Override
	public void build(List<GenericObjectProperty> properties, EntityType entityType) {
		setEntityType(entityType);
		grid.resize(properties.size(),2);
		
		
		for (int i=0;i<properties.size();i++) {
			GenericObjectProperty prop = properties.get(i);
			
			String lbl = prop.getLabel();
			grid.setWidget(i,0, new Label(lbl == null ? null : lbl+":"));
			grid.setWidget(i,1, widgetBuilder.buildWidget(prop.getName(),prop.getAttributes()));
			propertyMap.put(prop.getName(), prop);
		}
		widgetCreated();
	}

	@Override
	public void bindProcessors() {		
		int i=0;
		for (String key:propertyMap.keySet()) {
			GenericObjectProperty prop = propertyMap.get(key);
			bindProcessors(grid.getWidget(i, 1), 0, prop, this);
			i++;
		}
		
	}

}
