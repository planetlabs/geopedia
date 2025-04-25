package com.sinergise.geopedia.server.service.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVWriter;

import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.io.FeatureWriter;
import com.sinergise.common.util.format.DateFormatter;
import com.sinergise.common.util.property.DateProperty;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.common.util.property.descriptor.PropertyType;

public class CSVFileWriter implements FeatureWriter{
	
	CSVWriter writer;
	ArrayList<String> columnValues = null;
	DateFormatter dateFormat = DateFormatter.FORMATTER_ISO_DATE;
	String filebase ;
	public CSVFileWriter (File destFile) throws IOException {
		this(destFile,';','"');
	}
	
	public CSVFileWriter (File destFile, char separator, char quote) throws IOException {
		filebase = destFile.getAbsolutePath();
		if (filebase.toLowerCase().endsWith(".csv")) {
			filebase = filebase.substring(0, filebase.length()-4);
		}		
		writer  = new CSVWriter(new FileWriter(filebase+".csv"), separator,quote);
	}
	
	@Override
	public void append(CFeature feature) {
	
			CFeatureDescriptor fDesc = feature.getDescriptor();
			if (columnValues==null)
				columnValues = new ArrayList<String>(fDesc.size());

			columnValues.clear();
			for (PropertyDescriptor<?> pd : fDesc) {
				Property<?> prop = feature.getProperty(pd);
				PropertyType<?> type = pd.getType();
				if (type.isType(PropertyType.VALUE_TYPE_TEXT)) {
					columnValues.add(String.valueOf(prop.getValue()));
					
				} else if (type.isType(PropertyType.VALUE_TYPE_LONG)) {
					columnValues.add(String.valueOf(prop.getValue()));
					
				} else if (type.isType(PropertyType.VALUE_TYPE_REAL)) {
					columnValues.add(String.valueOf(prop.getValue()));
					
				} else if (type.isType(PropertyType.VALUE_TYPE_DATE)) {
					columnValues.add(dateFormat.formatDate(((DateProperty)prop).getValue()));
					
				} else if (type.isType(PropertyType.VALUE_TYPE_BOOLEAN)) {
					columnValues.add(String.valueOf(prop.getValue()));
					
				} else {
					// ignore
				}
			}
			
			String values[] = columnValues.toArray(new String[columnValues.size()]);
			writer.writeNext(values);		
	}
	
	@Override
	public void close() throws IOException {
		writer.close();
	}
}
