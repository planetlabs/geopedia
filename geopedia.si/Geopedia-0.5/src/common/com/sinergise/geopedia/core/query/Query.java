package com.sinergise.geopedia.core.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import com.sinergise.common.gis.filter.ElementDescriptor;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.geopedia.core.entities.ThemeTableLink;
import com.sinergise.geopedia.core.query.filter.FieldDescriptor;
import com.sinergise.geopedia.core.query.filter.TableMetaFieldDescriptor;

public class Query implements Serializable {
	
	public static final int UNDEFINED = -1;
	private static final long serialVersionUID = -6474247056803949032L;
	
	
	public void fetchEverything(boolean geometry) {
		options.clear();
		options.add(Options.FLDUSER_ALL);
		options.add(Options.FLDMETA_BASE);
		options.add(Options.FLDMETA_ENVLENCEN);
		if (geometry)
			options.add(Options.FLDMETA_GEOMETRY);		
	}
	
	public enum Options {FLDUSER_ALL, TOTALCOUNT, COUNTONLY, FLDMETA_BASE,FLDMETA_ENVLENCEN, FLDMETA_GEOMETRY, VISIBLE, NO_FOREIGNREF_RESOLVE};
	public enum OrderBy {ASC,DESC}
	public static class OrderHolder implements Serializable {
		public ElementDescriptor element;
		public OrderBy by;
	}
	
	public int tableId;
	public int scale = UNDEFINED;
	public ThemeTableLink themeTableLink = null;

	public FilterDescriptor filter;
	public int startIdx = UNDEFINED;
	public int stopIdx = UNDEFINED;
	public HashSet<Options> options = new HashSet<Options>();
	public ArrayList<OrderHolder> order = new ArrayList<OrderHolder>();
	public long dataTimestamp=0;
	
	public void resetOrderBy() {
		order.clear();
	}
	public void addOrderBy(ElementDescriptor element, OrderBy by) {
		if (!(element instanceof FieldDescriptor || element instanceof TableMetaFieldDescriptor)) {
			throw new RuntimeException("Illegal descriptor! Only FieldDescriptor or GeopediaPropertyDescriptor are allowed");
		}
		OrderHolder oh = new OrderHolder();
		oh.element=element;
		oh.by=by;
		order.add(oh);
	}
	
	public boolean hasStartStopIndexes() {
		if  (stopIdx>=0 && startIdx>=0)
			return true;
		return false;
	}

	public boolean hasOrder() {
		return order.size()>0;
	}

	public ArrayList<OrderHolder> getOrder() {
		return order;
	}
	public boolean hasOption(Options option) {
		return options.contains(option);
	}
}
