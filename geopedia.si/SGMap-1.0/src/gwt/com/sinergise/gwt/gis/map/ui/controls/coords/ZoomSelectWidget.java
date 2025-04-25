package com.sinergise.gwt.gis.map.ui.controls.coords;


import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.display.ScaleLevelsSpec.ZoomLevelsDisp;
import com.sinergise.common.geometry.util.CoordStringUtil;
import com.sinergise.common.geometry.util.CoordUtil;
import com.sinergise.common.util.format.NumberFormatUtil;
import com.sinergise.common.util.format.NumberFormatter;
import com.sinergise.gwt.gis.map.util.StyleConsts;

public class ZoomSelectWidget extends HorizontalPanel {
	
	public enum ZoomSelectWidgetType {
		PREDEFINED_ONLY(true, false),
		MANUAL_ONLY(false, true),
		PREDEFINED_AND_MANUAL(true, true);
		
		boolean hasPredefined;
		boolean hasManual;
		ZoomSelectWidgetType(boolean hasPredefined, boolean hasManual) {
			this.hasManual = hasManual;
			this.hasPredefined = hasPredefined;
		}
	}
	
	NumberFormatter nf=NumberFormatUtil.create("0");
	private ListBox zoomBox; // select zoom from drop-down or type it in manually
	private TextBox manualZoom=new TextBox();
	private final ZoomLevelsDisp spec;

	public ZoomSelectWidget(ZoomLevelsDisp spec) {
		this(ZoomSelectWidgetType.PREDEFINED_AND_MANUAL, spec);
	}
	
	public ZoomSelectWidget(ZoomSelectWidgetType type, ZoomLevelsDisp spec) {
		if (spec==null) spec=ScaleLevelsSpec.createStandard(5, 10e6);
		this.spec = spec;
		
		setStylePrimaryName(StyleConsts.ZOOM_SELECT_WIDGET);
		
		if(type.hasPredefined) {
			add(zoomBox = buildZoomBox());
			zoomBox.addStyleDependentName("predefined");
		}
		if(type.hasManual) {
			add(manualZoom);
			manualZoom.addKeyDownHandler(new KeyDownHandler() {
				
				@Override
				public void onKeyDown(KeyDownEvent event) {
					if (KeyCodes.KEY_ENTER==event.getNativeKeyCode()) {
						setScale(getScale());
					}
				}
			});
			manualZoom.addStyleDependentName("manual");
		}
	}
	
	public void addKeyDownHandler(KeyDownHandler handler) {
		if(zoomBox != null) {
			zoomBox.addKeyDownHandler(handler);
		}
		if(manualZoom != null) {
			manualZoom.addKeyDownHandler(handler);
		}
	}
	public void addKeyPressHandler(KeyPressHandler handler) {
		if(manualZoom != null) {
			manualZoom.addKeyPressHandler(handler);
		}
	}

	public double getScale() {
		if (manualZoom != null && manualZoom.getText() != null) {
			try {
				return CoordStringUtil.parseScale(manualZoom.getText(), spec.minWorldPerDisp(), spec.maxWorldPerDisp());
			} catch (Exception e) {
			}
		}
		return Double.NaN;
	}
	
	public void setScale(double scale) {
		if(zoomBox != null) {
			int idx=indexOfValue(String.valueOf(scale));
			if (idx<0) {
				zoomBox.setSelectedIndex(0);
			} else {
				zoomBox.setSelectedIndex(idx);
			}
		}
		updateManualZoom(scale);
	}
	
	private void updateManualZoom(double scale) {
		manualZoom.setText(CoordUtil.formatScale(scale,null));
	}

	protected int indexOfValue(String value) {
		if (value==null) value="";
		for (int i = 0; i < zoomBox.getItemCount(); i++) {
			if (value.equals(zoomBox.getValue(i))) return i;
		}
		return -1;
	}

	public Widget getCellWidget(int i) {
		if (i==0) return zoomBox;
		if (i==1) return manualZoom;
		return null;
	}
	
	// GUI
	private ListBox buildZoomBox() {
		ListBox listBox = new ListBox(false);
		listBox.addItem("", "");
		for (int i = spec.getMaxLevelId(); i >= spec.getMinLevelId(); i--) {
			double sc = spec.worldPerDisp(i);
			listBox.addItem("1:"+ CoordStringUtil.getScaleDenominatorString(sc, 0), String.valueOf(sc));
		}
		listBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				int sel=zoomBox.getSelectedIndex();
				if (sel>=0) {
					String val=zoomBox.getValue(sel);
					if (val!=null && val.length()>1) {
						try {
							updateManualZoom(new Double(val).doubleValue());
							return;
						} catch (Exception e) {
						}
					}
				}
				updateManualZoom(Double.NaN);
			}
		});
		return listBox;
	}
}
