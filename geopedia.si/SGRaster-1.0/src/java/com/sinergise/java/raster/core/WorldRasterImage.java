package com.sinergise.java.raster.core;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.raster.core.RasterUtil;
import com.sinergise.common.raster.core.RasterWorldInfo;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.io.IOUtil;
import com.sinergise.common.util.lang.SGCallable;
import com.sinergise.java.raster.colorfilter.ColorFilter;
import com.sinergise.java.raster.colorfilter.ColorFilterer;
import com.sinergise.java.util.DynamicReference;
import com.sinergise.java.util.MemoryUtil;


public class WorldRasterImage extends RasterWorldInfo implements Closeable {
	public static class WithFilter extends WorldRasterImage {
		protected ColorFilter cFilter = null;

		public WithFilter(URL imageURL, CRS worldCRS) throws IOException {
			super(imageURL, worldCRS);
		}

		public WithFilter(URL imageURL, DimI imgSize, SGRenderedImage img, AffineTransform2D transform) {
			super(imageURL, imgSize, img, transform);
		}

		public void setColorFilter(ColorFilter cFilter) {
			this.cFilter = cFilter;
		}

		@Override
		protected SGRenderedImage readRendered() throws IOException {
			SGRenderedImage ret = super.readRendered();
			return applyFilter(ret);
		}
		
		private SGRenderedImage applyFilter(RenderedImage img) {
			if (cFilter == null || img == null) {
				return RasterUtilJava.wrap(img);
			}
			if (img instanceof BufferedImage) {
				ColorFilterer filtered = ColorFilter.transformInPlace((BufferedImage)img, cFilter);
				setImage(filtered);
				return filtered;
			}
			SGRenderedImage ret = ColorFilter.transform(RasterUtilJava.wrap(img), cFilter);
			setImage(ret);
			return ret;
		}

		@Override
		protected BufferedImage readBuffered() throws IOException {
			return (BufferedImage)applyFilter(super.readBuffered());
		}
	}

	protected URL imageURL;

	protected transient DynamicReference<SGRenderedImage> image;
	private transient int readCnt = 0;
	protected transient boolean bufImgFailed = false;

	public WorldRasterImage(URL imageURL, CRS worldCRS) throws IOException {
		this(imageURL, RasterIO.readImageSize(imageURL), null, worldCRS, RasterUtilJava.readImageTransform(imageURL, worldCRS));
	}

	protected WorldRasterImage(URL imageURL, DimI imgSize, SGRenderedImage img, CRS worldCRS, double[] tfwData) {
		this(imageURL, imgSize, img, RasterUtil.affineFromCellBasedArrayImage(imgSize.w(), imgSize.h(), worldCRS, tfwData));
	}

	public WorldRasterImage(URL imageURL, SGRenderedImage img, AffineTransform2D transform) {
		this(imageURL, new DimI(img.getWidth(), img.getHeight()), img, transform);
	}

	public WorldRasterImage(URL imageURL, DimI imgSize, SGRenderedImage img, AffineTransform2D transform) {
		super(imgSize, transform);
		this.imageURL = imageURL;
		image = new DynamicReference<SGRenderedImage>(img, DynamicReference.TYPE_SOFT);
	}

	public synchronized SGRenderedImage getImage() throws IOException {
		if (image != null) {
			SGRenderedImage retImg = image.get();
			if (retImg != null) {
				return retImg;
			}
		}
		if (imageURL == null) {
			return null;
		}

		long t = System.currentTimeMillis();
		SGRenderedImage ret = readRendered();
		imgRead(System.currentTimeMillis() - t);
		image.set(ret);
		return ret;
	}

	@SuppressWarnings("unused")
	protected SGRenderedImage readRendered() throws IOException {
		return RasterIO.readRendered(imageURL);
	}

	protected void imgRead(long t) {
		readCnt++;
		System.out.println("Read " + imageURL + " (" + readCnt + ") took " + t + " ms.");
	}

	public URL getURL() {
		return imageURL;
	}

	public String getImageFileName() {
		if (imageURL == null) return null;
		String path = imageURL.getPath();
		return path.substring(path.lastIndexOf('/') + 1);
	}

	public String getImageFileNameNoSuffix() {
		String ret = getImageFileName();
		int idx = ret.lastIndexOf('.');
		if (idx < 0) return ret;
		return ret.substring(0, idx);
	}

	public void setHardCache(boolean hardCache) {
		image.setType(hardCache ? DynamicReference.TYPE_HARD : DynamicReference.TYPE_SOFT);
	}

	public boolean isHardCache() {
		return image.getType() == DynamicReference.TYPE_HARD;
	}

	public synchronized BufferedImage getAsBufferedImage() {
		SGRenderedImage img = image.get();
		if (img != null) {
			BufferedImage retBI = RasterUtilJava.toBufferedImage(img);
			image.set(RasterUtilJava.wrap(retBI));
			return retBI;
		}
		return MemoryUtil.executeWithOutOfMemoryRetry(2, 1000, new SGCallable<BufferedImage>() {
			@Override
			public BufferedImage call() {
				if (imageURL == null)
					throw new IllegalStateException("Image URL is not set");
				try {
					long t = System.currentTimeMillis();
					BufferedImage ret = readBuffered();
					imgRead(System.currentTimeMillis() - t);
					image.set(RasterUtilJava.wrap(ret));
					return ret;
				} catch(IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	public void setURL(URL newURL) {
		this.imageURL = newURL;
	}

	public String getImageSuffix() {
		String fn = getImageFileName();
		return fn.substring(fn.lastIndexOf('.') + 1);
	}

	public File save() throws IOException {
		File imgFile = new File(imageURL.getFile());
		boolean hc = isHardCache();
		try {
			setHardCache(true);
			RasterIO.writeRendered(getImage(), imgFile, RasterUtil.imageTypeFromSuffix(getImageSuffix()));
		} finally {
			setHardCache(hc);
		}
		RasterUtilJava.writeWorldFile(RasterUtil.affineToCellBasedArray(tr), RasterUtilJava.getWorldFile(imgFile));
		return imgFile;
	}

	@Override
	public String toString() {
		return getImageFileName() + " " + super.toString();
	}

	public void setImage(SGRenderedImage img) {
		updateImageSize(img);
		if (image == null) {
			image = new DynamicReference<SGRenderedImage>(img, DynamicReference.TYPE_SOFT);
			return;
		}
		if (image.get() == img) {
			return;
		}
		if (img == null) {
			image.clear();
		} else {
			image.set(img);
		}
	}

	private void updateImageSize(SGRenderedImage img) {
		if (img == null) {
			return;
		}
		w = img.getWidth();
		h = img.getHeight();
		updateImageSize();
	}

	public boolean hasImage() {
		return !image.isEmpty();
	}

	protected BufferedImage readBuffered() throws IOException {
		BufferedImage retBI = null;
		if (!bufImgFailed) {
			try {
				retBI = RasterIO.readBuffered(imageURL);
			} catch(Exception e) {
				bufImgFailed = true;
				System.err.println("Failed to read buffered image");
				e.printStackTrace();
				throw new IOException(e);
			}
		}
		setImage(RasterUtilJava.wrap(retBI));
		return retBI;
	}

	@Override
	public void close() throws IOException {
		IOUtil.close(image.get());
		image.clear();
		image.setType(DynamicReference.TYPE_WEAK);
	}
}
