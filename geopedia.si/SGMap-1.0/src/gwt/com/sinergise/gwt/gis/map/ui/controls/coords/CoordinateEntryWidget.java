package com.sinergise.gwt.gis.map.ui.controls.coords;



import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.TextBox;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CrsDescriptor;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.gwt.gis.map.util.StyleConsts;

public class CoordinateEntryWidget extends CoordinateDisplayWidget {
	
	@Override
	protected HasText createValueWidget() {
		TextBox input = new TextBox();
		input.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				updateCoord();
			}
		});
		input.addKeyDownHandler(new KeyDownHandler() {

			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					updateCoord();
				}
			}
		});
		input.setVisibleLength(24);
		return input;
	}
	
	public CoordinateEntryWidget() {
		setStylePrimaryName(StyleConsts.COORDS_ENTRY_WIDGET);
	}
	
	public CoordinateEntryWidget(CRS ...crss) {
		this();
		setSystems(crss);
	}
	
	public CoordinateEntryWidget(CrsDescriptor ...crss) {
		this();
		setSystems(crss);
	}

	public void updateCoord() {
		curCs=csSelection.getSelectedCrs();
		if (curCs==null) {
			curValue=null;
			updateText();
			return;
		}
		setValue(curCs.system, parseCoord(curCs.system, valueLabel.getText()));
	}

	public Point getValue(CRS targetCs) {
		if (targetCs == null || curCs == null) return null;
		if (targetCs.equals(curCs.system)) return curValue;
		Transform<CRS, CRS> tr=getTransform(curCs.system, targetCs);
		if (tr==null) return null;
		return tr.point(curValue, new Point());
	}

	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		if(valueLabel instanceof HasKeyDownHandlers) {
			return ((HasKeyDownHandlers)valueLabel).addKeyDownHandler(handler);
		}
		return null;
	}
	
}

