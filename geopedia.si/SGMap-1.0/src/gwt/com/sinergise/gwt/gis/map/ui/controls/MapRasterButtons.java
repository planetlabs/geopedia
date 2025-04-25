/**
 * 
 */
package com.sinergise.gwt.gis.map.ui.controls;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.MapContextLayers;
import com.sinergise.common.util.collections.tree.TreeListenerAdapter;
import com.sinergise.common.util.event.selection.ExcludeContext;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.ui.core.MouseHandler;


/**
 * @author tcerovski
 */
public class MapRasterButtons extends Composite {
	public static final String CSS_CLASS_RASTERBUTTONS = "sgwebgis-mapRasterButtons";
	
	private MapContextLayers mapLayers;
	private FlexTable table;
	
	private ExcludeContext exclude = new ExcludeContext();
	
	public MapRasterButtons(MapComponent map, LayerTreeElement... rasters) {
		this(map, true, rasters);
	}
	
	public MapRasterButtons(MapComponent map, boolean exclusive, LayerTreeElement... rasters) {
		this.mapLayers = map.getLayers();
		table = new FlexTable();
		table.setCellSpacing(5);
		initWidget(table);
		setStyleName(CSS_CLASS_RASTERBUTTONS);
		MouseHandler.preventContextMenu(getElement());
		
		int pos = 0;
		for (LayerTreeElement layer : rasters)
			addLayerButton(layer, pos++, exclusive);
	}
	
	private void addLayerButton(final LayerTreeElement layer, int pos, boolean exclusive) {
		if (exclusive) exclude.register(layer.selectableForOn());
		final HTML but = new HTML(layer.getTitle()) {
			{
				sinkEvents(Event.ONCLICK | Event.ONMOUSEDOWN);
			}
			@Override
			public void onBrowserEvent(Event event) {
				int typ = event.getTypeInt();
				if (typ == Event.ONCLICK || typ == Event.ONMOUSEDOWN) {
					event.stopPropagation();
					event.preventDefault();
				}
				super.onBrowserEvent(event);
			}
		};
		but.setStyleName(layer.isOn() ? "mapLayer-but-on" : "mapLayer-but-off");
		MouseHandler.preventContextMenu(but.getElement());
		
		but.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Event evt = DOM.eventGetCurrentEvent();
				if (evt != null) {
					DOM.eventCancelBubble(evt, true);
					DOM.eventPreventDefault(evt);
				}
				layer.setOn(!layer.isOn());
			}
		});
		table.setWidget(0, pos, but);
		
		mapLayers.addTreeListener(new TreeListenerAdapter<LayerTreeElement>() {
			@Override
			public void nodeChanged(LayerTreeElement node, String propertyName) {
				if (node != layer || !propertyName.equals(LayerTreeElement.PROP_ON)) return;
				but.setStyleName(layer.isOn() ? "mapLayer-but-on" : "mapLayer-but-off");
			}
		});
		
	}
	
}
