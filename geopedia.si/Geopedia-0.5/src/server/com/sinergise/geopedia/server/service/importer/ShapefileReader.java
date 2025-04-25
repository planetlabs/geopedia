package com.sinergise.geopedia.server.service.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Date;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.LatLonCRS;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.SwapLatLon;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.TransformUtil;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryTypes;
import com.sinergise.common.geometry.property.GeometryPropertyType;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.util.io.ObjectReader.ObjectReadException;
import com.sinergise.common.util.lang.TypeUtil;
import com.sinergise.common.util.naming.Identifier;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.common.util.property.descriptor.PropertyType;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.java.geometry.io.shp.DBFFile;
import com.sinergise.java.geometry.io.shp.SHPReader;
import com.sinergise.java.util.io.RandomAccessEndianFile;

public class ShapefileReader {

	String filebase;

	DBFReader dbfReader;
	SHPReader shxReader;
	InputStream dbfStream;

	RandomAccessEndianFile shpFile;
	private CFeatureDescriptor tableDescriptor = null;

	protected Transform<?, ?> transform = null;

	public ShapefileReader(File srcFile) throws IOException, ObjectReadException {

		filebase = srcFile.getAbsolutePath();
		if (filebase.toLowerCase().endsWith(".shp")) {
			filebase = filebase.substring(0, filebase.length() - 4);
		}
		String charsetName = null;
		File cpgFile = new File(filebase + ".cpg");
		if (cpgFile.exists()) {
			try {
				FileReader fr = null;
				try {
					fr = new FileReader(cpgFile);
					char[] buffer = new char[255];
					int len = fr.read(buffer);
					if (len > 0) {
						charsetName = new String(buffer, 0, len);
						if (!Charset.isSupported(charsetName)) {
							charsetName = null;
						}
					}
				} finally {
					if (fr != null) {
						try {
							fr.close();
						} catch (Throwable th) {
						}
					}
				}
			} catch (Throwable th) {
				charsetName = null;
			}
		}
		dbfStream = new FileInputStream(filebase + ".dbf");		
		dbfReader = new DBFReader(dbfStream);
		if (charsetName!=null) {
			dbfReader.setCharactersetName(charsetName);
		}
		shpFile = new RandomAccessEndianFile(filebase + ".shp", "r");
		shxReader = new SHPReader(shpFile);

		getFeatureDescriptor();
	}

	public int getGeometryType() {
		switch (shxReader.getShapeType()) {
			case POLYGON:
			case POLYGON_Z:
				return GeometryTypes.GEOM_TYPE_POLYGON;
			case POLYLINE:
			case POLYLINE_Z:
				return GeometryTypes.GEOM_TYPE_LINESTRING;
			case POINT:
			case MULTIPOINT:
			case POINT_Z:
			case MULTIPOINT_Z:
				return GeometryTypes.GEOM_TYPE_POINT;
			default:
				return GeometryTypes.GEOM_TYPE_NONE;
		}
	}

	public int getFeatureCount() {
		return dbfReader.getRecordCount(); // TODO fixthis
	}

	public CFeatureDescriptor getFeatureDescriptor() throws DBFException {
		if (tableDescriptor != null)
			return tableDescriptor;

		tableDescriptor = new CFeatureDescriptor(new Identifier(Identifier.ROOT, ""));

		int nDesc = dbfReader.getFieldCount() + 1;
		PropertyDescriptor descriptors[] = new PropertyDescriptor[nDesc];
		for (int i = 0; i < dbfReader.getFieldCount(); i++) {
			DBFField field = dbfReader.getField(i);
			PropertyType pType = null;
			switch (field.getDataType()) {
				case DBFField.FIELD_TYPE_C:
					pType = new PropertyType(PropertyType.VALUE_TYPE_TEXT);
					pType.setInfoInt(PropertyType.KEY_LENGTH, field.getFieldLength());
					break;
				case DBFField.FIELD_TYPE_D:
					pType = new PropertyType(PropertyType.VALUE_TYPE_DATE);
					break;
				case DBFField.FIELD_TYPE_L:
					pType = new PropertyType(PropertyType.VALUE_TYPE_BOOLEAN);
					break;
				case DBFField.FIELD_TYPE_N:
					if(field.getDecimalCount() <= 0){
						pType = new PropertyType(PropertyType.VALUE_TYPE_LONG);
						break;
					}//if it has decimal count bigger than 0 then we treat as real because of dbase iii which does not have F.
				case DBFField.FIELD_TYPE_F:
					pType = new PropertyType(PropertyType.VALUE_TYPE_REAL);
					break;
					
					
			}
			descriptors[i] = tableDescriptor.createPD(field.getName(), pType);
		}
		descriptors[nDesc - 1] = tableDescriptor.createPD("Geometry", GeometryPropertyType.GENERIC_GEOMETRY);
		tableDescriptor.setValueDescriptors(descriptors);
		tableDescriptor.setGeomIndex(nDesc - 1);
		return tableDescriptor;
	}

	public boolean hasMoreFeatures() throws ObjectReadException {
		return shxReader.hasNext();
	}

	@SuppressWarnings("unchecked")
	protected <T> T getPropertyValue(PropertyDescriptor<T> pDesc, Object value) {
		if (value == null) {
			return null;
		}
		PropertyType<T> pType = pDesc.getType();
		if (pType.isType(PropertyType.VALUE_TYPE_TEXT)) {
			return (T)StringUtil.trimNullEmpty(String.valueOf(value));
			
		} else if (pType.isType(PropertyType.VALUE_TYPE_DATE)) {
			return (T)(Date)value;
			
		} else if (pType.isType(PropertyType.VALUE_TYPE_LONG)) {
			return (T)TypeUtil.toLong(value);
			
		} else if (pType.isType(PropertyType.VALUE_TYPE_BOOLEAN)) {
			return (T)value;
			
		} else if (pType.isType(PropertyType.VALUE_TYPE_REAL)) {
			return (T)TypeUtil.toDouble(value);
		}
		throw new IllegalArgumentException("Unsupported PropertyType: '" + pType.getValueType() + "'");
	}

	public CFeature nextFeature() throws IOException, ObjectReadException {
		CFeature feature = new CFeature(tableDescriptor);
		Geometry geom = shxReader.readNext();
		if (transform != null) {
			TransformUtil.transformGeometry(transform, geom);
		}
		feature.setGeometry(geom);
		Object[] dbfRecords = dbfReader.nextRecord();

		for (int i = 0; i < tableDescriptor.size() - 1; i++) {
			feature.setPropertyValue(i, getPropertyValue(tableDescriptor.getValueDescriptor(i), dbfRecords[i]));
		}
		return feature;
	}

	public void done() throws IOException {
		dbfStream.close();
		shpFile.close();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setTransform(Transform<? extends CRS, ? extends CRS> crsTransform) {
		if (crsTransform == null)
			return;
		if (crsTransform.getTarget() instanceof LatLonCRS) {
			SwapLatLon swap = new SwapLatLon((LatLonCRS) crsTransform.getTarget());
			this.transform = Transforms.compose(crsTransform, swap);
		} else if (crsTransform.getSource() instanceof LatLonCRS) {
			SwapLatLon swap = new SwapLatLon((LatLonCRS) crsTransform.getSource());
			this.transform = Transforms.compose(swap, crsTransform);
		} else {
			this.transform = crsTransform;
		}
	}

	public static void main(String[] args) {
		try {
			/*ShapefileReader sfr = new ShapefileReader(new File("E:\\out\\test1\\testAg.shp"));
			CFeatureDescriptor cfd = sfr.getFeatureDescriptor();
			for (PropertyDescriptor pd : cfd) {
				System.out.println(pd.getType() + " " + pd.getSystemName());
			}

			while (sfr.hasMoreFeatures()) {
				CFeature feat = sfr.nextFeature();
				for (int i = 0; i < 8; i++) {
					System.out.print(feat.getProperty(i).toString() + " ");
				}
				System.out.println();
			}*/

			DBFFile dbfFile = new DBFFile();
			dbfFile.open(new RandomAccessEndianFile(new File("C:\\work\\sample.dbf")));
			int count = dbfFile.getColumnCount();
			for (int i = 0; i < count; i++) {
				com.sinergise.java.geometry.io.shp.DBFField fld = dbfFile.getField(i);
				System.out.println(fld.name);
			}
			DBFReader reader = new DBFReader(new FileInputStream(new File("C:\\work\\kraje1.dbf")));

		} catch (Throwable th) {
			th.printStackTrace();
		}

	}
}
