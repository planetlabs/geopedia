package com.sinergise.java.raster.dataraster;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.sinergise.common.geometry.crs.mu.MauritiusTransforms;
import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.raster.dataraster.AbstractShortDataBank;
import com.sinergise.common.raster.dataraster.SGDataBank;
import com.sinergise.common.raster.dataraster.ShortDataBank;
import com.sinergise.common.util.geom.EnvelopeL;
import com.sinergise.common.util.geom.RectSideOffsetsI;
import com.sinergise.java.geometry.tiles.TileUtilJava;
import com.sinergise.java.raster.dataraster.io.DataRasterIO;

/**
 * @author Miha
 */
public class DataPyramidBuilder {
	public final SGDataBank wholeData;
	public final double srcZmin;
	public final double srcZstep;
	public final double wholewx;
	public final double wholewy;
	public final TiledCRS cs;
	public final String outDir;
	public int outFormat = DataRasterIO.FORMAT_RAW;
	public final RectSideOffsetsI overlap;
	private ExecutorService execs = Executors.newFixedThreadPool(8);
	public boolean reduceZstep = true;

	public DataPyramidBuilder(AbstractShortDataBank sourceData, TiledCRS targetCRS, String outDIR) {
		this(sourceData, sourceData.zMin, sourceData.zScale, targetCRS, outDIR);
	}

	public DataPyramidBuilder(SGDataBank sourceData, double zMin, double zStep, TiledCRS targetCRS, String outDIR) {
		this.wholeData = sourceData;
		this.cs = targetCRS;
		this.outDir = outDIR;
		this.srcZmin = zMin;
		this.srcZstep = zStep;

		AffineTransform2D tr = wholeData.getWorldTr();
		double scale = tr.getScaleX();
		double scaley = tr.getScaleY();
		if (scale != scaley || !tr.isSimpleRectangular()) {
			throw new IllegalArgumentException("Non-square data grids not supported");
		}
		if (cs.zoomLevels.worldPerPix(cs.getMaxLevelId()) != scale) {
			throw new IllegalArgumentException("Lowest pyramid level should be the same as the source data step size");
		}

		double deltaX = (cs.getMinX() - wholeData.getWorldTr().x(-0.5, -0.5)) / scale;
		double isInteger = deltaX - Math.round(deltaX);
		if (Math.abs(isInteger) > 0.05) {
			throw new IllegalArgumentException(
				"Pyramid bounds are not compatible with the data grid's offset (value = " + isInteger + ")");
		}
		double deltaY = (cs.getMinY() - wholeData.getWorldTr().y(-0.5, -0.5)) / scaley;
		isInteger = deltaY - Math.round(deltaY);
		if (Math.abs(isInteger) > 0.05) {
			throw new IllegalArgumentException(
				"Pyramid bounds are not compatible with the data grid's offset (value = " + isInteger + ")");
		}

		wholewx = tr.getTranslateX();
		wholewy = tr.getTranslateY();

		overlap = cs.getOverlap();
	}

	public void go() throws IOException {
		TileUtilJava.saveForBaseDir(cs, new File(outDir));
		internalGO(cs.getMinLevelId(), 0, 0);
		execs.shutdown();
	}

	ShortDataBank internalGO(final int level, final int column, final int row) throws IOException {
		final double dpx = cs.zoomLevels.worldPerPix(level);
		final double retwx = cs.getMinX() + 0.5 * dpx;
		final double retwy = cs.getMinY() + 0.5 * dpx;

		final ShortDataBank ret = new ShortDataBank(AffineTransform2D.createTrScale(dpx, dpx, retwx, retwy),
			getZMin(level), getZScale(level));
		if (level == cs.getMaxLevelId()) {

			EnvelopeL tileEnv = getTileEnvelopeL(level, column, row, overlap);
			ret.expandToInclude(tileEnv.getMinX(), tileEnv.getMinY(), tileEnv.getMaxX(), tileEnv.getMaxY());

			// Whole data may have different offset than ours
			EnvelopeL srcInTile = getTileEnvelopeL(level, column, row, wholewx, wholewy, overlap);
			long deltaX = srcInTile.getMinX() - tileEnv.getMinX();
			long deltaY = srcInTile.getMinY() - tileEnv.getMinY();
			ret.translate(deltaX, deltaY);
			ret.overlay(wholeData, srcInTile);
			ret.translate(-deltaX, -deltaY);
		} else {
			//No overlap on higher levels, as we don't yet handle getting neighbouring tiles to do this properly
			EnvelopeL tileEnv = getTileEnvelopeL(level, column, row, RectSideOffsetsI.EMPTY);
			ret.expandToInclude(tileEnv.getMinX(), tileEnv.getMinY(), tileEnv.getMaxX(), tileEnv.getMaxY());

			Future<?> bl = processChild(ret, level + 1, 2 * column, 2 * row);
			Future<?> br = processChild(ret, level + 1, 2 * column + 1, 2 * row);
			Future<?> tl = processChild(ret, level + 1, 2 * column, 2 * row + 1);
			Future<?> tr = processChild(ret, level + 1, 2 * column + 1, 2 * row + 1);

			try {
				bl.get();
				br.get();
				tl.get();
				tr.get();
			} catch(Exception e) {
				throw (IOException)(new IOException().initCause(e));
			}
		}
		ret.compact();
		if (!ret.isEmpty()) {
			saveBank(level, column, row, ret);
		}
		return ret;
	}

	private EnvelopeL getTileEnvelopeL(int level, int column, int row, RectSideOffsetsI insets) {
		double dpx = cs.zoomLevels.worldPerPix(level);
		return getTileEnvelopeL(level, column, row, cs.getMinX() + 0.5 * dpx, cs.getMinY() + 0.5 * dpx, insets);
	}

	private EnvelopeL getTileEnvelopeL(int level, int column, int row, double worldOffX, double worldOffY,
		RectSideOffsetsI insets) {
		double dpx = cs.zoomLevels.worldPerPix(level);
		return cs.tileWorldBounds(level, column, row)
			.translate(-worldOffX + 0.5 * dpx, -worldOffY + 0.5 * dpx)
			.divide(dpx)
			.roundLong()
			.expand(insets);
	}

	private double getZScale(final int level) {
		return AbstractShortDataBank.calculateReducedZScale(srcZstep, cs.getMaxLevelId() - level, reduceZstep);
	}

	private double getZMin(final int level) {
		return AbstractShortDataBank.calculateReducedZMin(srcZmin, srcZstep, cs.getMaxLevelId() - level, reduceZstep);
	}

	protected Future<?> processChild(final ShortDataBank ret, final int level, final int column, final int row) {
		Runnable rnbl = new Runnable() {
			@Override
			public void run() {
				try {
					//TODO: if continuous data use average:
					rescaleAndAdd(ret, internalGO(level, column, row),
						getTileEnvelopeL(level, column, row, RectSideOffsetsI.EMPTY));
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		};
		if (level == cs.getMaxLevelId() - 1) {
			return execs.submit(rnbl);
		}
		rnbl.run();
		return new Future<Object>() {

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public boolean isDone() {
				return true;
			}

			@Override
			public Object get() {
				return null;
			}

			@Override
			public Object get(long timeout, TimeUnit unit) {
				return null;
			}
		};
	}

	private final void saveBank(int level, int column, int row, ShortDataBank data) throws IOException {
		String tDir = TileUtilGWT.tileInDirColRow(cs, level, column, row) + DataRasterIO.formatSuffix(outFormat);
		File prnt = new File(outDir);
		File f = new File(prnt, tDir);
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		FileOutputStream fos = new FileOutputStream(f);
		DataRasterIO.save(fos, data, outFormat);
		fos.close();
	}

	public static ShortDataBank shrinkBy2(ShortDataBank source) {
		AffineTransform2D srcTr = source.getWorldTr();
		EnvelopeL srcEn = source.getEnvelope();

		ShortDataBank ret = new ShortDataBank(2 * srcTr.getScaleX(), 2 * srcTr.getScaleY(), source.zMin, source.zScale);
		ret.expandToInclude(srcEn.expand(0, 0, 1, 1).divide(2));
		rescaleAndAdd(ret, source, source.getEnvelope());
		return ret;
	}

	public static void rescaleAndAdd(ShortDataBank ret, ShortDataBank source, EnvelopeL srcEnv) {
		synchronized(ret) {
			if (source.isEmpty()) {
				return;
			}
			// grid of the next level is in the middle of the previous level's cells
			EnvelopeL srcDiv2 = srcEnv.divide(2);
			final EnvelopeL env = srcDiv2.intersectWith(ret.getEnvelope());
			if (env.getWidth() > 196 || env.getHeight() > 196) {
				System.out.println(env + " " + ret.getEnvelope() + " " + source.getEnvelope() + " " + srcDiv2);
			}
			final int w = (int)env.getWidth();
			final int h = (int)env.getHeight();
			final long offX = 2 * env.getMinX();
			final long offY = 2 * env.getMinY();
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					try {
						final double avg;
						if (source.isContinous) {
							avg = source.getAverageDouble(2 * x + offX, 2 * y + offY, 2, 2);
						} else {
							avg = source.getMostCommonDouble(2 * x + offX, 2 * y + offY, 2, 2);
						}
						ret.setValue(env.xFromIndex(x), env.yFromIndex(y), avg);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		final String BASE_DIR = "D:\\Data\\GeoData\\mus\\dem\\";
		try {
			ShortDataBank src = DataRasterIO.load(new File(BASE_DIR + "Mauritius_DTM.sdm").toURI().toURL());
			DataPyramidBuilder bld = new DataPyramidBuilder(src, MauritiusTransforms.TILES_DMR_10, BASE_DIR + "out");
			bld.outFormat = DataRasterIO.FORMAT_SDM;
			bld.go();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
