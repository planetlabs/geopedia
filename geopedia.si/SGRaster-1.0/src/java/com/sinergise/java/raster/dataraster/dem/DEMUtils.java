package com.sinergise.java.raster.dataraster.dem;

import static com.sinergise.java.raster.dataraster.io.DataRasterIO.FORMAT_SDM;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.raster.dataraster.SGDataBank;
import com.sinergise.common.raster.dataraster.ShortDataBank;
import com.sinergise.common.util.math.AngleUtil;
import com.sinergise.java.raster.dataraster.dem.DEMReader.ASC;
import com.sinergise.java.raster.dataraster.dem.DEMReader.WithMatrix;
import com.sinergise.java.raster.dataraster.io.DataRasterIO;


public class DEMUtils {
	public static void readDMR(DEMReader rdr, SGDataBank bank) throws IOException {
		final double stepx = bank.getWorldTr().getScaleX();
		final double stepy = bank.getWorldTr().getScaleY();
		final double xOff = bank.getWorldTr().getTranslateX();
		final double yOff = bank.getWorldTr().getTranslateY();
		
		if (rdr instanceof WithMatrix) {
			WithMatrix mr = (WithMatrix)rdr;
			bank.expandToInclude(
					Math.round((mr.getMinX() - xOff)/mr.getStepX()), 
					Math.round((mr.getMinY() - yOff)/mr.getStepY()), 
					Math.round((mr.getMaxX() - xOff)/mr.getStepX()), 
					Math.round((mr.getMaxY() - yOff)/mr.getStepY()));
		}
		
		final double[] data = new double[3];
		while (rdr.next(data)) {
			double x = (data[0] - xOff) / stepx;
			double y = (data[1] - yOff) / stepy;
			long rndX = Math.round(x);
			long rndY = Math.round(y);
			if (Math.abs(x - rndX) > 0.1 || Math.abs(y - rndY) > 0.1) {
				System.err.println("Warning: Rounding "+x+" "+y);
			}
			bank.expandAndSet(rndX, rndY, data[2]);
		}
	}
	
	public static ShortDataBank readDMR(WithMatrix rdr, double zMin, double zStep, double xOff, double yOff) throws IOException {
		AffineTransform2D tr = AffineTransform2D.createTrScale(rdr.getStepX(), rdr.getStepY(), xOff, yOff);
		return readDMR(rdr, zMin, zStep, tr);
	}

	public static ShortDataBank readDMR(WithMatrix rdr, double zMin, double zStep, AffineTransform2D tr)
		throws IOException {
		ShortDataBank bank = new ShortDataBank(tr, zMin, zStep);
		readDMR(rdr, bank);
		return bank;
	}
	
	public static final double[] extremeVals(DEMReader rdr) throws IOException {
		double[] minMax = new double[]{
			Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
			Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
			Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
		extremeVals(rdr, minMax);
		return minMax;
	}
	
	public static final void extremeVals(DEMReader rdr, double[] retMinMaxXYZ) throws IOException {
		final double[] data = new double[3];
		while (rdr.next(data)) {
			for (int i = 0; i < 3; i++) {
				final double val = data[i];
				if (val < retMinMaxXYZ[2*i]) retMinMaxXYZ[2*i] = val;
				if (val > retMinMaxXYZ[2*i+1]) retMinMaxXYZ[2*i+1] = val;
			}
		}
	}
	
	public static void convertASC(String asc, String out, double zMin, double zStep, double xOff, double yOff, int format) throws IOException {
		ASC rdr = new ASC(asc);
		final ShortDataBank sdb;
		try {
			sdb = readDMR(new ASC(asc), zMin, zStep, xOff, yOff);
		} finally {
			rdr.close();
		}
		if (out.indexOf('.')<0) out = out + DataRasterIO.formatSuffix(format);
		FileOutputStream fos = new FileOutputStream(out);
		try {
			DataRasterIO.save(fos, sdb, format);
		} finally {
			fos.close();
		}
	}
	
	public static void main(String[] args) {
		String BASE_DIR="C:\\Data\\GeoData\\mus\\giles\\dem\\";
		try {
			int format = FORMAT_SDM;
			String suff = DataRasterIO.formatSuffix(format);
			
//			convertASC(BASE_DIR+"Mauritius_DTM_North_East.asc",BASE_DIR+"Mauritius_DTM_North_East"+suff, -327.68, 0.02, 517500, 7722000, format);
//			convertASC(BASE_DIR+"Mauritius_DTM_North_West.asc",BASE_DIR+"Mauritius_DTM_North_West"+suff, -327.68, 0.02, 517500, 7722000, format);
//			convertASC(BASE_DIR+"Mauritius_DTM_South_East.asc",BASE_DIR+"Mauritius_DTM_South_East"+suff, -327.68, 0.02, 517500, 7722000, format);
//			convertASC(BASE_DIR+"Mauritius_DTM_South_West.asc",BASE_DIR+"Mauritius_DTM_South_West"+suff, -327.68, 0.02, 517500, 7722000, format);

			ShortDataBank dmh = DataRasterIO.load(new File(BASE_DIR+"Mauritius_DTM_North_West"+suff).toURI().toURL());
			dmh.compact();
			{
				dmh.overlayAll(DataRasterIO.load(new File(BASE_DIR+"Mauritius_DTM_South_East"+suff).toURI().toURL()).compact());
			}
			System.gc();
			{
				dmh.overlayAll(DataRasterIO.load(new File(BASE_DIR+"Mauritius_DTM_North_East"+suff).toURI().toURL()).compact());
			}
			System.gc();
			{
				dmh.overlayAll(DataRasterIO.load(new File(BASE_DIR+"Mauritius_DTM_South_West"+suff).toURI().toURL()).compact());
			}
			System.gc();
			dmh.compact();
			DataRasterIO.save(new FileOutputStream(BASE_DIR+"Mauritius_DTM"+suff), dmh, format);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Slope from flat based on derivatives dz/dx and dz/dy. Flat is 0, vertical is PI/2.
	 * @param dzx
	 * @param dzy
	 * @return
	 */
	public static double slopeInRadians(double dzx, double dzy) {
		return Math.atan(Math.hypot(dzx, dzy));
	}
	
	/**
	 * Clockwise azimuth from North based on derivatives dz/dx and dz/dy. North is 0; East is PI/2; South is PI; West is 3 PI/2
	 * @param dzx
	 * @param dzy
	 * @return
	 */
	public static double azimuthInRadians(double dzx, double dzy) {
		if (dzx == 0 && dzy == 0) return Double.NaN;
		return AngleUtil.positiveNormalAngle(Math.atan2(-dzx, -dzy));
	}
}
