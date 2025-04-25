package com.sinergise.gwt.gis.map.shapes.editor.ui;

import static com.sinergise.gwt.gis.i18n.Messages.UI_MESSAGES;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.sinergise.common.util.event.ValueChangeListener;
import com.sinergise.common.util.event.selection.SourcesToggleEvents;
import com.sinergise.common.util.event.selection.ToggleListener;
import com.sinergise.gwt.gis.map.shapes.snap.PolygonAreaSnapProvider;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.ui.editor.DoubleEditor;
import com.sinergise.gwt.ui.maingui.extwidgets.SGToggleButton;

public class PolygonAreaSnapProviderWidget extends Composite {
	
	private final PolygonAreaSnapProvider snapProvider;
	
	private SGToggleButton butOn;
	private DoubleEditor tbArea; //TODO: because of edit mode, left and right arrow keys do not work in the editor.
	private FlowPanel pOptions;
	
	public PolygonAreaSnapProviderWidget(PolygonAreaSnapProvider snapProvider) {
		this(snapProvider, GisTheme.getGisTheme().gisStandardIcons().areaSnap(), UI_MESSAGES.geometryEditor_toggleAreaSnap());
	}
	
	public PolygonAreaSnapProviderWidget(PolygonAreaSnapProvider snapProvider, ImageResource icon, String title) {
		this.snapProvider = snapProvider;
		
		init(icon, title);
		updateUI();
	}
	
	private void init(ImageResource icon, String title) {
		tbArea = new DoubleEditor();
		tbArea.setVisibleLength(9);
		
		butOn = new SGToggleButton(new Image(icon), snapProvider.getEnabled());
		butOn.setTitle(title);
		
		pOptions = new FlowPanel();
		pOptions.add(tbArea);
		pOptions.add(new InlineLabel(UI_MESSAGES.geometryEditor_areaValue_sqmetre("")));
		
		FlowPanel pMain = new FlowPanel();
		pMain.setStyleName("polygonAreaSnapWidget");
		pMain.add(butOn);
		pMain.add(pOptions);
		
		initWidget(pMain);
		
		snapProvider.getEnabled().addToggleListener(new ToggleListener() {
			@Override
			public void toggleStateChanged(SourcesToggleEvents source, boolean newOn) {
				updateUI();
				if (newOn && !snapProvider.hasTargetArea()) {
					tbArea.setFocus(true);
				}
			}
		});
		
		snapProvider.addValueChangeListener(new ValueChangeListener<Double>() {
			@Override
			public void valueChanged(Object sender, Double oldValue, Double newValue) {
				updateUI();
			}
		});
		
		tbArea.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				applyValue();
			}
		});
		
		tbArea.addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				updateUI();
			}
		});
		
	}
	
	private void applyValue() {
		snapProvider.setTargetArea(tbArea.getEditorValue());
	}
	
	private void updateUI() {
		if (snapProvider.getTargetArea() != null) {
			tbArea.setEditorValue(snapProvider.getTargetArea());
		} else {
			tbArea.setEditorValue(null);
		}
		
		pOptions.setVisible(snapProvider.getEnabled().isSelected());
	}
}
