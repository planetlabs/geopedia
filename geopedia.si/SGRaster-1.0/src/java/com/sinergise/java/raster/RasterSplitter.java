package com.sinergise.java.raster;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.media.jai.operator.CropDescriptor;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.raster.core.RasterWorldInfo;
import com.sinergise.common.util.io.IOUtil;
import com.sinergise.java.raster.core.RasterIoJai;
import com.sinergise.java.raster.core.RasterProcessingStep;
import com.sinergise.java.raster.core.RasterUtilJava;
import com.sinergise.java.raster.core.SGRenderedImage;
import com.sinergise.java.raster.core.WorldRasterImage;


public class RasterSplitter<T extends WorldRasterImage> {
	public static enum LoadStrategy {
		AUTO, BUFFERED, RENDERED
	}

	public static int DEFAULT_SPLIT_SIZE = 2048;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			File inDir = new File(args[0]);
			File outDir = new File(args[1]);
			int prefSize = args.length < 3 ? DEFAULT_SPLIT_SIZE : Integer.parseInt(args[2]);

			process(inDir, outDir, CRS.MAP_CRS, prefSize);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void process(File inDir, File outDir, CRS cs, int prefSize) throws IOException {
		process(inDir, outDir, cs, prefSize, 0);
	}

	public static void process(File inDir, File outDir, CRS cs, int prefSize, int overlap) throws IOException {
		RasterSplitter<WorldRasterImage> splitter = new RasterSplitter<WorldRasterImage>(inDir, cs, outDir, prefSize,
			null);
		splitter.setOverlap(overlap);
		splitter.process();
	}

	private int overlap = 0;

	private void setOverlap(int overlap) {
		this.overlap = overlap;
	}

	public static <S extends WorldRasterImage> void split(WorldRasterImage t, File outDir, int prefSize,
		RasterProcessingStep<S> sink) throws IOException {
		RasterSplitter<S> rs = new RasterSplitter<S>(outDir, prefSize, sink);
		rs.split(t);
	}

	public static void splitAndSave(WorldRasterImage t, File outDir, int prefSize) throws IOException {
		RasterSplitter<WorldRasterImage> rs = new RasterSplitter<WorldRasterImage>(outDir, prefSize);
		rs.splitAndSave(t);
	}

	public void splitAndSave(WorldRasterImage t) throws IOException {
		final RasterProcessingStep<T> prevSink = sink;
		sink = new RasterProcessingStep<T>() {
			@Override
			@SuppressWarnings("unchecked")
			public T processRaster(RasterWorldInfo raster) {
				try {
					saveImage((WorldRasterImage)raster);
				} catch(Exception e) {
					e.printStackTrace();
				}
				if (prevSink != null) prevSink.processRaster(raster);
				return (T)raster;
			}

		};
		split(t);
		sink = prevSink;
	}

	File inDir;
	File outDir;
	int prefSize;
	RasterProcessingStep<T> sink;
	CRS cs;
	LoadStrategy loadStrategy = LoadStrategy.AUTO;

	public RasterSplitter(File outDir, int prefSize, RasterProcessingStep<T> sink) {
		this(null, null, outDir, prefSize, sink);
	}

	public RasterSplitter(File inDir, CRS inCS, File outDir, int prefSize, RasterProcessingStep<T> sink) {
		this.inDir = inDir;
		this.cs = inCS == null ? CRS.NONAME_WORLD_CRS : inCS;
		this.outDir = outDir;
		this.prefSize = prefSize;
		this.sink = sink;
	}

	public void setLoadStrategy(LoadStrategy strategy) {
		this.loadStrategy = strategy;
	}

	protected void saveImage(WorldRasterImage raster) throws IOException {
		raster.save();
	}

	public RasterSplitter(File outDir, int prefSize) {
		this(outDir, prefSize, null);
	}

	public void split(WorldRasterImage t) throws IOException {
		int w = t.w;
		int h = t.h;
		String baseName = t.getImageFileName().substring(0, t.getImageFileName().lastIndexOf('.'));
		String suffix = t.getImageSuffix();
		SGRenderedImage img = loadImage(t, w, h);
		try {
			t.setImage(null);

			int numX = (int)Math.round((double)w / prefSize);
			int numY = (int)Math.round((double)h / prefSize);

			double xSize = (double)w / numX;
			double ySize = (double)h / numY;

			int x0 = 0;
			int y0 = 0;
			int x1 = 0;
			int y1 = 0;
			for (int i = 0; i < numX; i++) {

				x1 = (int)Math.round((i + 1) * xSize);
				y0 = 0;
				for (int j = 0; j < numY; j++) {
					y1 = (int)Math.round((j + 1) * ySize);

					File f = new File(outDir, baseName + "_" + i + "_" + j + "." + suffix);

					double[] m = t.tr.paramsToArray();
					m[4] = t.tr.x(x0, y0);
					m[5] = t.tr.y(x0, y0);
					SGRenderedImage op = cropImage(img, x0, y0, x1 - 1 + overlap, y1 - 1 + overlap);
					WorldRasterImage ti = new WorldRasterImage(f.toURI().toURL(), op, new AffineTransform2D(
						CRS.MAP_PIXEL_CRS, CRS.NONAME_WORLD_CRS, m));
					ti.setHardCache(true);
					sink.processRaster(ti);
					y0 = y1;
				}
				x0 = x1;
			}
		} finally {
			IOUtil.close(img);
		}
	}

	protected SGRenderedImage cropImage(SGRenderedImage img, int x0, int y0, int x1, int y1) {
		x0 = Math.max(x0, 0);
		y0 = Math.max(y0, 0);
		x1 = Math.min(x1, img.getWidth() - 1);
		y1 = Math.min(y1, img.getHeight() - 1);
		if (img.isWrapperFor(BufferedImage.class)) {
			return RasterUtilJava.wrap(img.unwrap(BufferedImage.class).getSubimage(x0, y0, x1 - x0 + 1, y1 - y0 + 1));
		}
		return RasterIoJai.wrap(CropDescriptor.create(img.unwrap(), Float.valueOf(x0), Float.valueOf(y0), Float.valueOf(x1 - x0 + 1),
			Float.valueOf(y1 - y0 + 1), null));
	}

	protected SGRenderedImage loadImage(WorldRasterImage t, int w, int h) throws IOException {
		boolean verylarge = 4.0 * w * h > 64.0 * 1024 * 1024;

		if ((loadStrategy != LoadStrategy.BUFFERED) && verylarge) {
			SGRenderedImage ret = t.getImage();
			if ((loadStrategy != LoadStrategy.RENDERED) && ret.getNumXTiles() == 1 && ret.getNumYTiles() == 1) {
				ret.close();
				//no point in using Rendered if it's going to read the whole thing anyway
				t.setImage(null); // reset to make sure we don't try and convert
				BufferedImage retBi = t.getAsBufferedImage();
				t.setImage(null);
				return RasterUtilJava.wrap(retBi);
			}
			return ret;
		}
		BufferedImage retBi = t.getAsBufferedImage();
		t.setImage(null);
		return RasterUtilJava.wrap(retBi);
	}

	public void process() throws IOException {
		if (!inDir.isDirectory() || !inDir.exists()) { throw new IllegalArgumentException("Input directory '" + inDir.getAbsolutePath() + "' does not exist"); }
		if (!outDir.exists()) outDir.mkdirs();
		if (!outDir.isDirectory()) { throw new IllegalArgumentException("Output '" + outDir.getAbsolutePath()
			+ "' is not a directory"); }

		ArrayList<WorldRasterImage> tifs = new ArrayList<WorldRasterImage>();
		RasterUtilJava.scan(inDir, null, tifs, null, cs);
		int cnt = 0;
		for (WorldRasterImage t : tifs) {
			System.out.println("\nFile " + (++cnt) + "/" + tifs.size());
			splitAndSave(t);
		}
	}
	
	public void processNoSave() throws IOException {
		if (!inDir.isDirectory() || !inDir.exists()) {
			throw new IllegalArgumentException("Input directory '" + inDir.getAbsolutePath() + "' does not exist");
		}
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		if (!outDir.isDirectory()) {
			throw new IllegalArgumentException("Output '" + outDir.getAbsolutePath() + "' is not a directory");
		}

		ArrayList<WorldRasterImage> tifs = new ArrayList<WorldRasterImage>();
		RasterUtilJava.scan(inDir, null, tifs, null, cs);
		int cnt = 0;
		for (WorldRasterImage t : tifs) {
			System.out.println("\nFile " + (++cnt) + "/" + tifs.size());
			split(t);
		}
	}

	public static File[] splitFiles(File[] inFiles, File tempDir, CRS cs, int prefSize) throws IOException {
		final ArrayList<File> retFiles = new ArrayList<File>(inFiles.length);
		for (int i = 0; i < inFiles.length; i++) {
			File inFile = inFiles[i];
			File splitDir = new File(tempDir, "split" + i);
			if (!splitDir.exists()) {
				splitDir.mkdirs();
			}
			System.out.println("Splitting " + inFile.getPath());
			WorldRasterImage wri = new WorldRasterImage(inFile.toURI().toURL(), cs);
			RasterSplitter.split(wri, splitDir, prefSize, new RasterProcessingStep<WorldRasterImage>() {
				@Override
				public WorldRasterImage processRaster(RasterWorldInfo raster) {
					WorldRasterImage newImg = (WorldRasterImage)raster;
					try {
						File newF = newImg.save();
						retFiles.add(newF);
					} catch(Exception e) {
						e.printStackTrace();
					}
					return (WorldRasterImage)raster;
				}
			});
		}
		return retFiles.toArray(new File[0]);
	}

	public static void splitFile(File inFile, File splitDir, CRS cs, int prefSize, LoadStrategy loadStrategy)
		throws IOException {
		System.out.println("Splitting " + inFile.getPath());
		WorldRasterImage wri = new WorldRasterImage(inFile.toURI().toURL(), cs);
		RasterSplitter<WorldRasterImage> rs = new RasterSplitter<WorldRasterImage>(splitDir, prefSize,
			new RasterProcessingStep<WorldRasterImage>() {
				@Override
				public WorldRasterImage processRaster(RasterWorldInfo raster) {
					WorldRasterImage newImg = (WorldRasterImage)raster;
					try {
						newImg.save();
					} catch(Exception e) {
						e.printStackTrace();
					}
					return (WorldRasterImage)raster;
				}
			});
		rs.setLoadStrategy(loadStrategy);
		rs.split(wri);
	}
}
