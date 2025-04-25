/**
 * 
 */
package com.sinergise.gwt.gis.query;

import com.sinergise.common.gis.feature.CFeatureDataSource;
import com.sinergise.common.gis.feature.CFeatureDataSource.FeatureCollectionCallback;
import com.sinergise.common.gis.feature.FeatureAccessException;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.map.model.layer.info.FeatureItemCollector;
import com.sinergise.common.gis.query.Query;
import com.sinergise.common.util.event.status.StatusListener;
import com.sinergise.gwt.gis.i18n.Messages;
import com.sinergise.gwt.ui.maingui.ILoadingWidget;


/**
 * 
 * @author tcerovski
 */
public class FeatureQuerier {
	
	public FeatureQuerier(){}
	
	private ILoadingWidget loadingPanel;
	private boolean warnIfMoreHits = true;
	
	public FeatureQuerier(ILoadingWidget loadingPanel) {
		this.loadingPanel = loadingPanel;
	}
	
	/**
	 * Creates callback and executes query on layer data source.
	 * @param src CFeatureDataSource on which features will be queried
	 * @param queries Queries to perform
	 * @param featureCollector Where to put result features
	 * @param statusListener Who to notify on status updates
	 * @throws FeatureAccessException
	 */
	public void executeQuery(CFeatureDataSource src, Query[] queries, FeatureItemCollector featureCollector, StatusListener statusListener) throws FeatureAccessException {
        if(loadingPanel != null) {
        	loadingPanel.showLoading(Integer.MAX_VALUE);
        }
        src.queryFeatures(queries, createResultCallback(featureCollector, statusListener));
	}

	protected FeatureCollectionCallback createResultCallback(final FeatureItemCollector featureCollector, final StatusListener statusListener) {
        return new FeatureCollectionCallback() {
            @Override
			public void onSuccess(FeatureInfoCollection features) {
				try {
					int count = features.getItemCount();
					if (count < 1 && statusListener != null) {
						statusListener.setInfoStatus(Messages.INSTANCE.featureQuerier_noQueryResultsFound());
					} else {
						if (warnIfMoreHits && statusListener != null && features.getItemCount() < features.getHitCount()) {
							if (features.getHitCount() - features.getItemCount() == 1) {
								statusListener.setInfoStatus(Messages.INSTANCE.featureQuerier_tooManyQueryResultsUnknownTotalCount(features.getItemCount()));
							} else {
								statusListener.setInfoStatus(Messages.INSTANCE.featureQuerier_tooManyQueryResults(features.getItemCount(), features.getHitCount()));
							}
						} else if (statusListener != null) {
							statusListener.clearStatus();
						}

						featureCollector.addAll(features);
					}
				} finally {
					if (loadingPanel != null) loadingPanel.hideLoading();
				}
			}
            
            @Override
			public void onError(FeatureAccessException error) {
            	try {
            		if(statusListener != null) {
            			statusListener.setErrorStatus(Messages.INSTANCE.featureQuerier_errorOnQuery(error.getMessage()));
            		}
            	} finally {
            		if(loadingPanel != null) {
            			loadingPanel.hideLoading();
            		}
            	}
            }
        };
    }
	
	public void warnIfTooManyHits(boolean warn) {
		warnIfMoreHits = warn;
	}
	
	public void setLoadingWidget(ILoadingWidget loading) {
		this.loadingPanel = loading;
	}
}
