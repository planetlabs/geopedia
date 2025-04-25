package com.sinergise.java.geometry.io.shp;

import java.io.IOException;
import java.util.ArrayList;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.LatLonCRS;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.SwapLatLon;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.geometry.io.CRSTransformable;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.java.geometry.io.shp.SHPHeader.ShapefileType;
import com.sinergise.java.util.io.BinaryOutput.BinaryRandomAccessOutput;



public class SHPSHXWriter extends CRSTransformable {
	BinaryRandomAccessOutput shpTarget;
	BinaryRandomAccessOutput shxTarget;
	
	protected int recordIdx=0;
	protected int recordOffset=50; // record offset in 16-bit words
	
	protected SHPHeader header;
	protected EnvelopeBuilder fileMBR;
	
		
	public SHPSHXWriter (BinaryRandomAccessOutput shpFile, BinaryRandomAccessOutput shxFile) {
		fileMBR = new EnvelopeBuilder();
		header = new SHPHeader();
		this.shpTarget = shpFile;
		this.shxTarget = shxFile;
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setTransform(Transform<? extends CRS, ? extends CRS> crsTransform) {
		if (crsTransform.getTarget() instanceof LatLonCRS) {			
			SwapLatLon swap = new SwapLatLon((LatLonCRS) crsTransform.getTarget());
			this.transform = Transforms.compose(crsTransform, swap);			
		} else {
			this.transform = crsTransform;
		}
	}

	
	public void initialize (ShapefileType shapeType) throws IOException {
		header.setShapeType(shapeType);
		writeHeader(shpTarget, header);
		writeHeader(shxTarget, header);
	}
	
	public void done() throws IOException {
		
		// write MBR
		Point p = transform(fileMBR.getMaxX(), fileMBR.getMaxY());
		header.setXmax(p.x);
		header.setYmax(p.y);
		p = transform(fileMBR.getMinX(), fileMBR.getMinY());
		header.setXmin(p.x);
		header.setYmin(p.y);
		// write shp size
		header.setFileLengthIn16bitWords(recordOffset);	
		writeHeader(shpTarget, header);
		// write shx size
		header.setFileLengthIn16bitWords(50+4*recordIdx);
		writeHeader(shxTarget, header);
	}
	
	
	public void appendGeometry(Geometry geom) throws IOException {
		if (geom==null) {
			appendNull();
			return;
		}
		
		if (getShapeType() == null) {
			initialize(ShapefileType.forGeometry(geom));
		}
		
		fileMBR.expandToInclude(geom.getEnvelope());
		
		switch (header.getShapeType()) {
			case POINT:
				appendPoint((Point)geom);
				break;
			case MULTIPOINT:
				appendMultiPoint((MultiPoint)geom);
				break;
			case POLYGON:
				if (geom instanceof MultiPolygon) {
					appendMultiPolygon((MultiPolygon)geom);
				} else {
					appendMultiPolygon(new MultiPolygon(new Polygon[]{(Polygon)geom}));
				}
				break;
			case POLYLINE: 
				if (geom instanceof MultiLineString) {
					appendPolyLine((MultiLineString)geom);
				} else {
					appendPolyLine(new MultiLineString(new LineString[] {(LineString)geom}));
				}
				break;
			case NULL:
				appendNull();
				break;
			default:
				throw new IllegalArgumentException("Shape type "+header.getShapeType() +"not supported!");
		}
	}
	
	public ShapefileType getShapeType() {
		return header.getShapeType();
	}
		
	protected void writeHeader(BinaryRandomAccessOutput io, SHPHeader headerToWrite) throws IOException {
		io.seek(0);
		io.writeInt(SHPHeader.FILE_CODE);
		for (int i = 0; i < 5; i++) {
			io.writeInt(0); //Skip unused part of header
		}
		io.writeInt(headerToWrite.getFileLengthIn16bitWords());
		io.writeIntLE(SHPHeader.VERSION);
		io.writeIntLE(headerToWrite.getShapeType().getValue());
		io.writeDoubleLE(headerToWrite.getXmin());
		io.writeDoubleLE(headerToWrite.getYmin());
		io.writeDoubleLE(headerToWrite.getXmax());
		io.writeDoubleLE(headerToWrite.getYmax());
		for (int i = 0; i < 4; i++)
			io.writeDoubleLE(0); //Skip unused part of header
	}
	
	
	protected void appendRecordHeader(int contentLength) throws IOException {
		shpTarget.writeInt(recordIdx);
		shpTarget.writeInt(contentLength);
	}
	protected void appendIndexRecord(int contentLength) throws IOException {
		shxTarget.writeInt(recordOffset);
		shxTarget.writeInt(contentLength);
		recordOffset += contentLength + 4;
	}
	
	protected void appendMBR(Envelope mbr) throws IOException {
		if (mbr == null) {
			shpTarget.writeDoubleLE(-1E40);
			shpTarget.writeDoubleLE(-1E40);
			shpTarget.writeDoubleLE(-1E40);
			shpTarget.writeDoubleLE(-1E40);
		} else {
			Point p = transform(mbr.getMinX(), mbr.getMinY());
			shpTarget.writeDoubleLE(p.x);
			shpTarget.writeDoubleLE(p.y);
			p = transform(mbr.getMaxX(), mbr.getMaxY());
			shpTarget.writeDoubleLE(p.x);
			shpTarget.writeDoubleLE(p.y);
		}
	}
	
	protected final void appendNull() throws IOException {
		int contentLength = 2;
		recordIdx++;
		appendRecordHeader(contentLength);
		appendIndexRecord(contentLength);
		shpTarget.writeIntLE(ShapefileType.NULL.getValue());
	}
	
	protected final void appendPoint(Point point) throws IOException {
		//write record header
		int contentLength = 10;
		recordIdx++;
		appendRecordHeader(contentLength);
		appendIndexRecord(contentLength);

		shpTarget.writeIntLE(ShapefileType.POINT.getValue());
		
		//write coordinates
		Point p = transform(point);
		shpTarget.writeDoubleLE(p.x);
		shpTarget.writeDoubleLE(p.y);
	}
	
	
	
	protected final void appendMultiPoint(MultiPoint mpoint) throws IOException {
		int numPoints = mpoint.size();
	
		int contentLength = 20+numPoints*8;
		recordIdx++;
		appendRecordHeader(contentLength);
		appendIndexRecord(contentLength);
		shpTarget.writeIntLE(ShapefileType.MULTIPOINT.getValue());
		appendMBR(mpoint.getEnvelope());
		shpTarget.writeIntLE(numPoints);
		for (int i=0;i<numPoints;i++) {
			Point point = mpoint.get(i);
			Point p = transform(point);
			shpTarget.writeDoubleLE(p.x);
			shpTarget.writeDoubleLE(p.y);
		}
	}
	
	
	protected final void appendMultiPolygon(MultiPolygon mPoly) throws IOException  {
		int nPolygons = mPoly.size();
		int numRings=nPolygons;
		int numPoints=0;
		ArrayList<Integer> ringOffsets = new ArrayList<Integer>();
		for (int i=0;i<nPolygons;i++) {
			Polygon p = mPoly.get(i);
			numRings+=p.getNumHoles();
			ringOffsets.add(Integer.valueOf(numPoints));
			numPoints+=p.getOuter().getNumCoords();
			for (int j=0;j<p.getNumHoles();j++) {
				ringOffsets.add(Integer.valueOf(numPoints));
				numPoints+=p.getHole(j).getNumCoords();
			}
		}
		//write record header
		int contentLength = 22 + numRings * 2 + numPoints * 8;
		recordIdx++;
		appendRecordHeader(contentLength);
		appendIndexRecord(contentLength);

		shpTarget.writeIntLE(ShapefileType.POLYGON.getValue());
		appendMBR(mPoly.getEnvelope());
		
		shpTarget.writeIntLE(numRings);
		shpTarget.writeIntLE(numPoints);
		
		//rings offsets
		for (Integer offset:ringOffsets) {
			shpTarget.writeIntLE(offset.intValue());
		}
		
		for (int i=0;i<nPolygons;i++) {
			Polygon p = mPoly.get(i);
			LinearRing extRing = transform(p.getOuter()).ensureCW();			
			for(int j=0; j < extRing.getNumCoords(); j++) {
				shpTarget.writeDoubleLE(extRing.getX(j));
				shpTarget.writeDoubleLE(extRing.getY(j));
			}
			
			for (int k=0;k<p.getNumHoles();k++) {
				LinearRing intRing = transform(p.getHole(k)).ensureCCW();
				for(int j=0; j < intRing.getNumCoords(); j++) {
					shpTarget.writeDoubleLE(intRing.getX(j));
					shpTarget.writeDoubleLE(intRing.getY(j));
				}
			}
			
		}		
	}
	
	protected final void appendPolyLine(MultiLineString mline) throws IOException  {
		int numParts = mline.size(); //get parts count
		int numPoints = 0;
		int lines[] = new int[numParts];
		
		for(int i=0; i<numParts; i++){ //get points count
			lines[i] = numPoints;
			numPoints += mline.get(i).getNumCoords();
		}
		int contentLength = 22+2*numParts+numPoints*8;
		recordIdx++;
		appendRecordHeader(contentLength);
		appendIndexRecord(contentLength);

		shpTarget.writeIntLE(ShapefileType.POLYLINE.getValue());
		appendMBR(mline.getEnvelope());
		
		shpTarget.writeIntLE(numParts);
		shpTarget.writeIntLE(numPoints);
		
		//lines offsets
		for (int i = 0; i < numParts; i++) {
			shpTarget.writeIntLE(lines[i]);
		}
		//lines
		
		for(int i=0; i < numParts; i++) {
			LineString line = mline.get(i);
			for(int j=0; j < line.getNumCoords(); j++) { //points
				Point p = transform(line.getX(j),line.getY(j));
				shpTarget.writeDoubleLE(p.x);
				shpTarget.writeDoubleLE(p.y);				
			}
		}
	}

	
}
