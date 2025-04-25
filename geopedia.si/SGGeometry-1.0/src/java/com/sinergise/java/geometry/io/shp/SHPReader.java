package com.sinergise.java.geometry.io.shp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.io.GeometryReader;
import com.sinergise.common.geometry.topo.EvenOddPolygonizer;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.io.IOUtil;
import com.sinergise.java.geometry.io.shp.SHPHeader.ShapefileType;
import com.sinergise.java.util.io.BinaryInput.BinaryRandomAccessInput;
import com.sinergise.java.util.io.RandomAccessEndianFile;

//TODO: Check handling of null shape as well as M and Z geometries
public class SHPReader implements GeometryReader {

	private static final Logger logger = LoggerFactory.getLogger(SHPReader.class);

	private boolean closeSourceOnReaderClose = false;
	private final BinaryRandomAccessInput shpSource;

	private int posIn16bitWords = 50; // header size

	private SHPHeader header;

	public SHPReader(File shpFile) throws ObjectReadException, FileNotFoundException {
		this(RandomAccessEndianFile.createReadOnly(shpFile), true);
	}

	public SHPReader(BinaryRandomAccessInput shpSource) throws ObjectReadException {
		this(shpSource, false);
	}
	
	public SHPReader(BinaryRandomAccessInput shpSource, boolean closeSourceOnReaderClose) throws ObjectReadException {
		this.shpSource = shpSource;
		this.closeSourceOnReaderClose = closeSourceOnReaderClose;
		try {
			readHeader(shpSource, header = new SHPHeader());

			// try to fix broken shapefiles (sometimes the file length in the header is <= 0)
			if (header.getFileLengthIn16bitWords() <= 0) {
				header.setFileLengthIn16bitWords((int)(shpSource.length() / 2));
			}
		} catch(IOException e) {
			String msg = "Error reading shape header: " + e.getMessage();
			logger.error(msg, e);
			if (closeSourceOnReaderClose) {
				try {
					shpSource.close();
				} catch(IOException e1) {
					throw new RuntimeException(e1);
				}
			}
			throw new ObjectReadException(msg, e);
		}
	}
	
	public Envelope getFileMBR() {
		return new Envelope(
			header.getXmin(), header.getYmin(), 
			header.getXmax(), header.getYmax());
	}

	public ShapefileType getShapeType() {
		return header.getShapeType();
	}

	protected Envelope readMBR() throws IOException {
		return new Envelope(//
			shpSource.readDoubleLE(),// 
			shpSource.readDoubleLE(),//
			shpSource.readDoubleLE(),//
			shpSource.readDoubleLE());
	}

	@Override
	public Geometry readNext() throws ObjectReadException {
		if (!hasNext()) {
			throw new ObjectReadException("End of data reached!");
		}

		try {
			shpSource.seek(posIn16bitWords * 2);
			shpSource.readInt(); // recordIdx
			int contentLength = shpSource.readInt(); // contentLength
			posIn16bitWords += contentLength + 4; // +2 recordIdx, +2 contentLength
			ShapefileType shapeType = ShapefileType.valueOf(shpSource.readIntLE());

			switch (shapeType) {
				case NULL:
					return null;
				case MULTIPOINT:
					return readMultiPoint(false, contentLength);
				case MULTIPOINT_Z:
					return readMultiPoint(true, contentLength);
				case POINT:
					return readPoint(false, contentLength);
				case POINT_Z:
					return readPoint(true, contentLength);
				case POLYGON:
					return readPolygon(false, contentLength);
				case POLYGON_Z:
					return readPolygon(true, contentLength);
				case POLYLINE:
					return readPolyLine(false, contentLength);
				case POLYLINE_Z:
					return readPolyLine(true, contentLength);
				default:
					throw new IllegalArgumentException("Unknown geometry type " + shapeType);
			}
		} catch(IOException e) {
			String msg = "Error reading shape: " + e.getMessage();
			logger.error(msg, e);
			throw new ObjectReadException(msg, e);
		}
	}

	@Override
	public boolean hasNext() throws ObjectReadException {
		return posIn16bitWords < header.getFileLengthIn16bitWords();
	}

	@Override
	public void close() throws IOException {
		if (closeSourceOnReaderClose) {
			IOUtil.close(shpSource);
		}
	}

	protected void readHeader(BinaryRandomAccessInput io, SHPHeader outHeader) throws IOException {
		io.seek(0);
		if (io.readInt() != SHPHeader.FILE_CODE) {
			throw new IOException("This is not a valid SHP file!");
		}
		for (int i = 0; i < 5; i++) {
			io.readInt(); //Skip unused part of header
		}
		outHeader.setFileLengthIn16bitWords(io.readInt());
		if (io.readIntLE() != SHPHeader.VERSION) {
			throw new IOException("Illegal SHP file version!");
		}
		outHeader.setShapeType(ShapefileType.valueOf(io.readIntLE()));
		outHeader.setXmin(io.readDoubleLE());
		outHeader.setYmin(io.readDoubleLE());
		outHeader.setXmax(io.readDoubleLE());
		outHeader.setYmax(io.readDoubleLE());
		for (int i = 0; i < 4; i++) {
			io.readDoubleLE(); //Skip unused part of header
		}
		posIn16bitWords = 2 + 5 * 2 + 2 + 2 + 2 + 4 * 4 + 4 * 4;
	}

	protected MultiPoint readMultiPoint(boolean isMultipointZ, int dataSize) throws IOException {
		readMBR(); // MBR
		int nPoints = shpSource.readIntLE();
		boolean hasMeasurements = false;

		// type(2)+nRings(2)+nPoints(2)+nRings*2+mbr(4*4)+ZRange(2*4)+3(x,y,z)*4*nPoints
		int expectedSize = 2 + 2 + 4 * 4 + 2 * 4 + 3 * 4 * nPoints;
		if (expectedSize < dataSize) {
			hasMeasurements = true;
		}


		Point points[] = new Point[nPoints];
		for (int i = 0; i < nPoints; i++) {
			double x = shpSource.readDoubleLE();
			double y = shpSource.readDoubleLE();
			points[i] = new Point(x, y);
		}

		if (isMultipointZ) {
			shpSource.readDoubleLE(); // minZ
			shpSource.readDoubleLE(); // maxZ
			for (int i = 0; i < nPoints; i++) {
				points[i].z = shpSource.readDoubleLE();
			}
			if (hasMeasurements) {
				shpSource.readDoubleLE(); //minM
				shpSource.readDoubleLE(); //maxM
				for (int i = 0; i < nPoints; i++) {
					shpSource.readDoubleLE(); //points[i].m
				}
			}
		}

		return new MultiPoint(points);

	}

	protected Point readPoint(boolean isPointZ, int dataSize) throws IOException {
		boolean hasMeasurements = false;
		int expectedSize = 2 + 3 * 4;
		if (expectedSize < dataSize)
			hasMeasurements = true;

		double x = shpSource.readDoubleLE();
		double y = shpSource.readDoubleLE();
		Point ret = new Point(x, y);

		if (isPointZ) {
			ret.z = shpSource.readDouble();
			if (hasMeasurements) {
				shpSource.readDouble(); // ret.m
			}
		}
		return ret;
	}

	protected Geometry readPolyLine(boolean isPolyLineZ, int dataSize) throws IOException {
		readMBR(); // MBR

		int nParts = shpSource.readIntLE();
		int nPoints = shpSource.readIntLE();

		boolean hasMeasurements = false;

		// type(2)+nRings(2)+nPoints(2)+nRings*2+mbr(4*4)+ZRange(2*4)+3(x,y,z)*4*nPoints
		int expectedSize = 2 + 2 + 2 + nParts*2 + 4*4 + 2*4 + 3*4*nPoints;
		if (expectedSize < dataSize)
			hasMeasurements = true;

		int[] ringPoints = new int[nParts];
		int prevOffset = shpSource.readIntLE();

		for (int i = 0; i < nParts; i++) {
			if (i < (nParts - 1)) {
				int offset = shpSource.readIntLE();
				ringPoints[i] = offset - prevOffset;
				prevOffset = offset;
			} else {
				ringPoints[i] = nPoints - prevOffset;
			}
		}
		LineString[] lines = new LineString[nParts];
		for (int i = 0; i < nParts; i++) {
			double[] coords = new double[ringPoints[i] * 2];
			for (int j = 0; j < ringPoints[i]; j++) {
				coords[j * 2] = shpSource.readDoubleLE();
				coords[j * 2 + 1] = shpSource.readDoubleLE();
			}
			lines[i] = new LineString(coords);
		}

		if (isPolyLineZ) {
			shpSource.readDoubleLE(); // minZ
			shpSource.readDoubleLE(); // maxZ
			for (int i = 0; i < nPoints; i++) {
				shpSource.readDoubleLE(); //z[i]
			}
			if (hasMeasurements) {
				shpSource.readDoubleLE(); // minM
				shpSource.readDoubleLE(); // maxM
				for (int i = 0; i < nPoints; i++) {
					shpSource.readDoubleLE(); //m[i]
				}
			}
		}

		if (lines.length == 1) {
			return lines[0];
		}
		return new MultiLineString(lines);

	}

	protected Geometry readPolygon(boolean isPolygonZ, int dataSize) throws IOException {
		readMBR();
		int nRings = shpSource.readIntLE();
		int nPoints = shpSource.readIntLE();
		// type(2)+nRings(2)+nPoints(2)+nRings*2+mbr(4*4)+ZRange(2*4)+3(x,y,z)*4*nPoints
		int expectedSize = 2 + 2 + 2 + nRings * 2 + 4 * 4 + 2 * 4 + 3 * 4 * nPoints;
		boolean hasMeasurements = false;
		if (expectedSize < dataSize)
			hasMeasurements = true;

		int[] ringPoints = new int[nRings];
		int prevOffset = shpSource.readIntLE();

		for (int i = 0; i < nRings; i++) {
			if (i < (nRings - 1)) {
				int offset = shpSource.readIntLE();
				ringPoints[i] = offset - prevOffset;
				prevOffset = offset;
			} else {
				ringPoints[i] = nPoints - prevOffset;
			}
		}
		ArrayList<LinearRing> ringsHolder = new ArrayList<LinearRing>();
		for (int i = 0; i < nRings; i++) {
			double[] coords = new double[ringPoints[i] * 2];

			for (int j = 0; j < ringPoints[i]; j++) {
				coords[j * 2] = shpSource.readDoubleLE();
				coords[j * 2 + 1] = shpSource.readDoubleLE();
			}

			LinearRing lr = new LinearRing(coords, false);
			ringsHolder.add(lr);
		}


		if (isPolygonZ) {
			shpSource.readDoubleLE(); // minZ
			shpSource.readDoubleLE(); // maxZ
			for (int i = 0; i < nPoints; i++) {
				shpSource.readDoubleLE(); // z[i]
			}
			if (hasMeasurements) {
				shpSource.readDoubleLE(); //minM
				shpSource.readDoubleLE(); //maxM
				for (int i = 0; i < nPoints; i++) {
					shpSource.readDoubleLE(); // m[i]
				}
			}
		}

		return EvenOddPolygonizer.polygonize(ringsHolder);

	}

}
