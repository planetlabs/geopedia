package com.sinergise.java.gis.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.io.FeatureWriter;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.lang.Function;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.common.util.property.descriptor.PropertyType;
import com.sinergise.java.geometry.io.shp.DBFField;
import com.sinergise.java.geometry.io.shp.SHPHeader.ShapefileType;
import com.sinergise.java.geometry.io.shp.ShpWithAttributesWriter;
import com.sinergise.java.util.io.FileUtilJava;
import com.sinergise.java.util.io.RandomAccessEndianFile;


public class ShapefileFeatureWriter implements FeatureWriter {
	private ShpWithAttributesWriter shpDbfWriter;
	private ArrayList<Integer> writableProperties = new ArrayList<Integer>();

	public ShapefileFeatureWriter(File destFile) throws FileNotFoundException {
		this(destFile.getAbsolutePath());
	}

	public ShapefileFeatureWriter(String fileBase) throws FileNotFoundException {
		if (fileBase.toLowerCase().endsWith(".shp")) {
			fileBase = fileBase.substring(0, fileBase.length() - 4);
		}

		shpDbfWriter = new ShpWithAttributesWriter(new File(fileBase + ".shp"));
	}

	private void initializeDBF(CFeatureDescriptor descriptor) {
		ArrayList<DBFField> dbfFieldsList = new ArrayList<DBFField>();
		for (int i = 0; i < descriptor.size(); i++) {
			PropertyDescriptor<?> pd = descriptor.getValueDescriptor(i);
			PropertyType<?> type = pd.getType();
			DBFField dbfField = new DBFField();
			dbfField.setName(pd.getSystemName());
			boolean useField = true;
			int length = pd.getInfoInteger(PropertyType.KEY_LENGTH, 0);
			if (type.isType(PropertyType.VALUE_TYPE_TEXT)) {
				dbfField.type = DBFField.TYPE_CHARACTER;
				if (length > 0)
					dbfField.len = length;
				else
					dbfField.len = 255;
			} else if (type.isType(PropertyType.VALUE_TYPE_LONG)) {
				dbfField.type = DBFField.TYPE_NUMERIC;
				dbfField.len = 20;
			} else if (type.isType(PropertyType.VALUE_TYPE_REAL)) {
				dbfField.type = DBFField.TYPE_DOUBLE;
				dbfField.len = 30;
				dbfField.decimals = 10;
			} else if (type.isType(PropertyType.VALUE_TYPE_DATE)) {
				dbfField.type = DBFField.TYPE_DATE;
			} else if (type.isType(PropertyType.VALUE_TYPE_BOOLEAN)) {
				dbfField.type = DBFField.TYPE_LOGICAL;
			} else {
				useField = false;
				//logger.trace("Property '{}' with type '{}' won't be exported to DBF.",pd.getSystemName(), type);
			}
			if (useField) {
				dbfFieldsList.add(dbfField);
				writableProperties.add(Integer.valueOf(i));
			}

		}

		shpDbfWriter.initAttributes(dbfFieldsList.toArray(new DBFField[dbfFieldsList.size()]));
	}

	@Override
	public void append(final CFeature feature) throws ObjectWriteException {
		try {
			if (!shpDbfWriter.isAttributesInited()) {
				initializeDBF(feature.getDescriptor());
			}
			Object[] vals = CollectionUtil.map(writableProperties, new Object[writableProperties.size()], new Function<Integer, Object>() {
				@Override
				public Object execute(Integer pIdx) {
					return feature.getPropertyValue(pIdx.intValue());
				}
			});
			shpDbfWriter.appendRecord(feature.getGeometry(), vals);
		} catch(Exception e) {
			throw new ObjectWriteException("Error appending feature: " + e.getMessage(), e);
		}
	}

	public void initShapeType(Geometry g) throws IOException {
		shpDbfWriter.initShapeType(g);
	}

	@Override
	public void close() throws IOException {
		shpDbfWriter.close();
	}

	public void initShapeType(List<CFeature> features) throws IOException {
		for (CFeature cFeature : features) {
			Geometry g = cFeature.getGeometry();
			if (g != null) {
				shpDbfWriter.initShapeType(g);
				return;
			}
		}
		shpDbfWriter.initShapeType(ShapefileType.NULL);
	}

	public void zipFiles(File zipFile) throws IOException {
		FileUtilJava.zipFiles(zipFile, getFiles());
	}
	
	public List<File> getFiles() {
		return Arrays.asList(
			((RandomAccessEndianFile)shpDbfWriter.getShpFile()).getFile(),// 
			((RandomAccessEndianFile)shpDbfWriter.getShxFile()).getFile(),//
			((RandomAccessEndianFile)shpDbfWriter.getDbfFile()).getFile()
		);
	}
	
	public void deleteFilesOnExit(){
		for (File f : getFiles()) {
			f.deleteOnExit();
		}
	}
}
