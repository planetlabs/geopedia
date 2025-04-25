package com.sinergise.java.raster.dataraster.service;

import static java.lang.Math.toDegrees;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.URL;

import javax.servlet.*;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.raster.dataraster.*;
import com.sinergise.common.raster.dataraster.service.DemService;
import com.sinergise.common.raster.dataraster.service.average.DemDataAverage;
import com.sinergise.common.raster.dataraster.service.average.GetDemAverageRequest;
import com.sinergise.common.raster.dataraster.service.average.GetDemAverageResponse;
import com.sinergise.common.util.ServiceException;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeL;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.common.util.math.stats.StatsCollector;
import com.sinergise.common.util.url.URLUtil;
import com.sinergise.java.geometry.j2d.J2DUtil;
import com.sinergise.java.geometry.tiles.TileUtilJava;
import com.sinergise.java.raster.dataraster.PyramidDataset;
import com.sinergise.java.raster.dataraster.dem.DEMUtils;

public class PyramidDemServiceImpl extends RemoteServiceServlet implements DemService {
	public static final String PARAM_PYRAMID_URL = "baseUrl";

	private PyramidDataset dataset;
	
	@Override
	public GetDemAverageResponse getDEMAverage(GetDemAverageRequest req) throws ServiceException {
		SGDataBank data = null;
		try {
			data = getData(req);
			if (data == null) {
				return GetDemAverageResponse.createEmpty();
			}
			if (req.hasPolygon()) {
				return computePolygonAverage(data, req.getPolygon());
			}
			return computeEnvelopeAverage(data, req.getBBox());
		} 
		catch (IOException e) {
			throw new ServiceException(e);
		}
		finally {
			if (data != null) try {data.close();} catch (IOException e) {}
		}
	}
	
	public DemDataAverage getDEMAverage(Geometry input) throws IOException {
		SGDataBank data = getData(input.getArea(), input.getEnvelope());
		try {
			return computePolygonAverage(data, input).getAverage();
		} finally {
			data.close();
		}
	}

	public SGDataBank getData(GetDemAverageRequest req) throws IOException {
		double reqArea = getArea(req);
		Envelope reqEnv = req.getBBox();
		return getData(reqArea, reqEnv);
	}

	public SGDataBank getData(double reqArea, Envelope reqEnv) throws IOException {
		int zoomLevel = selectLevel(Math.sqrt(reqArea)/100);
		double pixSize = dataset.getTiledCRS().zoomLevels.worldPerPix(zoomLevel);
		return dataset.getSubRaster(reqEnv.expandedFor(pixSize), zoomLevel);
	}
	
	protected GetDemAverageResponse computePolygonAverage(SGDataBank dataBank, Geometry polygon) {
		return new PolygonAverageCalculator(dataBank, polygon).getResponse();
	}

	protected GetDemAverageResponse computeEnvelopeAverage(SGDataBank dataBank, Envelope bbox) {
		return new EnvelopeAverageCalculator(dataBank, bbox).getResponse();
	}

	private static double calculateSlope(double dzx, double dzy) {
		return DEMUtils.slopeInRadians(dzx, dzy);
	}

	private static double calculateAzimuth(double dzx, double dzy) {
		return DEMUtils.azimuthInRadians(dzx, dzy);
	}

	private int selectLevel(double maxDistance) {
		return dataset.getTiledCRS().zoomLevels.optimalZoomLevelPix(maxDistance, 1);
	}

	private static double getArea(GetDemAverageRequest req) {
		if (req.hasPolygon()) {
			return req.getPolygon().getArea();
		}
		return req.getBBox().getArea();
	}

	@Override
	public void init() throws ServletException {
		super.init();
		initialize(URLUtil.ensurePathSeparatorAtEnd(getServletConfig().getInitParameter(PARAM_PYRAMID_URL)));
	}

	public void initialize(String url) throws ServletException {
		try {
			dataset = new PyramidDataset(new URL(url));
		} catch(Exception e) {
			throw new ServletException(e);
		}
	}


	static abstract class AverageCalculator {
		final StatsCollector statCol = new StatsCollector(4, 0);
		final SGDataBank dataBank;
		final EnvelopeL cropEnv;
		
		public AverageCalculator(SGDataBank bank, Envelope dataEnv) {
			this.dataBank = bank;
			this.cropEnv = SGDataBank.getCropEnvelope(bank, dataEnv);
		}
		
		public GetDemAverageResponse getResponse() {
			initialize();
			addSamples();
			return createResponse();
		}
	
		private GetDemAverageResponse createResponse() {
			double avgAz = calculateAzimuth(statCol.getMean(1), statCol.getMean(2));
			DemDataAverage ret = new DemDataAverage(statCol.getMean(0), toDegrees(statCol.getMean(3)), toDegrees(avgAz));
			ret.setHeightMinMax(statCol.getMin(0), statCol.getMax(0));
			ret.setSlopeMinMax(toDegrees(statCol.getMin(3)), toDegrees(statCol.getMax(3)));
			return new GetDemAverageResponse(ret);
		}
	
		private void addSamples() {
			for (int y = 0; y < cropEnv.getHeight(); y++) {
				for (int x = 0; x < cropEnv.getWidth(); x++) {
					addSample(x, y, getWeight(x, y));
				}
			}
		}
	
		protected abstract double getWeight(int x, int y);
	
		protected abstract void initialize();
	
		private void addSample(int x, int y, double weight) {
			if (weight == 0) {
				return;
			}
			double[] vals = new double[3];
			dataBank.getValueWithDerivs(cropEnv.getMinX() + x, cropEnv.getMinY() + y, vals, 0);
			statCol.add(0, vals[0], weight);
			statCol.add(1, vals[1], weight);
			statCol.add(2, vals[2], weight);
			statCol.add(3, calculateSlope(vals[1], vals[2]), weight);
		}
	}

	static class EnvelopeAverageCalculator extends AverageCalculator {
		private Envelope mbr;
		private double fLeft;
		private double fRight;
		private double fTop;
		private double fBot;
		public EnvelopeAverageCalculator(SGDataBank bank, Envelope mbr) {
			super(bank, mbr);
			this.mbr = mbr;
		}
		
		@Override
		protected void initialize() {
			Envelope bankEnv = dataBank.getWorldEnvelopeForWholeCells(cropEnv);
			
			double sampX = dataBank.getWorldTr().getScaleX();
			fLeft  = MathUtil.clamp(0, 1 - (mbr.getMinX() - bankEnv.getMinX())/sampX, 1);
			fRight = MathUtil.clamp(0, 1 - (bankEnv.getMaxX() - mbr.getMaxX())/sampX, 1);
	
			double sampY = dataBank.getWorldTr().getScaleY();
			fBot  = MathUtil.clamp(0, 1 - (mbr.getMinY() - bankEnv.getMinY())/sampY, 1);
			fTop = MathUtil.clamp(0, 1 - (bankEnv.getMaxY() - mbr.getMaxY())/sampY, 1);
		}
		
		@Override
		protected double getWeight(int x, int y) {
			double ret = 1;
			if (x == 0) {
				ret *= fLeft;
			} else if (x == cropEnv.getWidth() - 1) {
				ret *= fRight;
			}
			if (y == 0) {
				ret *= fBot;
			} else if (y == cropEnv.getHeight() - 1) {
				ret *= fTop;
			}
			return ret;
		}
	}
	
	static class PolygonAverageCalculator extends AverageCalculator {
		Geometry geom;
		byte[] buf;
		
		public PolygonAverageCalculator(SGDataBank dataBank, Geometry polygon) {
			super(dataBank, polygon.getEnvelope());
			this.geom = polygon;
		}
	
		@Override
		protected double getWeight(int x, int y) {
			final int row = (int)(cropEnv.getHeight() - 1 - y);
			return (buf[(int)(row*cropEnv.getWidth() + x)] & 0xFF)/255.0;
		}
		
		@Override
		protected void initialize() {
			BufferedImage bi = renderPolygon((int)cropEnv.getWidth(), (int)cropEnv.getHeight(), dataBank.getWorldEnvelopeForWholeCells(cropEnv), geom);
			buf = ((DataBufferByte)bi.getRaster().getDataBuffer()).getData();
		}

		static BufferedImage renderPolygon(int width, int height, Envelope imgEnv, Geometry polygon) {
			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
			Graphics2D g = bi.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
			g.setColor(Color.WHITE);
			if (polygon instanceof MultiPolygon) {
				for (Polygon p : (MultiPolygon)polygon) {
					g.fill(J2DUtil.toShape(p, TileUtilJava.createAffineWorldToTile(width, height, imgEnv)));
				}
				
			} else if (polygon instanceof Polygon) {
				g.fill(J2DUtil.toShape((Polygon)polygon, TileUtilJava.createAffineWorldToTile(width, height, imgEnv)));
				
			} else {
				throw new IllegalArgumentException("Only polygons and multipolygons are supported");
			}
			return bi;
		}
	}
}
