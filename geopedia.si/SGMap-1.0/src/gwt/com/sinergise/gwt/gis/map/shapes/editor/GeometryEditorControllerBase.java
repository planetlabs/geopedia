package com.sinergise.gwt.gis.map.shapes.editor;

import static com.sinergise.gwt.gis.i18n.Messages.UI_MESSAGES;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.topo.ITopoMap;
import com.sinergise.common.geometry.topo.TopoBuilder;
import com.sinergise.common.gis.app.ApplicationContext;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.shapes.snap.ChainedSnapProvider;
import com.sinergise.common.gis.map.shapes.snap.SnapProvider;
import com.sinergise.common.ui.messages.MessageListener;
import com.sinergise.common.util.event.selection.Selectable;
import com.sinergise.common.util.event.selection.SelectableImpl;
import com.sinergise.common.util.event.selection.SourcesToggleEvents;
import com.sinergise.common.util.event.selection.ToggleListener;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasEnvelope;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.common.util.state.gwt.PropertyChangeListenerCollection;
import com.sinergise.common.util.state.gwt.SourcesPropertyChangeEvents;
import com.sinergise.gwt.gis.map.shapes.editor.TopoEditor.ITopoEditorBehavior;
import com.sinergise.gwt.gis.map.shapes.editor.TopoEditor.TopoEditorModificationListener;
import com.sinergise.gwt.gis.map.ui.MapComponent;

public abstract class GeometryEditorControllerBase implements SourcesPropertyChangeEvents<Object>, HasEnvelope {
	
	public static String PROP_ACTIVE 			= "active";
	public static String PROP_ACTIVE_LAYER 		= "activeLayer";
	
	private final static Logger logger = LoggerFactory.getLogger(GeometryEditorControllerBase.class);
	
	protected final MapComponent 	map;
	protected final TopoEditor 		editor;
	protected final TopoBuilder 	topoBuilder;
	
	protected double gridSize = 0;
	
	protected MessageListener msgListener = ApplicationContext.getInstance().getAppMessageListener();
	protected final PropertyChangeListenerCollection<Object> propChangeListeners = new PropertyChangeListenerCollection<Object>();

	
	protected GeometryEditorCallback editorCallback;
	protected FeatureDataLayer activeFeatureLayer = null;
	
	protected Selectable activeSelectable = new SelectableImpl(false);
	
	protected final ChainedSnapProvider snapProvider = new ChainedSnapProvider();
	
	public GeometryEditorControllerBase(MapComponent mapComp) {
		this.map = mapComp;
		this.editor = new TopoEditor(mapComp, createTopoEditorBehavior());
		this.topoBuilder = new TopoBuilder();
		
		activeSelectable.addToggleListener(new ToggleListener() {
			@Override
			public void toggleStateChanged(SourcesToggleEvents source, boolean newOn) {
				setActive(newOn);
			}
		});
		
		editor.setSnapProvider(snapProvider);
	}
	
	public Selectable getActiveSelecable() {
		return activeSelectable;
	}
	
	public void confirm() {
		//prevent double clicking on the confirm button
		if (confirmInProgress) {
			return;
		}
		
		Collection<CFeature> modified = getModifiedFeatureCopies();
		if (validate(modified) && editorCallback != null) {
			confirmInProgress = true;
			editorCallback.onConfirm(modified, confirmationCallback);
		}
	}
	
	@SuppressWarnings("unused")
	protected boolean validate(Collection<CFeature> modified) {
		return true;
	}
	
	public void cancel() {
		if (confirmInProgress) {
			return;
		}
		
		if (editorCallback != null) {
			editorCallback.onCancel();
		}
		stopEditing();
	}
	
	protected void setActive(boolean newActive) {
		boolean oldActive = isActive();
		if (oldActive == newActive) {
			return;
		}
		
		activeSelectable.setSelected(newActive);
		editor.setActive(newActive);
		editor.setVisible(newActive);
		propChangeListeners.fireChange(this, PROP_ACTIVE, Boolean.valueOf(oldActive), Boolean.valueOf(newActive));
		
		map.getDefaultHighlightLayer().setOn(!newActive);
	}
	
	public boolean isActive() {
		return editor.isActive();
	}
	
	public void editTopology(ITopoMap topology) {
		setActive(true);
		editor.setTopology(topology);
	}
	
	protected void stopEditing() {
		setActive(false);
		cleanup();
	}
	
	protected void cleanup() {
		editorCallback = null;
		editor.topoReset();
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener<Object> listener) {
		propChangeListeners.add(listener);
	}
	
	@Override
	public void removePropertyChangeListener(PropertyChangeListener<? super Object> listener) {
		propChangeListeners.remove(listener);
	}
	
	public void addModificationListener(TopoEditorModificationListener l) {
		editor.addModificationListener(l);
	}
	
	public void removeModificationListener(TopoEditorModificationListener l) {
		editor.removeModificationListener(l);
	}
	
	@Override
	public Envelope getEnvelope() {
		return editor.getEnvelope();
	}
	
	public Geometry getActiveGeometry() {
		return null;
	}
	
	public void setGridSize(double gridSize) {
		this.gridSize = gridSize;
		
		topoBuilder.setGridSize(gridSize);
		editor.getTopoModel().setGridSize(gridSize);
	}
	
	public double getGridSize() {
		return gridSize;
	}
	
	public MapComponent getMap() {
		return map;
	}
	
	public TopoEditor getTopoEditor() {
		return editor;
	}
	
	protected ITopoEditorBehavior createTopoEditorBehavior() {
		return new TopoEditor.DefaultTopoEditorBehavior();
	}
	
	protected void setActiveFeatureLayer(String featureTypeName) {
		FeatureDataLayer featureLayer = map.getLayers().findByFeatureType(featureTypeName);
		if (featureLayer != null) {
			setActiveFeatureLayer(featureLayer);
		} else {
			handleError(UI_MESSAGES.geometryEditor_errorLayerNotFound(featureTypeName), null, true);
		}
	}
	
	protected void setActiveFeatureLayer(FeatureDataLayer featureLayer) {
		FeatureDataLayer oldActive = activeFeatureLayer;
		activeFeatureLayer = featureLayer;
		propChangeListeners.fireChange(this, PROP_ACTIVE_LAYER, oldActive, activeFeatureLayer);
		
		if (!activeFeatureLayer.isEditable()) {
			handleError(UI_MESSAGES.geometryEditor_errorLayerNotEditable(featureLayer.getFeatureTypeName()), null, true);
		}
	}
	
	protected boolean confirmInProgress = false;
	protected final SGAsyncCallback<Void> confirmationCallback = new SGAsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
			confirmInProgress = false;
			updateForChanges();
			stopEditing();
		}
		
		@Override
		public void onFailure(Throwable caught) {
			confirmInProgress = false;
			handleError(UI_MESSAGES.geometryEditor_errorWhileSavingFeature(caught.getMessage()), caught, false);
		}
	};
	
	protected void updateForChanges() {
		if (activeFeatureLayer != null) {
			((Layer)activeFeatureLayer).setDirty();
		}
		
		map.getDefaultHighlightLayer().refresh();
		map.repaint(100);
	}
	
	public void registerSnapProvider(SnapProvider provider) {
		snapProvider.registerSnapProvider(provider);
	}
	
	public void registerSnapProvider(int index, SnapProvider provider) {
		snapProvider.registerSnapProvider(index, provider);
	}
	
	public Selectable getSelectableForLabelsDisplay() {
		return editor.getSelectableForLabelsDisplay();
	}
	
	protected abstract Collection<CFeature> getModifiedFeatureCopies();

	protected void handleError(Throwable ex, boolean cancelEditing) {
		handleError(UI_MESSAGES.error()+": "+ex.getMessage(), ex, cancelEditing);
	}
	
	protected void handleError(String msg, Throwable ex, boolean cancelEditing) {
		confirmInProgress = false;
		logger.error(msg, ex);
		msgListener.onMessage(MessageType.ERROR, msg);
		if (cancelEditing) {
			stopEditing();
		}
	}
	
	
	public interface SupportsReset {
		void reset();
	}
	
}
