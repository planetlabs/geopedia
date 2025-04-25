package com.sinergise.geopedia.client.ui.map;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;

public class StyledMapWidget extends MapWidget{
	
	private static FlowPanel createStyledDiv(String className){
		FlowPanel div = new FlowPanel();
		div.setStyleName(className);
		return div;
	}
	
	@Override
	protected void initialize() {
		super.initialize();

		
		FlowPanel border = new FlowPanel();
		DOM.setElementAttribute(border.getElement(),"id","border");
		
		border.add(createStyledDiv("top"));
		border.add(createStyledDiv("bottom"));
		border.add(createStyledDiv("left"));
		border.add(createStyledDiv("right"));
		border.add(createStyledDiv("tl"));
		border.add(createStyledDiv("tr"));
		border.add(createStyledDiv("bl"));
		border.add(createStyledDiv("br"));
		
				add(border);		
		DOM.setElementAttribute(getElement(), "id", "main_map");

	}
}
