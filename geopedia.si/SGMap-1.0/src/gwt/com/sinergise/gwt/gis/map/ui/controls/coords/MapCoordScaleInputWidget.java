package com.sinergise.gwt.gis.map.ui.controls.coords;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.event.CoordinatesListener;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.MapViewSpec;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.gwt.gis.i18n.Buttons;
import com.sinergise.gwt.gis.i18n.Labels;
import com.sinergise.gwt.gis.map.DefaultMap;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.controls.EffectElement;
import com.sinergise.gwt.gis.map.ui.controls.coords.ZoomSelectWidget.ZoomSelectWidgetType;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.gis.resources.map.SGWebGisMapResources;
import com.sinergise.gwt.ui.handler.EnterKeyDownHandler;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.table.FlexTableBuilder;

/**
 * @author tcerovski
 *
 */
public class MapCoordScaleInputWidget extends Composite {
	public static interface MapCoordScaleInputWidgetListener {
		void locationSet(MapViewSpec viewSpec);
	} 
	
	private final MapComponent map;
	private CoordinateEntryWidget wCoords;
	private ZoomSelectWidget wScale;
	private SGPushButton butSetPos;
	private EffectElement centerCross;
	private Image crossHair;
	private ArrayList<MapCoordScaleInputWidgetListener> listeners = new ArrayList<MapCoordScaleInputWidget.MapCoordScaleInputWidgetListener>();
	
	public MapCoordScaleInputWidget(MapComponent map, CRS ...crss) {
		this.map = map;
		
		FlexTableBuilder tb = new FlexTableBuilder();
		tb.addFieldLabelAndWidget(Labels.INSTANCE.coordinates()+":", wCoords = new CoordinateEntryWidget(crss));
		tb.newRow();
		tb.addFieldLabelAndWidget(Labels.INSTANCE.scale()+":", wScale = new ZoomSelectWidget(ZoomSelectWidgetType.MANUAL_ONLY, null));
		tb.newRow();
		tb.addBlank();
		tb.addFieldValueWidget(butSetPos = new SGPushButton(Buttons.INSTANCE.setPosition(), GisTheme.getGisTheme().gisStandardIcons().coordinates()));
		
		wScale.setScale(map.getScale());	
		wCoords.setValue(map.getCoordinateAdapter().worldCRS, new Position2D(map.getCoordinateAdapter().worldCenterX,map.getCoordinateAdapter().worldCenterY));
		
		SimplePanel wrapper = new SimplePanel();
		wrapper.add(tb.buildTable());
		wrapper.setStyleName(StyleConsts.MAP_COORDS_SCALE_INPUT_WIDGET);
		initWidget(wrapper);
		
		initListeners();
	}
	
	private void initListeners() {
		map.getCoordinateAdapter().addCoordinatesListener(new CoordinatesListener() {
			
			@Override
			public void displaySizeChanged(int newWidthPx, int newHeightPx) { }
			
			@Override
			public void coordinatesChanged(double newX, double newY, double newScale, 
											boolean coordsChanged, boolean scaleChanged) 
			{
				processCoordsChanged(newX, newY, newScale, coordsChanged, scaleChanged);
			}
		});
		
		butSetPos.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updatePosition();
			}
		});
		
		wScale.addKeyDownHandler(new EnterKeyDownHandler() {
			@Override
			public void onEnterDown(KeyDownEvent event) {
				updatePosition();
			}
		});
		
		wCoords.addKeyDownHandler(new EnterKeyDownHandler() {
			@Override
			public void onEnterDown(KeyDownEvent event) {
				updatePosition();
			}
		});
		
	}
	
	protected void updatePosition() {
		MapViewSpec spec = getCurrentViewSpec();
		if (spec != null) {
			map.getCoordinateAdapter().setWorldCenterAndScale(spec);
			map.repaint(500);
			fireLocationSet(spec);
		}
		
		showCenterCross(spec);
	}

	public MapViewSpec getCurrentViewSpec() {
		HasCoordinate p = wCoords.getCoords(map.coords.worldCRS);
		if (p == null) {
			return null;
		}
		return new MapViewSpec(p, wScale.getScale());
	}

	private void fireLocationSet(MapViewSpec spec) {
		for (MapCoordScaleInputWidgetListener l : listeners) {
			l.locationSet(spec);
		}
	}

	protected void processCoordsChanged(double newX, double newY, double newScale, boolean coordsChanged, boolean scaleChanged) {
		if (coordsChanged) {
			wCoords.setValue(map.getCoordinateAdapter().worldCRS, new Position2D(newX, newY));
		} 
		if (scaleChanged) {
			wScale.setScale(newScale);
		}
		hideCenterCross();
	}
	
	public void addCoordWidgetListener(MapCoordScaleInputWidgetListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}
	
	public boolean removeCoordWidgetListener(MapCoordScaleInputWidgetListener l) {
		return listeners.remove(l);
	}
	
	private void showCenterCross(HasCoordinate pos){
		if (!(map instanceof DefaultMap)) {
			return;
		}
		if(centerCross == null){
			initCenterCross();	
		}
			
		PointI centerPx = map.coords.pixFromWorld.pointInt(pos.x(), pos.y());
		centerCross.setCenterInPix(centerPx.x- crossHair.getWidth()/2, centerPx.y- crossHair.getHeight()/2);
		
		((DefaultMap)map).getEffectsOverlay().add(centerCross);
	}
	
	private void hideCenterCross(){
		if (!(map instanceof DefaultMap) ||centerCross == null) {
			return;
		}
		((DefaultMap)map).getEffectsOverlay().remove(centerCross);
	}
	
	private void initCenterCross(){
		crossHair = new Image();
		crossHair.setResource(SGWebGisMapResources.INSTANCE.mapCrosshair());
		centerCross = new EffectElement(crossHair.getElement());
	}
}
