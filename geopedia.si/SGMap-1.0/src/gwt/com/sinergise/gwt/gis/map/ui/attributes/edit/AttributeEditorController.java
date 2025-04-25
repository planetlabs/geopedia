package com.sinergise.gwt.gis.map.ui.attributes.edit;

import static com.sinergise.gwt.gis.i18n.Messages.UI_MESSAGES;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.sinergise.common.gis.app.ApplicationContext;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureDataSource;
import com.sinergise.common.gis.feature.CFeatureDataSource.TransactionCallback;
import com.sinergise.common.gis.feature.FeatureAccessException;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.ogc.wfs.response.WFSTransactionResponse;
import com.sinergise.common.ui.messages.MessageListener;
import com.sinergise.common.util.event.selection.Selectable;
import com.sinergise.common.util.event.selection.SelectableImpl;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.common.util.state.gwt.PropertyChangeListenerCollection;
import com.sinergise.common.util.state.gwt.SourcesPropertyChangeEvents;
import com.sinergise.gwt.gis.map.ui.MapComponent;

public class AttributeEditorController implements SourcesPropertyChangeEvents<Object> {
	
	public static String PROP_ACTIVE 			= "active";
	
	private final static Logger logger = LoggerFactory.getLogger(AttributeEditorController.class);
	
	protected final MapComponent 	map;
	protected FeatureDataLayer activeFeatureLayer = null;
	protected CFeature activeFeature = null;

	protected final Selectable activeSelectable = new SelectableImpl(false) {
		@Override
		public void setSelected(boolean sel) {
			super.setSelected(sel);
			setActive(sel);
		}
	};

	protected final PropertyChangeListenerCollection<Object> propChangeListeners = new PropertyChangeListenerCollection<Object>();
	protected MessageListener msgListener = ApplicationContext.getInstance().getAppMessageListener();
	protected AttributeEditorCallback editorCallback;
	
	
	public AttributeEditorController(MapComponent mapComp) {
		this.map = mapComp;
	}
	
	public Selectable getActiveSelectable() {
		return activeSelectable;
	}
	
	public boolean confirm(SGAsyncCallback<Void> confirmationCallback) {
		//prevent double clicking on the confirm button
		if (confirmInProgress) {
			return true;
		}
		CFeature modified = activeFeature;
		if (editorCallback != null) {
			confirmInProgress = true;
			editorCallback.onConfirm(modified, confirmationCallback);
		}
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
	
	public void doSave(final SGAsyncCallback<Void> confirmCallback) {
		CFeatureDataSource src = activeFeatureLayer.getFeaturesSource();
		try {
			src.insertOrUpdateFeatures(new TransactionCallback() {
				@Override
				public void onSuccess(WFSTransactionResponse response) {
					onFinished();
					confirmInProgress = false;
					updateForChanges();
					stopEditing();
					confirmCallback.onSuccess(null);
				}

				@Override
				public void onError(FeatureAccessException error) {
					onFinished();
					confirmInProgress = false;
					handleError(UI_MESSAGES.geometryEditor_errorWhileSavingFeature(error.getMessage()), error, false);
					confirmCallback.onFailure(error);
				}

			}, activeFeature);

		} catch (FeatureAccessException e) {
			confirmCallback.onFailure(e);
		}

	}
	
	public void onFinished() {
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				map.repaint(100);
				cancel();
			}
		});
	}

	
	protected void setActive(boolean newActive) {
		boolean oldActive = isActive();
		if (oldActive == newActive) {
			return;
		}
		
		activeSelectable.setSelected(newActive);
		propChangeListeners.fireChange(this, PROP_ACTIVE, Boolean.valueOf(oldActive), Boolean.valueOf(newActive));
	}
	
	public boolean isActive() {
		return activeSelectable.isSelected();
	}
	
	protected void stopEditing() {
		activeFeature = null;
		setActive(false);
		cleanup();
	}
	
	protected void cleanup() {
		editorCallback = null;
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener<Object> listener) {
		propChangeListeners.add(listener);
	}
	
	@Override
	public void removePropertyChangeListener(PropertyChangeListener<? super Object> listener) {
		propChangeListeners.remove(listener);
	}
	
	protected boolean confirmInProgress = false;
	
	
	protected void updateForChanges() {
		if (activeFeatureLayer != null) {
			((Layer)activeFeatureLayer).setDirty();
		}
		
		map.getDefaultHighlightLayer().refresh();
		map.repaint(100);
	}
	
	protected void setActiveFeature(CFeature feature) {
		activeFeatureLayer = map.getLayers().findByFeatureType(feature.getFeatureTypeName());
		activeFeature = feature;
		setActive(true);
	}
	
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
	
	public boolean isEditing(CFeature f) {
		return activeFeature != null && activeFeature.equals(f);
	}

	public void editFeatureAttibutes(CFeature f, AttributeEditorCallback callback) {
		this.editorCallback = callback;
		setActiveFeature(f);
	}
	
}
