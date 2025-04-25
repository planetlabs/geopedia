/*
 *
 */
package com.sinergise.common.gis.map.model.layer.info;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.gis.feature.FeaturesSource;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.common.util.web.MimeType;

/**
 * UC: provides access to the feature info functionality of the selected layers. Used e.g. on WMSLayer.
 * 
 * @author ? ?;
 */
public interface FeatureInfoSource extends FeaturesSource {
	public static final MimeType TYPE_HTML_STRING = MimeType.MIME_HTML;
	public static final MimeType TYPE_OBJECT_FEATUREINFO_RESULT = FeatureInfoCollection.MIME_OBJECT_FEATURE_INFO_COLLECTION;

	boolean supportsInfoType(MimeType type);

	// TODO: Remove reference to GWT's AsyncCallback
	<T> void getFeatureInfo(Layer[] visibleLayers, FeatureInfoLayer[] queryLayers, CRS crs, double wx, double wy, double radius, double scale, MimeType type, SGAsyncCallback<T> cb);
}
