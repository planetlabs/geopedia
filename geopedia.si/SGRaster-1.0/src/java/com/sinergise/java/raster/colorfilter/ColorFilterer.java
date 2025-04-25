package com.sinergise.java.raster.colorfilter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;

import sun.awt.image.ByteInterleavedRaster;
import sun.awt.image.IntegerInterleavedRaster;

import com.sinergise.java.raster.core.ImageLoaderNew;
import com.sinergise.java.raster.core.RasterIO;
import com.sinergise.java.raster.core.RasterUtilJava;
import com.sinergise.java.raster.core.SGRenderedImage;
import com.sinergise.java.raster.ui.RasterGUIUtils;


public final class ColorFilterer extends BufferedImage implements SGRenderedImage {
	public ColorFilterer(final RenderedImage src, final ColorFilter filter) {
		this(src, filter, BufferedImage.TYPE_INT_ARGB);
	}

	public ColorFilterer(RenderedImage src, final ColorFilter filter, final int preferredResultImageType) {
		super(src.getWidth(), src.getHeight(), preferredResultImageType);

		if (src instanceof SGRenderedImage) {
			src = ((SGRenderedImage)src).unwrap();
		}
		if (src instanceof BufferedImage) {
			RasterUtilJava.copy(((BufferedImage)src),this);
		} else {
			final Graphics2D gr = createGraphics();
			gr.drawRenderedImage(src, new AffineTransform());
		}
		final WritableRaster wr = getRaster();
		if (wr instanceof ByteInterleavedRaster) {
			final ByteInterleavedRaster bir = (ByteInterleavedRaster)wr;
			int[] byteOrder = null;
			if (preferredResultImageType == TYPE_4BYTE_ABGR) {
				byteOrder = new int[]{3, 2, 1, 0};
			} else if (preferredResultImageType == TYPE_3BYTE_BGR) {
				byteOrder = new int[]{2, 1, 0};
			}
			ColorFiltererRImg.filterBIR(filter, bir, bir, byteOrder);
		} else if (wr instanceof IntegerInterleavedRaster) {
			final IntegerInterleavedRaster iir = (IntegerInterleavedRaster)wr;
			ColorFiltererRImg.filterIIR(filter, iir, iir);
		} else {
			filter.setInputSampleSize(wr.getNumBands());
			final LookupOp op = new LookupOp(new LookupTable(0, 4) {
				final int[]	temp	= new int[4];

				@Override
				public final int[] lookupPixel(final int[] srcPx, int[] dest) {
					if (dest == null) dest = temp;
					if (srcPx != dest) System.arraycopy(srcPx, 0, dest, 0, 4);
					
					filter.filter(dest);
					return dest;
				}
			}, null);
			op.filter(this, this);
		}
	}

	@Override
	public Raster getData() {
		if (getNumXTiles() == 1 && getNumYTiles() == 1) {
			return getTile(0, 0);
		}
		return super.getData();
	}

	@Override
	public Raster getData(final Rectangle rect) {
		if (rect.x == 0 && rect.y == 0 && rect.width == getWidth() && rect.height == getHeight()) {
			return getData();
		}
		return super.getData(rect);
	}
	
	@Override
	public void close() {
	}
	
	@Override
	public boolean isWrapperFor(Class<? extends RenderedImage> type) {
		return type.isInstance(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S extends RenderedImage> S unwrap(Class<S> type) {
		if (type.isInstance(this)) {
			return (S)this;
		}
		throw new IllegalArgumentException("Not an instance of "+type);
	}

	@Override
	public RenderedImage unwrap() {
		return this;
	}

	public static void main(final String[] args) {
		try {
			//            RenderedImage input=ImageLoaderNew.load(new FileInputStream("C:\\Data\\Sinergise\\CTO\\SinerTech\\Colors\\subtractBGTest\\testBW.png"));
			RenderedImage input = RasterIO.readRendered(new File("C:\\Data\\Sinergise\\CTO\\SinerTech\\Colors\\subtractBGTest\\cesteReal.png").toURI().toURL());
			//            RenderedImage input=ImageLoaderNew.load(new FileInputStream("C:\\Data\\Sinergise\\CTO\\SinerTech\\Colors\\subtractBGTest\\aa_magenta.tif"));
			//            Color toSubtract = new Color(255,229,255);
			final Color toSubtract = Color.decode("#FFE5FF");

			//            System.out.println(input.getSampleModel());
			//            System.out.println(input.getColorModel());
			input = ImageLoaderNew.scale(input, 2f, 2f, ImageLoaderNew.INTERPOLATION_SMART_RESIZE);
			//            System.out.println(input.getSampleModel());
			//            System.out.println(input.getColorModel());

			//            RasterGUIUtils.showImage(new ColorFilterer(input,new SubtractBgFilter(Color.WHITE, 1, 1)), "WHITE");
			//            RasterGUIUtils.showImage(new ColorFilterer(input,new SubtractBgFilter(Color.BLACK, 1, 1)), "BLACK");
			//            RasterGUIUtils.showImage(new ColorFilterer(input,new SubtractBgFilter(Color.RED, 1, 1)), "RED", Color.RED);
			//            RasterGUIUtils.showImage(new ColorFilterer(input,new SubtractBgFilter(Color.GRAY, 1, 1)),"GRAY", Color.GRAY);
			//            RasterGUIUtils.showImage(new ColorFilterer(input,new SubtractBgFilter(Color.GREEN, 1, 1)),"GREEN", Color.GREEN);
			//            RasterGUIUtils.showImage(new ColorFilterer(input,new SubtractBgFilter(Color.BLUE, 1, 1)),"BLUE", Color.BLUE);
			//            RasterGUIUtils.showImage(new ColorFilterer(input,new SubtractBgFilter(Color.YELLOW, 1, 1)),"YELLOW", Color.YELLOW);

			final ColorFilter toApply = new SubtractBgSmoothFilter(toSubtract, 1, 1).setSmooth(false);
			//            ColorFilter toApply = new SubtractBgFilter(toSubtract, 1, 1);
			//            ColorFilter toApply = new ColorReplace(0xFFFF00FF, 0x00000000, false, true).setSmooth(true).setTolerance(256);

			RasterGUIUtils.showImage(new ColorFilterer(input, toApply), "MAGENTA", Color.MAGENTA);
			RasterGUIUtils.showImage(new ColorFilterer(input, toApply), "BLACK", Color.BLACK);
			RasterGUIUtils.showImage(new ColorFilterer(input, toApply), "WHITE", Color.WHITE);
			//            RasterGUIUtils.showImage(new ColorFilterer(input,new SubtractBgFilter(Color.CYAN, 1, 1)),"CYAN", Color.CYAN);
		} catch(final Exception e) {
			e.printStackTrace();
		}
	}

	public static int getCompatibleBIType(final BufferedImage srcBI, final ColorFilter filter) {
		final int dType = srcBI.getSampleModel().getDataType();
		final int tgtComps = filter.getNumComponents(srcBI.getColorModel().getNumComponents());
		if (dType == DataBuffer.TYPE_BYTE) {
			if (tgtComps == 4) {
				return BufferedImage.TYPE_4BYTE_ABGR;
			} else if (tgtComps == 4) {
				return BufferedImage.TYPE_3BYTE_BGR;
			} else if (tgtComps == 1) {
				return BufferedImage.TYPE_BYTE_GRAY;
			}
		} else {
			if (tgtComps == 4) {
				return BufferedImage.TYPE_INT_ARGB;
			} else if (tgtComps == 3) {
				return BufferedImage.TYPE_INT_RGB;
			} else if (tgtComps == 1) {
				return BufferedImage.TYPE_BYTE_GRAY;
			}
		}
		return BufferedImage.TYPE_INT_ARGB;
	}
}
