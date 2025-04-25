package com.sinergise.java.raster.core;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.Closeable;
import java.util.HashMap;
import java.util.Vector;

import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.java.util.SGWrapper;

public interface SGRenderedImage extends RenderedImage, SGWrapper<RenderedImage>, Closeable {
	public static abstract class RenderedImageWrapper extends SGWrapperImpl<RenderedImage> implements SGRenderedImage {
		public RenderedImageWrapper(RenderedImage image) {
			super(image);
		}

		@Override
		public Vector<RenderedImage> getSources() {
			return wrappedObj.getSources();
		}

		@Override
		public Object getProperty(String name) {
			return wrappedObj.getProperty(name);
		}

		@Override
		public String[] getPropertyNames() {
			return wrappedObj.getPropertyNames();
		}

		@Override
		public ColorModel getColorModel() {
			return wrappedObj.getColorModel();
		}

		@Override
		public SampleModel getSampleModel() {
			return wrappedObj.getSampleModel();
		}

		@Override
		public int getWidth() {
			return wrappedObj.getWidth();
		}

		@Override
		public int getHeight() {
			return wrappedObj.getHeight();
		}

		@Override
		public int getMinX() {
			return wrappedObj.getMinX();
		}

		@Override
		public int getMinY() {
			return wrappedObj.getMinY();
		}

		@Override
		public int getNumXTiles() {
			return wrappedObj.getNumXTiles();
		}

		@Override
		public int getNumYTiles() {
			return wrappedObj.getNumYTiles();
		}

		@Override
		public int getMinTileX() {
			return wrappedObj.getMinTileX();
		}

		@Override
		public int getMinTileY() {
			return wrappedObj.getMinTileY();
		}

		@Override
		public int getTileWidth() {
			return wrappedObj.getTileWidth();
		}

		@Override
		public int getTileHeight() {
			return wrappedObj.getTileHeight();
		}

		@Override
		public int getTileGridXOffset() {
			return wrappedObj.getTileGridXOffset();
		}

		@Override
		public int getTileGridYOffset() {
			return wrappedObj.getTileGridYOffset();
		}

		@Override
		public Raster getTile(int tileX, int tileY) {
			return wrappedObj.getTile(tileX, tileY);
		}

		@Override
		public Raster getData() {
			return wrappedObj.getData();
		}

		@Override
		public Raster getData(Rectangle rect) {
			return wrappedObj.getData(rect);
		}

		@Override
		public WritableRaster copyData(WritableRaster raster) {
			return wrappedObj.copyData(raster);
		}
	}

	public static class BufferedImageWrapper extends RenderedImageWrapper {
		public BufferedImageWrapper(BufferedImage img) {
			super(img);
		}

		@Override
		public BufferedImage unwrap() {
			return (BufferedImage)super.unwrap();
		}

		@Override
		public void close() {
			wrappedObj = null;
		}
	}

	public static abstract class DefaultSGRenderedImage implements SGRenderedImage {
		protected EnvelopeI tileZero;
		protected EnvelopeI dataEnvelope;
		protected ColorModel colorModel;
		protected SampleModel sampleModel;
		protected Vector<RenderedImage> sources = null;
		protected HashMap<String, Object> properties = new HashMap<String, Object>();

		@Override
		public ColorModel getColorModel() {
			return colorModel;
		}

		@Override
		public SampleModel getSampleModel() {
			return sampleModel;
		}

		@Override
		public int getTileWidth() {
			return tileZero.getWidth();
		}

		@Override
		public int getTileHeight() {
			return tileZero.getHeight();
		}

		@Override
		public int getTileGridXOffset() {
			return tileZero.minX();
		}

		@Override
		public int getTileGridYOffset() {
			return tileZero.minY();
		}

		@Override
		public int getWidth() {
			return dataEnvelope.getWidth();
		}

		@Override
		public int getHeight() {
			return dataEnvelope.getHeight();
		}

		@Override
		public int getMinX() {
			return dataEnvelope.minX();
		}

		@Override
		public int getMinY() {
			return dataEnvelope.minY();
		}

		@Override
		public int getMinTileX() {
			return getTileCol(getMinX());
		}

		public int getTileCol(int imageX) {
			return (imageX - getTileGridXOffset()) / getTileWidth();
		}

		@Override
		public int getMinTileY() {
			return (getMinY() - getTileGridYOffset()) / getTileHeight();
		}

		public int getTileRow(int imageY) {
			return (imageY - getTileGridYOffset()) / getTileHeight();
		}

		@Override
		public int getNumXTiles() {
			return getTileCol(dataEnvelope.maxX()) - getMinTileX() + 1;
		}

		@Override
		public int getNumYTiles() {
			return getTileRow(dataEnvelope.maxY()) - getMinTileY() + 1;
		}

		@Override
		public Vector<RenderedImage> getSources() {
			return sources;
		}

		@Override
		public Raster getData() {
			return getData(new Rectangle(dataEnvelope.minX(), dataEnvelope.minY(), dataEnvelope.getWidth(),
				dataEnvelope.getHeight()));
		}

		@Override
		public Raster getData(Rectangle bounds) {
			EnvelopeI paramBnds = EnvelopeI.withSize(bounds.x, bounds.y, bounds.width, bounds.height);
			if (!dataEnvelope.intersects(paramBnds)) {
				throw new IllegalArgumentException("Requested bounds should intersect this image's bounds (req = "
					+ paramBnds + " this=" + dataEnvelope + ")");
			}

			EnvelopeI tileEnv = getTileEnvelope(paramBnds);

			if (tileEnv.getArea() == 1) {
				Raster tile = getTile(tileEnv.minX(), tileEnv.minY());
				return tile.createChild(bounds.x, bounds.y, bounds.width, bounds.height, bounds.x, bounds.y, null);
			}
			// Recalculate the tile limits if the data bounds are not a subset of the image bounds.
			if (!dataEnvelope.contains(paramBnds)) {
				EnvelopeI xsect = paramBnds.intersection(dataEnvelope);
				tileEnv = getTileEnvelope(xsect);
			}

			// Create a WritableRaster of the desired size
			SampleModel sm = sampleModel.createCompatibleSampleModel(bounds.width, bounds.height);

			// Translate it
			WritableRaster dest = Raster.createWritableRaster(sm, bounds.getLocation());

			// Loop over the tiles in the intersection.
			for (PointI tIdx : tileEnv) {
				// Retrieve the tile.
				Raster tile = getTile(tIdx.x, tIdx.y);
				// Create a child of the tile for the intersection of
				// the tile bounds and the bounds of the requested area.
				Rectangle intersectRect = bounds.intersection(tile.getBounds());
				Raster liveRaster = tile.createChild(intersectRect.x, intersectRect.y, intersectRect.width, intersectRect.height, intersectRect.x, intersectRect.y, null);
				// Copy the data from the child.
				dest.setRect(liveRaster);
			}

			return dest;
		}

		public EnvelopeI getTileEnvelope(EnvelopeI paramBnds) {
			int startCol = getTileCol(paramBnds.minX());
			int startRow = getTileRow(paramBnds.minY());
			int endCol = getTileCol(paramBnds.maxX());
			int endRow = getTileRow(paramBnds.maxY());
			return EnvelopeI.create(startCol, startRow, endCol, endRow);
		}

		@Override
		public boolean isWrapperFor(Class<? extends RenderedImage> type) {
			return type.isInstance(this);
		}

		@Override
		public RenderedImage unwrap() {
			return this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <S extends RenderedImage> S unwrap(Class<S> type) {
			if (isWrapperFor(type)) {
				return (S)this;
			}
			throw new IllegalArgumentException("Cannot unwrap " + getClass() + " to " + type);
		}

		@Override
		public Object getProperty(String name) {
			Object ret = properties.get(name.toLowerCase());
			return ret == null ? Image.UndefinedProperty : ret;
		}

		@Override
		public String[] getPropertyNames() {
			return properties.keySet().toArray(new String[properties.size()]);
		}
	}
}
