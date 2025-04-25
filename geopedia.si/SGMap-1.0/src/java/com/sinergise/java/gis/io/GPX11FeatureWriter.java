package com.sinergise.java.gis.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.io.FeatureWriter;
import com.sinergise.java.geometry.io.gpx.GPX11Writer;

public class GPX11FeatureWriter implements FeatureWriter {
	
	private final GPX11Writer wrapped;
	
	public GPX11FeatureWriter(OutputStream out) {
		wrapped = new GPX11Writer(new OutputStreamWriter(out));
	}
	
	public GPX11FeatureWriter(Writer writer) {
		wrapped = new GPX11Writer(writer);
	}

	@Override
	public void append(CFeature feature) throws com.sinergise.common.util.io.ObjectWriter.ObjectWriteException {
		wrapped.append(feature.getGeometry());
	}
	
	@Override
	public void close() throws IOException {
		wrapped.close();
	}
	
}
