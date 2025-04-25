package com.sinergise.java.raster.pyramid;

import static com.sinergise.java.raster.core.RasterUtilJava.getBufImgType;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.raster.core.RasterColorType;
import com.sinergise.common.raster.core.RasterColorType.ColorRep;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.io.IOUtil;
import com.sinergise.common.util.lang.TimeSpec;
import com.sinergise.common.util.lang.TimeSpec.Resolution;
import com.sinergise.common.util.math.ColorUtil;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.java.raster.core.OffsetBufferedImage;
import com.sinergise.java.raster.core.RasterIO;
import com.sinergise.java.raster.core.RasterUtilJava;
import com.sinergise.java.raster.ui.ShowImagesPanel;
import com.sinergise.java.util.io.ByteArrayOutputStream;
import com.sinergise.java.util.io.FileUtilJava;


public class RasterProcessor {
	public static final String SYSPROP_NUM_WORKERS = "com.sinergise.java.raster.pyramid.RasterProcessor.NUM_WORKERS";
	private static final int CLR_SUBS = 0xFF008000;
	private static final int CLR_SUBS_EMPTY = 0xFFC0FFC0;

	private static final int CLR_NULL_UNKNOWN = 0xFFF0C0C0;
	private static final int CLR_NULL_EXCEPTION = 0xFFFF0000;

	private static final int CLR_SRC_ONLY = 0xFF00FFFF;
	private static final int CLR_SRCONLY_EMPTY = 0xFFC0FFFF;

	private static final int CLR_SRC_COMPOSITED = 0xFFFF00FF;
	private static final int CLR_SRCCOMPOSITED_EMPTY = 0xFFFFC0FF;

	private static final int CLR_USED_EXISTING = 0xFFFFFF00;
	private static final int CLR_EXISTING_EMPTY = 0xFFFFFFC0;

	private static int getNumWorkers() {
		try {
			int val = Integer.parseInt(System.getProperty(SYSPROP_NUM_WORKERS));
			System.out.println("Will use "+val+" workers");
			return val;
		} catch (Exception e) {
			return 4;
		}
	}
	
	
	private static ExecutorService createThreadPool(final String name) {
		return Executors.newFixedThreadPool(getNumWorkers(), new ThreadFactory() {
			AtomicInteger tNum = new AtomicInteger(1);
            SecurityManager s = System.getSecurityManager();
            ThreadGroup group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(group, r, name+" "+tNum.incrementAndGet());
				return t;
			}
		});
	}

	
	public static final class OutputEncoder {
		private LinkedBlockingQueue<OutputImageChunk> q = new LinkedBlockingQueue<RasterProcessor.OutputImageChunk>(100);
		private boolean productionStopped = false;
		private final String name;
		private final DiskWriter writer;
		private final ExecutorService exec;
		AtomicLong tilesWritten = new AtomicLong(0);
		AtomicLong timeSpent = new AtomicLong(0);
		Appendable console = System.out; 
		
		public OutputEncoder(String name, Appendable console) {
			this.name = name;
			this.console = console;
			writer = new DiskWriter(name, console);
			exec = createThreadPool(name + " OutputEncoder");
		}
		
		public void add(final OutputImageChunk chunk) {
			if (chunk == null) return;
			if (productionStopped) {
				throw new IllegalStateException("Can't write after output queue was shut down. Probably there has been an error while encoding a previous tile.");
			}
			try {
				q.put(chunk);
				exec.execute(new Runnable() {
					@Override
					public void run() {
						try {
							long startT = System.nanoTime();
							OutputBytesChunk bytesChunk = chunk.write();
							timeSpent.addAndGet(System.nanoTime() - startT);
							writer.add(bytesChunk);
							q.remove(chunk);
							reportProgress();
						} catch(Exception e) {
							reportError(console, "Fatal error while encoding image. Processing will stop.", e);
							productionStopped = true;
						}
					}
				});
			} catch(InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		public void shutdown() throws InterruptedException {
			this.productionStopped = true;
			exec.shutdown();
			if (!exec.awaitTermination(2, TimeUnit.MINUTES)) {
				println(console, "ERROR: Encoder failed to terminate in 2 minutes.");
			}
			if (writer != null) {
				println(console, "Waiting for diskwriter to finish");
				writer.shutdown();
			}
		}

		private void reportProgress() {
			long totalTiles = tilesWritten.incrementAndGet();
			if (totalTiles % 200 == 0) {
				println(console, name+"  ENCODER "+ (long)(timeSpent.get()/tilesWritten.get()*1e-6)+" ms / tile ("+q.size()+" in queue)");
			}
		}
		
		public void setConsole(Appendable console) {
			this.console = console;
			if (writer != null) {
				writer.setConsole(console);
			}
		}
	}
	
	public static class DiskWriter {
		private LinkedBlockingQueue<OutputBytesChunk> q = new LinkedBlockingQueue<OutputBytesChunk>(100);
		private boolean productionStopped = false;
		private ExecutorService exec;
		private final String name;

		long startT =0;
		AtomicLong tilesWritten = new AtomicLong(0);
		AtomicLong bytesWritten = new AtomicLong(0);
		private Appendable console;
		
		public DiskWriter(String name, Appendable console) {
			this.name = name;
			this.console = console;
			exec = createThreadPool(name+" DiskWriter");
		}
		
		public void add(final OutputBytesChunk chunk) {
			if (startT == 0) {
				startT = System.currentTimeMillis();
			}
			if (productionStopped) {
				throw new IllegalStateException("Can't write to disk after queue was shut down. Probably there has been an error while writing a previous tile.");
			}
			try {
				q.put(chunk);
				exec.execute(new Runnable() {
					@Override
					public void run() {
						try {
							chunk.write();
							q.remove(chunk);
							reportProgress(chunk.len);
						} catch(Exception e) {
							reportError(console, "Fatal error while writing image to disk. Processing will stop.", e);
							productionStopped = true;
						}
					}
				});
			} catch(InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		public void shutdown() throws InterruptedException {
			this.productionStopped = true;
			exec.shutdown();
			if (!exec.awaitTermination(2, TimeUnit.MINUTES)) {
				println(console, "ERROR: Disk writer failed to finish writing chunks in 2 minutes.");
			}
		}

		private void reportProgress(int curTileSize) {
			long totalTiles = tilesWritten.incrementAndGet();
			long totalBytes = bytesWritten.addAndGet(curTileSize);
			if (totalTiles % 200 == 0) {
				long deltaT = System.currentTimeMillis() - startT;
				long msPerTile = deltaT / totalTiles;
				double MBitsPerSec = (double)(8 * totalBytes) / 1024 / 1024 / (deltaT/1000);
				println(console, name + " DISK WRITER "+msPerTile +" ms/tile "+MBitsPerSec+" Mbit/s ("+q.size()+" in queue)");
			}
		}
		
		public void setConsole(Appendable console) {
			this.console = console;
		}
	}

	public static class ImageOutput {
		public File[] histDirs = null;
		public boolean overwriteHist = false;
		public GPTilesProvider refSrc = null;
		private TiledCRS myCs;
		public final File[] outDirs;

		public final String[] outTypes;
		private final String name;
		private OutputEncoder outEncoder = null;
		public int quality = 90;
		private Appendable console;

		public ImageOutput(String name, File[] outDirs, String[] outTypes, TiledCRS cs, Appendable console) {
			this.name = name;
			this.console = console;

			this.outTypes = outTypes;
			this.outDirs = outDirs;

			this.myCs = cs;
			for (int i = 0; i < outDirs.length; i++) {
				File prnt = outDirs[i].getParentFile();
				if (!prnt.getParentFile().exists()) {
					throw new IllegalArgumentException("Parent dir does not exist " + outDirs[i].getParentFile());
				}
				try {
					FileUtilJava.forceMkDir(prnt);
				} catch(IOException e) {
					throw new IllegalStateException("Couldn't create output directory "+prnt, e);
				}
			}
		}

		public GPTilesProvider createProvider() throws IOException {
			return new GPTilesProvider(outDirs[0], myCs, outTypes[0]);
		}

		public void shutdown() {
			if (outEncoder != null) {
				try {
					logger.info("Waiting for output outEncoder to finish.");
					outEncoder.shutdown();
				} catch(InterruptedException e) {
					throw new RuntimeException("Interrupted while waiting for output outEncoder to finish", e);
				}
			}
		}

		public void write(int scale, int x, int y, BufferedImage ret) {
			for (int i = 0; i < outTypes.length; i++) {
				outDirs[i].mkdirs();
				synchronized(this) {
					if (outEncoder == null) {
						outEncoder = new OutputEncoder(name, console);
					}
				}
				File f = outFile(outDirs[i], myCs, scale, x, y, outTypes[i]);
				if (histDirs != null) {
					if (f.exists()) {
						File bkpF = outFile(histDirs[i], myCs, scale, x, y, outTypes[i]);
						backupTile(f, bkpF);
					}
				}
				outEncoder.add(new OutputImageChunk(f, ret, outTypes[i], quality));
			}
		}

		public void remove(int scale, int x, int y) {
			for (int i = 0; i < outTypes.length; i++) {
				synchronized(this) {
					if (outEncoder == null) {
						outEncoder = new OutputEncoder(name, console);
					}
				}
				File f = outFile(outDirs[i], myCs, scale, x, y, outTypes[i]);
				if(f.exists()) f.delete();
				
				// TODO: histDirs not yet done  
//				if (histDirs != null) {
//					if (f.exists()) {
//						File bkpF = outFile(histDirs[i], myCs, scale, x, y, outTypes[i]);
//						backupTile(f, bkpF);
//					}
//				}
			}
		}

		private void backupTile(File f, File bkpF) {
			try {
				if (bkpF.exists()) {
					if (!overwriteHist) {
						return;
					}
					FileUtilJava.forceDelete(bkpF);
				} else {
					FileUtilJava.forceMkDirs(bkpF.getParentFile());
				}	
				FileUtilJava.forceRename(f, bkpF);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public void writeEmpty(int scale, int x, int y) throws IOException {
			for (int i = 0; i < outTypes.length; i++) {
				File f = outFile(outDirs[i], myCs, scale, x, y, outTypes[i]);
				if (histDirs != null && f.exists()) {
					File bkpF = outFile(histDirs[i], myCs, scale, x, y, outTypes[i]);
					FileUtilJava.forceDelete(bkpF);
					FileUtilJava.forceMkDirs(bkpF.getParentFile());
					FileUtilJava.forceRename(f, bkpF);
				}
				if (refSrc !=null) {
					FileUtilJava.forceMkDirs(f.getParentFile());
					FileUtilJava.forceCreateNewFile(f);
				}
			}
		}

		public void setConsole(Appendable progressText) {
			if (outEncoder != null) {
				outEncoder.setConsole(progressText);
			}
		}
	}

	private static class ImageSource {
		public final RasterColorType colorType;
		public final TileProviderJava srcProvider;

		public ImageSource(TileProviderJava tiles, RasterColorType colorType) {
			this.srcProvider = tiles;
			this.colorType = colorType;
		}

		public void reportErrors(Appendable out) {
			if (srcProvider instanceof SourceImageProvider) {
				((SourceImageProvider)srcProvider).reportErrors(out);
			}
		}

		public void cleanup() {
			if (srcProvider instanceof SourceImageProvider) {
				((SourceImageProvider)srcProvider).cleanup();
			}
		}
	}

	public static final class OutputImageChunk {
		private final BufferedImage data;
		private final File file;
		private final String type;
		private final int quality;

		public OutputImageChunk(File f, BufferedImage data, String type, int quality) {
			this.file = f;
			this.data = data;
			this.type = type;
			this.quality = quality;
		}

		public OutputBytesChunk write() throws Exception {
			final ByteArrayOutputStream baos;
			if (data.getColorModel().hasAlpha() && "jpg".equalsIgnoreCase(type)) {
				baos = RasterIO.encodeInMemory(doBackground(data), type, quality);
			} else {
				baos = RasterIO.encodeInMemory(data, type, quality);
			}
			return new OutputBytesChunk(baos.getInternalBuffer(), baos.size(), file);
		}
	}
	
	public static final class OutputBytesChunk {
		private final byte[] data;
		private final File file;
		private final int len;

		public OutputBytesChunk(byte[] data, int len, File f) {
			this.file = f;
			this.data = data;
			this.len = len;
		}

		public void write() throws Exception {
			file.getParentFile().mkdirs();
			
			final File tmpFile = new File(file.getParent(), file.getName()+".tmp");
			FileUtilJava.writeBytesToFile(data, 0, len, tmpFile);
			try {
				if (!tmpFile.renameTo(file)) {
					if (file.exists()) {
						FileUtilJava.forceDelete(file);
					}
					FileUtilJava.forceRename(tmpFile, file);
				}
			} catch (Exception e) {
				tmpFile.delete();
				throw new RuntimeException(e);
			}
		}
	}

	private Appendable progressText = null;
	private BufferedImage progressImage = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
	private int progressImageScale;

	static ShowImagesPanel sip;

	public static RasterProcessor create(TileProviderJava input, File[] outDirs, String[] outTypes, RasterColorType inputCType, int maxScale, TiledCRS cs) throws IOException {
		return create(input, outDirs, outTypes, inputCType, maxScale, cs, System.out);
	}
	public static RasterProcessor create(TileProviderJava input, File[] outDirs, String[] outTypes, RasterColorType inputCType, int maxScale, TiledCRS cs, Appendable console) throws IOException {
		return create(input, outDirs, outTypes, inputCType, null, maxScale, cs, console);
	}
	public static RasterProcessor create(TileProviderJava input, File[] outDirs, String[] outTypes, RasterColorType inputCType, File deltaBase, int maxScale, TiledCRS cs, Appendable console) throws IOException {
		ImageSource src = new ImageSource(input, inputCType);
		ImageOutput outImg = (outTypes == null) ? null : new ImageOutput("RasterProcessor", outDirs, outTypes, cs, console);
		
		GPTilesProvider origDeltaSrc = null;
		if(deltaBase!=null && outImg!=null) {
			try {
				VersionedTilesProvider vtp = new VersionedTilesProvider(deltaBase.getParentFile()); 
				origDeltaSrc = vtp.getForVersion(deltaBase.getName(), outImg.outTypes[0]);
			} catch(TransformerException e) {
				throw new IOException(e);
			}
		}
			
		RasterProcessor ret = new RasterProcessor(maxScale, src, outImg, origDeltaSrc, cs, null);
		ret.setProgressTextOutput(console);
		return ret;
	}

	public static RasterProcessor create(TileProviderJava input, String outDir, String[] outTypes, RasterColorType inputCType, int maxScale,
			TiledCRS cs) throws IOException {
		String[] outDirs = new String[outTypes.length];
		for (int i = 0; i < outDirs.length; i++) {
			outDirs[i] = outDir;
		}
		return create(input, outDirs, outTypes, inputCType, maxScale, cs);
	}

	public static RasterProcessor create(TileProviderJava input, String[] outDirs, String[] outTypes, RasterColorType inputCType, int maxScale,
			TiledCRS cs) throws IOException {
		File[] outD = new File[outDirs.length];
		for (int i = 0; i < outD.length; i++) {
			outD[i] = new File(outDirs[i]);
		}
		return create(input, outD, outTypes, inputCType, maxScale, cs);
	}

	private synchronized void displayInGUI(int scale, int i, OffsetBufferedImage ret) {
		if (sip == null) {
			if (!showUI) return;
			sip = ShowImagesPanel.framed("RasterMerger");
			sip.setImage(0, 0, progressImage);
		} else {
			progressImage = sip.getImage(0, 0);
		}
		if (ret != null) {
			sip.setImage(scale, i, RasterUtilJava.cloneBI(ret.bi));
		}
	}

	static File outFile(File baseDir, TiledCRS cs, int level, int x, int y, String type) {
		return PyramidUtil.fileFor(cs, baseDir, level, x, y, "." + type);
	}

	public static void rebuildMissing(String outDir, String[] outTypes, TiledCRS cs, RasterColorType cType, int maxLevel, Envelope mbr) {
		try {
			File out = new File(outDir);
			ImageOutput outImg = new ImageOutput("rebuildMissing",new File[]{out}, outTypes, cs, System.out);
			ImageSource src = new ImageSource(outImg.createProvider(), cType);
			// TODO: rebuilding for combined version  
			RasterProcessor rp = new RasterProcessor(maxLevel, src, outImg, null, cs, mbr);
			rp.skipIfExists = true;
			rp.go(cs.zoomLevels.getMinLevelId(), 0, 0);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	protected static final Logger logger = LoggerFactory.getLogger(RasterProcessor.class);

	public Color backColor = Color.WHITE;

	public TiledCRS cs;

	private int minScale;
	public int maxScale;
	
	private Envelope mbr;
	public ImageOutput out;
	public GPTilesProvider outSrc;
	public GPTilesProvider origSrc;
	public GPTilesProvider origDeltaSrc;
	public boolean skipIfExists = false;
	private ImageSource src;
	protected long srcTiles = 0;
	protected long totalSrcTiles;

	protected long timeBatchStart;
	protected long tilesBatchStart;

	TileCache tilesCacheLarge;
	TileCache tilesCacheSmall;

	protected long tStarted;
	protected long tFinished = -1;

	public boolean updateWithSrc = false;

	public boolean skipEmpty;
	private boolean cancelled;

	public static boolean showUI = true;
	private ExecutorService execs = createThreadPool("RasterProcessor Tiling");
	private AtomicInteger freeExecs = new AtomicInteger();
	
	public DeltaWriter deltaSink;
	
	protected RasterSubsamplingStrategy subsampler = new RasterSubsamplingStrategy.Default();

	public RasterProcessor(int maxLevel, ImageSource src, ImageOutput outImg, GPTilesProvider origDeltaSrc, TiledCRS cs, Envelope mbr) throws IOException {
		this.maxScale = maxLevel;
		this.src = src;
		this.out = outImg;
		this.cs = cs;
		this.mbr = mbr;
		this.outSrc = new GPTilesProvider(outImg.outDirs[0], cs, outImg.outTypes[0]);
		this.origSrc = outSrc;
		this.origDeltaSrc = origDeltaSrc;
		this.progressImageScale = cs.zoomLevels.getMinLevelId() + 9;
		this.backColor = src.colorType.isOpaque() ? Color.WHITE : ColorUtil.TRANSPARENT_WHITE;
		if (!src.srcProvider.getTiledCRS().isCompatibleWith(this.origSrc.getTiledCRS())) {
			throw new IllegalArgumentException("Incompatible tiledCRSs");
		}
		
		paintProgress(cs.getMinLevelId(), 0, 0, 0xFF101010);
	}
	
	public void setProgressTextOutput(Appendable progressText) {
		this.progressText = progressText;
		if (out != null) {
			out.setConsole(progressText);
		}
	}
	
	public final void progressText(final String progressString) {
		println(progressText, progressString);
	}
	
	public void checkTileCaches() {
		DimI tSize = cs.tileSizeInPix(cs.getMaxLevelId());
		RasterColorType type = src.colorType.getForSubsampling();

		if (tilesCacheSmall == null) {
			int bgColor = backColor.getRGB();
			tilesCacheSmall = new TileCache(tSize.w(), tSize.h(), getBufImgType(type), bgColor);
		}
		if (tilesCacheLarge == null) {
			int bgColor = backColor.getRGB();
			tilesCacheLarge = new TileCache(2 * tSize.w(), 2 * tSize.h(), getBufImgType(type), bgColor);
		}
	}

	public void go() throws IOException {
		prepareBeforeExecution();
		go(minScale, 0, 0);
	}

	private void cleanupAfterExecution() {
		cleanup();
		tFinished = System.currentTimeMillis();
		src.reportErrors(progressText);
		progressText("Total time: " + TimeSpec.durationToString(tFinished - tStarted, Resolution.MILLISECOND, "#0.###") + ". " + srcTiles + " tiles.");
	}

	private void prepareBeforeExecution() throws IOException {
		minScale = cs.zoomLevels.getMinLevelId();
		freeExecs.set(Math.max(1, getNumWorkers()));
		totalSrcTiles = src.srcProvider.estimateNumTiles();
		tStarted = System.currentTimeMillis();
		tFinished = -1;
		timeBatchStart = tStarted;
		out.refSrc = (origSrc == outSrc) ? null : origSrc;
		checkTileCaches();
	}

	private void go(int scale, int xx, int yy) throws IOException {
		try {
			internalGo(scale, xx, yy);
		} finally {
			cleanupAfterExecution();
		}
	}

	private void cleanup() {
		try {
			execs.shutdown();
			if (!execs.awaitTermination(2, TimeUnit.MINUTES)) {
				println(progressText, "ERROR: Worker pool failed to terminate in 2 min.");
			}
			if (out != null) {
				out.shutdown();
			}
			if (deltaSink != null) {
				deltaSink.shutdown();
			}
			if (src != null) {
				src.cleanup();
			}
			IOUtil.closeSilent(origSrc, outSrc, tilesCacheLarge, tilesCacheSmall);
		} catch (Exception e) {
			reportError(progressText, "Exception while cleaning up", e);
		}
	}

	private OffsetBufferedImage internalGo(int scale, int xx, int yy) throws IOException {
		if (cancelled) {
			return null;
		}
		AtomicInteger retClr = new AtomicInteger(CLR_NULL_UNKNOWN);

		if (!inMBR(scale, xx, yy) && (skipIfExists == !outSrc.hasTile(scale, xx, yy)) && !(updateWithSrc && (outSrc.hasTile(scale, xx, yy) || origSrc.hasTile(scale, xx, yy)))) {
			notifyDeltaSink(scale, xx, yy, null, false, false);
			return null;
		}

		if (useExisting(scale, xx, yy)) {
			try {
				OffsetBufferedImage ret = outSrc.getTile(scale, xx, yy); //use existing first
				if (ret == null) {
					ret = origSrc.getTile(scale, xx, yy); //use original if existing not found
				}
				ret = checkEmpty(ret);
				if (ret != null) {
					paintProgress(scale, xx, yy, CLR_USED_EXISTING);
				}
				notifyDeltaSink(scale, xx, yy, ret, false, true);
				return ret;
			} catch(Exception e) {
				reportError(progressText, "Error while getting existing tile "+scale+" "+xx+" "+yy, e);
				retClr.set(CLR_NULL_EXCEPTION);
			}
		}

		if (scale == progressImageScale || (scale == maxScale && scale < progressImageScale)) {
			paintProgress(scale, xx, yy, 0xFFFF0000);
			displayInGUI(scale, 0, null);
		}
		if (sip != null && scale == progressImageScale-1) {
			sip.repaint();
		}
		try {
			if (scale == maxScale) {
				return getTileAtMaxScale(scale, xx, yy, retClr);
			}
			return getTileWithSubs(scale, xx, yy, retClr);
		} finally {
			if (scale == progressImageScale || (progressImageScale > maxScale && scale == maxScale)) {
				paintProgress(scale, xx, yy, retClr.get());
				displayInGUI(scale, 0, null);
			}
		}
	}

	public OffsetBufferedImage getTileWithSubs(int scale, int xx, int yy, AtomicInteger retClr) throws IOException {
		OffsetBufferedImage[] subs = goSubs(scale, xx, yy);

		BufferedImage retBufImg = shrinkSubTiles(subs);

		if (retBufImg == null) {
			notifyDeltaForSubs(scale, xx, yy, null);
			retClr.set(CLR_SUBS_EMPTY);
			if (out != null && getTileOrig(scale, xx, yy) != null) {
				out.writeEmpty(scale, xx, yy);
			}
			return null;
		}
		
		if (cancelled) {
			return null;
		}

		OffsetBufferedImage ret = new OffsetBufferedImage(retBufImg);
		displayInGUI(scale, 0, ret);
		notifyDeltaForSubs(scale, xx, yy, retBufImg);
		retClr.set(CLR_SUBS);
		if (out != null) {
			out.write(scale, xx, yy, retBufImg);
		}
		return ret;
	}

	public BufferedImage shrinkSubTiles(OffsetBufferedImage[] subs) {
		if (subs == null) {
			return null;
		}
		BufferedImage[] subImgs = new BufferedImage[4];
		for (int i = 0; i < subImgs.length; i++) {
			subImgs[i] = subs[i] == null ? null : subs[i].bi;
		}
		
		return shrinkSubTiles(subImgs, subsampler, tilesCacheLarge, tilesCacheSmall, cs);
	}
	
	public static BufferedImage shrinkSubTiles(BufferedImage[] subs, RasterSubsamplingStrategy subsampler, TileCache tcLarge, TileCache tcSmall, TiledCRS tiledCS) {
		if (subs == null ||  ArrayUtil.countEquals(subs, null) == 4) {
			return null;
		}
		
		BufferedImage largeImg = tcLarge.createTile();
		Graphics2D g = largeImg.createGraphics();
		g.setComposite(AlphaComposite.Src);
		BufferedImage smallImg = tcSmall.createTile();
		
		for (int a = 0; a < 4; a++) {
			BufferedImage sub = subs[a];
			if (sub == null) {
				continue;
			}
			int subx = (a & 1);
			int suby = (a >>> 1);

			if (!tiledCS.isColumnLeftToRight()) {
				subx = 1 - subx;
			}
			if (tiledCS.isRowBottomToTop()) {
				suby = 1 - suby;
			}
			g.drawImage(sub, subx * sub.getWidth(), suby * sub.getHeight(), null);
		}
		BufferedImage retBufImg = subsampler.shrinkBy2(largeImg, smallImg); 
		tcLarge.releaseTile(largeImg);
		return retBufImg;
	}

	public OffsetBufferedImage getTileAtMaxScale(int scale, int xx, int yy, AtomicInteger retColour) throws IOException {
		OffsetBufferedImage ret = null;
		boolean srcUsed = false;

		// Read existing target
		boolean tgtUsed = hasTileOrig(scale, xx, yy);
		if (tgtUsed && !src.srcProvider.isOpaque(scale, xx, yy) && updateWithSrc) {
			ret = getTileOrig(scale, xx, yy);
		}

		if (ret == null) {
			// Read source
			retColour.set(CLR_SRC_ONLY);
			ret = src.srcProvider.getTile(scale, xx, yy);
			if (ret != null) {
				srcUsed = true;
			}
		} else {
			tgtUsed = true;
			// Paint source over existing target
			retColour.set(CLR_USED_EXISTING);
			ret = makeCompatibleWithSrc(ret);
			boolean changed = src.srcProvider.renderTile(ret, scale, xx, yy);
			if (changed) {
				srcUsed = true;
				retColour.set(CLR_SRC_COMPOSITED);
			}
		}
		if (cancelled) {
			return null;
		}

		// Output the result
		ret = checkEmpty(ret);
		if (ret == null) {
			if (out != null && tgtUsed) {
				out.writeEmpty(scale, xx, yy);
			}
			setColorForEmpty(retColour);
			notifyDeltaSink(scale, xx, yy, null, false, false);
			return null;
		}
		incTileCount();
		displayInGUI(scale, 0, ret);
		if (out != null) {
			out.write(scale, xx, yy, ret.bi);
		}
		notifyDeltaSink(scale, xx, yy, ret, srcUsed, tgtUsed);
		return ret;
	}


	public OffsetBufferedImage getTileOrig(int scale, int xx, int yy) throws IOException {
		OffsetBufferedImage ret = outSrc.getTile(scale, xx, yy); //use existing first
		if (ret == null) {
			ret = origSrc.getTile(scale, xx, yy); //use original if existing not found
		}
		return checkEmpty(ret);
	}
	
	public OffsetBufferedImage getTileOrigDelta(int scale, int xx, int yy) throws IOException {
		OffsetBufferedImage ret = origDeltaSrc.getTile(scale, xx, yy); //use existing first
		return checkEmpty(ret);
	}

	public boolean hasTileOrig(int scale, int xx, int yy) throws IOException {
		boolean ret = outSrc.hasTile(scale, xx, yy); //use existing first
		if (!ret) {
			ret = origSrc.hasTile(scale, xx, yy); //use original if existing not found
		}
		return ret;
	}
	
	public OffsetBufferedImage combineDelta(int scale, int xx, int yy) throws IOException {
		
		if(origDeltaSrc == null) return null;
		
		OffsetBufferedImage obi = origDeltaSrc.getTile(scale, xx, yy);
		if(obi == null || obi.bi == null) return null;
		
		obi = makeCompatibleWithSrc(obi);
		src.srcProvider.renderTile(obi, scale, xx, yy);
		return obi;
	}
	
	public void notifyDeltaSink(int scale, int xx, int yy, OffsetBufferedImage usedTile, boolean srcUsed, boolean tgtUsed) throws IOException {
		
		if (deltaSink != null) {
			if (usedTile == null) {
				deltaSink.pushEmptyTile(scale, xx, yy);
			} else if (!srcUsed) {
				deltaSink.pushNoDelta(scale, xx, yy);
			} else if (!tgtUsed) {
				deltaSink.pushWholeTileUsed(scale, xx, yy, usedTile.bi);
			} else {
				OffsetBufferedImage deltaTile = checkEmpty(src.srcProvider.getTile(scale, xx, yy));
				if (deltaTile == null) {
					deltaSink.pushNoDelta(scale, xx, yy);
				} else if (checkSame(deltaTile, usedTile)) {
					deltaSink.pushWholeTileUsed(scale, xx, yy, usedTile.bi);
				} else {
					// get combined delta
					OffsetBufferedImage obi = combineDelta(scale, xx, yy);
					BufferedImage bi = null;
					if(obi!=null) bi = obi.bi;
					deltaSink.pushPartiallyUsed(scale, xx, yy, deltaTile.bi, bi);
				}
			}
		}

		
	}
	

	private void notifyDeltaForSubs(int scale, int xx, int yy, BufferedImage bi) {
		if (deltaSink != null) {
			if (bi == null) {
				deltaSink.pushEmptyTile(scale, xx, yy);
			} else {
				deltaSink.pushSubData(scale, xx, yy, bi);
			}
		}
	}

	private boolean checkSame(OffsetBufferedImage tileA, OffsetBufferedImage tileB) {
		if (tileA == tileB) {
			return true;
		}
		if (tileA.offX != tileB.offX) {
			return false; 
		}
		if (tileA.offY != tileB.offY) {
			return false;
		}
		return checkSame(tileA.bi, tileB.bi);
	}

	private boolean checkSame(BufferedImage a, BufferedImage b) {
		if (a == b) {
			return true;
		}
		if (a.getWidth() != b.getWidth()) {
			return false;
		}
		if (a.getHeight() != b.getHeight()) {
			return false;
		}
		int bgClr = backColor.getRGB();
		for (int i = 0; i < a.getHeight(); i++) {
			for (int j = 0; j < a.getWidth(); j++) {
				int pxA = a.getRGB(i, j);
				int pxB = b.getRGB(i, j);
				if (pxA == pxB) {
					continue;
				}
				if (pxA == bgClr || (pxA >>> 24 == 0)) {
					if (pxB == bgClr || (pxB >>> 24 == 0)) {
						continue;
					}
				}
				return false;
			}
		}
		return true;
	}

	public void incTileCount() {
		srcTiles++;
		if (srcTiles % 200 == 0) {
			progressText(performanceInfoString());
			resetPerformanceInfo();
		}
	}

	public void setColorForEmpty(AtomicInteger retClr) {
		if (retClr.compareAndSet(CLR_USED_EXISTING, CLR_EXISTING_EMPTY)) {
			return;
		}
		if (retClr.compareAndSet(CLR_SRC_ONLY, CLR_SRCONLY_EMPTY)) {
			return;
		}
		if (retClr.compareAndSet(CLR_SRC_COMPOSITED, CLR_SRCCOMPOSITED_EMPTY)) {
			return;
		}
		return;
	}

	protected OffsetBufferedImage[] goSubs(final int scale, int xx, int yy) throws IOException {
		final int subScale = scale + 1;
		try {
			if (freeExecs.addAndGet(-3) > 0) {
				return goSubsAsync(subScale, xx, yy);
			}
		} finally {
			freeExecs.addAndGet(3);
		}
		OffsetBufferedImage[] subs = new OffsetBufferedImage[4];
		subs[0] = internalGo(subScale, 2*xx, 2*yy);
		subs[1] = internalGo(subScale, 2*xx+1, 2*yy);
		subs[2] = internalGo(subScale, 2*xx, 2*yy+1);
		subs[3] = internalGo(subScale, 2*xx+1, 2*yy+1);
		for (OffsetBufferedImage img : subs) {
			if (img != null) {
				return subs;
			}
		}
		return null;
	}

	private OffsetBufferedImage[] goSubsAsync(int subScale, int xx, int yy) throws IOException {
		Future<OffsetBufferedImage> f1 = internalGoAsync(subScale, 2*xx, 2*yy);
		Future<OffsetBufferedImage> f2 = internalGoAsync(subScale, 2*xx+1, 2*yy);
		Future<OffsetBufferedImage> f3 = internalGoAsync(subScale, 2*xx, 2*yy+1);
		OffsetBufferedImage i4 = internalGo(subScale, 2*xx+1, 2*yy+1);
		try {
			OffsetBufferedImage i1 = f1.get();
			OffsetBufferedImage i2 = f2.get();
			OffsetBufferedImage i3 = f3.get();
			if (i1 == null && i2 == null && i3==null && i4==null) {
				return null;
			}
			return new OffsetBufferedImage[]{i1,i2,i3,i4}; 
		} catch(Exception e) {
			throw new IOException(e);
		}
	}

	private Future<OffsetBufferedImage> internalGoAsync(final int scale, final int x, final int y) {
		return execs.submit(new Callable<OffsetBufferedImage>() {
			@Override
			public OffsetBufferedImage call() throws Exception {
				return internalGo(scale, x, y);
			}
		});
	}

	private void resetPerformanceInfo() {
		timeBatchStart = System.currentTimeMillis();
		tilesBatchStart = srcTiles;
	}

	public String performanceInfoString() {
		final long tCur = tFinished < 0 ? System.currentTimeMillis() : tFinished;
		
		final double msPerTileBatch = (tCur - timeBatchStart) / (double) (srcTiles - tilesBatchStart);
		final double msToFinishBatch = msPerTileBatch * (totalSrcTiles - srcTiles);
		final String ratioBatch = String.valueOf(Math.round(msPerTileBatch));
		final String etcBatch = TimeSpec.durationToString(msToFinishBatch, Resolution.MILLISECOND, "#0.#");

		final double msPerTileTotal = (tCur - tStarted) / (double) srcTiles;
		final double msToFinishTotal = msPerTileTotal * (totalSrcTiles - srcTiles);
		final String ratioTotal = String.valueOf(Math.round(msPerTileTotal));
		final String etcTotal = TimeSpec.durationToString(msToFinishTotal, Resolution.MILLISECOND, "#0.#");
		final String elapsedTotal = TimeSpec.durationToString((tCur - tStarted), Resolution.MILLISECOND, "#0.#");
		
		return StringUtil.padWith(String.valueOf(srcTiles), ' ', 9, true) + " Rate: " + ratioTotal +" ms/tile (inst: "+ ratioBatch+ ")  |  Elapsed: "+elapsedTotal+"  |  Left: "+ etcTotal +" (inst: "+etcBatch+"))";
	}

	private OffsetBufferedImage makeCompatibleWithSrc(OffsetBufferedImage ret) {
		if (ret == null || ret.bi == null) {
			return ret;
		}
		ColorModel cm = ret.bi.getColorModel();
		boolean ok = true;
		if (src.colorType.hasAlpha() && cm.hasAlpha()) ok = false;
		if (src.colorType.getColorBands() == ColorRep.RGB && cm.getNumColorComponents() < 3) ok = false;
		if (src.colorType.getColorBands() == ColorRep.GRAY && cm.getComponentSize(0) < 8) ok = false;
		
		if (ok) return ret;
		BufferedImage newBI = tilesCacheSmall.createCompatible(ret.bi.getWidth(), ret.bi.getHeight());
		newBI.createGraphics().drawImage(ret.bi, null, 0, 0);
		return new OffsetBufferedImage(newBI, ret.offX, ret.offY);
	}

	protected void paintProgress(int scale, int xx, int yy, int retClr) {
		int prgScale = progressImageScale - scale;
		if (prgScale < 0) return;
		
		final int w = 1 << prgScale;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < w; j++) {
				progressImage.setRGB(xx * w + i, 511 - yy * w - j, retClr);
			}
		}
	}

	protected boolean inMBR(int scale, int xx, int yy) throws IOException {
		Envelope curTileEnv = cs.tileWorldBounds(scale, xx, yy);
		boolean inMbr = mbr == null ? true : mbr.intersects(curTileEnv);
		if (inMbr && updateWithSrc) {
			inMbr = src.srcProvider.hasData(scale, xx, yy);
		}
		// If there's no tile at smaller scale, there isn't any at larger scales either
		if (!inMbr) {
			logger.trace("No data in the target area: {}.", curTileEnv);
		}
		return inMbr;
	}

	private OffsetBufferedImage checkEmpty(OffsetBufferedImage ret) {
		if (ret == null || ret.bi == null) {
			return null;
		}
		BufferedImage retBI = checkEmpty(ret.bi);
		if (retBI != null) {
			return ret;
		}
		return null;
	}
	
	private BufferedImage checkEmpty(BufferedImage bi) {
		if (RasterUtilJava.isEmpty(bi, backColor.getRGB())) {
			return null;
		}
		return bi;
	}

	public void rebuildMissing(String outDir, String[] outTypes, TiledCRS outCS, RasterColorType cType, int maxLevel) {
		skipIfExists = true;
		try {
			File outFile = new File(outDir);
			out = (outTypes == null) ? null : new ImageOutput("rebuildMissing", new File[]{outFile.getParentFile()}, outTypes, outCS, progressText);
			outSrc = out.createProvider();
			origSrc = outSrc;
			src = new ImageSource(outSrc, cType);

			this.cs = outCS;
			this.maxScale = maxLevel;

			go(outCS.zoomLevels.getMinLevelId(), 0, 0);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private boolean useExisting(int scale, int xx, int yy) throws IOException {
		if (skipIfExists && outSrc.hasTile(scale, xx, yy, false)) {
			return true;
		}
		if (updateWithSrc && !src.srcProvider.hasData(scale, xx, yy)) {
			return true;
		}
		return false;
	}

	static BufferedImage doBackground(BufferedImage ret) {
		BufferedImage newBI = new BufferedImage(ret.getWidth(), ret.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D gr = newBI.createGraphics();
		gr.setBackground(Color.WHITE);
		gr.clearRect(0, 0, newBI.getWidth(), newBI.getHeight());
		gr.drawImage(ret, 0, 0, null);
		gr.dispose();
		ret = newBI;
		return ret;
	}

	public static List<String> moveTiles(File srcDir, File tgtDir, File bkpDir, boolean overwrite) throws IOException {
		List<String> ret = new ArrayList<String>();
		for (File f : srcDir.listFiles()) {
			if (f.isDirectory()) {
				File fTgt = new File(tgtDir, f.getName());
				FileUtilJava.forceMkDir(fTgt);
				
				if (bkpDir != null) {
					File fBkp = new File(bkpDir, f.getName());
					fBkp.mkdir();
				
					FileUtilJava.moveFilesBackupExisting(f, fTgt, fBkp, null, overwrite, ret);
				} else {
					FileUtilJava.moveFilesInDirs(f, fTgt, null, overwrite, ret);
				}
				if (f.list().length == 0) {
					FileUtilJava.forceDelete(f);
				}
			}
		}
		FileUtilJava.deleteIfEmpty(srcDir, false, true);
		return ret;
	}
	
	public static void moveTilesNoBkp(File srcDir, File tgtDir, boolean overwrite) throws IOException {
		ArrayList<String> errors = new ArrayList<String>();
		for (File f : srcDir.listFiles()) {
			if (f.isDirectory()) {
				File fTgt = new File(tgtDir, f.getName());
				FileUtilJava.forceMkDir(fTgt);
				FileUtilJava.moveFilesInDirs(f, fTgt, null, overwrite, errors);
				FileUtilJava.deleteIfEmpty(f, false, false);
			}
		}
		FileUtilJava.deleteIfEmpty(srcDir, false, true);
	}

	public BufferedImage getProgressImage() {
		return progressImage;
	}

	public void cancel() {
		cancelled = true;
	}

	public void setSubsamplingStrategy(RasterSubsamplingStrategy subsamplingStrategy) {
		this.subsampler = subsamplingStrategy;
	}
	
	public static void reportError(Appendable console, String msg, Exception e) {
		println(console, msg);
		println(console, extractStackTrace(e));
	}
	
	public static void println(Appendable console, String msg) {
		if (console==null) {
			return;
		}
		try {
			console.append(msg);
			console.append('\n');
			if (console != System.out && console != System.err) {
				System.out.println(msg);
			}
		} catch(IOException e1) {
			try {
				console.append("Error while logging "+e1);
				System.out.println("Error while logging "+e1);
			} catch (Throwable t) {
				System.err.println("XXX");
				t.printStackTrace();
			}
		}
	}
	
	public static String extractStackTrace(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
}
