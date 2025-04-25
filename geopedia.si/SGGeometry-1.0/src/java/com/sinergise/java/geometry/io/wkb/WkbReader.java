package com.sinergise.java.geometry.io.wkb;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.geometry.io.GeometryReader.AbstractGeometryReaderImpl;
import com.sinergise.common.geometry.io.OgcGeomType;
import com.sinergise.common.geometry.io.OgcShapeType;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.io.IOUtil;
import com.sinergise.java.util.io.BinaryInput;
import com.sinergise.java.util.io.ByteArrayInputStream;

/**
 * Not thread-safe. One instance per thread is OK
 *  
 * @author pkolaric, mkadunc
 */
public class WkbReader extends AbstractGeometryReaderImpl {
	private BinaryInput input;
	
	protected boolean curBE;
	protected OgcShapeType curShapeType;
	protected boolean curHasZ;
	protected boolean curHasM;
	
	public WkbReader() {
	}
	
	public WkbReader(BinaryInput input) {
		setInput(input);
	}

	public void setInput(BinaryInput input) {
		this.input = input;
		super.reset();
	}

	public Geometry readGeometry(BinaryInput bInput) throws ObjectReadException {
		try {
			setInput(bInput);
			return readNext();
		} finally {
			this.input = null;
		}
	}

	@SuppressWarnings("resource")
	public Geometry readGeometry(byte[] wkbBytes) throws ObjectReadException {
		if (ArrayUtil.isNullOrEmpty(wkbBytes)) {
			return null;
		}
		return readGeometry(new ByteArrayInputStream(wkbBytes));
	}

	@Override
	protected Geometry internalReadNext() throws ObjectReadException {
		try {
			return internalReadGeometry(OgcShapeType.GEOMETRY);
		} catch (EOFException eof) {
			return null;
		} catch (IOException ioe) {
			throw new ObjectReadException("Unexpected IO exception", ioe);
		}
	}
	
	protected Geometry internalReadGeometry(OgcShapeType expectedType) throws ObjectReadException, IOException {
		readByteOrder();
		readGeomType();
		return readGeomContent();
	}

	private void readByteOrder() throws IOException {
		curBE = WkbByteOrder.valueOf(input.readByte()) == WkbByteOrder.BIG_ENDIAN;
	}
	
	protected int readInt32() throws IOException {
		return curBE ? input.readInt() : input.readIntLE();
	}

	protected void readGeomType() throws IOException {
		int int32Val = readInt32();
		curShapeType = OgcShapeType.fromWkbHeader(int32Val);
		final int coordBits = OgcGeomType.getCoordBits(int32Val);
		curHasZ = OgcGeomType.hasZ(coordBits);
		curHasM = OgcGeomType.hasM(coordBits);
	}

	protected Geometry readGeomContent() throws IOException, ObjectReadException {
		switch (curShapeType) {
			case POINT:
				return readCoord();
			case LINESTRING:
				return readLineStringContent();
			case POLYGON:
				return readPolygonContent();
			case MULTIPOINT:
			case MULTILINESTRING:
			case MULTIPOLYGON:
			case GEOMETRYCOLLECTION:
				return readCompositeContent();
			default:
				throw new UnsupportedOperationException("Shape type: '" + curShapeType + "' is not supported!");
		}
	}

	protected Point readCoord() throws IOException {
		Point p = new Point(readDouble(), readDouble());
		if (curHasZ) {
			p.setZ(readDouble());
		}
		if (curHasM) {
			readDouble(); //TODO: set M when we supported it; for now just read
		}
		return p;
	}

	protected double readDouble() throws IOException {
		return curBE ? input.readDouble() : input.readDoubleLE();
	}

	protected LineString readLineStringContent() throws IOException {
		return new LineString(readCoords());
	}

	protected double[] readCoords() throws IOException {
		int numPoints = readInt32();
		double coordinates[] = new double[numPoints*2];
	
		int n = 0;
		for (int i = numPoints; i > 0; i--) {
			coordinates[n++] = readDouble();
			coordinates[n++] = readDouble();
			if (curHasZ) {
				readDouble(); //TODO: Store Z when we support it; for now just read
			}
			if (curHasM) {
				readDouble(); //TODO: Store M when we support it; for now just read
			}
		}
		return coordinates;
	}

	protected Polygon readPolygonContent() throws IOException {
		int size = readInt32();
		LineString[] members = new LineString[size];
		for (int i = 0; i < size; i++) {
			members[i] = readLineStringContent();
		}
		return Polygon.create(Arrays.asList(members));
	}
	
	@SuppressWarnings("unchecked")
	protected <C extends Geometry> C readCompositeContent() throws IOException, ObjectReadException {
		int size = readInt32();
		OgcShapeType compositeType = curShapeType;
		Geometry[] members = compositeType.createMemberArray(size);
		OgcShapeType memberType = compositeType.getMemberType(); //Store local because it will be overridden
		for (int i = 0; i < size; i++) {
			members[i] = internalReadGeometry(memberType);
		}
		return (C)compositeType.createInstance(Arrays.asList(members));
	}

	@Override
	public void close() throws IOException {
		IOUtil.close(input);
	}
	
	public static Geometry readWkb(byte[] wkbData) throws ObjectReadException {
		WkbReader rdr = new WkbReader();
		try {
			return rdr.readGeometry(wkbData);
		} finally {
			IOUtil.closeSilent(rdr);
		}
	}
}
