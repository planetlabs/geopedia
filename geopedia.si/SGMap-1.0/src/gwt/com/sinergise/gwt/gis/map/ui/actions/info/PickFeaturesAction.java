package com.sinergise.gwt.gis.map.ui.actions.info;

import static com.sinergise.common.util.messages.MessageType.ERROR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.sinergise.common.gis.feature.CFeatureDataSource.FeatureCollectionCallback;
import com.sinergise.common.gis.feature.FeatureAccessException;
import com.sinergise.common.gis.filter.BBoxOperation;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.query.Query;
import com.sinergise.common.ui.action.ToggleAction;
import com.sinergise.common.ui.i18n.Messages;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.ui.core.MouseClickAction;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.ui.dialog.MessageDialog;

public abstract class PickFeaturesAction extends ToggleAction {
	
	private static final String DEFAULT_PICK_CURSOR = "url('"+GWT.getModuleBaseURL()+"style/cur/info.cur'), default";
	private static final int DEFAULT_DISTANCE = 1;
	
	private final Logger logger = LoggerFactory.getLogger(PickFeaturesAction.class);

	private MapComponent map;
	private FeatureDataLayer layer;
	
	private int distBuffer = DEFAULT_DISTANCE;
	
	public PickFeaturesAction(MapComponent map, String featureTypeName) {
		this(map, map.getLayers().findByFeatureType(featureTypeName));
		if (layer != null) {
			throw new IllegalArgumentException(featureTypeName+" layer not found.");
		}
	}
	
	public PickFeaturesAction(MapComponent map, FeatureDataLayer layer) {
		super("PickFeaturesAction");
		this.map = map;
		setLayer(layer);
	}
	
	public abstract void gotFeatures(FeatureInfoCollection features);
	
	protected void setLayer(FeatureDataLayer layer) {
		this.layer = layer;
	}
	
	protected void setDistanceBuffer(int distBuffer) {
		this.distBuffer = distBuffer;
	}
	
	protected void setPickCursor(String cursor) {
		leftClickAct.setCursor(cursor);
	}
	
	protected Envelope createQueryMBR(int x, int y) {
		EnvelopeBuilder bld = new EnvelopeBuilder();
		bld.expandToInclude(map.coords.worldFromPix.x(x), map.coords.worldFromPix.y(y));
		bld.expandFor(map.coords.worldFromPix.length(distBuffer));
		return bld.getEnvelope();
	}
	
	protected FeatureDataLayer getLayer() {
		return layer;
	}
	
	protected void pickAt(int x, int y) {
        try {
        	getLayer().getFeaturesSource().queryFeatures(new Query[]{
        	   new Query(getLayer().getFeatureTypeName(), buildQueryFilter(x, y))
        	}, new FeatureCollectionCallback() {
				
				@Override
				public void onSuccess(FeatureInfoCollection features) {
					gotFeatures(features);
				}
				
				@Override
				public void onError(FeatureAccessException e) {
					handleException("Error picking features: "+e.getMessage(), e);
				}
			});
        	
        } catch (Exception e) {
        	handleException("Error building query to pick features: "+e.getMessage(), e);
        }
    }
	
	protected FilterDescriptor buildQueryFilter(int x, int y) {
		return new BBoxOperation(createQueryMBR(x, y));
	}
	
	protected void handleException(String msg, Throwable e) {
		logger.error(msg, e);
		MessageDialog.createMessage(Messages.INSTANCE.error(), msg, false, ERROR);
	}

	@Override
	protected void selectionChanged(boolean newSelected) {
		if (newSelected) {
            startMode();
        } else {
            endMode();
        }
	}
	
	private void endMode() {
        map.mouser.deregisterAction(leftClickAct);
        map.mouser.deregisterAction(rightClickAct);
    }

    private void startMode() {
        map.mouser.registerAction(leftClickAct, MouseHandler.BUTTON_LEFT, MouseHandler.MOD_NONE, 1);
        map.mouser.registerAction(rightClickAct, MouseHandler.BUTTON_RIGHT, MouseHandler.MOD_ANY, 1);
    }
    
    protected void cancelPick() {
    	setSelected(false);
    }
    
	
	protected final MouseClickAction leftClickAct = new MouseClickAction("PickFeatures") {
		{
			setCursor(DEFAULT_PICK_CURSOR);
		}
		
        @Override
		protected boolean mouseClicked(int x, int y) {
        	pickAt(x,y);
            return false;
        }
    };
    
	protected final MouseClickAction rightClickAct = new MouseClickAction("CancelPickFeatures") {
		@Override
		protected boolean mouseClicked(int x, int y) {
			cancelPick();
			return false;
		}
	};
}
