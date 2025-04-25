package com.sinergise.java.raster.io;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.io.ObjectReader.ObjectReadException;
import com.sinergise.java.geometry.io.shp.DBFField;
import com.sinergise.java.geometry.io.shp.DBFFile;
import com.sinergise.java.geometry.io.shp.SHPHeader.ShapefileType;
import com.sinergise.java.geometry.io.shp.ShpWithAttributesReader;
import com.sinergise.java.geometry.io.shp.ShpWithAttributesWriter;
import com.sinergise.java.raster.core.WorldImageCollection.SrcImageSettings;

public class SrcImageShpWriter {
	
	public static void write(Iterable<? extends SrcImageSettings> data, File outShpFile) throws IOException {
		ShpWithAttributesWriter writer = new ShpWithAttributesWriter(outShpFile);
		try {
			writer.initAttributes(createDescriptor());
			writer.initShapeType(ShapefileType.POLYGON);
			for (SrcImageSettings s : data) {
				writeRecord(s, writer);
			}
		} finally {
			writer.close();
		}
	}

	@SuppressWarnings("boxing")
	public static void writeRecord(SrcImageSettings s, ShpWithAttributesWriter writer) throws IOException {
		DimI size = s.getImageSize();
		AffineTransform2D tr = s.getAffineTransform(CRS.NONAME_WORLD_CRS);
		writer.appendRecord( //
			s.getImagePoly(), //
			new Object[] { //
				s.getExternalId(),//
				s.getFilename(),//
				s.getFileSize(), new Date(s.getFileTime()),//
				s.getWorldFileSize(), new Date(s.getWorldFileTime()),//
				size.w(), size.h(),//
				tr.getScaleX(), tr.getShearX(),//
				tr.getShearY(), tr.getScaleY(),//
				tr.getTranslateX(), tr.getTranslateY()//
			}
		);
	}
	
	public static Iterable<SrcImageSettings> read(File shpFile) throws IOException, ObjectReadException, ParseException {
		ShpWithAttributesReader rdr = new ShpWithAttributesReader(shpFile);
		try {
			List<SrcImageSettings> ret = new ArrayList<SrcImageSettings>();
			while (rdr.hasNext()) {
				ret.add(readNext(rdr));
			}
			return ret;
		} finally {
			rdr.close();
		}
	}

	private static SrcImageSettings readNext(ShpWithAttributesReader rdr) throws ParseException, UnsupportedEncodingException, ObjectReadException {
		rdr.next();
		String[] atts = rdr.getAttributes();
		SrcImageSettings ret = new SrcImageSettings(//
			atts[1], //
			DBFFile.parseDbfDate(atts[3]).getTime(), Long.parseLong(atts[2]),  //
			DBFFile.parseDbfDate(atts[5]).getTime(), Long.parseLong(atts[4]), //
			Integer.parseInt(atts[6]), Integer.parseInt(atts[7]), //
			new double[] {
				Double.parseDouble(atts[8]),
				Double.parseDouble(atts[9]),
				Double.parseDouble(atts[10]),
				Double.parseDouble(atts[11]),
				Double.parseDouble(atts[12]),
				Double.parseDouble(atts[13])
			});
		ret.setExternalId(atts[0]);
		return ret;
	}

	private static DBFField[] createDescriptor() {
		return new DBFField[] {
			createTextField("EXT_ID"), 
			new DBFField("FILENAME", DBFField.TYPE_CHARACTER, 254, (byte)0), 
			createLongField("FILESIZE"),
			createDateField("FILETIME"),
			createLongField("TFWSIZE"),
			createDateField("TFWTIME"),
			createLongField("W"),
			createLongField("H"),
			createDoubleField("TXX"),
			createDoubleField("TXY"),
			createDoubleField("TYX"),
			createDoubleField("TYY"),
			createDoubleField("TOFFX"),
			createDoubleField("TOFFY")};
	}

	private static DBFField createDateField(String name) {
		return new DBFField(name, DBFField.TYPE_DATE, (byte)0, (byte)0);
	}

	private static DBFField createLongField(String name) {
		return new DBFField(name, DBFField.TYPE_NUMERIC, (byte)20, (byte)0);
	}

	private static DBFField createDoubleField(String name) {
		return new DBFField(name, DBFField.TYPE_NUMERIC, (byte)30, (byte)10);
	}

	private static DBFField createTextField(String name) {
		return new DBFField(name, DBFField.TYPE_CHARACTER, (byte)100, (byte)0);
	}
}
