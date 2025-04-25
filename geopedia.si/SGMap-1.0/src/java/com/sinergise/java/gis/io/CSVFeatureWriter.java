package com.sinergise.java.gis.io;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;

import au.com.bytecode.opencsv.CSVWriter;

import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.io.FeatureWriter;
import com.sinergise.common.util.format.DateFormatter;
import com.sinergise.common.util.property.DateProperty;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.common.util.property.descriptor.PropertyType;

public class CSVFeatureWriter implements FeatureWriter {
	
	private CSVWriter writer;
	private ArrayList<String> columnValues = null;
	private DateFormatter dateFormat = DateFormatter.FORMATTER_ISO_DATE;
	private int rownum=0;
	private Writer  csvWriter;
	
	public CSVFeatureWriter (Writer  csvWriter) {
		this(csvWriter,';','"');
	}
	
	public CSVFeatureWriter (Writer  csvWriter, char separator, char quote) {	
		this.csvWriter = csvWriter;
		writer  = new CSVWriter(csvWriter, separator,quote);
	}
	
	public void setDataFormatter(DateFormatter df) {
		dateFormat=df;
	}
	
	@Override
	public void append(CFeature feature) {
		ArrayList<PropertyDescriptor<?>> pDescriptors = new ArrayList<PropertyDescriptor<?>>();
		for (PropertyDescriptor<?> pd : feature.getDescriptor()) {
			if (!pd.isHidden() || pd.isExportable()) {
				pDescriptors.add(pd);
			}
		}
		Collections.sort(pDescriptors, new PropertyDescriptor.OrderComparator());
		
		if (rownum==0) {
			ArrayList<String> headerValues = new ArrayList<String>(pDescriptors.size());
			for (PropertyDescriptor<?> pd : pDescriptors) {
				
				PropertyType<?> type = pd.getType();
				if (type.isType(PropertyType.VALUE_TYPE_TEXT) || 
					type.isType(PropertyType.VALUE_TYPE_LONG) ||
					type.isType(PropertyType.VALUE_TYPE_REAL) ||
					type.isType(PropertyType.VALUE_TYPE_DATE) ||
					type.isType(PropertyType.VALUE_TYPE_BOOLEAN)) {

					String title = pd.getTitle();
					 if (title==null || title.length()==0) {
						 title = pd.getSystemName();
					 }
					 headerValues.add(title);
				}
			} 
			rownum++;
			String values[] = headerValues.toArray(new String[headerValues.size()]);
			writer.writeNext(values);
		}
		
		if (columnValues==null)
			columnValues = new ArrayList<String>(pDescriptors.size());

		CFeatureDescriptor fd = feature.getDescriptor();
		columnValues.clear();
		
		for (PropertyDescriptor<?> pd : pDescriptors) {
			
			Property<?> prop = feature.getProperty(fd.getValueIndex(pd.getSystemName()));
			PropertyType<?> type = pd.getType();
			if (prop==null) {
				columnValues.add("");
				continue;
			}
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
		rownum++;	
	}
	
	@Override
	public void close() throws IOException {
		writer.close();
		csvWriter.close();
	}
}
