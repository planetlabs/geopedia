package com.sinergise.gwt.gis.map.shapes.editor.ui;

import static com.sinergise.gwt.gis.i18n.Messages.UI_MESSAGES;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorControllerBase.SupportsReset;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.resources.Theme;

public class ResetGeometryEditorButton extends SGPushButton{
	
	public ResetGeometryEditorButton(SupportsReset resetable) {
		this(resetable, UI_MESSAGES.geometryEditor_reset(), Theme.getTheme().standardIcons().refresh());
	}

	public ResetGeometryEditorButton(final SupportsReset resetable, String text, ImageResource icon) {
		super(icon, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				resetable.reset();
			}
		});
		setTitle(text);
	}

}
