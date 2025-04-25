package com.sinergise.geopedia.pro.client.ui.feature;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.geometry.topo.TopologyException;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.geopedia.client.components.editor.GeometryEditor;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.theme.featureedit.FeatureEditStyle;
import com.sinergise.gwt.gis.map.shapes.editor.PointEditor;
import com.sinergise.gwt.gis.map.shapes.editor.TopoEditor;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.NotificationPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGHeaderPanel;

public class GeometryValidationPanel extends SGHeaderPanel {
	private final String CSS_STYLE_STATUS_OK= FeatureEditStyle.INSTANCE.featureGeometry().ok();
	private final String CSS_STYLE_STATUS_ERROR= FeatureEditStyle.INSTANCE.featureGeometry().error();
	
	FlexTable table = null;
	SimplePanel scroll;
	SGFlowPanel notificationPanel;
	int tblCurrentRow;
	private int lineSegmentCount;
	private int polygonCount;
	private int pointCount;
	private boolean isValid;
	protected MapWidget mapWidget;
	
	public GeometryValidationPanel(MapWidget mapWidget) {
		FeatureEditStyle.INSTANCE.featureGeometry().ensureInjected();
		
		this.mapWidget = mapWidget;
		notificationPanel = new SGFlowPanel();
		FlowPanel header = new FlowPanel();
		header.add(notificationPanel);
		setHeaderWidget(header);			
	}
	
	
	private void addNotification(String text, MessageType type) {
		NotificationPanel lbl = new NotificationPanel(text, type);
		notificationPanel.add(lbl);
	}
	
		
	private Geometry validatePoints(GeometryCollection<?> gCollection,
			GeomType geomType, PointEditor pointEditor) {

		isValid = true;
		tblCurrentRow=0;
		pointCount=0;
		
		if (scroll!=null)
			scroll.removeFromParent();
		scroll = new SimplePanel();
		scroll.setStyleName(FeatureEditStyle.INSTANCE.featureGeometry().scrollPanel());
		setContentWidget(scroll);
		
		if (table!=null)
			table.removeFromParent();
		table = new FlexTable();
		scroll.add(table);
		
		onResize();
		
		notificationPanel.clear();
		table.getRowFormatter().setStyleName(0, FeatureEditStyle.INSTANCE.featureGeometry().tableTitle());
		table.setText(0, 0, ProConstants.INSTANCE.point());
		table.setText(0, 1, ProConstants.INSTANCE.status());
		table.setText(0, 2, ProConstants.INSTANCE.actions());
		table.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT);
		
		for (Geometry geom: gCollection) {
			if (geom instanceof Point) {
				addPoint((Point) geom);
			} else {
				addNotification(ProConstants.INSTANCE.unsupportedGeometry(), MessageType.ERROR);
				isValid=false;
			}
		}				
		table.addStyleName(FeatureEditStyle.INSTANCE.featureGeometry().geometryValidationTable());
		
		if (isValid && gCollection.size()==1) {
			addNotification(ProConstants.INSTANCE.topologyOk(), MessageType.SUCCESS);
			return gCollection.get(0);
		}
		addNotification(ProConstants.INSTANCE.topologyError(), MessageType.ERROR);
		return null;
	}

	public Geometry validate(GeometryEditor gEditor) throws TopologyException {
		GeometryCollection<?> gCollection = gEditor.toGeometryCollection();

		if(gEditor.getGeomType().equals(GeomType.POINTS) || gEditor.getGeomType().equals(GeomType.POINTS_M)){
			Geometry validatedPoints = validatePoints(gCollection, gEditor.getGeomType(), gEditor.getPointEditor());
			if(isValid)
				return validatedPoints;
		} else {
			Geometry validatedLinesAndPolygons = validateLinesAndPolygons(gEditor, gCollection);
			if(isValid)
				return validatedLinesAndPolygons;
		}
	
		//return null if not valid!
		return null;
		
	}

	public Geometry validateLinesAndPolygons(GeometryEditor gEditor, GeometryCollection<?> gCollection) {
		

		GeomType geomType = gEditor.getGeomType();
		TopoEditor topoEditor = gEditor.getTopoEditor();
		
		isValid = true;
		tblCurrentRow=0;
		polygonCount=0;
		lineSegmentCount=0;
		
		
		if (scroll!=null)
			scroll.removeFromParent();
		scroll = new SimplePanel();
		scroll.setStyleName(FeatureEditStyle.INSTANCE.featureGeometry().scrollPanel());
		setContentWidget(scroll);
		
		if (table!=null)
			table.removeFromParent();
		table = new FlexTable();
		scroll.add(table);
		
		onResize();
		
		notificationPanel.clear();
		table.getRowFormatter().setStyleName(0, FeatureEditStyle.INSTANCE.featureGeometry().tableTitle());
		table.setText(0, 0, ProConstants.INSTANCE.segment());
		table.setText(0, 1, ProConstants.INSTANCE.status());
		table.setText(0, 2, ProConstants.INSTANCE.actions());
		table.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT);
		
		for (Geometry geom: gCollection) {
			if (geom instanceof LineString) {
				addLineString((LineString) geom, geomType, topoEditor);
			} else if (geom instanceof MultiLineString) {
				MultiLineString mls = (MultiLineString)geom;
				for (int i=0;i<mls.size();i++) {
					addLineString(mls.get(i), geomType, topoEditor);	
				}
			} else if (geom instanceof Polygon) {
				addPolygon((Polygon) geom, geomType, topoEditor);
			} else if (geom instanceof MultiPolygon) {
				MultiPolygon mpoly = (MultiPolygon)geom;
				for (int i=0;i<mpoly.size();i++) {
					addPolygon(mpoly.get(i),geomType, topoEditor);
				}
			} else {
				addNotification(ProConstants.INSTANCE.unsupportedGeometry(), MessageType.ERROR);
				isValid=false;
				
			}
		}				
		table.addStyleName(FeatureEditStyle.INSTANCE.featureGeometry().geometryValidationTable());
		
		try {
			if (isValid && gEditor.getValidGeometry()!=null) {
				addNotification(ProConstants.INSTANCE.topologyOk(), MessageType.SUCCESS);
				return gCollection.get(0);
			}
		} catch (Exception ex) {
			// IGNORE since we already know it's a topology error
		}
		addNotification(ProConstants.INSTANCE.topologyError(), MessageType.ERROR);
		return null;
	}
	
	private void addLineString(final LineString ls, GeomType geomType, final TopoEditor topoEditor) {
		tblCurrentRow++;
		lineSegmentCount++;			
		Anchor anchor = new Anchor(ProConstants.INSTANCE.segment()+" "+lineSegmentCount);
		anchor.setTitle(ProConstants.INSTANCE.zoomToSegment());
		anchor.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				mapWidget.getMapComponent().ensureVisible(ls.getEnvelope(), true, false);
			}
		});
		table.setWidget(tblCurrentRow, 0, anchor);
		FlowPanel lblStatus = createStatusPanel(ProConstants.INSTANCE.ok(), null, CSS_STYLE_STATUS_OK);

		if (geomType.isPolygon()) {
			lblStatus = createStatusPanel(ProConstants.INSTANCE.error(), ProConstants.INSTANCE.onlyPolygons(), CSS_STYLE_STATUS_ERROR);
			isValid=false;
		} else if (geomType.isLine()) {
			if (lineSegmentCount>1 && geomType==GeomType.LINES) {
				lblStatus = createStatusPanel(ProConstants.INSTANCE.error(), ProConstants.INSTANCE.onlySingleLine(), CSS_STYLE_STATUS_ERROR);
				isValid=false;
			}
		}
		table.setWidget(tblCurrentRow,1,lblStatus);
		
		ImageAnchor btnShowStart = new ImageAnchor(FeatureEditStyle.INSTANCE.startPoint(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {					
				mapWidget.getMapComponent().ensureVisible(new Envelope(ls.getX(0), ls.getY(0),ls.getX(0),ls.getY(0)), false, true);
				topoEditor.selectNode(new Position2D(ls.getX(0), ls.getY(0)));
			}
		});
		btnShowStart.setTitle(ProConstants.INSTANCE.showStartPoint());
		ImageAnchor btnShowEnd = new ImageAnchor(FeatureEditStyle.INSTANCE.endPoint(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int idx = ls.getNumCoords()-1;
				mapWidget.getMapComponent().ensureVisible(new Envelope(ls.getX(idx), ls.getY(idx),ls.getX(idx),ls.getY(idx)), false, true);
				topoEditor.selectNode(new Position2D(ls.getX(idx), ls.getY(idx)));
			}
		});
		btnShowEnd.setTitle(ProConstants.INSTANCE.showEndPoint());
		
		FlowPanel btnPanel = new FlowPanel();
		btnPanel.add(btnShowStart);
		btnPanel.add(btnShowEnd);
		btnPanel.setStyleName(FeatureEditStyle.INSTANCE.featureGeometry().btnPanel());
		table.setWidget(tblCurrentRow,2,btnPanel);
		table.getCellFormatter().setHorizontalAlignment(tblCurrentRow, 2, HasHorizontalAlignment.ALIGN_RIGHT);
	}

	private void addPoint(final Point point) {
		tblCurrentRow++;
		pointCount++;
		Anchor anchor = new Anchor(ProConstants.INSTANCE.point()+" "+pointCount);
		anchor.setTitle(ProConstants.INSTANCE.zoomToPoint());
		anchor.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				mapWidget.getMapComponent().ensureVisible(point.getEnvelope(), true, true);
			}
		});
		table.setWidget(tblCurrentRow, 0, anchor);

		FlowPanel lblStatus = createStatusPanel(ProConstants.INSTANCE.ok(), null, CSS_STYLE_STATUS_OK);
		table.setWidget(tblCurrentRow,1,lblStatus);
		
		ImageAnchor btnShowPoint = new ImageAnchor(FeatureEditStyle.INSTANCE.startPoint(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {					
				mapWidget.getMapComponent().ensureVisible(point.getEnvelope(), false, true);
			}
		});
		btnShowPoint.setTitle(ProConstants.INSTANCE.showPoint());
		
		FlowPanel btnPanel = new FlowPanel();
		btnPanel.add(btnShowPoint);
		btnPanel.setStyleName(FeatureEditStyle.INSTANCE.featureGeometry().btnPanel());
		table.setWidget(tblCurrentRow,2,btnPanel);
		table.getCellFormatter().setHorizontalAlignment(tblCurrentRow, 2, HasHorizontalAlignment.ALIGN_RIGHT);
	}
	
	
	private static FlowPanel createStatusPanel(String text, String tooltip, String styleName) {
		FlowPanel fp = new FlowPanel();
		final Label lbl = new Label(text);
		fp.add(lbl);
		
		if (tooltip!=null){
			lbl.addStyleName("hasTooltip");
			final PopupPanel panel = new PopupPanel(true);
			panel.setWidget(new Label(tooltip));
			lbl.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					panel.showRelativeTo(lbl);
				}
			});
		}
		if (styleName!=null)
			fp.setStyleName(styleName);
		return fp;
	}
	
	private void addPolygon(final Polygon poly, GeomType geomType, TopoEditor topoEditor) {
		tblCurrentRow++;
		polygonCount++;
		
		Anchor anchor = new Anchor(ProConstants.INSTANCE.polygon()+" "+polygonCount);
		anchor.setTitle(ProConstants.INSTANCE.zoomToPolygon());
		anchor.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				mapWidget.getMapComponent().ensureVisible(poly.getEnvelope(), true, false);
			}
		});
		table.setWidget(tblCurrentRow, 0, anchor);
		
		FlowPanel lblStatus = createStatusPanel(ProConstants.INSTANCE.ok(), null, CSS_STYLE_STATUS_OK);
		if (geomType.isLine()) {
			lblStatus = createStatusPanel(ProConstants.INSTANCE.error(), ProConstants.INSTANCE.onlyLines(), CSS_STYLE_STATUS_ERROR);
			isValid=false;
		} else if (geomType.isPolygon()) {
			if (polygonCount>1 && geomType == GeomType.POLYGONS) {
				lblStatus = createStatusPanel(ProConstants.INSTANCE.error(), ProConstants.INSTANCE.onlySinglePolygon(), CSS_STYLE_STATUS_ERROR);					
				isValid=false;
			}
		}
		
		table.setWidget(tblCurrentRow,1,lblStatus);
		table.setWidget(tblCurrentRow,2,null);
	}

}