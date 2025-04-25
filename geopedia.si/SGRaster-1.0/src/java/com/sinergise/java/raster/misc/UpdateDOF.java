package com.sinergise.java.raster.misc;

import static com.sinergise.java.raster.pyramid.PyramidUtil.VERSION_INFO_FILE_NAME;
import static com.sinergise.java.raster.pyramid.PyramidUtil.timeSpecToVersionName;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.raster.core.RasterColorType;
import com.sinergise.common.raster.core.RasterWorldInfo;
import com.sinergise.common.raster.core.TilesIndex;
import com.sinergise.common.raster.core.VersionedRasterDataset;
import com.sinergise.common.raster.core.VersionedRasterDataset.RasterDatasetVersionInfo;
import com.sinergise.common.util.ImageUtil;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.lang.TimeSpec;
import com.sinergise.java.geometry.tiles.TileUtilJava;
import com.sinergise.java.raster.RasterSplitter;
import com.sinergise.java.raster.colorfilter.ColorReplace;
import com.sinergise.java.raster.colorfilter.SubtractBgFilter;
import com.sinergise.java.raster.core.WorldRasterImage;
import com.sinergise.java.raster.io.PyramidIndexIO;
import com.sinergise.java.raster.pyramid.DeltaWriter;
import com.sinergise.java.raster.pyramid.GPTilesProvider;
import com.sinergise.java.raster.pyramid.PyramidUtil;
import com.sinergise.java.raster.pyramid.RasterProcessor;
import com.sinergise.java.raster.pyramid.RasterSubsamplingStrategy;
import com.sinergise.java.raster.pyramid.SourceImageProvider;
import com.sinergise.java.raster.pyramid.VersionedTilesProvider;
import com.sinergise.java.raster.util.GiselleLayerMapping;
import com.sinergise.java.util.UtilJava;
import com.sinergise.java.util.cmdline.CmdLineParserSG;
import com.sinergise.java.util.io.FileUtilJava;


public class UpdateDOF {
	static {
		UtilJava.initStaticUtils();
	}
	
	
	@Argument(index = 0, required = true, metaVar = "<src>", usage = "either:\n-file containing the list of source images, or\n-a directory containing the images")
	public File sourcesFile;

	@Argument(index = 1, required = true, metaVar = "<out_dir>", usage = "output directory, can contain existing image pyramid to be updated")
	public File outputDir;

	@Argument(index = 2, required = false, metaVar = "<out_types>", usage = "output image type(s), separated by comma (e.g. tif,jpg or tif)")
	public String outputTypesString = null;

	@Option(name = "-tempDir", required = false, multiValued = false, metaVar = "<org_dir>", usage = "temporary directory, used for splitting files into more manageable chunks")
	public File tempDir;

	@Option(name = "-originalDir", required = false, multiValued = false, metaVar = "dir", usage = "existing image pyramid to be updated, in which case only the new tiles will be created in <out_dir>")
	public File originalDir;

	@Option(name = "-crs", required = false, multiValued = false, metaVar = "C", usage = "either:"
		+ "\n- coordinate reference system code character, or" + "\n- path to the xml file describing the tiledCRS")
	public String crsSpec = null;

	@Option(name = "-h", required = false, multiValued = false, metaVar = "dir", usage = "optional directory where old data will be stored")
	public File histDir = null;

	@Option(name = "-histOverwrite", required = false, multiValued = false, usage = "flag that causes existing tiles in the -h directory to be overwritten")
	public boolean histOverwrite = false;

	@Option(name = "-doTypeDirs", required = false, multiValued = false, usage = "flag that causes the use of separate output dirs for image types"
		+ "\n- only used in case of multiple output types"
		+ "\n- names of the output dirs are the lowercase of the image type")
	public boolean dirsForTypes = true;

	@Option(name = "-trans", required = false, multiValued = false, usage = "flag that produces transparent output tiles (if the output type supports it)")
	public boolean doTransparent = false;

	@Option(name = "-bgColor", required = false, multiValued = false, usage = "color in AARRGGBB hexadecimal format")
	public Color bgColor = null;

	@Option(name = "-r", required = false, multiValued = false, usage = "scan for sources recursively in case <src> is a directory")
	public boolean recursive = false;

	@Option(name = "-s", required = false, multiValued = false, usage = "initial zoom level (default is maximum of the tiledCRS)")
	public int userScale = Integer.MIN_VALUE;

	@Option(name = "-transColor", required = false, multiValued = false, usage = "color to be replaced with transparency")
	public Color transColor = null;

	@Option(name = "-transSmooth", required = false, multiValued = false, usage = "when replacing with transparent this flag will cause the background color to be subtracted from the whole image")
	public boolean transSmooth = false;

	//	@Option(name = "-transTrim", required = false, multiValued = false, usage = "input images will be trimmed to ")
	//	public boolean transTrim = false;


	@Option(name = "-skipEmpty", required = false, multiValued = false, usage = "flag that causes empty tiles to be omitted from the output")
	public boolean skipEmpty = false;

	@Option(name = "-srcSort", required = false, multiValued = false, usage = "choice of (scale,name)")
	public String srcSort = "scale";

	@Option(name = "-printGiselleXML", required = false, multiValued = false, usage = "before the processing starts, the XML needed to create Giselle layer is printed out")
	public boolean printGiselleXML = false;

	@Option(name = "-noUI", required = false, multiValued = false, usage = "don't display the progress user interface")
	public boolean noUI = false;

	@Option(name = "-revision", required = false, multiValued = false, usage = "the name of the revision; this will either cause update of the existing revision, if it exists, or create a new revision of dataset using the source imagery")
	public String revision = null;

	@Option(name = "-revTime", required = false, multiValued = false, usage = "the reference timestamp of the revision, in ISO 8601 extended format (YYYY-MM-DDThh:mm:ss.sss); ")
	public TimeSpec revTime = null;

	@Option(name = "-revBase", required = false, multiValued = false, usage = "the name of the original revision to be updated")
	public String revBase = null;

	@Option(name = "-revKeepHist", required = false, multiValued = false, usage = "if set to false, existing revision tiles will not be stored")
	public boolean revKeepHist = true;

	@Option(name = "-deltaDir", required = false, multiValued = false, usage = "if set, a separate pyramid will be created depicting only the source images")
	public File deltaDir = null;

	@Option(name = "-deltaCombinedDir", required = false, multiValued = false, usage = "directory of the combined separate pyramid")
	public File deltaCombinedDir = null;
	
	@Option(name = "-deltaBase", required = false, multiValued = false, usage = "if set, the separate pyramid will upgrade on an existing  pyramid that uses source images only; used for combining several versions into a single one.")
	public File deltaBase = null;
	
	@Option(name = "-interpolate", required = false, multiValued = false, usage = "use bilinear interpolation when scaling images (default false)")
	public boolean interpolate = false;
	
	String[] outputTypes;
	private File[] inFiles;
	File datasetRootDir;
	private RevisionUpdateInfo revData;
	RasterProcessor processor;
	SourceImageProvider sourceImageProvider;
	TiledCRS resolvedCRS;
	ArrayList<File> filesCreated = new ArrayList<File>();
	private Appendable progressText = new StringBuilder();
	private boolean readFilesWhenPreparing = true;
	private List<Envelope> clearEnvelopes = Collections.emptyList();

	boolean resumed;
	boolean cancelled = false;

	public int quality = 90;

	private RasterSubsamplingStrategy subsamplingStrategy = null;
	
	private static final Logger logger = LoggerFactory.getLogger(UpdateDOF.class);
	
	public void setResumed(boolean resumed) {
		this.resumed = resumed;
	}

	public void setProgressTextOutput(Appendable output) {
		this.progressText = output;
	}

	public static void main(String[] args) {
		try {
			UpdateDOF ud = construct(args);
			if (ud == null) return;
			ud.go();
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	public static UpdateDOF construct(String[] args) {
		return CmdLineParserSG.parseArgs(new UpdateDOF(), args, "UpdateDOF");
	}

	/**
	 * If set and if sourcesFile is null, these will be the raster files used in the dataset update
	 * 
	 * @param inFiles
	 */
	public void setInputFiles(File[] inFiles) {
		this.inFiles = inFiles;
	}

	public static final File[] scanSourceDir(File dir, boolean recursive) {
		ArrayList<File> out = new ArrayList<File>();
		scanSourceFile(dir, recursive, out);
		return out.toArray(new File[out.size()]);
	}

	private static final void scanSourceFile(File file, boolean recursive, ArrayList<File> out) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				if (recursive || !f.isDirectory()) {
					scanSourceFile(f, recursive, out);
				}
			}
		}
		if (ImageUtil.isImageFile(file.getName())) out.add(file);
	}

	public void go() throws Exception {
		boolean ok = false;
		try {
			prepare();
			doProcess();
			ok = true;
			try {
				commit();
			} catch(Throwable t) {
				throw new Exception("Exception while committing changes. Will not rollback!", t);
			}
		} finally {
			try {
				if (!ok) rollback();
			} catch(Throwable t) {
				progressText("Failed to rollback: " + t);
				progressText(RasterProcessor.extractStackTrace(t));
			}
		}

	}

	public void prepare() throws Exception {
		RasterProcessor.showUI = !noUI;
		
		resolveInputFiles();
		checkOutputDir();
		resolveOutputTypes();
		resolvedCRS = resolveCRS();
		prepareRevision();
		resolveColors();

		final File[] outputDirs = createDirsForTypes(outputDir, outputTypes, dirsForTypes);

		File[] histDirs = null;
		if (histDir != null) {
			if (histDir.exists() && !histDir.isDirectory()) {
				throw new IllegalArgumentException("History dir (" + histDir.getPath() + ") should be a directory.");

			} else if (!histDir.getParentFile().exists()) { throw new IllegalArgumentException(
				"History dir could not be created: missing parent directory " + histDir.getParent());

			}
			FileUtilJava.forceMkDir(histDir);
			filesCreated.add(histDir);

			histDirs = new File[outputTypes.length];
			for (int i = 0; i < histDirs.length; i++) {
				histDirs[i] = dirsForTypes ? new File(histDir, outputTypes[i]) : histDir;
				if (histDirs[i].isFile()) {
					throw new IllegalArgumentException("History dir (" + histDirs[i].getPath() + ") should be a directory; the path exists, but is a file.");
				}
				FileUtilJava.forceMkDir(histDirs[i]);
				filesCreated.add(histDirs[i]);
			}
		}

		sourceImageProvider = initializeSIP(inFiles, resolvedCRS);
		if (tempDir != null && sourceImageProvider.getMaxImageSize() > 6000) {
			Arrays.sort(inFiles, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					return ((File)o1).getAbsolutePath().compareTo(((File)o2).getAbsolutePath());
				}
			});
			File[] splitFiles = RasterSplitter.splitFiles(inFiles, tempDir, resolvedCRS.baseCRS, 4000);
			sourceImageProvider = initializeSIP(splitFiles, resolvedCRS);
		}
		if (readFilesWhenPreparing) {
			sourceImageProvider.checkInit();
		}

		int scale = resolveScale(sourceImageProvider);

		if (printGiselleXML) {
			progressText("============ Giselle Config ==============\n");
			progressText(GiselleLayerMapping.toGiselleLayerXML(true, resolvedCRS, scale, outputTypes[0], outputDirs[0]));
			progressText("\n==========================================\n");
		}

		final RasterColorType inputCT = doTransparent ? RasterColorType.TYPE_RGB_ALPHA : RasterColorType.TYPE_RGB;
		processor = RasterProcessor.create(sourceImageProvider, outputDirs, outputTypes, inputCT, deltaBase, scale, resolvedCRS, progressText);
		if (originalDir != null) {
			processor.origSrc = createProviderForDir(originalDir, outputTypes[0]);
		}
		if (subsamplingStrategy != null) {
			processor.setSubsamplingStrategy(subsamplingStrategy);
		}
				
		processor.skipIfExists = resumed;
		processor.backColor = bgColor;
		processor.updateWithSrc = true;
		processor.out.histDirs = histDirs;
		processor.skipEmpty = skipEmpty;
		processor.out.overwriteHist = histOverwrite;


		if (quality >= 0) {
			processor.out.quality = quality;
		}
		
		prepareDelta();
	}

	public void resolveOutputTypes() {
		outputTypes = outputTypesString.split(",");
		for (int i = 0; i < outputTypes.length; i++) {
			outputTypes[i] = outputTypes[i].trim();
		}
	}

	public void resolveInputFiles() throws IOException {
		if (inFiles == null || sourcesFile != null) {
			if (sourcesFile.isDirectory()) {
				inFiles = scanSourceDir(sourcesFile, recursive);
			} else if (FileUtilJava.isSuffixIgnoreCase(sourcesFile, "xml")) {
				inFiles = new File[] {sourcesFile};
			} else {
				SourceImageProvider.readSourcesFile(sourcesFile);
			}
		}
	}

	public static File[] createDirsForTypes(File baseOut, String[] outTypes, boolean separateTypeDirs) {
		final File[] outputDirs = new File[outTypes.length];
		// Create out directories for all types, with sub-directories for types if necessary 
		for (int i = 0; i < outputDirs.length; i++) {
			outputDirs[i] = separateTypeDirs ? new File(baseOut, outTypes[i]) : baseOut;
		}
		return outputDirs;
	}

	protected void prepareDelta() {
		if (deltaDir != null) {
			processor.deltaSink = new DeltaWriter(processor, deltaDir, deltaCombinedDir, deltaBase, progressText);
		}
	}

	protected int resolveScale(SourceImageProvider sip) {
		int scale = resolvedCRS.getMaxLevelId();
		if (userScale >= 0) scale = userScale;
		else scale = resolvedCRS.zoomLevels.optimalZoomLevelPix(sip.getOptimalPixelSize(), 1);
		if (scale < 0) scale = resolvedCRS.zoomLevels.getMaxLevelId();
		sip.setMaxLevelId(scale);

		progressText("\nReference scale: " + resolvedCRS.zoomLevels.worldPerPix(scale) + " m/px\n");
		return scale;
	}

	private void progressText(String string) {
		if (progressText != null) try {
			progressText.append(string);
			if (progressText != System.out && progressText != System.err) {
				System.out.println(string);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private GPTilesProvider createProviderForDir(File baseDir, String type) throws Exception {
		File verFile = new File(originalDir, PyramidUtil.VERSION_INFO_FILE_NAME);
		if (!verFile.exists()) {
			return new GPTilesProvider(baseDir, resolvedCRS, type);
		}
		VersionedTilesProvider vtp = new VersionedTilesProvider(baseDir.getParentFile());
		return vtp.getForVersion(baseDir.getName(), type);
	}

	public void doProcess() throws Exception {
		saveCRS(resolvedCRS, processor.maxScale);
		sourceImageProvider.checkInit();
		if (revData != null) revData.preProcess();
		
		// if doing the combined delta we first copy the delta base file 
		if(deltaCombinedDir!=null) {
			copyTiles(deltaBase, deltaCombinedDir);
		}
		
		processor.go();
		if (printGiselleXML) {
			progressText("============ Giselle Config ==============\n");
			progressText(GiselleLayerMapping.toGiselleLayerXML(true, processor.cs, processor.maxScale,
				processor.out.outTypes[0], processor.out.outDirs[0]));
			progressText("\n==========================================\n");
		}
	}
	
	public static void copyTiles(File srcDir, File tgtDir) throws IOException {
		logger.info("Copying tiles from delta base:" + srcDir.getAbsolutePath() + " to the new combined delta: " + tgtDir.getAbsolutePath());
		
		for (File f : srcDir.listFiles()) {
			if (f.isDirectory()) {
				File fTgt = new File(tgtDir, f.getName());
				FileUtilJava.forceMkDir(fTgt);
				FileUtilJava.copyFiles(f.getAbsolutePath(), fTgt.getAbsolutePath());
			}
		}		
	}

	public void commit() throws Exception {
		if (revData != null) {
			revData.commit();
		}
	}

	public void rollback() {
		if (revData != null) {
			revData.rollback();
		}
		ArrayList<IOException> failed = new ArrayList<IOException>();
		for (File f : filesCreated) {
			if (f.isFile()) {
				try {
					FileUtilJava.forceDelete(f);
				} catch(IOException e) {
					failed.add(e);
				}
			}
		}
		for (File f : filesCreated) {
			if (f.isDirectory()) {
				try {
					safeDeleteDir(f);
				} catch(IOException e) {
					failed.add(e);
				}
			}
		}
		if (!failed.isEmpty()) {
			for (IOException e : failed) {
				progressText(RasterProcessor.extractStackTrace(e));
			}
			throw new RuntimeException("Failed to delete dirs", failed.get(0));
		}
	}

	private void saveCRS(TiledCRS cs, int scale) throws IOException {
		TiledCRS newCS = cs.createWithMaxLevel(scale);
		File csFile = new File(datasetRootDir, TileUtilGWT.FILENAME_TILEDCRS);
		if (!csFile.exists()) {
			TileUtilJava.saveForBaseDir(newCS, datasetRootDir);
			filesCreated.add(csFile);
		}
	}

	private void checkOutputDir() {
		if (outputDir.exists() && !outputDir.isDirectory()) { throw new IllegalArgumentException("Output dir ("
			+ outputDir.getPath() + ") should be a directory."); }
		if (!outputDir.exists()) {
			File pDir = outputDir.getParentFile();
			if (!pDir.exists()) {
				throw new IllegalArgumentException("Cannot create output dir - " + pDir.getPath() + " does not exist.");
			}
			try {
				FileUtilJava.forceMkDir(outputDir);
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
			filesCreated.add(outputDir);
		}
		datasetRootDir = outputDir;
	}

	protected void prepareRevision() throws Exception {
		if (revision == null && revTime == null) {
			return;
		}
		revData = new RevisionUpdateInfo();
		revData.prepare();
	}

	protected void resolveColors() {
		if (transColor != null) doTransparent = true;

		if (bgColor == null) {
			bgColor = new Color(0x00FFFFFF, doTransparent);

		} else if (doTransparent) {
			if (bgColor.getAlpha() == 255) doTransparent = false;

		} else if (bgColor.getAlpha() != 255) {
			doTransparent = true;
		}
	}

	protected TiledCRS resolveCRS() {
		return resolveCRS(originalDir);
	}
	
	protected TiledCRS resolveCRS(File oldVersion) {
		ArrayList<File> lst = new ArrayList<File>();

		addDirAndParents(lst, outputDir, 3);
		addDirAndParents(lst, oldVersion, 3);

		Set<File> srcDirs = FileUtilJava.extractParentDirs(inFiles);
		if (srcDirs != null) {
			lst.addAll(srcDirs);
		}
		if (tempDir != null) {
			lst.add(tempDir);
		}
		return TileUtilJava.resolveCRS(lst, crsSpec);
	}
	

	private void addDirAndParents(ArrayList<File> lst, File dir, int numDirs) {
		if (numDirs < 1 || dir == null) {
			return;
		}
		lst.add(dir);
		addDirAndParents(lst, dir.getParentFile(), numDirs-1);
	}

	protected SourceImageProvider initializeSIP(File[] inputFls, TiledCRS cs) {
		SourceImageProvider sip = new SourceImageProvider(inputFls, cs.baseCRS);
		if ("name".equals(srcSort)) {
			sip.setComparator(new Comparator<RasterWorldInfo>() {
				@Override
				public int compare(RasterWorldInfo oo1, RasterWorldInfo oo2) {
					WorldRasterImage o1 = (WorldRasterImage)oo1;
					WorldRasterImage o2 = (WorldRasterImage)oo2;
					return o2.getImageFileName().compareTo(o1.getImageFileName());
				}
			});
		} else if ("scale".equals(srcSort)) {
			sip.setComparator(new RasterWorldInfo.ScaleComparator());
		}
		if (transColor != null) {
			if (transSmooth) sip.setColorFilter(new SubtractBgFilter(transColor, 1, 1));
			else sip.setColorFilter(new ColorReplace(transColor.getRGB(), 0x00ffffff));
			
			sip.setImgOpaque(false);
		} else {
			sip.setImgOpaque(!doTransparent);
		}
		
		if (!clearEnvelopes.isEmpty()) {
			sip.setClearEnvelopes(clearEnvelopes);
		}
		
		sip.setBgColor(bgColor);
		sip.setTiledCRS(cs);
		sip.alwaysInterpolate = interpolate;
		return sip;
	}

	private class RevisionUpdateInfo {
		private RasterDatasetVersionInfo targetInfo;
		private RasterDatasetVersionInfo baseInfo;
		private File originalIndex;
		private File outTarget;
		private File outOldHist;
		private File outTemp;
		private boolean usingTemp;

		public RevisionUpdateInfo() {}

		public void rollback() {
			//delete new files
			if (outTemp.isDirectory() && filesCreated.contains(outTemp)) {
				try {
					safeDeleteDir(outTemp);
				} catch(IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		public void commit() throws Exception {
			if (usingTemp) {
				List<String> errors;
				// Do the version switch
				if (outOldHist != null) {
					FileUtilJava.forceMkDir(outOldHist);
					FileUtilJava.forceRename(originalIndex, new File(outOldHist, originalIndex.getName()));
					errors = RasterProcessor.moveTiles(outTemp, outTarget, outOldHist, false);
				} else {
					File tgtIndex = PyramidIndexIO.getIndexFile(outTarget);
					FileUtilJava.forceDelete(tgtIndex);
					File tempIndex = PyramidIndexIO.getIndexFile(outTemp);
					if (tempIndex.exists()) {
						FileUtilJava.forceRename(tempIndex, tgtIndex);
					}

					RasterDatasetVersionInfo tgtVerInfo = PyramidUtil.loadVersionInfo(outTarget.toURI().toURL());
					tgtVerInfo.versionName = revision;
					tgtVerInfo.versionTime = revTime;
					PyramidUtil.saveVersionInfo(tgtVerInfo, outTarget);

					errors = RasterProcessor.moveTiles(outTemp, outTarget, null, true);
				}
				if (!errors.isEmpty()) {
					for (String msg : errors) {
						progressText(msg);
					}
					throw new Exception("Errors while committing pyramid changes (count="+errors.size()+")"); 
				}
				safeDeleteDir(outTemp);
			}
		}

		protected void prepare() throws Exception {
			resolveVersionNameFromRevTime();

			VersionedRasterDataset dataset = null;
			if (datasetRootDir.isDirectory()) {
				dataset = VersionedTilesProvider.load(datasetRootDir, resolvedCRS, outputTypes);
				if (revBase == null) {
					revBase = dataset.latestVersionName();
				}
				if (revision != null && revTime == null) {
					RasterDatasetVersionInfo info = dataset.getVersionInfo(revision);
					if (info != null) {
						revTime = info.versionTime;
					} else {
						revTime = TimeSpec.createForCurrentDate();
					}
				}
			} else {
				if (revision != null && revTime == null) {
					revTime = TimeSpec.createForCurrentDate();
				}
				if (revBase == null) {
					revBase = revision;
				}
			}
			File outBase = new File(datasetRootDir, revBase);
			if (checkOrCleanRev(outBase)) {
				baseInfo = PyramidUtil.loadVersionInfo(outBase.toURI().toURL());
				
			} else if (dataset != null) {
				baseInfo = dataset.getVersionInfo(revBase);
				if (baseInfo != null) {
					outBase = new File(baseInfo.path);
					if (!outBase.isAbsolute()) {
						outBase = new File(datasetRootDir, baseInfo.path);
					}
				}
			}
			outTarget = new File(datasetRootDir, revision);
			outTemp = new File(datasetRootDir, PyramidUtil.REV_SYSTEM_TEMP);

			if (!resumed) {
				safeDeleteDir(outTemp);
			}
			usingTemp = checkOrCleanRev(outTarget);
			if (resumed) {
				usingTemp = checkOrCleanRev(outTemp);
			}
			if (usingTemp) {
				// Update existing target
				targetInfo = PyramidUtil.loadVersionInfo(outTarget.toURI().toURL());
				originalDir = outTarget;
				outputDir = outTemp;

				if (revKeepHist) {
					//Check hist file now before doing anything serious
					outOldHist = new File(datasetRootDir, timeSpecToVersionName(targetInfo.versionTime));
					if (outOldHist.exists() && !resumed) {
						throw new IllegalArgumentException("Cannot store previous version - directory already exists '"
							+ outOldHist + "'");
					}
				}
			} else if (outBase.exists()) {
				// Create a completely new revision based on 'revBase'
				originalDir = outBase;
				outputDir = outTarget;
			} else {
				// Create a completely new version dir, without the temp and without reference
				originalDir = null;
				outputDir = outTarget;
			}
			filesCreated.add(outputDir);
		}

		public boolean checkOrCleanRev(File revDir) throws IOException {
			if (!revDir.exists()) {
				return false;
			}
			try {
				PyramidUtil.loadVersionInfo(revDir.toURI().toURL());
				return true;
			} catch (Throwable t) {
				progressText("Revision dir exists ("+revDir+") but version info couldn't be loaded. Will rename dir");
				t.printStackTrace();
				safeDeleteDir(revDir);
				return false;
			}
		}

		public void resolveVersionNameFromRevTime() {
			if (revision == null && revTime != null) {
				revision = timeSpecToVersionName(revTime);
			}
		}

		protected void preProcess() throws Exception {
			String tempRevName = revision;
			String tempDelegateName = baseInfo == null ? null : baseInfo.versionName;
			if (usingTemp) {
				tempRevName = PyramidUtil.REV_SYSTEM_TEMP;
				tempDelegateName = revision;
				originalIndex = PyramidIndexIO.getIndexFile(outTarget);
				if (!originalIndex.exists() && revKeepHist) {
					if (targetInfo.delegateVersionName != null) {
						throw new UnsupportedOperationException("Can't update a delegating dataset with keepHist option if the dataset doesn't already have an index because it would require re-building the index");
					}
					//save original index of target to its version dir
					TilesIndex lastIdx = PyramidIndexIO.buildFromDirs(resolvedCRS, originalDir, outputTypes[0]);
					PyramidIndexIO.saveIndexFile(lastIdx, originalIndex);
					filesCreated.add(originalIndex);
				}
			}

			//save new revision info to version dir
			PyramidUtil.saveVersionInfo(new RasterDatasetVersionInfo(tempRevName, revTime, tempDelegateName), outputDir);
			filesCreated.add(new File(outputDir, VERSION_INFO_FILE_NAME));
		}
	}

	public String getStatusString() {
		return progressText.toString();
	}

	public String getPerformanceString() {
		return processor == null ? "Not Started Processing" : processor.performanceInfoString();
	}

	public BufferedImage getStatusImage() {
		return processor == null ? null : processor.getProgressImage();
	}

	public void setReadFilesWhenPreparing(boolean readFilesWhenPreparing) {
		this.readFilesWhenPreparing = readFilesWhenPreparing;
	}
	
	public void stopProcessing() {
		cancelled = true;
		if (processor != null) {
			processor.cancel();
		}
	}
	
	public void setClearEnvelopes(List<Envelope> clearEnvs) {
		this.clearEnvelopes = clearEnvs;
	}
	
	public void setSubsamplingStrategy(RasterSubsamplingStrategy subsamplingStrategy) {
		this.subsamplingStrategy = subsamplingStrategy;
	}

	public static boolean safeDeleteDir(File origDir) throws IOException {
		if (origDir == null || !origDir.exists()) {
			return true;
		}
		if (!FileUtilJava.deleteIfEmpty(origDir, false, true)) {
			return renameOldTemp(origDir);
		}
		return true;
	}
	
	private static boolean renameOldTemp(File origDir) {
		int suffix = 1;
		File newDir;
		do {
			newDir = new File(origDir.getParentFile(), "."+origDir.getName()+"_"+suffix);
			suffix++;
		} while (newDir.exists());
		return FileUtilJava.renameWithRetry(origDir, newDir, 1000);
	}

	public boolean isUsingTemp() {
		return revData != null && revData.usingTemp;
	}
 }
