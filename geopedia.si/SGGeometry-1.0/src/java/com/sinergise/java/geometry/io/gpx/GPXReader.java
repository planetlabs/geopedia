package com.sinergise.java.geometry.io.gpx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.io.GeometryReader;

/**
 * @author tcerovski
 */
public class GPXReader implements GeometryReader {
	
	private static final Logger logger = LoggerFactory.getLogger(GPXReader.class);
	
	public static List<Geometry> readGeometries(Reader reader) throws ObjectReadException {
		List<Geometry> results = new ArrayList<Geometry>();
		
		GPXReader gpxReader = new GPXReader(reader);
		while (gpxReader.hasNext()) {
			results.add(gpxReader.readNext());
		}
		
		return results;
	}
	
	private final Reader reader;
	private GeometryReader instance;
	
	public GPXReader(Reader reader) {
		this.reader = new BufferedReader(reader);
	}
	
	private synchronized GeometryReader getInstance() throws ObjectReadException {
		if (instance == null) {
			try {
				//determine version
				reader.mark(20480); //20 Kb (to take into account all namespace definitions in the tag)
				XMLStreamReader streamReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
				streamReader.nextTag();
				String version = streamReader.getAttributeValue(null, "version");
				
				//choose reader for right version
				if (GPX11Reader.supportsVersion(version)) {
					instance = new GPX11Reader(reader);
				} else if (GPX10Reader.supportsVersion(version)) {
					instance = new GPX10Reader(reader);
				} else {
					throw new ObjectReadException("Unknown version: "+version);
				}
				
				if (instance != null) {
					reader.reset();
				}
			} catch(Exception t) {
				ObjectReadException ex = new ObjectReadException("Error reading GPX: "+t.getMessage(), t);
				logger.error(ex.getMessage(), ex);
				throw ex;
			}
		}
		return instance;
	}
	
	@Override
	public synchronized boolean hasNext() throws ObjectReadException {
		return getInstance().hasNext();
	}
	
	@Override
	public synchronized Geometry readNext() throws ObjectReadException {
		return getInstance().readNext();
	}
	
	@Override
	public void close() throws IOException {
		try {
			getInstance().close();
		} catch(ObjectReadException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

}
