package com.sinergise.java.geometry.io.gpx;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.io.GeometryReader;
import com.topografix.gpx.schema10.Gpx;
import com.topografix.gpx.schema10.Gpx.Rte;
import com.topografix.gpx.schema10.Gpx.Rte.Rtept;
import com.topografix.gpx.schema10.Gpx.Trk;
import com.topografix.gpx.schema10.Gpx.Trk.Trkseg;
import com.topografix.gpx.schema10.Gpx.Trk.Trkseg.Trkpt;
import com.topografix.gpx.schema10.Gpx.Wpt;

/**
 * @author tcerovski
 */
class GPX10Reader implements GeometryReader {
	
	private static final String GPX_VERSION = "1.0";
	
	static boolean supportsVersion(String version) {
		return GPX_VERSION.equals(version);
	}
	
	private final Reader reader;
	
	private List<Geometry> geometries;
	private int pos = -1;
	
	GPX10Reader(Reader reader) {
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
			
			JAXBContext jc = JAXBContext.newInstance(Gpx.class.getPackage().getName());
			
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			Gpx root = (Gpx) unmarshaller.unmarshal(reader);
			
			geometries.addAll(readWayPoints(root.getWpt()));
			geometries.addAll(readRoutes(root.getRte()));
			geometries.addAll(readTracks(root.getTrk()));
		} catch (Throwable t) {
			throw new ObjectReadException("Error reading GPX 1.0: "+t.getMessage(), t);
		}
	}
	
	List<Point> readWayPoints(List<Wpt> wpts) {
		
		List<Point> points = new ArrayList<Point>();
		for (Wpt wpt : wpts) {
			if (wpt.getLat() != null && wpt.getLon() != null) {
				points.add(new Point(wpt.getLat().doubleValue(), wpt.getLon().doubleValue()));
			}
		}
		
		return points;
	}
	
	List<LineString> readRoutes(List<Rte> routes) {
		
		List<LineString> lines = new ArrayList<LineString>();
		for (Rte route : routes) {
			lines.add(readRoutPointsSequence(route.getRtept()));
		}
		
		return lines;
	}
	
	List<MultiLineString> readTracks(List<Trk> tracks) {
		
		List<MultiLineString> multilines = new ArrayList<MultiLineString>();
		for (Trk track : tracks) {
			List<LineString> lines = new ArrayList<LineString>();
			for (Trkseg segment : track.getTrkseg()) {
				lines.add(readTrackPointsSequence(segment.getTrkpt()));
			}
			multilines.add(new MultiLineString(lines.toArray(new LineString[lines.size()])));
		}
		return multilines;
	}
	
	LineString readRoutPointsSequence(List<Rtept> wpts) {
		double coords[] = new double[wpts.size()*2];
		int i = 0;
		for (Rtept wpt : wpts) {
			coords[i++] = wpt.getLat() != null ? wpt.getLat().doubleValue() : Double.NaN;
			coords[i++] = wpt.getLon() != null ? wpt.getLon().doubleValue() : Double.NaN;
		}
		return new LineString(coords);
	}
	
	LineString readTrackPointsSequence(List<Trkpt> wpts) {
		double coords[] = new double[wpts.size()*2];
		int i = 0;
		for (Trkpt wpt : wpts) {
			coords[i++] = wpt.getLat() != null ? wpt.getLat().doubleValue() : Double.NaN;
			coords[i++] = wpt.getLon() != null ? wpt.getLon().doubleValue() : Double.NaN;
		}
		return new LineString(coords);
	}
	
}
