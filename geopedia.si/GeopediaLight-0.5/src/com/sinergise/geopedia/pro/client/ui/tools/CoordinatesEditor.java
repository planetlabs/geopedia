package com.sinergise.geopedia.pro.client.ui.tools;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.LatLonCRS;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.display.DisplayBounds;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.util.CoordStringUtil;
import com.sinergise.common.geometry.util.CoordinateFormat;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.core.crs.CRSSettings;
import com.sinergise.geopedia.light.client.i18n.LightMessages;
import com.sinergise.gwt.ui.BoldText;
import com.sinergise.gwt.ui.editor.DoubleEditor;
import com.sinergise.gwt.ui.handler.EnterKeyDownHandler;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;

//TODO: refactor! why? what is wrong and where is it wrong??
public class CoordinatesEditor extends FlowPanel {

	AbstractCoordinateEditor coord1Editor;
	AbstractCoordinateEditor coord2Editor;
	
	private final List<EnterKeyDownHandler> enterHandlers = new ArrayList<EnterKeyDownHandler>();
	
	public interface AbstractCoordinateEditor {
		public Double getValue();
		public String getValueString();
		public void setValue(double value);
	}
	
	private class GeographicsCoordinateEditor extends FlowPanel implements AbstractCoordinateEditor {
		
		private SGTextBox editor;
		private Double coordinateValue = null;
		private FlowPanel notifPanel = null;
		  //private CoordinateFormat formatXY = new CoordinateFormat("XA YA: XC{0.###} YC{0.###}");
		private CoordinateFormat format=new CoordinateFormat("[Xd°m''S{0.##}\"]{Yd°m''S{0.##}\"w}");
		private CRS crs;
		private int coordId;
		public GeographicsCoordinateEditor(CRS crs, int coordId, double sampleValue) {
			editor = new SGTextBox();
//			editor.setEmptyText(getFormattedValue(sampleValue));
			editor.setText(getFormattedValue(sampleValue));
			this.coordId=coordId;
			this.crs=crs;
			add(new BoldText(crs.getCoordName(coordId)));
			add(editor);
			notifPanel = new FlowPanel();
			notifPanel.setStyleName("notifPanel");
			add(notifPanel);
			notifPanel.setVisible(false);
			editor.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					parseCoordinates();
				}
			});
			
			editor.addKeyDownHandler(new EnterKeyDownHandler() {
				@Override
				public void onEnterDown(KeyDownEvent event) {
					fireEnterKeyDown(event);
				}
			});
		}
		
		private double applyHemisphere(double coord, String sign, String hemi) {
			if (sign!=null) {
				if (sign.equalsIgnoreCase("-")) {
					coord*=-1;
				}
			} else if (hemi!=null) {
				if (hemi.equalsIgnoreCase("s") || hemi.equalsIgnoreCase("w")) {
					coord*=-1;
				}
			}
			return coord;
		}
		
		private boolean parseCoordinates() {
			coordinateValue=null;
			RegExp regexp = RegExp.compile("^([+-]){0,1}(\\d{1,3})\u00B0([0-5]?[0-9])'([0-5]?[0-9]|[0-5]?[0-9][\\.,]\\d{1,3})\"([NWSEnwse]){0,1}$", "g");
			MatchResult matcher = regexp.exec(editor.getText());
			notifPanel.setVisible(false);
			if(matcher != null) {
				if (!(matcher.getGroup(1)!=null && matcher.getGroup(5)!=null)) {
					String secStr = matcher.getGroup(4).replace(",", ".");
					String minStr = matcher.getGroup(3);
					String degStr = matcher.getGroup(2);
					coordinateValue=Double.parseDouble(degStr)
							+ (Double.parseDouble(minStr)/60)
							+ (Double.parseDouble(secStr)/3600);
					coordinateValue = applyHemisphere(coordinateValue, matcher.getGroup(1),  matcher.getGroup(5));
					return true;
				}
			} 
			regexp = RegExp.compile("^([+-]){0,1}(\\d{1,3}[\\.,]\\d{6})[\u00B0]{0,1}([NWSEnwse]){0,1}$","g");
			matcher = regexp.exec(editor.getText());
			if (matcher != null) {
				if (!(matcher.getGroup(1)!=null && matcher.getGroup(3)!=null)) {
					String str = matcher.getGroup(2).replace(",",".");
					coordinateValue = Double.parseDouble(str);
					coordinateValue = applyHemisphere(coordinateValue, matcher.getGroup(1),  matcher.getGroup(3));
					return true;
				}
			}
			if (!StringUtil.isNullOrEmpty(editor.getText())) {
				notifPanel.clear();
				notifPanel.add(new InlineLabel(LightMessages.INSTANCE.wrongCoordinate() + " 46°05'09,23\")"));
				notifPanel.setVisible(true);
			}
			return false;
		}
		
		public Double getValue() {
			parseCoordinates();
			return coordinateValue;
		}
		
		@Override
		public String getValueString() {
			return editor.getText();
		}

		private String getFormattedValue(double value) {			
			if (coordId==0) {
				Point  p = new Point(value,0);
				String formatted = format.format(crs, p);
				return formatted.substring(1,formatted.indexOf(']'));
			} else {
				Point p = new Point(0,value);
				String formatted = format.format(crs, p);
				return formatted.substring(formatted.indexOf('{')+1,formatted.indexOf('}')-1);
			}
		}
		
		@Override
		public void setValue(double val) {			
			editor.setValue(getFormattedValue(val));
		}

	}
	
	private class ProjectedCoordinateEditor extends FlowPanel implements AbstractCoordinateEditor {
		DoubleEditor editor;
		
		public ProjectedCoordinateEditor(String coordName, double sampleValue) {
			NumberFormat format = NumberFormat.getFormat("#.00");
			editor = new DoubleEditor(format);
			editor.setEmptyText(format.format(sampleValue));
			
			add(new BoldText(coordName));
			add(editor);
			
			editor.addKeyDownHandler(new EnterKeyDownHandler() {
				@Override
				public void onEnterDown(KeyDownEvent event) {
					fireEnterKeyDown(event);
				}
			});
		}
		public Double getValue() {
			return editor.getEditorValue();
		}
		
		@Override
		public String getValueString() {
			return editor.getText();
		}
		
		@Override
		public void setValue(double value) {
			editor.setEditorValue(value);
		}
	}

	
	private CRS crs = null;
	
	public void setCRS(CRS  crs) {
		this.crs = crs;
		
		String nameX = crs==null?"X":crs.getCoordName(0);
		String nameY = crs==null?"Y":crs.getCoordName(1);
		clear();
		CRSSettings crsSettings = ClientGlobals.crsSettings; 
		DisplayBounds mapBounds = crsSettings.getMainCRS().getBounds();
		Point centrePoint = new Point(mapBounds.mbr.getMinX()+ (mapBounds.mbr.getMaxX()-mapBounds.mbr.getMinX())*0.5,
									  mapBounds.mbr.getMinY()+ (mapBounds.mbr.getMaxY()-mapBounds.mbr.getMinY())*0.5);
		
		Transform<?, ?> transform = crsSettings.getTransform(crsSettings.getMainCrsId(), crs.getDefaultIdentifier(), false);
		transform.point(centrePoint, centrePoint);
		if (crs instanceof LatLonCRS) {
			GeographicsCoordinateEditor c1e = new GeographicsCoordinateEditor(crs,0, centrePoint.x());
			GeographicsCoordinateEditor c2e = new GeographicsCoordinateEditor(crs,1, centrePoint.y());
			coord1Editor = c1e;
			coord2Editor = c2e;
			add(c1e);
			add(c2e);
			
		} else {
			ProjectedCoordinateEditor c1e = new ProjectedCoordinateEditor(nameX, centrePoint.x());
			ProjectedCoordinateEditor c2e = new ProjectedCoordinateEditor(nameY, centrePoint.y());
			coord1Editor = c1e;
			coord2Editor = c2e;
			add(c1e);
			add(c2e);
		}
		setStyleName("coordinatesEditor");
	}
	
	public Point getCoordinate() {
		if (coord1Editor == null || coord2Editor == null) {
			return null;
		}
		
		return CoordStringUtil.parseCoords(crs, coord1Editor.getValueString() + " " + coord2Editor.getValueString());
	}

	public void setCoordinate1(double x) {
		if (coord1Editor!=null) {
			coord1Editor.setValue(x);
		}
	}
	
	public void setCoordinate2(double y) {
		if (coord2Editor!=null) {
			coord2Editor.setValue(y);
		}
	}
	
	private void fireEnterKeyDown(KeyDownEvent event) {
		for (EnterKeyDownHandler handler : enterHandlers) {
			handler.onEnterDown(event);
		}
	}

	public void addEnterKeyDownHandler(EnterKeyDownHandler handler) {
		enterHandlers.add(handler);
	}
	
	public boolean removeEnterKeyDownHandler(EnterKeyDownHandler handler) {
		return enterHandlers.remove(handler);
	}
	
}
