package com.sinergise.geopedia.core.service.params;

import java.io.Serializable;
import java.util.List;

import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.util.crs.CrsIdentifier;

public class ExportSettings implements Serializable{
	public static final int FMT_SHP = 1;
	public static final int FMT_CSV = 2;
	public static final int FMT_XLSX = 3;
	public static final int FMT_GPX = 4;
	
	private static final long serialVersionUID = 8721063049376517748L;
	
	public int exportFormat;
	public Integer tableID;
	public CrsIdentifier crsTransformID;
	public List<Integer> fieldIDs;
	public boolean resolveLookups = true;
	public boolean exportCentroidAsField = false;
	public FilterDescriptor filterDescriptor = null;
}
