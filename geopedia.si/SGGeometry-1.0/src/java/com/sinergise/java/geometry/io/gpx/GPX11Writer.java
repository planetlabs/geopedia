package com.sinergise.java.geometry.io.gpx;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CrsRepository;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.GeometryCollection;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.geometry.io.GeometryWriter;
import com.sinergise.common.geometry.io.TransformingGeometryWriter;
import com.sinergise.common.geometry.util.GeometryVisitor;
import com.topografix.gpx.schema11.GpxType;
import com.topografix.gpx.schema11.ObjectFactory;
import com.topografix.gpx.schema11.TrkType;
import com.topografix.gpx.schema11.TrksegType;
import com.topografix.gpx.schema11.WptType;

public class GPX11Writer implements GeometryWriter {
	
	private static final String GPX_VERSION = "1.1";
	
	private static final Logger logger = LoggerFactory.getLogger(GPX11Writer.class);
	
	private final Writer writer;
	private final GPX11WriterVisitor visitor;
	private boolean closed = false;
	
	public GPX11Writer(OutputStream out) {
		this(new OutputStreamWriter(out));
	}
	
	public GPX11Writer(Writer writer) {
		this.writer = writer;
		this.visitor = new GPX11WriterVisitor();
	}

	@Override
	public void append(Geometry g) throws ObjectWriteException {
		if (g == null) {
			visitor.addNull();
			return;
		}
		
		if (!CrsRepository.INSTANCE.equals(CRS.WGS84.getDefaultIdentifier(), g.getCrsId())) {
			throw new ObjectWriteException("Unsupported SRID: "+g.getCrsId()+", or SRID not set. Only "+CRS.WGS84.getDefaultIdentifier()
				+ " supported. Try using TransformingGeometryWriter to transform geometries.");
		}
		
		g.accept(visitor);
	}
	
	/**
	 * Sets gpx tag creator attribute value.
	 * @param value to set
	 */
	public void setCreator(String value) {
		visitor.root.setCreator(value);
	}

	@Override
	public void close() throws IOException {
		if (closed) {
			throw new IOException("Writer already closed.");
		}
		try {
			visitor.root.setVersion(GPX_VERSION);
			
			JAXBContext jc = JAXBContext.newInstance(GpxType.class.getPackage().getName());
			Marshaller marshaller = jc.createMarshaller();
			marshaller.marshal(new ObjectFactory().createGpx(visitor.root), writer);
			closed = true;
		} catch(JAXBException e) {
			String msg = "Error writing GPX document: "+e.getMessage();
			logger.error(msg, e);
			throw new IOException(msg, e);
		}
	}
	
	private static class GPX11WriterVisitor implements GeometryVisitor {
		
		final GpxType root = new GpxType();
		
		@Override
		public void visitPoint(Point point) {
			WptType wpt = new WptType();
			wpt.setLat(BigDecimal.valueOf(point.x));
			wpt.setLon(BigDecimal.valueOf(point.y));
			root.getWpt().add(wpt);
		}
		
		@Override
		public void visitLineString(LineString line) {
			TrkType trk = new TrkType();
			trk.getTrkseg().add(coordsToTrkseg(line.coords));
			root.getTrk().add(trk);
		}
		
		public void addNull() {
			TrkType trk = new TrkType();
			root.getTrk().add(trk);
		}
		
		@Override
		public void visitPolygon(Polygon poly) {
			TrkType trk = new TrkType();
			trk.getTrkseg().add(coordsToTrkseg(poly.outer.coords));
			for (int i=0; i<poly.getNumHoles(); i++) {
				trk.getTrkseg().add(coordsToTrkseg(poly.getHole(i).coords));
			}
			root.getTrk().add(trk);
		}
		
		@Override
		public void visitCollection(GeometryCollection<? extends Geometry> collection) {
			for (int i=0; i<collection.size(); i++) {
				collection.get(i).accept(this);
			}
		}
		
		@Override
		public void visitMultiLineString(MultiLineString mls) {
			visitCollection(mls);
		}
		
		@Override
		public void visitMultiPoint(MultiPoint mp) {
			visitCollection(mp);
		}
		
		@Override
		public void visitMultiPolygon(MultiPolygon mp) {
			visitCollection(mp);
		}
		
		TrksegType coordsToTrkseg(double[] coords) {
			TrksegType seg = new TrksegType();
			for (int i=0; i<coords.length-1;) {
				WptType wpt = new WptType();
				wpt.setLat(BigDecimal.valueOf(coords[i++]));
				wpt.setLon(BigDecimal.valueOf(coords[i++]));
				seg.getTrkpt().add(wpt);
			}
			return seg;
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		List<Geometry> geoms = new ArrayList<Geometry>();
		geoms.add(new Point (480000, 140000));
		geoms.add(new Point (480005, 140005));
		geoms.add(new Point (480010, 140010));
	
		geoms.add(new LineString(new double[]{480000, 140000, 480005, 140005, 480010, 140010}));
		
		GeometryWriter writer =
			new TransformingGeometryWriter(
				new GPX11Writer(new FileOutputStream("E:\\Temp\\gpx_out_"+System.currentTimeMillis()+".gpx")), 
				Transforms.D48_TO_WGS84);
		
		for (Geometry g : geoms) {
			writer.append(g);
		}
		writer.close();
		
	}

}
