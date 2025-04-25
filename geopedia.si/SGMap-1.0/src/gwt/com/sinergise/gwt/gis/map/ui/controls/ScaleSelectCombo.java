/*
 *
 */
package com.sinergise.gwt.gis.map.ui.controls;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.geometry.display.event.CoordinatesListener;
import com.sinergise.common.geometry.util.CoordStringUtil;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.util.format.Format;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.gis.resources.icons.GisStandardIcons;
import com.sinergise.gwt.ui.ActionUtilGWT;
import com.sinergise.gwt.ui.CompositeExt;


public class ScaleSelectCombo extends CompositeExt {
	

	private static final int IDX_CUSTOM = 0;
	final MapComponent map;
	final ScaleLevelsSpec zooms;
	boolean ignoreEvents = false;
	boolean allowCustomScales = true;
	public static GisStandardIcons STANDARD_ICONS = GWT.create(GisStandardIcons.class);
	
	private Label label = new Label("1:");
	ListBox box = new ListBox(false);
	private HorizontalPanel panel = new HorizontalPanel();
	
	final Widget moreButton = ActionUtilGWT.createActionButton(new Action("") {
		{
			setStyle(StyleConsts.SCALE_SELECT_MORE_BUTTON);
			setIcon(STANDARD_ICONS.coordinates());
		}
		@Override
		protected void actionPerformed() {
			final String oldVal = String.valueOf((int) map.getScale());
			final ScalePromtDialog d = new ScalePromtDialog(oldVal);
			d.addCloseHandler(new CloseHandler<PopupPanel>() {
				@Override
				public void onClose(CloseEvent<PopupPanel> event) {
					if ((d.newValue != null) && (!oldVal.equalsIgnoreCase(d.newValue.toString()))) {
						double scale = CoordStringUtil.parseScale(d.newValue.toString(), map.coords.bounds
							.minScale(), map.coords.bounds.maxScale());
						map.setScale(scale);
					}
				}
			});
			d.showRelativeTo(moreButton);

			d.newValueEdtor.setTabIndex(0);
			d.newValueEdtor.setFocus(true);
		}
	});
	
	public ScaleSelectCombo(MapComponent mapComponent, ScaleLevelsSpec spec) {
		super();
		panel.add(label);
		panel.add(box);
		panel.add(moreButton);
		
		panel.setCellHeight(label, "100%");
		panel.setCellVerticalAlignment(label, HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setCellHeight(box, "100%");
		panel.setCellVerticalAlignment(box, HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setCellHeight(moreButton, "100%");
		
		initWidget(panel);
		setStylePrimaryName(StyleConsts.SCALE_SELECT_COMBO);
		
		box.setVisibleItemCount(1);
		this.map = mapComponent;
		this.zooms = spec;
		if (allowCustomScales) box.addItem(" ", " ");
		for (int i = spec.getMaxLevelId(); i >= spec.getMinLevelId(); i--) {
			double lev = spec.scale(i, map.coords.pixSizeInMicrons);
			box.addItem(CoordStringUtil.getScaleDenominatorString(lev, 0), String.valueOf(lev));
		}
		fixItemsWidth();
		map.coords.addCoordinatesListener(new CoordinatesListener() {
			@Override
			public void coordinatesChanged(double newX, double newY, double newScale,
					boolean coordsChanged, boolean scaleChanged) {
				if (ignoreEvents) return;
				if (scaleChanged) {
					ignoreEvents = true;
					setScale(newScale);
					ignoreEvents = false;
				}
			}
			
			@Override
			public void displaySizeChanged(int newWidthPx, int newHeightPx) {}
		});
		box.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				if (ignoreEvents) return;
				int idx = box.getSelectedIndex();
				if (allowCustomScales) {
					if (idx == IDX_CUSTOM) return;
					box.setItemText(IDX_CUSTOM, " ");
				}
				ignoreEvents = true;
				map.setScale(zooms.scale(zooms.getMaxLevelId() - idx + (allowCustomScales ? 1 : 0),
					map.coords.pixSizeInMicrons));
				map.repaint(500);
				ignoreEvents = false;
			}
		});
		
		addStyleName(StyleConsts.SCALE_SELECT_WIDGET);
	}
	
	@Override
	public void setStylePrimaryName(String style) {
		moreButton.removeStyleName(style + "-moreButton");
		super.setStylePrimaryName(style);
		label.setStylePrimaryName(style + "-label");
		box.setStylePrimaryName(style + "-combo");
		moreButton.addStyleName(style + "-moreButton");
	}
	
	private void fixItemsWidth() {
		int maxLen = 0;
		for (int i = 0; i < box.getItemCount(); i++) {
			String val = box.getItemText(i).trim();
			if (val != null && val.length() > maxLen) {
				maxLen = val.length();
			}
		}
		for (int i = 0; i < box.getItemCount(); i++) {
			box.setItemText(i, Format.padWith(box.getItemText(i).trim(), ' ', maxLen, true));
		}
	}
	
	public void setScale(double scale) {
		String scaleStr = CoordStringUtil.getScaleDenominatorString(scale, 0);
		if (!allowCustomScales) {
			box.setSelectedIndex(indexOfText(scaleStr));
			return;
		}
		int idx = indexOfText(scaleStr);
		// System.out.println(scale+" "+CoordStringUtil.getScaleString(scale));
		if (idx > 0) {
			box.setItemText(IDX_CUSTOM, " ");
			fixItemsWidth();
		} else {
			idx = 0;
			box.setItemText(IDX_CUSTOM, scaleStr);
			fixItemsWidth();
		}
		box.setSelectedIndex(idx);
	}
	
	public int indexOf(double scale) {
		return zooms.nearestZoomLevel(scale, map.coords.pixSizeInMicrons) - zooms.getMinLevelId()
			+ (allowCustomScales ? 1 : 0);
	}
	
	public int indexOfText(String scaleString) {
		for (int i = box.getItemCount() - 1; i >= 0; i--) {
			if (scaleString.equals(box.getItemText(i).trim())) return i;
		}
		return -1;
	}
}
