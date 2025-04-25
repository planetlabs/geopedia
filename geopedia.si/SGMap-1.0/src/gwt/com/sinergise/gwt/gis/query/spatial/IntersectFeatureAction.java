package com.sinergise.gwt.gis.query.spatial;

import static com.sinergise.common.gis.feature.CFeatureUtils.toFeatureList;
import static com.sinergise.common.gis.filter.FilterCapabilities.SPATIAL_OP_INTERSECT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.property.GeometryProperty;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureCollection;
import com.sinergise.common.gis.feature.CFeatureDataSource.FeatureDescriptorCallback;
import com.sinergise.common.gis.feature.FeatureAccessException;
import com.sinergise.common.gis.feature.HasFeatures;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.filter.ElementDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.GeometryReference;
import com.sinergise.common.gis.filter.IdentifierOperation;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;
import com.sinergise.common.gis.filter.Literal;
import com.sinergise.common.gis.filter.LogicalOperation;
import com.sinergise.common.gis.filter.NoOperation;
import com.sinergise.common.gis.filter.SpatialOperation;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoItem;
import com.sinergise.common.gis.map.model.layer.info.FeatureItemCollector;
import com.sinergise.common.gis.map.model.layer.info.SingleFeatureCollection;
import com.sinergise.common.gis.query.Query;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.ui.action.ActionSelection;
import com.sinergise.common.util.event.status.StatusListener;
import com.sinergise.gwt.gis.i18n.Messages;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.gis.query.FeatureQuerier;
import com.sinergise.gwt.gis.resources.GisTheme;

public class IntersectFeatureAction extends ActionSelection {

	private static final Logger logger = LoggerFactory.getLogger(IntersectFeatureAction.class);
	
	public IntersectFeatureAction(CFeature feature, FeatureDataLayer featureLayer, FeatureItemCollector collector , FeatureDataLayer ...withLayers) {
		this(new CFeatureCollection(feature), featureLayer, collector, withLayers);
	}
	
	public IntersectFeatureAction(HasFeatures featuresProvider, FeatureDataLayer featuresLayer, FeatureItemCollector collector , FeatureDataLayer ...withLayers) {
		super(Tooltips.INSTANCE.intersectFeatureWithLayer());		
		setDescription(getName());
		setIcon(GisTheme.getGisTheme().gisStandardIcons().intersectDD());
		
		Action[] actions = new Action[withLayers.length];
		for (int i = 0; i < withLayers.length; i++) {
			FeatureDataLayer dataLayer = withLayers[i];
			actions[i] = createAction(featuresProvider, featuresLayer, collector, dataLayer);
		}
		setSelections(actions);
	}

	protected IntersectWithLayerAction createAction(HasFeatures featuresProvider, FeatureDataLayer featuresLayer, FeatureItemCollector collector, FeatureDataLayer dataLayer) {
		return new IntersectWithLayerAction(featuresProvider, featuresLayer, dataLayer, collector);
	}
	
	
	protected static class IntersectWithLayerAction extends Action {
		
		final HasFeatures featuresProvider;
		final FeatureDataLayer featureLayer;
		final FeatureDataLayer withLayer;
		final FeatureItemCollector featureCollector;
		
		protected boolean includeIntersectionArea = true;
		protected boolean includeIntersectionGeom = false;
		
		public IntersectWithLayerAction(HasFeatures featuresProvider, FeatureDataLayer featureLayer , FeatureDataLayer withLayer, FeatureItemCollector featureCollector) {
			super("Intersect"+withLayer.getFeatureTypeName());
			this.featuresProvider = featuresProvider;
			this.featureLayer = featureLayer;
			this.withLayer = withLayer;
			this.featureCollector = featureCollector;
			
			setDescription(((Layer)withLayer).getTitle());
		}
		
		public void setIncludeIntersectionGeom(boolean includeIntersectionGeom) {
			this.includeIntersectionGeom = includeIntersectionGeom;
		}
		
		private StatusListener getStatusListener() {
			return featureCollector instanceof StatusListener ? (StatusListener)featureCollector : null;
		}
		
		protected Query buildQuery(Geometry geometry) throws InvalidFilterDescriptorException {
			ElementDescriptor left = new GeometryReference();
			ElementDescriptor right = Literal.newInstance(new GeometryProperty(geometry));
			
			Query ret = new Query(withLayer.getFeatureTypeName(), new SpatialOperation(left, SPATIAL_OP_INTERSECT, right));
			if (includeIntersectionArea) {
				ret.putOption(Query.OPTION_INCLUDE_INTERSECTION_AREA);
				ret.putOption(Query.OPTION_INCLUDE_INTERSECTION_GEOM);
			}
			return ret;
		}
		
		private void fetchGeometryAndExecuteQuery() throws FeatureAccessException, InvalidFilterDescriptorException {
			
			final Collection<CFeature> features = featuresProvider.getFeatures();
			final Geometry geom = getFeatureGeometries(features);
			
			//check geometry and fetch if not present
			if (geom == null || geom.isEmpty()) {
				
				final CFeatureDescriptor fd = featureLayer.getDescriptor();
				
				new FeatureQuerier().executeQuery(featureLayer.getFeaturesSource(), 
					new Query[]{new Query(featureLayer.getFeatureTypeName(),
						new String[]{fd.getGeomDescriptor().getSystemName()},
						getFeaturesFilter(features))},
						
					new FeatureItemCollector() {
						
						@Override
						public void clearFeatures() { }
						
						@Override
						public void addAll(FeatureInfoCollection result) {
							if (result.getItemCount() > 0) {
								try {
									executeQuery(getFeatureGeometries(toFeatureList(result)));
								}  catch (Exception e) {
									String msg = Messages.INSTANCE.featureQuerier_errorOnQuery(e.getMessage());
									logger.error(msg, e);
									if (getStatusListener() != null) getStatusListener().setErrorStatus(msg);
								}
							}
						}
						
						@Override
						public void add(FeatureInfoItem fItem) {
							addAll(new SingleFeatureCollection(fItem));
						}
					}, getStatusListener());
				
			} else {
				executeQuery(geom);
			}
		}
		
		private void executeQuery(Geometry geometry) throws FeatureAccessException, InvalidFilterDescriptorException {
			new FeatureQuerier().executeQuery(withLayer.getFeaturesSource(), 
				new Query[]{buildQuery(geometry)}, featureCollector, getStatusListener());
		}
		
		@Override
		protected void actionPerformed() {
			final StatusListener listener = getStatusListener();
			
			featureCollector.clearFeatures();
			try {
				if (withLayer.getDescriptor() == null || !withLayer.getDescriptor().hasGeometry() 
					|| featureLayer.getDescriptor() == null || !featureLayer.getDescriptor().hasGeometry()) {
					
					//fetch descriptor if none yet on layer or if incomplete (without geometry field)
					//TODO: clean this up when problems with descriptors are resolved (mkadunc)
					withLayer.getFeaturesSource().getDescriptor(
						new String[]{withLayer.getFeatureTypeName(), featureLayer.getFeatureTypeName()}, 
						new FeatureDescriptorCallback() {
							@Override
							public void onSuccess(CFeatureDescriptor[] result) {
								actionPerformed();
							}
							
							@Override
							public void onError(FeatureAccessException e) {
								String msg = Messages.INSTANCE.structuredQueryBuilder_errorLoadingDescriptor(e.getMessage());
								logger.error(msg, e);
								if (listener != null) listener.setErrorStatus(msg);
							}
						});
					return;
				}
				
				fetchGeometryAndExecuteQuery();
			} catch (Exception e) {
				String msg = Messages.INSTANCE.featureQuerier_errorOnQuery(e.getMessage());
				logger.error(msg, e);
				if (listener != null) listener.setErrorStatus(msg);
			}
		}
		
		private static FilterDescriptor getFeaturesFilter(Collection<CFeature> features) {
			
			FilterDescriptor[] exprs = new FilterDescriptor[features.size()];
			int cnt=0;
			for (CFeature f : features) {
				exprs[cnt++] = new IdentifierOperation(f.getLocalID());
			}
			
			if (exprs.length == 0) {
				return new NoOperation();
			} else if (exprs.length == 1) {
				return exprs[0];
			} else {
				return new LogicalOperation(exprs, FilterCapabilities.SCALAR_OP_LOGICAL_OR);
			}
		}
		
		private static Geometry getFeatureGeometries(Collection<CFeature> features) {
			List<Geometry> geoms = new ArrayList<Geometry>(features.size());
			for (CFeature f : features) {
				if (f.hasGeometry() && !f.getGeometry().isEmpty()) {
					geoms.add(f.getGeometry());
				}
			}
			
			if (!geoms.isEmpty()) {
				return new GeometryCollection<Geometry>(geoms);
			}
			return null;
		}
		
	}

}
