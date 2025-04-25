package com.sinergise.gwt.gis.map.ui.controls.coords;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ChangeListenerCollection;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CrsDescriptor;
import com.sinergise.common.geometry.crs.LatLonCRS;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.util.CoordStringUtil;
import com.sinergise.common.geometry.util.CoordinateFormat;
import com.sinergise.common.util.Util;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.gwt.util.html.CSS;

@SuppressWarnings("deprecation")
public class CoordinateDisplayWidget extends Composite implements SourcesChangeEvents {
	
	private Logger logger = LoggerFactory.getLogger(CoordinateDisplayWidget.class);
	
	CoordSystemsCombo csSelection=new CoordSystemsCombo();
	{
		csSelection.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				updateText();
			}
		});
	}
	CrsDescriptor curCs;
	Point curValue=null;

	HasText valueLabel;

	private ChangeListenerCollection listeners;
	
	public CoordinateDisplayWidget(CRS ...crss) {
		this();
		setSystems(crss);
	}
	
	public CoordinateDisplayWidget() {
		valueLabel=createValueWidget();
		FlowPanel fp=new FlowPanel();
		fp.add(getCellWidget(0));
		fp.add(getCellWidget(1));
		initWidget(fp);
	}
	
	protected HasText createValueWidget() {
		Label ret=new Label("",false);
		CSS.fontSize(ret.getElement(), "85%");
		return ret;
	}
	
	@Override
	public void addChangeListener(ChangeListener listener) {
		if (listeners==null) listeners=new ChangeListenerCollection();
		listeners.add(listener);
	}
	
	@Override
	public void removeChangeListener(ChangeListener listener) {
		if (listeners==null) return;
		listeners.remove(listener);
	}
	
	protected void fireChange() {
		if (listeners==null) return;
		listeners.fireChange(this);
	}
	public Widget getCellWidget(int i) {
		if (i==0) return csSelection;
		if (i==1) return (Widget)valueLabel;
		return null;
	}

	@SuppressWarnings("rawtypes")
	public void updateText() {
		Point oldValue = curValue;
		try {
		CrsDescriptor target=csSelection.getSelectedCrs();
		logger.trace("In value: "+curCs+" "+curValue); 
		if (target!=null && curValue!=null && curCs != null) {
			Transform tr=getTransform(curCs.system, target.system);
			
			if (tr!=null) {
				Point targetPt=tr.point(curValue, new Point());
				valueLabel.setText(formatCoord(target.system, targetPt));
				logger.trace("Out value: "+target+" "+ targetPt); 
				return;
			}
			
			int tgtIdx=csSelection.indexOf(curCs);
			if (tgtIdx>=0) {
				csSelection.setSelectedCrs(curCs);
				valueLabel.setText(formatCoord(curCs.system, curValue));
				return;
			}
			}
			valueLabel.setText("");
			return;
		} finally {
			try {
				if (!Util.safeEquals(oldValue, curValue)) fireChange();
			} catch (Exception e) {
				logger.error("Firing change",e);
				e.printStackTrace();
			}
		}
		
	}

	public void setValue(CRS crs, HasCoordinate value) {
		if (value instanceof Point) setValue(crs, (Point)value);
		else if (value == null) setValue(crs, (Point)null); 
		else setValue(crs, new Point(value));
	}
	
	public void setValue(CRS crs, Point value) {
		curCs=csSelection.descriptorFor(crs);
		curValue=value;
		updateText();
	}
	

	public void setSelectedCrs(CRS system) {
		csSelection.setSelectedCrs(csSelection.descriptorFor(system));
		updateText();
	}
	
	public CrsDescriptor getSelectedCRS() {
		return csSelection.getSelectedCrs();
	}
	
	public Point getCoords() {
		if (curCs == null) return null;
		return getCoords(curCs.system);
	}
	
	public Point getCoords(CRS crs) {
		if (curCs == null) {
			return null;
		}

		if (crs == curCs.system) {
			return curValue;
		}

		Transform<?,?> tr = getTransform(curCs.system, crs);
		if (tr != null) {
			return tr.point(curValue, new Point());
		}

		return null;
	}
	
	public CrsDescriptor getCoordsCrs() {
		return curCs;
	}
	
	public void setSystems(CrsDescriptor[] systems) {
		csSelection.setSystems(systems);
	}
	
	public void setSystems(CRS ...systems) {
		csSelection.setSystems(systems);
	}

	public static String formatCoord(CRS system, Point value) {
		if (system instanceof LatLonCRS) {
			return CoordinateFormat.LAT_LON.format(system, value);
		}
		return CoordinateFormat.XY.format(system, value);
	}

	public static <A extends CRS, B extends CRS> Transform<A,B> getTransform(A source, B target) {
		return Transforms.find(source, target);
	}

	public static Point parseCoord(CRS crs, String text) {
		return CoordStringUtil.parseCoords(crs, text);
	}
}

