/**
 * 
 */
package com.sinergise.common.gis.query;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;
import com.sinergise.common.gis.filter.LogicalOperation;


/**
 * Query descriptor for WFS getFeature request.
 * 
 * @author tcerovski
 */
public class Query implements Serializable {
	public static final String OPTION_INCLUDE_INTERSECTION_AREA = "INTSCN_AREA";
	public static final String OPTION_INCLUDE_INTERSECTION_GEOM = "INTSCN_GEOM";

	private static final long serialVersionUID = 1L;

	/** Maximal number of results returned by this query, Negative value for no limit. */
	private int maxResults = Integer.MIN_VALUE;
	
	private String featureTypeId;
	private String[] properties;
	private String[] sortBy;
	private FilterDescriptor filter;
	
	private HashSet<String> options = new HashSet<String>();
	
	/**
     * @deprecated Serialization only
     */
	@Deprecated
	public Query() {}
	
	/**
	 * Query all features, described with all properties.
	 * @param featureTypeId Name of layer to query.
	 */
	public Query(String featureTypeId) {
		this(featureTypeId, null, null);
	}
	
	/**
	 * Query all features, described only with provided properties.
	 * @param featureTypeId Name of layer to query.
	 * @param properties Feature properties to fetch.
	 */
	public Query(String featureTypeId, String[] properties) {
		this(featureTypeId, properties, null);
	}
	
	/**
	 * Query features passed by the provided filter, described with all properties.
	 * @param featureTypeId Name of layer to query.
	 * @param filter FilterDescriptor for filtering of features.
	 */
	public Query(String featureTypeId, FilterDescriptor filter) {
		this(featureTypeId, null, filter);
	}
	
	/**
	 * Query features passed by the provided filter, described only with provided properties.
	 * @param featureTypeId Name of layer to query.
	 * @param properties Feature properties to fetch.
	 * @param filter FilterDescriptor for filtering of features.
	 */
	public Query(String featureTypeId, String[] properties, FilterDescriptor filter) {
		this(featureTypeId, properties, filter, Integer.MIN_VALUE, null);
	}
	
	/**
	 * Query features passed by the provided filter, described only with provided properties,
	 * returns only less or <code>maxResults</code> of features.
	 * @param featureTypeId Name of layer to query.
	 * @param properties Feature properties to fetch.
	 * @param filter FilterDescriptor for filtering of features.
	 * @param maxResults Maximal number of results returned by this Query. Negative value for no limit.
	 * @param sortBy Feature properties that should be used to order the result.<br>
	 * The value of <code>sortBy</code> parameter shall have the form {PropertyName [A|D][,PropertyName [A|D],…]} 
	 * where the letter A is used to indicate an ascending sort and the letter D is used to indicate a descending sort. 
	 * If neither A nor D are specified, the default sort order shall be ascending. An example value might be: 
	 * {Field1 D,Field2 D,Field3}. In this case the results are sorted by Field 1 descending, Field2 descending and Field3 ascending.
	 */
	public Query(String featureTypeId, String[] properties, FilterDescriptor filter, int maxResults, String[] sortBy) {
		this.featureTypeId = featureTypeId;
		this.properties = properties;
		this.filter = filter;
		this.maxResults = maxResults;
		this.sortBy = sortBy;
	}
	
	public void putOption(String option) {
		options.add(option);
	}

	public String getFeatureTypeId() {
		return featureTypeId;
	}

	public String[] getProperties() {
		return properties;
	}

	public FilterDescriptor getFilter() {
		return filter;
	}
	
	public int getMaxResults() {
		return maxResults;
	}
	
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}
	
	public String[] getSortBy() {
		return sortBy;
	}
	
	/** @param sortBy Feature properties that should be used to order the result.<br>
	 * The value of <code>sortBy</code> parameter shall have the form {PropertyName [A|D][,PropertyName [A|D],…]} 
	 * where the letter A is used to indicate an ascending sort and the letter D is used to indicate a descending sort. 
	 * If neither A nor D are specified, the default sort order shall be ascending. An example value might be: 
	 * {Field1 D,Field2 D,Field3}. In this case the results are sorted by Field 1 descending, Field2 descending and Field3 ascending.
	 */
	public void setSortBy(String[] sortByProperties) {
		this.sortBy = sortByProperties;
	}
	
	/**
	 * Appends FilterDescriptor to existing filter using aggregating with AND.
	 * @param condition FilterDescriptor to add as additional condition.
	 */
	public void addFilterCondition(FilterDescriptor condition) {
		try {
			FilterDescriptor newFilter = new LogicalOperation(
					new FilterDescriptor[]{this.filter, condition}, FilterCapabilities.SCALAR_OP_LOGICAL_AND);
			//set new filter if no exception thrown
			this.filter = newFilter;
		} catch (InvalidFilterDescriptorException e) {
			throw new RuntimeException(e);
		}
	}

	public void setProperties(String[] propNames) {
		this.properties = propNames;
	}

	public boolean hasOption(String option) {
		return options.contains(option);
	}

	@Deprecated
	public Set<String> getOptions() {
		return options;
	}
}
