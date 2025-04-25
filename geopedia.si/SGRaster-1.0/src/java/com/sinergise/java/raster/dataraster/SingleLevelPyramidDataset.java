package com.sinergise.java.raster.dataraster;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.TransformerException;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.raster.dataraster.SGDataBank;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeL;
import com.sinergise.common.util.geom.PointI;

public class SingleLevelPyramidDataset {
	final PyramidDataset source;
	final int zoomLevel;
	
	public SingleLevelPyramidDataset(URL baseURL) throws IOException, TransformerException {
		this(new PyramidDataset(baseURL));
	}

	public SingleLevelPyramidDataset(PyramidDataset source) {
		this(source, source.tiles.getMaxLevelId());
	}
	
	public SingleLevelPyramidDataset(PyramidDataset source, int zoomLevel) {
		super();
		this.source = source;
		this.zoomLevel = zoomLevel;
	}


	public double getValueAt(long col, long row) throws MalformedURLException, IOException {
		return source.getValueAt(col, row, zoomLevel);
	}

	public SGDataBank getSubRasterForIndices(EnvelopeL indices) throws IOException {
		return source.getSubRasterForIndices(indices, zoomLevel);
	}

	public SGDataBank getSubRaster(Envelope mbr) throws IOException {
		return source.getSubRaster(mbr, zoomLevel);
	}

	public SGDataBank getSubRasterCropped(Envelope mbr) throws IOException {
		return source.getSubRasterCropped(mbr, zoomLevel);
	}
	
	public Envelope getEnvelope(EnvelopeL indices){
		return source.getEnvelope(indices, zoomLevel);
	}

	public TiledCRS getTiledCRS() {
		return source.getTiledCRS();
	}
}
