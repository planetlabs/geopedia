package com.sinergise.geopedia.pro.client.ui.feature;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.gwt.ui.BoldText;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.resources.Theme;

public class GeometryEditorFlowPanel extends SGFlowPanel {
	
	private InlineLabel geomLabel = new InlineLabel(ProConstants.INSTANCE.drawGeometry());
	private BoldText geomTypeLbl;
	private MapWidget mapWidget;


	public GeometryEditorFlowPanel(MapWidget mapWidget, String string) {
		super(string);
		this.mapWidget=mapWidget;
		add(geomLabel);
	}
	
	private static String getGeometryTypeI18NString(GeomType geomType) {
		switch (geomType) {
			case POINTS:
				return GeopediaTerms.INSTANCE.drawPoint();
			case POINTS_M:
				return GeopediaTerms.INSTANCE.multiPoints();
			case LINES:
				return GeopediaTerms.INSTANCE.drawLine();
			case LINES_M:
				return GeopediaTerms.INSTANCE.multiLines();
			case POLYGONS:
				return GeopediaTerms.INSTANCE.drawPolygon();
			case POLYGONS_M:
				return GeopediaTerms.INSTANCE.multiPolygons();
		default:
			break;
		}
		return geomType.name();
	}

	public void setGeometryType(GeomType geometryType) {
		geomTypeLbl = new BoldText(getGeometryTypeI18NString(geometryType));
		add(geomTypeLbl);
	}

	public void addToMap() {
		mapWidget.add(this);
	}

	public void showError(String error) {
		geomTypeLbl.removeFromParent();
		clear();
		add(geomLabel);
		add(new Image(Theme.getTheme().standardIcons().error()));
		addStyleName("error");
		geomLabel.setText(error);
	}

}
