package com.sinergise.java.raster.colorfilter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JPanel;

import sun.awt.image.ByteInterleavedRaster;
import sun.awt.image.IntegerInterleavedRaster;

import com.sinergise.java.raster.core.RasterIO;
import com.sinergise.java.raster.core.SGRenderedImage;
import com.sinergise.java.util.ui.RunnerHelper;


public class ColorFiltererRImg implements SGRenderedImage {
	public static final boolean canHandle(final RenderedImage img, final ColorFilter filter) {
		if (img.getColorModel() instanceof IndexColorModel) {
			return false;
		}
		final int imgComp = img.getColorModel().getNumComponents();
		return filter.getNumComponents(imgComp) == imgComp;
	}

	SGRenderedImage	src;
	RasterOp		op;
	ColorModel		cModel;
	ColorFilter		filter;

	public ColorFiltererRImg(final SGRenderedImage src, final ColorFilter filter) {
		this.src = src;
		cModel = src.getColorModel();
		this.filter = filter;
		op = new LookupOp(new LookupTable(0, cModel.getNumComponents()) {
			final int len = cModel.getNumComponents();
			final int[] temp = new int[len];

			@Override
			public int[] lookupPixel(final int[] srcPx, int[] destPx) {
				if (destPx == null || destPx.length < len) destPx = temp;
				if (srcPx != destPx) System.arraycopy(srcPx, 0, destPx, 0, len);
				filter.filter(destPx);
				return destPx;
			}
		}, null);
	}
	
	@Override
	public WritableRaster copyData(final WritableRaster raster) {
		return applyFilter(src.copyData(raster), null);
	}

	protected WritableRaster applyFilter(final WritableRaster raster, final int[] byteOrder) {
		//    	if (1==1) return raster;
		if (raster instanceof ByteInterleavedRaster && !(getColorModel() instanceof IndexColorModel)) {
			ByteInterleavedRaster bir = (ByteInterleavedRaster)raster;
			if (filterBIR(filter, bir, bir, byteOrder)) {
				return raster;
			}
		}
		filter.setInputSampleSize(raster.getNumBands());
		return op.filter(raster, raster);
	}

	public static boolean filterBIR(final ColorFilter filter, final ByteInterleavedRaster src,
		final ByteInterleavedRaster tgt, final int[] byteOrder) {
		
		filter.setInputSampleSize(src.getNumBands());
		final DataBufferByte srcBuf = (DataBufferByte)src.getDataBuffer();
		final DataBufferByte tgtBuf = (DataBufferByte)tgt.getDataBuffer();
		final int sampSize = src.getNumBands();

		for (int i = srcBuf.getNumBanks() - 1; i >= 0; i--) {
			final byte[] data = srcBuf.getData(i);
			final byte[] tgtData = tgtBuf.getData(i);
			for (int j = data.length - sampSize; j >= 0; j -= sampSize) {
				filter.filterBytes(data, j, tgtData, j, byteOrder);
			}
		}
		return true;
	}

	public static boolean filterIIR(final ColorFilter filter, final IntegerInterleavedRaster src, final IntegerInterleavedRaster tgt) {
		filter.setInputSampleSize(src.getNumBands());
		final DataBufferInt srcBuf = (DataBufferInt)src.getDataBuffer();
		final DataBufferInt tgtBuf = (DataBufferInt)tgt.getDataBuffer();
		final int nmBnks = srcBuf.getNumBanks();
		for (int i = nmBnks - 1; i >= 0; i--) {
			final int[] data = srcBuf.getData(i);
			final int[] tgtData = tgtBuf.getData(i);
			for (int j = data.length - 1; j >= 0; j--) {
				tgtData[j] = filter.filterInt(data[j]);
			}
		}
		return true;
	}

	@Override
	public ColorModel getColorModel() {
		return cModel;
	}

	@Override
	public Raster getData() {
		throw new UnsupportedOperationException("getData");
	}

	@Override
	public Raster getData(final Rectangle rect) {
		final Raster ret = src.getData(rect);
		if (ret.getNumBands() == filter.getNumComponents(ret.getNumBands()) && (ret instanceof WritableRaster)) {
			return applyFilter((WritableRaster)ret, null);
		}
		throw new UnsupportedOperationException("getData");
	}

	@Override
	public int getHeight() {
		return src.getHeight();
	}

	@Override
	public int getWidth() {
		return src.getWidth();
	}

	@Override
	public int getMinTileX() {
		return src.getMinTileX();
	}

	@Override
	public int getMinTileY() {
		return src.getMinTileY();
	}

	@Override
	public int getMinX() {
		return src.getMinX();
	}

	@Override
	public int getMinY() {
		return src.getMinY();
	}

	@Override
	public int getNumXTiles() {
		return src.getNumXTiles();
	}

	@Override
	public int getNumYTiles() {
		return src.getNumYTiles();
	}

	@Override
	public Object getProperty(final String name) {
		return src.getProperty(name);
	}

	@Override
	public String[] getPropertyNames() {
		return src.getPropertyNames();
	}

	@Override
	public SampleModel getSampleModel() {
		return src.getSampleModel();
	}

	@Override
	public Vector<RenderedImage> getSources() {
		return src.getSources();
	}

	@Override
	public Raster getTile(final int tileX, final int tileY) {
		final Raster rstr = src.getTile(tileX, tileY);
		if (rstr instanceof ByteInterleavedRaster) {
			final ByteInterleavedRaster bir = (ByteInterleavedRaster)rstr;
			final ByteInterleavedRaster tgt = (ByteInterleavedRaster)bir.createCompatibleWritableRaster();
			filterBIR(filter, bir, tgt, null);
			return tgt;
		}
		return rstr;
		//return src.getTile(tileX, tileY);
	}

	@Override
	public int getTileGridXOffset() {
		return src.getTileGridXOffset();
	}

	@Override
	public int getTileGridYOffset() {
		return src.getTileGridYOffset();
	}

	@Override
	public int getTileHeight() {
		return src.getTileHeight();
	}

	@Override
	public int getTileWidth() {
		return src.getTileWidth();
	}

	@Override
	public void close() throws IOException {
		src.close();
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
		if (type.isInstance(this)) {
			return (S)this;
		}
		throw new IllegalArgumentException("Not an instance of "+type);
	}
	
	static class TestPanel extends JPanel implements MouseMotionListener, MouseListener {
		/**
		 * 
		 */
		private static final long	serialVersionUID	= 1L;
		SGRenderedImage				img;
		int							offx				= 100;
		int							offy				= 100;

		int							lastx				= 0;
		int							lasty				= 0;

		RenderedImage				bco;
		ColorFilter					tcf;

		public TestPanel(final SGRenderedImage img) {
			super();
			this.img = img;
			addMouseListener(this);
			addMouseMotionListener(this);

			tcf =
			//            	new CompositeFilter(new ColorFilter[]{
					new SubtractWhiteFilter()
			//            			new BandTo2Colors(3, new Color(0,0,0,255),new Color(0,128,128,0)),
			//            			,new BlendFilter(new Color(255,0,0,128),1)
			//            			})
			;
			//            bco=new ColorFiltererRImg(img, tcf);
		}

		@Override
		public void paintComponent(final Graphics g1) {
			final Graphics2D g = (Graphics2D)g1;

			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(Color.BLACK);
			g.fillRect(20, 20, 400, 400);
			final long t = System.currentTimeMillis();
			bco = ColorFilter.transform(img, tcf);
			g.drawRenderedImage(bco, AffineTransform.getTranslateInstance(offx, offy));
			System.out.println((System.currentTimeMillis() - t) + " ms");
			if (tcf instanceof BandTo2Colors) {
				final BandTo2Colors g2c = (BandTo2Colors)tcf;
				g.setColor(g2c.getColorMin());
				g.fillRect(0, 0, 50, 50);
				g.setColor(g2c.getColorMax());
				g.fillRect(0, 50, 50, 50);
			}

		}

		@Override
		public void mouseDragged(final MouseEvent e) {
			final int currx = e.getX();
			final int curry = e.getY();
			offx += (currx - lastx);
			offy += (curry - lasty);
			lastx = currx;
			lasty = curry;
			repaint();
		}

		@Override
		public void mousePressed(final MouseEvent e) {
			lastx = e.getX();
			lasty = e.getY();
		}

		@Override
		public void mouseMoved(final MouseEvent e) {}

		@Override
		public void mouseClicked(final MouseEvent e) {}

		@Override
		public void mouseEntered(final MouseEvent e) {}

		@Override
		public void mouseExited(final MouseEvent e) {}

		@Override
		public void mouseReleased(final MouseEvent e) {}
	}

	public static void main(final String[] args) {
		try {
			final SGRenderedImage input = RasterIO.readRendered(new File("C:\\Data\\Sinergise\\Design\\Applications\\cSVT\\Defra_XXL.gif").toURI().toURL());
			System.out.println(input.getSampleModel());
			System.out.println(input.getColorModel());
			//input=ImageLoaderNew.scale(input,0.2f,0.2f,ImageLoaderNew.INTERPOLATION_SMART_RESIZE);
			System.out.println(input.getSampleModel());
			System.out.println(input.getColorModel());
			final TestPanel panel = new TestPanel(input);
			RunnerHelper.runComponent(panel, 800, 600);
		} catch(final Exception e) {
			e.printStackTrace();
		}
	}
}
