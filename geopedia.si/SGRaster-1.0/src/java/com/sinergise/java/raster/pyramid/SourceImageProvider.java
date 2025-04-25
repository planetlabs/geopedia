package com.sinergise.java.raster.pyramid;

import static com.sinergise.common.util.math.ColorUtil.TRANSPARENT_WHITE;
import static com.sinergise.java.raster.RasterDeskew.deskew;
import static java.awt.RenderingHints.KEY_COLOR_RENDERING;
import static java.awt.RenderingHints.KEY_DITHERING;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY;
import static java.awt.RenderingHints.VALUE_DITHER_DISABLE;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.raster.core.ImageFileFilter;
import com.sinergise.common.raster.core.RasterWorldInfo;
import com.sinergise.common.util.ImageUtil;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.common.util.io.IOUtil;
import com.sinergise.common.util.lang.SGProgressMonitor;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.java.raster.colorfilter.ColorFilter;
import com.sinergise.java.raster.core.ImageWithRect;
import com.sinergise.java.raster.core.OffsetBufferedImage;
import com.sinergise.java.raster.core.RasterUtilJava;
import com.sinergise.java.raster.core.SGRenderedImage;
import com.sinergise.java.raster.core.SGRenderedImage.BufferedImageWrapper;
import com.sinergise.java.raster.core.WorldImageCollection;
import com.sinergise.java.raster.core.WorldRasterImage;
import com.sinergise.java.raster.core.WorldRasterImage.WithFilter;
import com.sinergise.java.util.io.FileUtilJava;

public class SourceImageProvider implements TileProviderJava {
	public static Logger logger = LoggerFactory.getLogger(SourceImageProvider.class);

	public static final File[] readSourcesFile(File f) throws IOException {
		FileReader fr = new FileReader(f);
		try {
			ArrayList<File> retFls = new ArrayList<File>();

			LineNumberReader lnr = new LineNumberReader(fr);
			String ln = null;
			while ((ln = lnr.readLine()) != null) {
				if (StringUtil.isNullOrEmpty(ln))
					continue;

				File fl = new File(ln);

				if (!fl.isAbsolute())
					fl = new File(f.getParentFile(), ln);

				if (!fl.exists())
					logger.error("WARNING - source file does not exist:" + ln);
				else
					retFls.add(fl);
			}
			return retFls.toArray(new File[retFls.size()]);

		} finally {
			fr.close();
		}
	}



	int refScale = Integer.MIN_VALUE;
	private HashMap<URL, WorldRasterImage> deskewed = null;
	private ImageFileFilter filter;
	private TiledCRS tilesCS;
	private double pixSize;
	private int tileW;
	private int tileH;

	private Color bgColor = Color.WHITE;
	ColorFilter colorFilter = null;
	private boolean imgOpaque = true;
	public boolean alwaysInterpolate = false;

	public volatile boolean initCalled = false;

	File[] baseDirs;

	private Comparator<? super RasterWorldInfo> comp;

	private ArrayList<Throwable> readErrors = new ArrayList<Throwable>();

	private WorldImageCollection<WithFilter> images = new WorldImageCollection<WithFilter>() {
		@Override
		protected synchronized WithFilter createImageInfo(File f) throws IOException {
			WithFilter wf = new WithFilter(f.toURI().toURL(), getCRS());
			wf.setColorFilter(colorFilter);
			return wf;
		}

		@Override
		protected synchronized WithFilter createImageInfo(URL fileURL, DimI dimI, AffineTransform2D tr) {
			WithFilter wf = new WithFilter(fileURL, dimI, null, tr);
			wf.setColorFilter(colorFilter);
			return wf;
		}
	};
	private List<Envelope> clearEnvelopes = Collections.emptyList();

	public SourceImageProvider(File[] baseDirs, CRS sourceCS) {
		this(baseDirs, (String)null, sourceCS);
	}

	public SourceImageProvider(File[] baseDirs, final String imageSuffix, CRS sourceCS) {
		this(baseDirs, sourceCS, new ImageFileFilter() {
			final String suff = imageSuffix == null ? null : imageSuffix.toLowerCase();

			@Override
			public boolean acceptDirectory(String dirName) {
				return true;
			}

			@Override
			public boolean acceptFile(String nm) {
				if (suff == null)
					return ImageUtil.isImageFile(nm);
				return nm.toLowerCase().endsWith(suff);
			}

			@Override
			public boolean acceptImage(RasterWorldInfo imgCandidate) {
				return true;
			}
		});
	}

	public SourceImageProvider(File[] baseDirs, CRS sourceCS, ImageFileFilter iff) {
		this.baseDirs = baseDirs;
		this.filter = iff;
		images.setUseCache(false);
		images.setCRS(sourceCS);

		if (baseDirs.length == 1 && FileUtilJava.isSuffixIgnoreCase(baseDirs[0], "xml")) {
			try {
				initializeFromSourcesFile(baseDirs[0]);
			} catch(IOException e) {
				throw new RuntimeException(e);
			} catch(URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public TiledCRS getTiledCRS() {
		return tilesCS;
	}

	public void setTiledCRS(TiledCRS tileCS) {
		this.tilesCS = tileCS;
		if (refScale == Integer.MIN_VALUE) {
			refScale = tileCS.getMaxLevelId();
		}
		pixSize = tileCS.zoomLevels.worldPerPix(refScale);
		tileW = tileCS.tileSizeInPix(refScale).w();
		tileH = tileCS.tileSizeInPix(refScale).h();
	}

	@Override
	public String toString() {
		return Arrays.toString(baseDirs);
	}

	public void setBgColor(Color bgColor) {
		this.bgColor = bgColor;
	}

	public void initializeSources(SGProgressMonitor monitor) throws IOException {
		initCalled = true;
		images.clear();
		images.loadFromDirs(baseDirs, filter, monitor);
		getOptimalPixelSize();
	}

	public void initializeFromSourcesFile(File fl) throws IOException, URISyntaxException {
		initCalled = true;
		images.clear();
		images.loadFromSourcesFile(fl, filter);
		getOptimalPixelSize();
	}

	public double getOptimalPixelSize() {
		int cnt = 0;
		double mean = 0;

		Entry<Double, Integer>[] hist = images.computePixSizeHistogram();

		for (Map.Entry<Double, Integer> e : hist) {
			int freq = e.getValue().intValue();
			mean += e.getKey().doubleValue() * freq;
			cnt += freq;
		}
		mean = mean / cnt;

		double variance = 0;
		for (Map.Entry<Double, Integer> e : hist) {
			double delta = e.getKey().intValue() - mean;
			variance += delta * delta * e.getValue().doubleValue();
		}
		double stdev = Math.sqrt(variance / cnt);

		// Take the values between 10% and 30%
		int partCnt = 0; // count of accumulated values
		int partMeanInc = 0; // count of values included in part's mean calculation
		double partMean = 0;
		for (Map.Entry<Double, Integer> e : hist) {
			int freq = e.getValue().intValue();
			partCnt += freq;

			if (partCnt < 0.1 * cnt)
				continue;
			partMean += e.getKey().doubleValue() * freq;
			partMeanInc += freq;
			if (partCnt > 0.3 * cnt)
				break;
		}
		partMean = partMean / partMeanInc;

		return MathUtil.roundWithBase(10, partMean, 2 * stdev); // We like powers of 10 more than powers of 2?
	}

	private SGRenderedImage getRenderedImage(WorldRasterImage tif) throws IOException {
		synchronized(tif) {
			checkInit();
			long fullMem = Runtime.getRuntime().maxMemory();
			long availableMem = fullMem - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
			SGRenderedImage rImg = tif.getImage();
			try {
				// Don't bother with RenderedImage if the image is quite small or we're forcibly required to read the whole image into memory  
				long nBytes = 4L * tif.h * tif.w;
				if ((nBytes < Integer.MAX_VALUE)
					&& (RasterUtilJava.getHintForceBuffered() || nBytes < (availableMem / 3))) {
					BufferedImageWrapper bufImg = RasterUtilJava.wrap(RasterUtilJava.toBufferedImage(rImg, readErrors));
					tif.setImage(bufImg);
					return bufImg;
				}
			} catch(Exception e) {
				readErrors.add(e);
				System.err.println("Failed to read as BufferedImage. Will return RenderedImage.");
				e.printStackTrace();
			}
			return rImg;
		}
	}

	protected RenderedImage colorTransformIfNeeded(SGRenderedImage tif) {
		if (colorFilter == null)
			return tif;
		return ColorFilter.transform(tif, colorFilter);
	}

	@Override
	public int getMaxLevelId() {
		return refScale;
	}


	public void setMaxLevelId(int maxLevelId) {
		refScale = maxLevelId;
		if (tilesCS != null)
			setTiledCRS(tilesCS);
	}

	@Override
	public boolean hasTile(int scale, int x, int y) throws IOException {
		if (scale != refScale) {
			throw new IllegalArgumentException("Scale does not equal nominal scale for this source");
		}
		return hasData(scale, x, y);
	}

	private List<WithFilter> doTifSearch(Envelope tileWorldEnv) {
		return images.search(tileWorldEnv, comp);
	}

	@Override
	public boolean isOpaque(int scale, int x, int y) throws IOException {
		if (scale != refScale)
			throw new IllegalArgumentException("Scale does not equal nominal scale for this source");
		checkInit();
		Envelope tileEnv = tilesCS.tileWorldBounds(refScale, x, y);
		for (Envelope cEnv : clearEnvelopes) {
			if (cEnv.contains(tileEnv)) {
				return true;
			}
		}
		if (!imgOpaque) {
			return false;
		}
		return images.covers(tileEnv);
	}

	public void setComparator(Comparator<? super RasterWorldInfo> comp) {
		this.comp = comp;
	}

	@Override
	public OffsetBufferedImage getTile(int sc, int x, int y) throws IOException {
		return renderOrGetTile(null, sc, x, y);
	}

	@Override
	public boolean renderTile(OffsetBufferedImage tileImg, int sc, int x, int y) throws IOException {
		OffsetBufferedImage retBI = renderOrGetTile(tileImg.bi, sc, x, y);
		if (retBI != null && retBI.bi != tileImg.bi) {
			System.out.println("Something fishy");
		}
		return retBI != null;
	}

	@SuppressWarnings("resource")
	public OffsetBufferedImage renderOrGetTile(BufferedImage targetImage, int sc, int x, int y) throws IOException {
		if (sc != refScale) {
			throw new IllegalArgumentException("Scale does not equal nominal scale for this source");
		}
		checkInit();
		
		Envelope tileEnv = tilesCS.tileWorldBounds(sc, x, y);
		boolean mod = applyClearEnv(tileEnv, targetImage);

		List<WithFilter> tifs = doTifSearch(tileEnv);
		final double minX = tileEnv.getMinX();
		final double maxY = tileEnv.getMaxY();

		// tifSearcher was executed above
		BufferedImage tileImage = targetImage;
		for (RasterWorldInfo oldTif : tifs) {
			if (!(oldTif instanceof WorldRasterImage)) {
				continue;
			}
			WorldRasterImage tif = (WorldRasterImage)oldTif;
			try {
				tif = ensureSimpleRectangular(tif);
				ImageWithRect imgRect = prepareImageAndRect(tif, minX, maxY);
				if (imgRect == null) {
					continue; // Rounding causes no rendering :(
				}
				try {
					if (tileImage == null && canReturnSubImage(tifs, imgRect)) {
						BufferedImage bufImg;
						if (imgRect.origSizeEquals(tileW, tileH)) {
							bufImg = RasterUtilJava.toBufferedImage(imgRect.image, readErrors);
						} else {
							bufImg = RasterUtilJava.stretchBI(imgRect.image, tileW, tileH);
						}
						return new OffsetBufferedImage(bufImg);
					}
					if (tileImage == null) {
						tileImage = createTileImage(imgRect.image);
					}
					renderLayer(tileImage.createGraphics(), imgRect);
					mod = true;
				} finally {
					imgRect.dispose();
				}
			} finally {
				if (tif != null) {
					tif.setHardCache(false);
				}
			}
		}
		return mod ? new OffsetBufferedImage(tileImage) : null;
	}

	private boolean applyClearEnv(Envelope tileEnv, BufferedImage tileImage) {
		if (tileImage == null) {
			return false;
		}
		boolean mod = false;
		for (Envelope clearEnv : clearEnvelopes) {
			if (clearEnv.intersects(tileEnv)) {
				clear(tileImage, tileEnv, clearEnv, bgColor);
				mod = true;
			}
		}
		return mod;
	}

	public static void clear(BufferedImage tileImage, Envelope tileEnv, Envelope clearEnv, Color backColor) {
		Graphics2D g = tileImage.createGraphics();
		try {
			g.setBackground(backColor);
			if (clearEnv.contains(tileEnv)) {
				g.clearRect(0, 0, tileImage.getWidth(), tileImage.getHeight());
			} else {
				Envelope intEnv = tileEnv.intersectWith(clearEnv);
				double pxSize = tileEnv.getWidth() / tileImage.getWidth();
				int left = (int)Math.round((intEnv.getMinX() - tileEnv.getMinX()) / pxSize);
				int bot = (int)Math.round((intEnv.getMinY() - tileEnv.getMinY()) / pxSize);
				int right = (int)Math.round((tileEnv.getMaxX() - intEnv.getMaxX()) / pxSize);
				int top = (int)Math.round((tileEnv.getMaxY() - intEnv.getMaxY()) / pxSize);
				int w = tileImage.getWidth() - left - right;
				int h = tileImage.getHeight() - top - bot;
				if (w <= 0 || h <= 0) {
					return;
				}
				g.clearRect(left, top, w, h);
			}
		} finally {
			g.dispose();
		}
	}

	public WorldRasterImage ensureSimpleRectangular(WorldRasterImage tif) {
		if (!tif.isSimpleRectangular()) {
			WorldRasterImage origTif = tif;
			tif = deskewImage(pixSize, tif);
			origTif.setHardCache(false);
			tif.setHardCache(true); // Hard cache the new one for now
		}
		return tif;
	}

	public BufferedImage ensureDeltaImg(BufferedImage targetImage, BufferedImage tileImage, BufferedImage deltaImg) {
		if (deltaImg != null) {
			return deltaImg;
		}
		if (targetImage == null && tileImage.getColorModel().hasAlpha()) {
			return tileImage;
		}
		return createTransparentTileImage();
	}

	private boolean canReturnSubImage(List<WithFilter> tifs, ImageWithRect imgRect) {
		if (!imgRect.positionRect.sizeEquals(tileW, tileH)) {
			return false;
		}
		ColorModel colorModel = imgRect.image.getColorModel();
		if (bgColor != null && bgColor.getAlpha() > 0 && colorModel.hasAlpha()) {
			return false;
		}
		if (alwaysInterpolate && (imgRect.image.getWidth() != tileW || imgRect.image.getHeight() != tileH)) {
			return false;
		}
		if (tifs.size() == 1 || !colorModel.hasAlpha()) {
			return true;
		}
		return false;
	}

	protected BufferedImage createTileImage(RenderedImage src) {
		BufferedImage ret;
		if (bgColor.getAlpha() != 255 || (src == null)) {
			ret = createTransparentTileImage();

		} else if (alwaysInterpolate && (src.getColorModel() instanceof IndexColorModel)) {
			ret = new BufferedImage(tileW, tileH, BufferedImage.TYPE_INT_RGB);

		} else {
			ret = RasterUtilJava.createCompatible(src, tileW, tileH);
		}
		RasterUtilJava.clear(ret, bgColor);
		return ret;
	}

	public BufferedImage createTransparentTileImage() {
		return new BufferedImage(tileW, tileH, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * Precomputes the boundaries of the image in the tile's coordinate system.
	 * 
	 * @param tif
	 * @param tMinX
	 * @param tMaxY
	 * @return
	 * @throws IOException
	 */
	protected ImageWithRect prepareImageAndRect(WorldRasterImage tif, double tMinX, double tMaxY) throws IOException {
		AffineTransform2D imgToWorld = tif.tr;
		AffineTransform2D tileToWorld = AffineTransform2D.createTrScale(pixSize, -pixSize, tMinX, tMaxY);

		AffineTransform2D imgToTile = imgToWorld.concatenateWith(tileToWorld.inverse());
		AffineTransform2D tileToImg = imgToTile.inverse();

		EnvelopeI tilePartInImage = tileToImg.envelope(new Envelope(0, 0, tileW, tileH)).roundOutside();
		tilePartInImage = tilePartInImage.intersection(0, 0, tif.w - 1, tif.h - 1);
		if (tilePartInImage.isEmpty()) {
			return null;
		}

		final EnvelopeI croppedPartInTile = imgToTile.envelope(tilePartInImage.toDoubleEnvelope()).round();
		if (!croppedPartInTile.intersects(0, 0, tileW - 1, tileH - 1)) {
			return null;
		}

		final SGRenderedImage croppedImg = RasterUtilJava.crop(getRenderedImage(tif), tilePartInImage);
		return new ImageWithRect(croppedImg, croppedPartInTile);
	}

	private WorldRasterImage deskewImage(double targetPixSize, WorldRasterImage tif) {
		try {
			WorldRasterImage newTif = null;
			if (deskewed != null)
				newTif = deskewed.get(tif.getURL());
			if (newTif == null || !newTif.hasImage()) {
				newTif = deskew(tif, targetPixSize, targetPixSize, alwaysInterpolate, TRANSPARENT_WHITE, true);
				if (deskewed == null)
					deskewed = new HashMap<URL, WorldRasterImage>();
				deskewed.put(tif.getURL(), newTif);
			}
			tif = newTif;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return tif;
	}

	/**
	 * @param g
	 * @param toDraw
	 * @param drawTX
	 */
	public void renderLayer(Graphics2D g, ImageWithRect imgRect) {
		Object oldHint = g.getRenderingHint(KEY_INTERPOLATION);
		if (oldHint == null)
			oldHint = VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
		try {
			if (alwaysInterpolate || imgRect.getFactX() < 0.98) {
				g.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
				g.setRenderingHint(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_QUALITY);
				g.setRenderingHint(KEY_DITHERING, VALUE_DITHER_DISABLE);
			} else {
				g.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			}

			if (imgRect.getFactX() == 1 && imgRect.getFactY() == 1) {
				int posX = imgRect.getOffX();
				int posY = imgRect.getOffY();
				if (imgRect.image.isWrapperFor(BufferedImage.class)) {
					g.drawImage(imgRect.image.unwrap(BufferedImage.class), posX, posY, null);
				} else {
					g.drawRenderedImage(imgRect.image.unwrap(), AffineTransform.getTranslateInstance(posX, posY));
				}
			} else {
				AffineTransform drawTX = new AffineTransform(imgRect.getFactX(), 0, 0, imgRect.getFactY(),
					imgRect.getOffX(), imgRect.getOffY());
				g.drawRenderedImage(imgRect.image.unwrap(), drawTX);
				// throw new IllegalStateException("Should not do non-translate AT");
			}

		} finally {
			g.setRenderingHint(KEY_INTERPOLATION, oldHint);
		}
	}

	@Override
	public boolean hasData(int scale, int x, int y) throws IOException {
		checkInit();
		Envelope tEnv = tilesCS.tileWorldBounds(scale, x, y);
		for (Envelope clearEnv : clearEnvelopes) {
			if (clearEnv.intersects(tEnv)) {
				return true;
			}
		}
		return !doTifSearch(tEnv).isEmpty();
	}

	public void checkInit() throws IOException {
		if (!initCalled) {
			initializeSources(SGProgressMonitor.NO_OP);
		}
	}

	public void setColorFilter(ColorFilter cFilter) {
		this.colorFilter = cFilter;
		this.imgOpaque = false;
	}

	public int getMaxImageSize() {
		double maxSqr = 0;
		List<WorldRasterImage> lst = images.queryAll();
		for (WorldRasterImage wri : lst) {
			double curSqr = (double)wri.h * wri.w;
			if (curSqr > maxSqr)
				maxSqr = curSqr;
		}
		return (int)Math.ceil(Math.sqrt(maxSqr));
	}

	public void setImgOpaque(boolean b) {
		this.imgOpaque = b;
	}

	static AffineTransform concat(AffineTransform a, AffineTransform b) {
		AffineTransform out = new AffineTransform(a);
		out.concatenate(b);
		return out;
	}

	@SuppressWarnings("rawtypes")
	public WorldImageCollection getImages() {
		return images;
	}

	@Override
	public long estimateNumTiles() {
		double wTileW = pixSize * tileW;
		double wTileH = pixSize * tileH;
		double wTileAr = wTileW * wTileH;
		double sumImgAr = 0;

		for (WorldRasterImage img : images.queryAll()) {
			sumImgAr += img.getWorldAreaPerPix() * img.w * img.h;
		}
		return (long)(sumImgAr / wTileAr);
	}

	public void reportErrors(Appendable out) {
		if (!readErrors.isEmpty()) {
			String intro = "ERRORS WHILE READING IMAGES:";
			System.out.println(intro);
			try {
				out.append(intro);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		for (Throwable t : readErrors) {
			StringWriter sw = new StringWriter();
			t.printStackTrace(new PrintWriter(sw));
			String txt = sw.toString();
			System.out.println(txt);
			try {
				out.append(txt);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		readErrors.clear();
	}

	public void cleanup() {
		if (deskewed != null) {
			IOUtil.closeSilent(deskewed.values().toArray(new WorldRasterImage[deskewed.size()]));
			deskewed.clear();
		}
		if (images != null) {
			images.close();
		}
	}

	public void setClearEnvelopes(List<Envelope> clearEnvelopes) {
		this.clearEnvelopes = clearEnvelopes;
	}
}
