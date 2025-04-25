package com.sinergise.gwt.gis.map.shapes.editor.action;

import java.util.Collection;

import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureDataSource;
import com.sinergise.common.gis.feature.CFeatureDataSource.TransactionCallback;
import com.sinergise.common.gis.feature.FeatureAccessException;
import com.sinergise.common.gis.ogc.wfs.response.WFSTransactionResponse;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorController;

public class WFSEditGeometryAction extends EditGeometryAction {

	public WFSEditGeometryAction(GeometryEditorController controller, CFeature feature) {
		super(controller, feature);
	}

	@Override
	protected void doSave(final Collection<CFeature> modified, final SGAsyncCallback<Void> confirmCallback) {
		final String featureType = extractFeatureType(modified);
		CFeatureDataSource src = map.getLayers().findByFeatureType(featureType).getFeaturesSource();
		
		try {
			src.insertOrUpdateFeatures(new TransactionCallback() {
				@Override
				public void onSuccess(WFSTransactionResponse response) {
					applyModifiedGeometry();
					confirmCallback.onSuccess(null);
				}
				
				@Override
				public void onError(FeatureAccessException error) {
					confirmCallback.onFailure(error);
				}
				
				private void applyModifiedGeometry() {
					for (CFeature mf : modified) {
						if (feature.getIdentifier().equals(mf.getIdentifier())) {
							feature.setGeometry(mf.getGeometry());
						}
					}
				}
				
			}, modified.toArray(new CFeature[modified.size()]));
			
		} catch(FeatureAccessException error) {
			confirmCallback.onFailure(error);
		}
	}
	
	private static String extractFeatureType(Collection<CFeature> features) throws IllegalArgumentException {
		String featureType = null;
		for (CFeature f : features) {
			if (featureType == null) {
				featureType = f.getFeatureTypeName();
			} else if (!featureType.equals(f.getFeatureTypeName())) {
				throw new IllegalArgumentException("Can only save features of same type in one transaction");
			}
		}
		
		return featureType;
	}
	
	
	public static class WFSEditGeometryActionFactory implements EditGeometryActionFactory {
		
		public static final WFSEditGeometryActionFactory INSTANCE = new WFSEditGeometryActionFactory();
		
		private WFSEditGeometryActionFactory() { }
		
		@Override
		public EditGeometryAction createAction(GeometryEditorController controller, CFeature feature) {
			return new WFSEditGeometryAction(controller, feature);
		}
	}
	
}
