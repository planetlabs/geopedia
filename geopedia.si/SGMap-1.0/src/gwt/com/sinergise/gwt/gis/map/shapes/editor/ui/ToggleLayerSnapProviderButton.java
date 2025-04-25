package com.sinergise.gwt.gis.map.shapes.editor.ui;

import static com.sinergise.gwt.gis.i18n.Messages.UI_MESSAGES;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.sinergise.common.gis.map.shapes.snap.SnapProvider;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.ui.maingui.extwidgets.SGToggleButton;

public class ToggleLayerSnapProviderButton extends SGToggleButton {
	
	public ToggleLayerSnapProviderButton(SnapProvider snapProvider) {
		this(snapProvider, 
			GisTheme.getGisTheme().gisStandardIcons().magnet(), 
			UI_MESSAGES.geometryEditor_toggleLayerSnap());
	}

	public ToggleLayerSnapProviderButton(SnapProvider snapProvider, ImageResource icon, String title) {
		super(new Image(icon), snapProvider.getEnabled());
		setTitle(title);
	}

}
