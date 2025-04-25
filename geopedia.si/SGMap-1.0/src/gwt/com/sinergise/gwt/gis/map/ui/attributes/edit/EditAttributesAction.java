package com.sinergise.gwt.gis.map.ui.attributes.edit;

import static com.sinergise.gwt.gis.i18n.Messages.UI_MESSAGES;

import com.sinergise.common.ui.action.Action;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.gwt.ui.resources.Theme;

public class EditAttributesAction extends Action {

	private final FeatureAttrDetailsTabWithEdit attributesTab;

	public EditAttributesAction(final AttributeEditorController controller, final FeatureAttrDetailsTabWithEdit attributesTab) {
		super("editFeature");
		this.attributesTab = attributesTab;

		setDescription(UI_MESSAGES.attributesEditor_actionEdit());
		setIcon(Theme.getTheme().standardIcons().edit());
		
		controller.addPropertyChangeListener(new PropertyChangeListener<Object>() {
			@Override
			public void propertyChange(Object sender, String propertyName, Object oldValue, Object newValue) {
				if(AttributeEditorController.PROP_ACTIVE.equals(propertyName) && newValue instanceof Boolean) {
					setExternalEnabled(!((Boolean) newValue).booleanValue());
				}
			}
		});
		
		setExternalEnabled(!controller.isActive());
	}

	@Override
	protected void actionPerformed() {
		attributesTab.editFeature();
	}

}
