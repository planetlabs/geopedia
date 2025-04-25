/*
 *
 */
package com.sinergise.java.raster.dataraster.dem;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import com.sinergise.common.raster.dataraster.ShortDataBank;
import com.sinergise.common.util.geom.EnvelopeL;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.java.raster.dataraster.io.DataRasterIO;
import com.sinergise.java.util.io.BinaryInput;
import com.sinergise.java.util.io.StreamBinaryInput;

public interface DEMReader {
	public abstract static class WithMatrix implements DEMReader {
		private int colIdx = 0;
		private int rowIdx = 0;
		public int h = -1;
		public int w = -1;
		private double stepX;
		private double stepY;
		private double minX;
		private double minY;

		public WithMatrix() {}

		public abstract double getZ(int east, int south);

		@Override
		public boolean hasSlopeAz() {
			return true;
		}

		@Override
		@SuppressWarnings("unused")
		public boolean next(double[] xyzOut) throws IOException {
			if (w < 0 && h < 0) {
				throw new IllegalStateException("Initialize should have been called");
			}
			if (colIdx == w) {
				rowIdx++;
				colIdx = 0;
			}
			if (rowIdx == h)
				return false;

			xyzOut[0] = minX + getStepX() * colIdx;
			xyzOut[1] = minY + getStepY() * (h - rowIdx - 1);

			double z = getZ(colIdx, rowIdx);
			xyzOut[2] = z;
			if (xyzOut.length > 4) {
				double difX = getDifX(colIdx, rowIdx);
				double difY = getDifY(colIdx, rowIdx);

				double az = Math.toDegrees(Math.atan2(-difX, -difY));
				if (az < 0)
					az += 360;
				//SLOPE is measured from HORIZONTAL (0deg.) towards VERTICAL (90deg.) 
				double sl = Math.toDegrees(Math.atan2(Math.hypot(difX, difY), 1));

				xyzOut[3] = sl;
				xyzOut[4] = az;
			}
			colIdx++;
			return true;
		}

		public double getMaxY() {
			return minY + getStepY() * (h - 1);
		}

		public double getMinY() {
			return minY;
		}

		public double getMaxX() {
			return minX + getStepX() * (w - 1);
		}

		public double getDifX(int e, int s) {
			if (e > 0 && e < w - 1) {
				return (getZ(e + 1, s) - getZ(e - 1, s)) / (2.0 * getStepX());
			} else if (e == 0) {
				return (getZ(e + 1, s) - getZ(e, s)) / getStepX();
			} else if (e == w - 1) {
				return (getZ(e, s) - getZ(e - 1, s)) / getStepX();
			} else
				throw new IllegalArgumentException("east out of range");
		}

		public double getDifY(int e, int s) {
			if (s > 0 && s < h - 1) {
				return (getZ(e, s - 1) - getZ(e, s + 1)) / (2.0 * getStepY());
			} else if (s == 0) {
				return (getZ(e, s) - getZ(e, s + 1)) / getStepY();
			} else if (s == h - 1) {
				return (getZ(e, s - 1) - getZ(e, s)) / getStepY();
			} else
				throw new IllegalArgumentException("south out of range");
		}

		protected void initialize(int initW, int initH, double factX, double factY, double xMin, double yMin) {
			this.w = initW;
			this.h = initH;
			this.setStepX(factX);
			this.setStepY(factY);
			this.minX = xMin;
			this.minY = yMin;
			colIdx = 0;
			rowIdx = 0;
		}

		public void getZSlopeAz(int e, int s, double[] tmp) {
			tmp[0] = getZ(e, s);
			double difX = getDifX(e, s);
			double difY = getDifY(e, s);

			double az = Math.toDegrees(Math.atan2(-difX, -difY));
			if (az < 0)
				az += 360;
			//SLOPE is measured from HORIZONTAL (0deg.) towards VERTICAL (90deg.) 
			double sl = Math.toDegrees(Math.atan2(Math.hypot(difX, difY), 1));

			tmp[1] = sl;
			tmp[2] = az;
		}

		public double getMinX() {
			return minX;
		}

		public void setStepX(double stepX) {
			this.stepX = stepX;
		}

		public double getStepX() {
			return stepX;
		}

		public void setStepY(double stepY) {
			this.stepY = stepY;
		}

		public double getStepY() {
			return stepY;
		}
	}

	public static class DAT implements DEMReader {
		BufferedReader demSource;

		public DAT(String fileName) throws FileNotFoundException {
			this(new FileReader(fileName));
		}

		public DAT(Reader in) {
			demSource = new BufferedReader(in);
		}

		@Override
		public boolean hasSlopeAz() {
			return true;
		}

		@Override
		public boolean next(double[] xyzSlopeAzOut) throws IOException {
			String line = demSource.readLine();
			if (line == null)
				return false;
			line = line.trim();
			if (line.length() < 3)
				return false;
			String[] data = line.split(";");
			xyzSlopeAzOut[0] = Double.parseDouble(data[0]);
			xyzSlopeAzOut[1] = Double.parseDouble(data[1]);
			xyzSlopeAzOut[2] = Double.parseDouble(data[2]);
			if (data.length > 4 && xyzSlopeAzOut.length > 4) {
				xyzSlopeAzOut[3] = Double.parseDouble(data[3]);
				xyzSlopeAzOut[4] = Double.parseDouble(data[4]);
			}
			return true;
		}

		@Override
		public void close() throws IOException {
			demSource.close();
		}
	}

	public static class GPDRaw extends DEMReader.WithMatrix {
		ShortDataBank dmh;

		public GPDRaw(String dataFile, double step) throws IOException {
			ShortDataBank src = DataRasterIO.loadRaw(dataFile);
			dmh = new ShortDataBank(step, step, 0, 0.1, src.getDataBuffer());
			EnvelopeL srcEnv = src.getEnvelope();
			dmh.translate(srcEnv.getMinX(), srcEnv.getMinY());

			EnvelopeL dmEnv = dmh.getEnvelope();
			initialize((int)dmEnv.getWidth(), (int)dmEnv.getHeight(), step, step, step * dmEnv.getMinX(),
				step * dmEnv.getMinY());
		}

		@Override
		public double getZ(int east, int south) {
			return dmh.getValue(dmh.getEnvelope().getMinX() + east, dmh.getEnvelope().getMaxY() - south);
		}

		@Override
		public void close() {
			dmh = null;
		}
	}

	public static class HGT implements DEMReader {
		int w;
		int h;
		double minX;
		double minY;
		double maxY;
		double factorX;
		double factorY;
		double noData;

		int colIdx = 0;
		int rowIdx = 0;

		BinaryInput bi;

		public HGT(String fileName) throws IOException {
			File f = new File(fileName);
			fileName = f.getName();
			String name = fileName.substring(0, fileName.lastIndexOf('.'));
			int eIdx = name.indexOf('E');
			String N = name.substring(1, eIdx);
			String E = name.substring(eIdx + 1, name.length());
			minX = Double.parseDouble(E);
			minY = Double.parseDouble(N);
			maxY = minY + 1;

			double numPx = Math.sqrt(0.5 * f.length());
			w = (int)Math.round(numPx);
			h = w;
			factorX = 1.0 / (w - 1);
			factorY = factorX;
			System.out.println("(" + minX + "|" + minY + ") " + w + "," + h + " " + factorX * 3600);

			bi = new StreamBinaryInput(new BufferedInputStream(new FileInputStream(f)));
		}

		@Override
		public boolean hasSlopeAz() {
			return false;
		}

		@Override
		public void close() throws IOException {
			bi.close();
		}

		@Override
		public boolean next(double[] xyzOut) throws IOException {
			if (colIdx == w) {
				colIdx = 0;
				rowIdx++;
			}
			if (rowIdx >= h)
				return false;
			xyzOut[0] = minX + factorX * colIdx;
			xyzOut[1] = maxY - factorY * rowIdx;

			xyzOut[2] = bi.readShort();

			colIdx++;
			return true;
		}
	}


	public static class GeoTiff extends WithMatrix {
		BufferedImage img;
		double noData;

		public GeoTiff(String fileName, double[] minCoord, double[] factors, double noData) throws IOException {
			this(new FileImageInputStream(new File(fileName)), minCoord, factors, noData);

		}

		public GeoTiff(ImageInputStream imageIOSource, double[] minCoord, double[] factors, double noData)
			throws IOException {
			super();
			img = ImageIO.read(imageIOSource);

			double minX = minCoord[0];
			double minY = minCoord[1];

			double factorX = factors[0];
			double factorY = factors[1];

			this.noData = noData;
			initialize(img.getWidth(), img.getHeight(), factorX, factorY, minX, minY);
		}

		@Override
		public void close() {
			img = null;
		}

		@Override
		public double getZ(int colIdx, int rowIdx) {
			return img.getRaster().getSampleDouble(colIdx, rowIdx, 0);
		}
	}

	public static class XYZ implements DEMReader {
		BufferedReader demSource;

		public XYZ(String fileName) throws FileNotFoundException {
			this(new FileReader(fileName));
		}

		public XYZ(Reader in) {
			demSource = new BufferedReader(in);
		}

		@Override
		public boolean next(double[] xyzOut) throws IOException {
			String line = demSource.readLine();
			if (line == null)
				return false;
			line = line.trim();
			if (line.length() < 3)
				return false;
			Matcher m = Pattern.compile("\\s+").matcher(line);
			if (!m.find())
				return false;
			int idx1 = m.start();
			if (!m.find())
				return false;
			int idx2 = m.start();
			xyzOut[0] = Double.parseDouble(line.substring(0, idx1));
			xyzOut[1] = Double.parseDouble(line.substring(idx1, idx2));
			xyzOut[2] = Double.parseDouble(line.substring(idx2, line.length()));
			return true;
		}

		@Override
		public void close() throws IOException {
			demSource.close();
		}

		@Override
		public boolean hasSlopeAz() {
			return false;
		}
	}

	public static class GTM extends WithMatrix {
		float[] data;
		float nodata;
		public float minVal = Float.POSITIVE_INFINITY;
		public float maxVal = Float.NEGATIVE_INFINITY;

		public GTM(String fileName) throws IOException {
			this(new FileInputStream(fileName));
		}

		@SuppressWarnings("unused")
		public GTM(InputStream in) throws IOException {
			final BinaryInput data_in = new StreamBinaryInput(new BufferedInputStream(in, 256 * 1024));
			try {
				String magic = data_in.readASCII(4); // 4
				if (!magic.startsWith("GTM")) {
					throw new IOException("Stream should begin with 'GTM', was '" + magic + "'");
				}

				final int int1 = data_in.readIntLE(); // 8
				final int int2 = data_in.readIntLE(); // 12

				final double minX = data_in.readDoubleLE(); // 20
				final double minY = data_in.readDoubleLE(); // 28
				final double maxX = data_in.readDoubleLE(); // 36
				final double maxY = data_in.readDoubleLE(); // 44

				final float hMinVal = data_in.readFloatLE(); // 48
				final float hMaxVal = data_in.readFloatLE(); // 52

				final double xRes = data_in.readDoubleLE(); // 60
				final double yRes = data_in.readDoubleLE(); // 68

				nodata = data_in.readFloatLE(); // 72

				final int initW = data_in.readIntLE(); // 76
				final int initH = data_in.readIntLE(); // 80

				data_in.skipFully(256 - 80);

				initialize(initW, initH, xRes, yRes, minX, minY);

				data = new float[w * h];
				for (int i = 0; i < w * h; i++) {
					float fVal = data_in.readFloatLE();
					if (fVal != nodata) {
						minVal = Math.min(minVal, fVal);
						maxVal = Math.max(maxVal, fVal);
						data[i] = fVal;
					} else {
						data[i] = Float.NaN;
					}
				}
			} finally {
				data_in.close();
			}
		}

		@Override
		public double getZ(int east, int south) {
			return data[w * (h - south - 1) + east];
		}

		@Override
		public void close() {
			data = null;
		}
	}

	public static class GEBCO extends WithMatrix {

		double noData;
		short[] data;

		public GEBCO(String fileName, int width, int height, double factX, double factY, double noData)
			throws IOException {
			this(new DataInputStream(new FileInputStream(new File(fileName))), width, height, factX, factY, noData);
		}

		public GEBCO(DataInputStream source, int width, int height, double factorX, double factorY, double noData)
			throws IOException {
			super();
			// skip header
			source.skipBytes(612);

			data = new short[height * width];
			try {
				for (int i = 0; i < data.length; i++)
					data[i] = source.readShort();
			} catch(EOFException e) {
				e.printStackTrace();
			}
			source.close();

			this.noData = noData;
			initialize(width, height, factorX, factorY, 0, 0);
		}

		@Override
		public void close() {}

		@Override
		public double getZ(int colIdx, int rowIdx) {
			return data[colIdx + (rowIdx) * h];
		}
	}

	public static class ASC extends DEMReader.WithMatrix {
		double nodata;
		double[] data;

		private String curLine = null;

		public ASC(String fileName) throws IOException {
			this(new FileReader(fileName));
		}

		public ASC(Reader in) throws IOException {
			BufferedReader demSource = new BufferedReader(in, 65536);
			Map<String, String> header = readHeader(demSource);

			int width = Integer.parseInt(header.get("ncols"));
			int height = Integer.parseInt(header.get("nrows"));
			double step = StringUtil.parseDoubleWithSeparator(header.get("cellsize"), ",");

			final double minX;
			if (header.containsKey("xllcorner")) {
				minX = Double.parseDouble(header.get("xllcorner")) + 0.5 * step;
			} else {
				minX = Double.parseDouble(header.get("xllcenter"));
			}

			final double minY;
			if (header.containsKey("yllcorner")) {
				minY = Double.parseDouble(header.get("yllcorner")) + 0.5 * step;
			} else {
				minY = Double.parseDouble(header.get("yllcenter"));
			}
			nodata = header.containsKey("nodata_value") ? Double.parseDouble(header.get("nodata_value")) : Double.NaN;

			initialize(width, height, step, step, minX, minY);

			data = new double[width * height];
			int off = 0;
			do {
				int howMany = readNextLine(demSource, off);
				if (howMany < 0)
					break;
				off += howMany;
			} while (off < w * h);
			if (off != w * h) {
				System.err.println("Error reading ASC DEM: wrong number of values. Was: " + off + " should be: "
					+ (w * h) + " | llCenter=" + minX + " " + minY);
			}
		}

		private Map<String, String> readHeader(BufferedReader demSource) throws IOException {
			HashMap<String, String> ret = new HashMap<String, String>();
			while (true) {
				curLine = demSource.readLine();
				if (curLine == null) {
					return ret;
				}
				curLine = curLine.trim();
				if (curLine.length() == 0) {
					continue;
				}
				String[] split = curLine.split("\\s+", 2);
				if (split[0].matches("[a-zA-Z_]+")) {
					ret.put(split[0].toLowerCase(), split[1]);
				} else {
					return ret;
				}
			}
		}

		private int readNextLine(BufferedReader demSource, int off) throws IOException {
			try {
				if (curLine == null) {
					return -1;
				}
				if (curLine.length() == 0) {
					return 0;
				}
				String[] strD = curLine.split(" ");
				int numWritten = 0;
				for (int i = 0; i < strD.length; i++) {
					if (strD[i].length() == 0)
						continue;
					double val = Double.parseDouble(strD[i]);
					if (val == nodata) {
						val = Double.NaN;
					}
					data[off + i] = val;
					numWritten++;
				}
				return numWritten;
			} finally {
				curLine = demSource.readLine();
			}
		}

		@Override
		public void close() {
			data = null;
		}

		@Override
		public double getZ(int east, int south) {
			return data[w * south + east];
		}
	}

	public static final class Cached implements DEMReader {
		final DEMReader src;
		ArrayList<double[]> dataCache = new ArrayList<double[]>();
		int idx = 0;

		public Cached(DEMReader src) throws IOException {
			this.src = src;
			fetchData();
		}

		private void fetchData() throws IOException {
			int size = src.hasSlopeAz() ? 5 : 3;
			while (true) {
				double[] data = new double[size];
				if (src.next(data)) {
					dataCache.add(data);
				} else {
					return;
				}
			}
		}

		@Override
		public boolean next(double[] xyzOut) {
			if (idx >= dataCache.size()) {
				return false;
			}
			System.arraycopy(dataCache.get(idx), 0, xyzOut, 0, xyzOut.length);
			idx++;
			return true;
		}

		@Override
		public void close() {
			dataCache.clear();
		}

		@Override
		public boolean hasSlopeAz() {
			return src.hasSlopeAz();
		}
	}

	boolean next(double[] xyzOut) throws IOException;

	void close() throws IOException;

	boolean hasSlopeAz();
}
