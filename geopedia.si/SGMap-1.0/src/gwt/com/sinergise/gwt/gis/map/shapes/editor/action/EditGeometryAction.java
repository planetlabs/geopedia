package com.sinergise.gwt.gis.map.shapes.editor.action;

import static com.sinergise.gwt.gis.i18n.Messages.UI_MESSAGES;
import static com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorControllerBase.PROP_ACTIVE;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.feature.HasFeatureRepresentations;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.ui.action.ToggleAction;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorCallback;
import com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorController;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.attributes.FeatureActionsProvider;
import com.sinergise.gwt.ui.resources.Theme;

public abstract class EditGeometryAction extends ToggleAction {
	
	public interface EditGeometryActionFactory {
		EditGeometryAction createAction(GeometryEditorController controller, CFeature feature);
	}
	
	public static class EditGeometryActionProvider implements FeatureActionsProvider {
		
		final GeometryEditorController controller;
		final MapComponent map;
		final EditGeometryActionFactory factory;
		
		public EditGeometryActionProvider(GeometryEditorController controller, EditGeometryActionFactory factory) {
			this.controller = controller;
			this.map = controller.getMap();
			this.factory = factory;
		}
		
		@Override
		public List<? extends Action> getFeatureActions(HasFeatureRepresentations fRep, Object requestor) {
			CFeature feature = CFeatureUtils.getCFeatureIfHasExactlyOne(fRep);
			if (feature != null) { //handles only single features
				FeatureDataLayer layer = map.getLayers().findByFeatureType(feature.getFeatureTypeName());
				if (layer != null && layer.isEditable()) {
					return Collections.singletonList(factory.createAction(controller, feature));
				}
			}
			return Collections.emptyList();
		}
	}
	
	
	
	final CFeature feature;
	final MapComponent map;
	final GeometryEditorController controller;
	
	final GeometryEditorCallback callback;

	public EditGeometryAction(final GeometryEditorController controller, final CFeature feature) {
		super("editFeature");
		this.feature = feature;
		this.controller = controller;
		this.map = controller.getMap();
		this.callback = new GeometryEditorCallback() {
			@Override
			public void onConfirm(Collection<CFeature> modified, SGAsyncCallback<Void> confirmCallback) {
				doSave(modified, confirmCallback);
			}
			
			@Override
			public void onCancel() {
				//nothing to do...
			}
		};
		
		controller.addPropertyChangeListener(new PropertyChangeListener<Object>() {
			@Override
			public void propertyChange(Object sender, String propertyName, Object oldValue, Object newValue) {
				if (PROP_ACTIVE.equals(propertyName) && newValue instanceof Boolean 
					&& feature.equals(controller.getActiveFeature())) 
				{
					setSelected(((Boolean)newValue).booleanValue());
				}
			}
		});
		
		setDescription(UI_MESSAGES.geometryEditor_actionEdit());
		setIcon(Theme.getTheme().standardIcons().edit());
	}
	
	@Override
	protected void selectionChanged(boolean doEdit) {
		if (!doEdit && controller.isActive()) {
			controller.cancel();
		} else if (doEdit) {
			controller.editFeatureGeometry(feature, callback);
		}
	}
	
	protected abstract void doSave(Collection<CFeature> modified, SGAsyncCallback<Void> confirmCallback);
	
}
