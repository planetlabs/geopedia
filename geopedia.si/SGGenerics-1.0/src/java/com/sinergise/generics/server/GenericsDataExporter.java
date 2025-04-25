package com.sinergise.generics.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import au.com.bytecode.opencsv.CSVWriter;

import com.sinergise.common.util.format.DateFormatUtil;
import com.sinergise.common.util.format.DateFormatter;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.GenericObjectProperty;
import com.sinergise.generics.core.LookupPrimitiveValue;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.PrimitiveValue;
import com.sinergise.generics.core.TypeAttribute;
import com.sinergise.generics.core.Types;
import com.sinergise.generics.core.services.GenericsService.DataExportTypes;
import com.sinergise.generics.core.util.EntityUtils;
import com.sinergise.java.util.format.JavaFormatProvider;

public abstract class GenericsDataExporter {
	public abstract  void addHeaderRow(GenericObjectProperty[] properties, EntityType et);
	public abstract void addDataRow(GenericObjectProperty[] properties, EntityObject eo, EntityType et);
	public abstract void close() throws IOException;

	
	public static String getExtensionForType (DataExportTypes type) {
		switch (type) {
		case XLSX:
			return ".xlsx";
		case CSV:
			return ".csv";
		}
		return "";
	}
	public static GenericsDataExporter create(File exportFile, DataExportTypes type) throws IOException {
		if (type==DataExportTypes.CSV) {
			return new CSV(exportFile);
		} else if (type == DataExportTypes.XLSX) {
			return new XLSX(exportFile);
		}
		return  new CSV(exportFile);
	}
	
	public static class XLSX extends GenericsDataExporter {

		private FileOutputStream fos;
		private final Workbook wb;
		private final Sheet sh;
		private int rowNum = -1;
		private DataFormat format = null; 
		private XLSX(File exportFile) throws IOException {
			wb = new XSSFWorkbook();
			sh = wb.createSheet();			
            format = wb.createDataFormat();              
			fos = new FileOutputStream(exportFile);
		}
		
		@Override
		public void addHeaderRow(GenericObjectProperty[] properties, EntityType et) {
			rowNum++;
			Row headerRow = sh.createRow(rowNum);
			
			for (int i = 0; i < properties.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellType(Cell.CELL_TYPE_STRING);
				cell.setCellValue(properties[i].getLabel());
			}
		}

		@Override
		public void addDataRow(GenericObjectProperty[] properties,	EntityObject eo, EntityType et) {
			
			rowNum++;
			Row row = sh.createRow(rowNum);
			
			for (int j = 0; j < properties.length; j++) {
				GenericObjectProperty gop = properties[j];
				Cell cell = row.createCell(j);
				TypeAttribute ta = et.getAttribute(properties[j]
						.getName());
				if (ta == null)
					continue;
				int type = ta.getPrimitiveType();
				if (type == Types.INT) {
					Long lv = EntityUtils.getLongValue(eo, ta.getName());
					if (lv != null) {
						cell.setCellType(Cell.CELL_TYPE_NUMERIC);
						cell.setCellValue(lv);
					}
				} else if (type == Types.FLOAT) {
					Double dv = EntityUtils.getDoubleValue(eo, ta.getName()); 
					if (dv != null) {
						cell.setCellType(Cell.CELL_TYPE_NUMERIC);
						cell.setCellValue(dv);
					}						
				} else if (type == Types.DATE) {
					Long lv = EntityUtils.getLongValue(eo,ta.getName());
					if (lv != null) {
						Date date = new Date(lv);						
						cell.setCellType(Cell.CELL_TYPE_NUMERIC);
						CellStyle cellStyle = wb.createCellStyle();
						cellStyle.setDataFormat(format.getFormat(gop.getAttributes().get(
								MetaAttributes.VALUE_FORMAT)));
						cell.setCellStyle(cellStyle);
						cell.setCellValue(date);
					}
				} else {
					PrimitiveValue pv = (PrimitiveValue) eo.getValue(ta
							.getId());
					if (pv != null) {
						String value = pv.value;
						if (pv instanceof LookupPrimitiveValue) {
							LookupPrimitiveValue lpv = (LookupPrimitiveValue) pv;
							if (lpv.lookedUpValue != null) {
								value = lpv.lookedUpValue;
							}
						}
						cell.setCellType(Cell.CELL_TYPE_STRING);
						cell.setCellValue(value);										
					}
					
				}
			}
		}

		@Override
		public void close() throws IOException {
			wb.write(fos);
			fos.close();
		}
		
	}
	public static class CSV extends GenericsDataExporter {

		CSVWriter csvWriter;
		OutputStreamWriter osw;
		
		private CSV(File exportFile) throws IOException {
			osw = new OutputStreamWriter(new FileOutputStream(exportFile), "UTF-8");
			osw.write('\ufeff'); // BOM
			 csvWriter = new CSVWriter(osw, ';', '"');
		}

		@Override
		public void addHeaderRow(GenericObjectProperty[] properties, EntityType et) {
			String[] labels = new String[properties.length];
			for (int i = 0; i < properties.length; i++) {
				labels[i] = properties[i].getLabel();
			}
			csvWriter.writeNext(labels);			
		}

		@Override
		public void addDataRow(GenericObjectProperty[] properties, EntityObject eo, EntityType et) {
			String row[] = new String[properties.length];
			for (int j = 0; j < properties.length; j++) {
				GenericObjectProperty gop = properties[j];
				TypeAttribute ta = et.getAttribute(properties[j]
						.getName());
				if (ta == null)
					continue;
				int type = ta.getPrimitiveType();
				if (type == Types.INT || type == Types.FLOAT) {
					String valueFormat = gop.getAttributes().get(
							MetaAttributes.VALUE_FORMAT);
					String value = eo.getPrimitiveValue(ta.getId());
					if (!StringUtil.isNullOrEmpty(valueFormat)) {
						Double doubleValue = EntityUtils
								.getDoubleValue(eo, ta.getName());
						if (doubleValue != null)
							value = JavaFormatProvider.create(
									valueFormat).format(doubleValue);
					}
					row[j] = value;
				} else if (type == Types.DATE) {
					Long lv = EntityUtils.getLongValue(eo,ta.getName());
					if (lv!=null) {
						Date date = new Date(lv);
						String valueFormat = gop.getAttributes().get(
								MetaAttributes.VALUE_FORMAT);
						DateFormatter df = DateFormatter.FORMATTER_DEFAULT_DATETIME;
						if (!StringUtil.isNullOrEmpty(valueFormat)) {
							df = DateFormatUtil.create(valueFormat);
						}
						row[j] = df.formatDate(date);
					}
				
				} else {
					PrimitiveValue pv = (PrimitiveValue) eo.getValue(ta
							.getId());
					if (pv != null) {
						row[j] = pv.value;
						if (pv instanceof LookupPrimitiveValue) {
							LookupPrimitiveValue lpv = (LookupPrimitiveValue) pv;
							if (lpv.lookedUpValue != null) {
								row[j] = lpv.lookedUpValue;
							}
						}
					}
				}
			}
			csvWriter.writeNext(row);			
		}

		@Override
		public void close() throws IOException {
			csvWriter.close();
			osw.close();
		}
		
	}
}
