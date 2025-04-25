package com.sinergise.java.raster.core;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.raster.core.ImageFileFilter;
import com.sinergise.common.raster.core.RasterFileInfo;
import com.sinergise.common.raster.core.RasterUtil;
import com.sinergise.common.raster.core.RasterWorldInfo;
import com.sinergise.common.util.ImageUtil;
import com.sinergise.common.util.collections.SearchItemReceiver;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.common.util.io.IOUtil;
import com.sinergise.common.util.lang.SGProgressMonitor;
import com.sinergise.common.util.settings.Settings;
import com.sinergise.java.geometry.util.APIMapping;
import com.sinergise.java.util.settings.ObjectStorage;
import com.sinergise.java.util.string.StringSerializer;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.quadtree.Quadtree;


public class WorldImageCollection<T extends RasterWorldInfo> implements Closeable {
	private static final String SRC_IMAGES_FILE = "source_images.xml";

	public static class SrcImagesCache implements Settings, Iterable<SrcImageSettings> {
		ArrayList<SrcImageSettings> images;
	
		public void add(SrcImageSettings img) {
			if (images == null) images = new ArrayList<SrcImageSettings>();
			images.add(img);
		}
	
		
		@Override
		public Iterator<SrcImageSettings> iterator() {
			return images.iterator();
		}
		
		public void saveInDir(File baseDir) throws IOException {
			if (images == null) {
				return;
			}
			save(new File(baseDir,SRC_IMAGES_FILE));
		}

		public void save(File file) throws FileNotFoundException, IOException {
			FileOutputStream fos = new FileOutputStream(file);
			try {
				ObjectStorage.store("SourceImages", this, fos);
			} finally {
				fos.close();
			}
		}
		
		public static SrcImagesCache load(InputStream fis) throws IOException {
			try {
				return ObjectStorage.load(fis, new SrcImagesCache());
			} catch(TransformerException e) {
				throw new IOException(e);
			}
		}


	}

	public static class SrcImageSettings implements Settings {
		String externalId;
		String fileName;
		
		long fileTime;
		long fileSize;
		
		long worldFileTime;
		long worldFileSize;

		int w;
		int h;
		double[] tr;
		
		public SrcImageSettings() {
		}
		
		public SrcImageSettings(RasterFileInfo info) throws URISyntaxException {
			this(new File(info.getImageURL().toURI()).getAbsolutePath(), 0, 0, 0, 0, info.getWorldInfo());
		}
		
		public SrcImageSettings(String fName, long fTime, long fSize, long tfwTime, long tfwSize, int w, int h, double[] tr) {
			this.w = w;
			this.h = h;
			this.tr = tr;
			this.fileName = fName;
			this.fileTime = fTime;
			this.fileSize = fSize;
			this.worldFileTime = tfwTime;
			this.worldFileSize = tfwSize;
		}
		
		public SrcImageSettings(String fileName, long fTime, long fSize, long tfwTime, long tfwSize, RasterWorldInfo img) {
			this(fileName, fTime, fSize, tfwTime, tfwSize, img.w, img.h, img.tr.paramsToArray());
		}
		
		public SrcImageSettings(String fileName, String externalId) {
			this.fileName = fileName;
			this.externalId = externalId;
		}

		public URL getFileURL(URL baseURL) throws MalformedURLException, URISyntaxException {
			if (baseURL.getProtocol().toLowerCase().startsWith("file")) {
				File f = new File(fileName);
				if (f.isAbsolute()) {
					return f.toURI().toURL();
				}
				return new File(new File(baseURL.toURI()), fileName).toURI().toURL();
			}
			return new URL(baseURL, fileName);
		}

		@Override
		public String toString() {
			return fileName + " @"+fileTime+" "+fileSize+" "+w+"x"+h+" "+Arrays.toString(tr)+" @"+worldFileTime+" "+worldFileSize;
		}

		public static String encode(RasterFileInfo rfi) {
			StringBuilder ret = new StringBuilder("<item w=\"");
			ret.append(rfi.w()).append("\" h=\"").append(rfi.h()).append("\" tr=\"");
			ret.append(StringSerializer.storePrimitiveArray(RasterUtil.affineToCellBasedArray(rfi.getTransform())));
			ret.append("\" fileName=\"").append(RasterUtilJava.toFile(rfi.getImageURL())).append("\"/>");
			return ret.toString();
		}
		
		public String getFilename() {
			return fileName;
		}
		
		public long getFileSize() {
			return fileSize;
		}
		
		public long getFileTime() {
			return fileTime;
		}
		
		public long getWorldFileSize() {
			return worldFileSize;
		}
		
		public long getWorldFileTime() {
			return worldFileTime;
		}
		
		public DimI getImageSize() {
			return new DimI(w, h);
		}
		
		public AffineTransform2D getAffineTransform(CRS worldCRS) {
			return new AffineTransform2D(CartesianCRS.createImageCRS(w, h), worldCRS, tr);
		}

		public String getExternalId() {
			return externalId;
		}

		public void setExternalId(String extId) {
			externalId = extId;
		}

		public Polygon getImagePoly() {
			return calculatePoly(getAffineTransform(CRS.NONAME_WORLD_CRS), getImageSize());
		}
		
		private static Polygon calculatePoly(AffineTransform2D tr, DimI size) {
			double[] coords = new double[10];
			writePolyCoord(tr, coords, 0, 0.0, 0.0);
			writePolyCoord(tr, coords, 1, 0.0, size.h());
			writePolyCoord(tr, coords, 2, size.w(), size.h());
			writePolyCoord(tr, coords, 3, size.w(), 0.0);
			coords[8] = coords[0];
			coords[9] = coords[1];
			if (GeomUtil.isCCW(coords)) {
				GeomUtil.reversePackedCoords(coords);
			}
			return new Polygon(new LinearRing(coords), null);
		}

		private static void writePolyCoord(AffineTransform2D tr, double[] out, int idx, double srcX, double srcY) {
			Position2D outpt = tr.point(srcX, srcY);
			out[2 * idx] = outpt.x();
			out[2 * idx + 1] = outpt.y();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((externalId == null) ? 0 : externalId.hashCode());
			result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			SrcImageSettings other = (SrcImageSettings)obj;
			if (externalId == null) {
				if (other.externalId != null) {
					return false;
				}
			} else if (!externalId.equals(other.externalId)) {
				return false;
			}
			if (fileName == null) {
				if (other.fileName != null) {
					return false;
				}
			} else if (!fileName.equals(other.fileName)) {
				return false;
			}
			return true;
		}
	}

	private static class TifsSearcher<T extends RasterWorldInfo> implements ItemVisitor {
		public RasterWorldInfo[] results;
		public int cnt;
		private Envelope env;
		private Comparator<? super T> comp;
	
		public TifsSearcher() {
		}
	
		public void reset() {
			if (results == null) {
				results = new RasterWorldInfo[1];
			}
			cnt = 0;
		}
	
		@Override
		public void visitItem(Object arg0) {
			WorldRasterImage t = (WorldRasterImage)arg0;
			if (env.intersects(t.wEnv)) {
				if (cnt >= results.length) {
					RasterWorldInfo[] tmp = new RasterWorldInfo[results.length * 2];
					System.arraycopy(results, 0, tmp, 0, results.length);
					results = tmp;
				}
				results[cnt++] = t;
			}
		}
	
		@SuppressWarnings("unchecked")
		public void doIt(Quadtree tree, double minX, double minY, double maxX, double maxY) {
			reset();
			env = new Envelope(minX, minY, maxX, maxY);
			tree.query(APIMapping.toJTS(env), this);
			if (cnt > 1 && comp != null) {
				Arrays.sort(results, 0, cnt, (Comparator<RasterWorldInfo>)comp);
			}
		}
	
		public boolean covers(Quadtree tree, double minX, double minY, double maxX, double maxY) {
			doIt(tree, minX, minY, maxX, maxY);
			if (cnt == 0) return false;
			for (int i = 0; i < cnt; i++) {
				if (results[i].wEnv.contains(minX, minY, maxX, maxY)) return true;
			}
			return false;
		}
	
		public void setComp(Comparator<? super T> comp) {
			this.comp = comp;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(WorldImageCollection.class);

	private Quadtree tifs = new Quadtree();
	private EnvelopeBuilder gEnv = new EnvelopeBuilder();

	private CRS commonCRS;
	private boolean useCache = false;
	
	public synchronized void addImage(T info) {
		if (commonCRS == null) commonCRS = info.targetCRS;
		else if (info.targetCRS != null && !commonCRS.equals(info.targetCRS)) {
			throw new IllegalArgumentException("Cannot combine images with different CRSs");
		}
		gEnv.expandToInclude(info.wEnv);
		synchronized(tifs) {
			tifs.insert(APIMapping.toJTS(info.wEnv), info);
		}
		logger.info("Image added {}", info);
	}
	
	@SuppressWarnings("unchecked")
	public List<T> search(Envelope worldRect, Comparator<? super T> comp) {
		if (!gEnv.getEnvelope().intersects(worldRect)) {
			return Collections.emptyList();
		}
		TifsSearcher<T> srchr = new TifsSearcher<T>();
		srchr.setComp(comp);
		synchronized(tifs) {
			srchr.doIt(tifs, worldRect.getMinX(), worldRect.getMinY(), worldRect.getMaxX(), worldRect.getMaxY());
		}
		if (srchr.cnt <= 0) return Collections.emptyList();
		return (List<T>)Arrays.asList(srchr.results).subList(0, srchr.cnt);
	}
	
	@SuppressWarnings("unchecked")
	public List<WorldRasterImage> queryAll() {
		synchronized(tifs) {
			return tifs.queryAll();
		}
	}
	
	public boolean covers(Envelope env) {
		if (!gEnv.getEnvelope().contains(env)) return false;
		synchronized(tifs) {
			return new TifsSearcher<T>().covers(tifs, env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
		}
	}

	
	@SuppressWarnings("unchecked")
	public synchronized Map.Entry<Double, Integer>[] computePixSizeHistogram() {
		HashMap<Double, Integer> freq = new HashMap<Double, Integer>();
		for (WorldRasterImage t : (List<WorldRasterImage>)tifs.queryAll()) {
			Double scale = Double.valueOf(Math.sqrt(t.getWorldAreaPerPix()));
			Integer val = freq.get(scale);
			if (val == null) {
				freq.put(scale, Integer.valueOf(1));
			} else {
				freq.put(scale, Integer.valueOf(val.intValue() + 1));
			}
		}
		Map.Entry<Double, Integer>[] hist = freq.entrySet().toArray(new Map.Entry[0]);
		Arrays.sort(hist, new Comparator<Map.Entry<Double, Integer>>() {
			@Override
			public int compare(Entry<Double, Integer> o1, Entry<Double, Integer> o2) {
				Double k1 = o1.getKey();
				Double k2 = o2.getKey();
				if (k1 == null) return k2 == null ? 0 : -k2.compareTo(k1);
				return k1.compareTo(k2);
			}
		});
		return hist;
	}

	public synchronized void setCRS(CRS sourceCS) {
		this.commonCRS = sourceCS;
	}
	
	public CRS getCRS() {
		return commonCRS;
	}

	public synchronized void loadFromDirs(File[] baseDirs, ImageFileFilter filter, SGProgressMonitor monitor) throws IOException {
		loadFromDirs(baseDirs, filter, monitor, true);
	}
	public synchronized void loadFromDirs(File[] baseDirs, ImageFileFilter filter, SGProgressMonitor monitor, boolean recursive) throws IOException {
		monitor.progress("Scanning image files", 0);
		double stageSize = 0.99/baseDirs.length;
		for (File f : baseDirs) {
			monitor.nestedStageStarting("Scanning "+f.getAbsolutePath(), stageSize);
			try {
				scan(f, filter, monitor, recursive);
			} finally {
				monitor.nestedStageFinished();
			}
		}
		monitor.progress("Computing pixel size statistics", 0.99);
		computePixSizeHistogram();
		monitor.progress("Scanning complete", 1);
	}

	@SuppressWarnings("unchecked")
	protected synchronized T createImageInfo(File f) throws IOException {
		return (T)new WorldRasterImage(f.toURI().toURL(), commonCRS);
	}
	
	@SuppressWarnings("unchecked")
	protected synchronized T createImageInfo(URL fileURL, DimI dimI, AffineTransform2D tr) {
		return (T)new WorldRasterImage(fileURL, dimI, null, tr);
	}

	private synchronized boolean readSourcesFile(File baseDir, ImageFileFilter filter) throws IOException, URISyntaxException {
		File fl = new File(baseDir, SRC_IMAGES_FILE);
		if (!useCache || !fl.exists()) {
			return false;
		}
		return loadFromSourcesFile(fl, filter);
	}

	public boolean loadFromSourcesFile(File fl, ImageFileFilter filter) throws FileNotFoundException, IOException, MalformedURLException, URISyntaxException {
		FileInputStream fis = new FileInputStream(fl);
		try {
			//TODO: Check for new files in the directory
			SrcImagesCache cache = SrcImagesCache.load(fis);
			for (SrcImageSettings img : cache.images) {
				if (!filter.acceptFile(img.fileName)) {
					continue;
				}
				AffineTransform2D tr = new AffineTransform2D(CartesianCRS.createImageCRS(img.w, img.h), commonCRS, img.tr);
				T wri = createImageInfo(img.getFileURL(fl.getParentFile().toURI().toURL()), new DimI(img.w, img.h), tr);
				//TODO: Check if file exists, compare modification timestamps and sizes for image and world files
				if (filter.acceptImage(wri)) {
					addImage(wri);
				}
			}
			return true;
		} finally {
			fis.close();
		}
	}

	private synchronized void scan(File base, ImageFileFilter filter, SGProgressMonitor monitor, boolean recursive) throws IOException {
		if (base.isDirectory() && !base.exists()) {
			throw new IllegalArgumentException("Directory " + base + " does not exist");
		}
		scanFileOrDir(base, "", filter, monitor, recursive, null);
	}

	public synchronized SrcImageSettings scanFileOrDir(File f, String relDir, ImageFileFilter filter, SGProgressMonitor monitor, boolean recursive, Collection<SrcImageSettings> out) throws IOException {
		if (f.isDirectory() && filter.acceptDirectory(f.getName())) {
			try {
				if (readSourcesFile(f, filter)) {
					return null;
				}
			} catch(Exception e) {
				logger.warn("Exception when reading sources file",e);
			}
			String[] fls = f.list();
			if (fls != null) {
				SrcImagesCache dirCache = new SrcImagesCache();
				for (String ff : fls) {
					if (monitor != null) monitor.nestedStageStarting("Reading "+ff, 1.0/fls.length);
					File childF = new File(f, ff);
					if (childF.isDirectory() && !recursive) {
						return null;
					}
					try {
						SrcImageSettings img = scanFileOrDir(childF, relDir + File.separator + childF.getName(), filter, monitor, recursive, out);
						if (img != null) dirCache.add(img);
					} catch (Exception e) {
						System.err.println("ERROR READING "+f);
						e.printStackTrace();
					} finally {
						if (monitor != null) monitor.nestedStageFinished();
					}
				}
				dirCache.saveInDir(f);
			}
			return null;
		}
		if (!ImageUtil.isImageFile(f.getName())) {
			return null;
		}
		
		File tfwFile = RasterUtilJava.getWorldFile(f);
		if (!tfwFile.isFile()) {
			if (RasterUtilJava.getHintFailOnMissingWorldFile()) {
				throw new IOException("No world file found: "+tfwFile.getAbsolutePath());
			}
			logger.warn("WORLD FILE FOR {}; no world file found ({}). Will try to read as GeoTiff", f, tfwFile.getName());
		}
		T info = createImageInfo(f);
		if (filter.acceptFile(f.getName()) && 
			filter.acceptImage(info)) {
			addImage(info);
			if (info instanceof WorldRasterImage) { //Close the resource
				((WorldRasterImage)info).setImage(null);
			}
		}
		if (out != null) {
			out.add(new SrcImageSettings(relDir, f.lastModified(), f.length(), tfwFile.lastModified(), tfwFile.length(), info));
		}
		return new SrcImageSettings(f.getName(), f.lastModified(), f.length(), tfwFile.lastModified(), tfwFile.length(), info);
	}
	
	private static final class ImageItemVisitor<T extends RasterWorldInfo> implements ItemVisitor {
		SearchItemReceiver<? super T> delegate;
		
		public ImageItemVisitor(SearchItemReceiver<? super T> imageVisitor) {
			this.delegate = imageVisitor;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public void visitItem(Object item) {
			delegate.execute((T)item);
		}
	}

	public void query(Envelope mbr, SearchItemReceiver<? super T> imageVisitor) {
		synchronized (tifs) {
			tifs.query(APIMapping.toJTS(mbr), new ImageItemVisitor<T>(imageVisitor));
		}
	}

	public Envelope getBounds() {
		return gEnv.getEnvelope();
	}

	public void clear() {
		logger.debug("Clearing image collection. Size was {}, envelope was {}.", Integer.valueOf(tifs.size()), gEnv);
		gEnv.clear();
		tifs = new Quadtree();
		commonCRS = null;
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void close() {
		for (RasterWorldInfo info : (List<RasterWorldInfo>)tifs.queryAll()) {
			if (info instanceof WorldRasterImage) {
				IOUtil.closeSilent((WorldRasterImage)info);
			}
		}
		clear();
	}
	


	public static Set<SrcImageSettings> loadInfoFromFiles(Set<SrcImageSettings> inputSets) {
		return loadInfoFromFiles(inputSets, true);
	}
	
	public static Set<SrcImageSettings> loadInfoFromFiles(Set<SrcImageSettings> inputSets, boolean failOnError) {
		HashSet<SrcImageSettings> ret = new HashSet<SrcImageSettings>();
		for (SrcImageSettings s : inputSets) {
			try {
				SrcImageSettings newS = WorldImageCollection.createImageSettings(s.getFilename());
				newS.setExternalId(s.getExternalId());
				ret.add(newS);
			} catch (Throwable t) {
				ret.add(s);
				if (failOnError) {
					throw new RuntimeException(t);
				}
				logger.error("Failed to load info from file for "+s.getFilename(), t);
			}
		}
		return ret;
	}

	public static SrcImageSettings createImageSettings(String fileName) throws MalformedURLException, IOException {
		File f = new File(fileName);
		if (!f.exists()) {
			throw new FileNotFoundException(fileName);
		}
		if (!ImageUtil.isImageFile(fileName)) {
			throw new IllegalArgumentException(fileName + " is not an image file");
		}
		
		File tfwFile = RasterUtilJava.getWorldFile(f);
		if (!tfwFile.isFile()) {
			if (RasterUtilJava.getHintFailOnMissingWorldFile()) {
				throw new IOException("No world file found: "+tfwFile.getAbsolutePath());
			}
			logger.warn("WORLD FILE FOR {}; no world file found ({}). Will try to read as GeoTiff", f, tfwFile.getName());
		}
		WorldRasterImage info = new WorldRasterImage(f.toURI().toURL(), CRS.NONAME_WORLD_CRS);
		info.setImage(null);
		return new SrcImageSettings(f.getAbsolutePath(), f.lastModified(), f.length(), tfwFile.lastModified(), tfwFile.length(), info);
	}
}
