package com.sinergise.common.gis.io.rpc;

import static com.sinergise.common.util.collections.CollectionUtil.first;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.format.DateFormatter;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.common.util.web.MimeType;
import com.sinergise.common.web.service.FileExporterRequest;

public class ExportFeaturesRequest extends FileExporterRequest {
	
	
	private ArrayList<CFeature> features; 
	
	
	@Deprecated /** Serialization only */
	protected ExportFeaturesRequest() { }
	
	public ExportFeaturesRequest(Collection<CFeature> features, MimeType exportType) {
		this (features, exportType, null);
	}
	
	public ExportFeaturesRequest(Collection<CFeature> featuresCollection, MimeType exportType, String filename) {
		super(exportType, filename);
		this.features = new ArrayList<CFeature>(featuresCollection);
	}
	
	public boolean isEmpty() {
		return CollectionUtil.isNullOrEmpty(features);
	}
	
	@Override
	public String getFilename() {
		if (StringUtil.isNullOrEmpty(filename) && !isEmpty()) {
			CFeatureDescriptor fDesc = first(features).getDescriptor(); 
			String layerName = fDesc.getLocalID().replaceAll("\\s", "_");
			return "export_"+layerName+"_"+DateFormatter.FORMATTER_ISO_DATE.formatDate(new Date())+"_";
		}
		return filename;
	}
	
	public List<CFeature> getFeatures() {
		return Collections.unmodifiableList(features);
	}
	
}
