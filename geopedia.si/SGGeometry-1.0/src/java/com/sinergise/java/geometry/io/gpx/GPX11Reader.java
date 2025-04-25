package com.sinergise.java.geometry.io.gpx;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.io.GeometryReader;
import com.topografix.gpx.schema11.GpxType;
import com.topografix.gpx.schema11.RteType;
import com.topografix.gpx.schema11.TrkType;
import com.topografix.gpx.schema11.TrksegType;
import com.topografix.gpx.schema11.WptType;

/**
 * @author tcerovski
 */
class GPX11Reader implements GeometryReader {

	private static final String GPX_VERSION = "1.1";
	
	static boolean supportsVersion(String version) {
		return GPX_VERSION.equals(version);
	}
	
	private final Reader reader;
	
	private List<Geometry> geometries;
	private int pos = -1;
	
	GPX11Reader(Reader reader) {
		this.reader = reader;
	}
	
	private synchronized List<Geometry> getGeometries() throws ObjectReadException {
		if (geometries == null) {
			readAll();
			pos = 0;
		}
		return geometries;
	}
	
	@Override
	public synchronized boolean hasNext() throws ObjectReadException {
		return pos < getGeometries().size();
	}
	
	@Override
	public synchronized Geometry readNext() throws ObjectReadException {
		if (!hasNext()) {
			throw new ObjectReadException("End of data reached!");
		}
		return geometries.get(pos++);
	}
	
	@Override
	public void close() throws IOException {
		geometries = null;
	}
	
	
	private void readAll() throws ObjectReadException {
		geometries = new ArrayList<Geometry>();
		
		try {
			JAXBContext jc = JAXBContext.newInstance(GpxType.class.getPackage().getName());
			
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			GpxType root = (GpxType)((JAXBElement<?>) unmarshaller.unmarshal(reader)).getValue();

			geometries.addAll(readWayPoints(root.getWpt()));
			geometries.addAll(readRoutes(root.getRte()));
			geometries.addAll(readTracks(root.getTrk()));
		} catch (Throwable t) {
			throw new ObjectReadException("Error reading GPX 1.1: "+t.getMessage(), t);
		}
	}
	
	List<Point> readWayPoints(List<WptType> wpts) {
		
		List<Point> points = new ArrayList<Point>();
		for (WptType wpt : wpts) {
			if (wpt.getLat() != null && wpt.getLon() != null) {
				points.add(new Point(wpt.getLat().doubleValue(), wpt.getLon().doubleValue()));
			}
		}
		
		return points;
	}
	
	List<LineString> readRoutes(List<RteType> routes) {
		
		List<LineString> lines = new ArrayList<LineString>();
		for (RteType route : routes) {
			lines.add(readWayPointsSequence(route.getRtept()));
		}
		
		return lines;
	}
	
	List<MultiLineString> readTracks(List<TrkType> tracks) {
		
		List<MultiLineString> multilines = new ArrayList<MultiLineString>();
		for (TrkType track : tracks) {
			List<LineString> lines = new ArrayList<LineString>();
			for (TrksegType segment : track.getTrkseg()) {
				lines.add(readWayPointsSequence(segment.getTrkpt()));
			}
			multilines.add(new MultiLineString(lines.toArray(new LineString[lines.size()])));
		}
		return multilines;
	}
	
	LineString readWayPointsSequence(List<WptType> wpts) {
		double coords[] = new double[wpts.size()*2];
		int i = 0;
		for (WptType wpt : wpts) {
			coords[i++] = wpt.getLat() != null ? wpt.getLat().doubleValue() : Double.NaN;
			coords[i++] = wpt.getLon() != null ? wpt.getLon().doubleValue() : Double.NaN;
		}
		return new LineString(coords);
	}
	
}
