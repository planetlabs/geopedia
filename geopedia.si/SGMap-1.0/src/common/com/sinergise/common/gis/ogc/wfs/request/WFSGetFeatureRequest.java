package com.sinergise.common.gis.ogc.wfs.request;

import java.util.HashSet;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CrsRepository;
import com.sinergise.common.gis.feature.CFeatureDataSource;
import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.ogc.OGCException;
import com.sinergise.common.gis.ogc.wms.WMSUtil;
import com.sinergise.common.gis.query.Query;
import com.sinergise.common.util.geom.Envelope;


public class WFSGetFeatureRequest extends WFSRequest implements CommonParams.XLinkList {
	private static final long serialVersionUID = 1L;
	
	public static final String REQ_GET_FEATURE = "GetFeature";

	/**
	 * The requested feature type / layer name (String).
	 */
	public static final String PARAM_FEATURE_TYPE = "TYPENAME";

	/**
	 * The type of the results returned (full results / hit count only)
	 */
	public static final String PARAM_RESULT_TYPE = "RESULTTYPE";

	public static final String RESULT_TYPE_RESULTS = "results";
	public static final String RESULT_TYPE_HITS = "hits";

	public static final String FEATURE_VERSION_ALL = "ALL";
	
	/**
	 * List of desired returned properties
	 */
	public static final String PARAM_PROPERTY_NAME = "PROPERTYNAME";

	/**
	 * The optional maxFeatures attribute can be used to limit the number of explicitly requested features (i.e. features specified via the GetFeature/Query/@typeName attribute) that a GetFeature request presents in the response document. The maxFeatures value applies to whole result set and the constraint is applied to the features in the order in which they are presented. In addition, feature members contained in a requested feature collection do not count – the requested feature collection counts as one feature. Once the maxFeatures limit is reached, the result set is truncated at that point. There is no default value defined and the absence of the attribute means that all feature type instances in the result should should be returned to the client.
	 */
	public static final String PARAM_MAX_FEATURES = "MAXFEATURES";
	
	/**
	 * Max number of returned results per query.
	 */
	public static final String PARAM_MAX_QUERY_FEATURES = "MAXQUERYFEATURES";

	/**
	 * The filter used to perform the query
	 */
	public static final String PARAM_FILTER = "FILTER";

	/**
	 * The property names used for sorting the results
	 */
	public static final String PARAM_SORT_BY = "SORTBY";

	/**
	 * If versioning is supported, the FEATUREVERSION parameter directs the WFS
	 * on which feature version to fetch. A value of ALL indicates to fetch all
	 * versions of a feature. An integer value fetches the Nth version of a
	 * feature. No value indicates that the latest version of the feature should
	 * be fetched.
	 */
	public static final String PARAM_FEATURE_VERSION = "FEATUREVERSION";

	public static final String PARAM_SRS_NAME = "SRSNAME";
	
	public static final String PARAM_FEATURE_ID = "FEATUREID";
	
	public static final String PARAM_BBOX = "BBOX";
	
	public WFSGetFeatureRequest() {
		super(REQ_GET_FEATURE);
	}
	
	/**
	 * @deprecated Store in the state so that we can use URL encoding for filters
	 * TODO: Generic encoding of filters
	 */
	@Deprecated
	private FilterDescriptor[] filters;
	@Deprecated
	private HashSet<String>[] options;
	
	public String[][] getSortBy() {
		return WMSUtil.decode2DArray(get(PARAM_SORT_BY, ""));
	}

	public String getFeatureVersion() {
		return get(PARAM_FEATURE_VERSION,null);
	}
	
	public Envelope getBBox() {
		return WMSUtil.fromWMSBBox(get(PARAM_BBOX));
	}
	
	public String[] getFeatureIds() {
		return WMSUtil.decodeArray(get(PARAM_FEATURE_ID,null));
	}
	
	public CFeatureIdentifier[] getFeatureIds(CFeatureDataSource dataSource) {
		return CFeatureIdentifier.decode(dataSource, getFeatureIds());
	}

	
	public void setBBox(double minX, double minY, double maxX, double maxY) {
        set(PARAM_BBOX, minX+","+minY+","+maxX+","+maxY);
	}
	
	public String[][] getPropertyNames() {
		String strVal=get(PARAM_PROPERTY_NAME);
		if (strVal==null) return null;
		strVal=strVal.trim();
		if (strVal.length()==0 || "*".equals(strVal)) return null;
		String[][] ret=WMSUtil.decode2DArray(strVal);
		for (int i = 0; i < ret.length; i++) {
			String[] arri=ret[i];
			if (arri != null && arri.length==1 && "*".equals(arri[0])) {
				ret[i]=null;
			}
		}
		return ret;
	}
	
	protected FilterDescriptor[] getFilters() {
		return filters;
	}
	
	protected void setFilters(FilterDescriptor[] filters) {
		this.filters=filters;
	}
	
	public void setFeatureIds(CFeatureIdentifier[] featureIDs) {
		set(PARAM_FEATURE_ID, CFeatureIdentifier.encode(featureIDs));
	}
	
	public String getResultType() {
		return get(PARAM_RESULT_TYPE,RESULT_TYPE_RESULTS);
	}
	
	public void setResultType(String resultType) {
		set(PARAM_RESULT_TYPE, resultType);
	}

	public void setSortBy(String[][] propertyNames) {
		set(PARAM_SORT_BY, WMSUtil.encode2DArray(propertyNames));
	}

	public String[] getFeatureTypeNames() {
		return WMSUtil.decodeArray(get(PARAM_FEATURE_TYPE, null));
	}

	public void setFeatureType(String[] featureTypeNames) {
		set(PARAM_FEATURE_TYPE, WMSUtil.encodeArray(featureTypeNames));
	}

	public int getMaxFeatures() {
		return getInt(PARAM_MAX_FEATURES, Integer.MIN_VALUE);
	}

	public void setMaxFeatures(int maxFeatures) {
		setInt(PARAM_MAX_FEATURES, maxFeatures);
	}
	
	public int[] getMaxQueryFeatures() {
		return WMSUtil.decodeIntArray(get(PARAM_MAX_QUERY_FEATURES, null));
	}
	
	public void setMaxQueryFeatures(int[] maxQueryFeatures) {
		set(PARAM_MAX_QUERY_FEATURES, WMSUtil.encodeIntArray(maxQueryFeatures));
	}
	
	/**
	 * 
	 * @return
	 */
	public CRS getCRS() {
		String crs=get(PARAM_SRS_NAME);
		if (crs==null || crs.length()==0) return null;
		return CrsRepository.INSTANCE.get(crs);
	}
	
	@Override
	public void validate() throws OGCException {
		super.validate();
		boolean filter=containsParam(PARAM_FILTER) || filters!=null;
		boolean fid=containsParam(PARAM_FEATURE_ID);
		boolean bbox=containsParam(PARAM_BBOX);
		if (!containsParam(PARAM_FEATURE_ID)) {
			validateNotNull(PARAM_FEATURE_TYPE);
		}
		if (fid && filter) throw new OGCException(this, "Cannot set both "+PARAM_FILTER+" and "+PARAM_FEATURE_ID); 
		if (fid && bbox) throw new OGCException(this, "Cannot set both "+PARAM_BBOX+" and "+PARAM_FEATURE_ID); 
		if (filter && bbox) throw new OGCException(this, "Cannot set both "+PARAM_FILTER+" and "+PARAM_BBOX); 
	}

	@SuppressWarnings("unchecked")
	public void setQueries(Query[] queries) {
		String[] featureTypes=new String[queries.length];
		FilterDescriptor[] fDescs=new FilterDescriptor[queries.length];
		String[][] propertyNames=new String[queries.length][];
		String[][] sortBy = new String[queries.length][];
		int[] maxQueryFeatures = new int[queries.length];
		options = new HashSet[queries.length];
		for (int i = 0; i < queries.length; i++) {
			featureTypes[i]=queries[i].getFeatureTypeId();
			fDescs[i]=queries[i].getFilter();
			propertyNames[i]=queries[i].getProperties();
			sortBy[i]=queries[i].getSortBy();
			maxQueryFeatures[i]=queries[i].getMaxResults();
			options[i] = (HashSet<String>)queries[i].getOptions();
		}
		setFeatureType(featureTypes);
		setPropertyNames(propertyNames);
		setFilters(fDescs);
		setSortBy(sortBy);
		setMaxQueryFeatures(maxQueryFeatures);
	}
	
	public Query[] getQueries() {
		FilterDescriptor[] fDescs = getFilters();
		String[] featureTypes = getFeatureTypeNames();
		if (fDescs == null || featureTypes == null) {
			return null;
		}
		
		Query[] qrys = new Query[featureTypes.length];
		String[][] props = getPropertyNames();
		String[][] sortBy = getSortBy();
		int[] maxResults = getMaxQueryFeatures();
		for (int i = 0; i < qrys.length; i++) {
			if (props==null || props[i]==null) {
				qrys[i]=new Query(featureTypes[i], fDescs[i]);
			} else {
				qrys[i]=new Query(featureTypes[i], props[i], fDescs[i]);
			}
			if(maxResults != null && maxResults.length > i)
				qrys[i].setMaxResults(maxResults[i]);
			if(sortBy != null && sortBy.length > i) {
				qrys[i].setSortBy(sortBy[i]);
			}
			for (String opt : options[i]) {
				qrys[i].putOption(opt);
			}
		}
		
		return qrys;
	}

	private void setPropertyNames(String[][] propertyNames) {
		set(PARAM_PROPERTY_NAME, WMSUtil.encode2DArray(propertyNames));
	}
	
//	public QuerySpec getFilter() {
//		return query;
//	}
//	public void setFilter(QuerySpec spc) {
//		this.query=spc;
//	}
}
