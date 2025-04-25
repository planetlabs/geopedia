package com.sinergise.java.gis.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.io.FeatureWriter;
import com.sinergise.common.util.property.BooleanProperty;
import com.sinergise.common.util.property.DateProperty;
import com.sinergise.common.util.property.DoubleProperty;
import com.sinergise.common.util.property.LongProperty;
import com.sinergise.common.util.property.Property;
import com.sinergise.common.util.property.TextProperty;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.common.util.property.descriptor.PropertyType;

public class XLSXFeatureWriter implements FeatureWriter {

	private final OutputStream outputStream;
	private final Workbook wb;
	
	private final Map<String, SheetFeatureWriter> sheetWriters = new HashMap<String, XLSXFeatureWriter.SheetFeatureWriter>();
	
	public XLSXFeatureWriter (OutputStream outputStream) {
		this.outputStream = outputStream;
		this.wb = new SXSSFWorkbook(20);
	}

	@Override
	public void append(CFeature feature) {
		getSheetWriter(feature).append(feature);
	}
	
	private synchronized SheetFeatureWriter getSheetWriter(CFeature feature) {
		String title = feature.getDescriptor().getTitle();
		
		SheetFeatureWriter sw = sheetWriters.get(title);
		if (sw == null) {
			sheetWriters.put(title, sw = new SheetFeatureWriter(wb.createSheet(title)));
		}
		return sw;
	}

	@Override
	public void close() throws IOException {		
        wb.write(outputStream);
        outputStream.close();
	}
	
	private class SheetFeatureWriter {

		final Sheet sh;
		int rownum=0;
		
		SheetFeatureWriter(Sheet sh) {
			this.sh = sh;
		}
		
		public void append(CFeature feature) {
			
			ArrayList<PropertyDescriptor<?>> pDescriptors = new ArrayList<PropertyDescriptor<?>>();
			for (PropertyDescriptor<?> pd : feature.getDescriptor()) {
				if (!pd.isHidden() || pd.isExportable()) {
					pDescriptors.add(pd);
				}
			}
			Collections.sort(pDescriptors, new PropertyDescriptor.OrderComparator());
			
			int column=0;
			if (rownum==0) {
				Row headerRow = sh.createRow(rownum);
				for (PropertyDescriptor<?> pd : pDescriptors) {
					
					PropertyType<?> type = pd.getType();
					if (type.isType(PropertyType.VALUE_TYPE_TEXT) || 
						type.isType(PropertyType.VALUE_TYPE_LONG) ||
						type.isType(PropertyType.VALUE_TYPE_REAL) ||
						type.isType(PropertyType.VALUE_TYPE_DATE) ||
						type.isType(PropertyType.VALUE_TYPE_BOOLEAN)) 
					{
						Cell cell = headerRow.createCell(column);	
						 cell.setCellType(Cell.CELL_TYPE_STRING);
						 String title = pd.getTitle();
						 if (title==null || title.length()==0)
							 title = pd.getSystemName();
						 cell.setCellValue(title);
						 column++;
					}
				} 
				rownum++;
				column=0;
			}
			
			CFeatureDescriptor fd = feature.getDescriptor();
			Row row = sh.createRow(rownum);
			
			for (PropertyDescriptor<?> pd : pDescriptors) {
				
				Property<?> prop = feature.getProperty(fd.getValueIndex(pd.getSystemName()));
				PropertyType<?> type = pd.getType();
				if (prop==null ||  type == null){
					continue;
				}
				if (prop.getValue()==null &&  (
						type.isType(PropertyType.VALUE_TYPE_TEXT) || 
						type.isType(PropertyType.VALUE_TYPE_LONG) ||
						type.isType(PropertyType.VALUE_TYPE_REAL) ||
						type.isType(PropertyType.VALUE_TYPE_DATE) ||
						type.isType(PropertyType.VALUE_TYPE_BOOLEAN))) 
				{
					column++;
					continue;
				}
				if (type.isType(PropertyType.VALUE_TYPE_TEXT)) {
					 Cell cell = row.createCell(column);	
					 cell.setCellType(Cell.CELL_TYPE_STRING);
					 cell.setCellValue(((TextProperty)prop).getValue());
					 column++;
				} else if (type.isType(PropertyType.VALUE_TYPE_LONG)) {
					 Cell cell = row.createCell(column);	
					 cell.setCellType(Cell.CELL_TYPE_NUMERIC);
					 cell.setCellValue(((LongProperty)prop).getValue().longValue());
					 column++;
				} else if (type.isType(PropertyType.VALUE_TYPE_REAL)) {
					 Cell cell = row.createCell(column);	
					 cell.setCellType(Cell.CELL_TYPE_NUMERIC);
					 cell.setCellValue(((DoubleProperty)prop).getValue().doubleValue());
					 column++;
				} else if (type.isType(PropertyType.VALUE_TYPE_DATE)) {
					 Cell cell = row.createCell(column);	
					 cell.setCellType(Cell.CELL_TYPE_STRING);
					 cell.setCellValue(((DateProperty)prop).toString());
					 column++;
				} else if (type.isType(PropertyType.VALUE_TYPE_BOOLEAN)) {
					 Cell cell = row.createCell(column);	
					 cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
					 cell.setCellValue(((BooleanProperty)prop).getValue().booleanValue());
					 column++;
				} else {
					// ignore
				}
			}
			rownum++;
			
		}
	}

}
