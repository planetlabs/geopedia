package com.sinergise.java.raster.misc;

import java.awt.Color;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.transform.TransformerException;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.raster.core.TilesIndex;
import com.sinergise.common.raster.core.VersionedRasterDataset;
import com.sinergise.common.raster.core.VersionedRasterDataset.RasterDatasetVersionInfo;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.common.util.io.FileUtil;
import com.sinergise.common.util.lang.TimeSpec;
import com.sinergise.java.raster.io.PyramidIndexIO;
import com.sinergise.java.raster.pyramid.GPTilesProvider;
import com.sinergise.java.raster.pyramid.PyramidUtil;
import com.sinergise.java.raster.pyramid.RasterProcessor;
import com.sinergise.java.raster.pyramid.VersionedTilesProvider;
import com.sinergise.java.util.UtilJava;
import com.sinergise.java.util.io.FileUtilJava;

public class UpdateVersionedPyramid {
	static {
		UtilJava.initStaticUtils();
	}
	
	@Argument(index = 0, required = true, metaVar = "<src>", usage = "either:\n-file containing the list of source images, or\n-a directory containing the images")
	public File sourcesFile;

	@Argument(index = 1, required = true, metaVar = "<base_dir>", usage = "output directory, can contain existing image pyramid to be updated")
	public File baseDir;
	
	@Argument(index = 2, required = false, metaVar = "<out_types>", usage = "output image type(s), separated by comma (e.g. tif,jpg or tif)")
	public String outputTypesString = null;

	@Option(name = "-crs", required = false, multiValued = false, metaVar = "C", usage = "either:"
		+ "\n- coordinate reference system code character, or" + "\n- path to the xml file describing the tiledCRS")
	public String crsSpec = null;

	@Option(name = "-trans", required = false, multiValued = false, usage = "flag that produces transparent output tiles (if the output type supports it)")
	public boolean doTransparent = false;

	@Option(name = "-bgColor", required = false, multiValued = false, usage = "color in AARRGGBB hexadecimal format")
	public Color bgColor = null;

	@Option(name = "-s", required = false, multiValued = false, usage = "initial zoom level (default is maximum of the tiledCRS)")
	public int userScale = Integer.MIN_VALUE;

	@Option(name = "-transColor", required = false, multiValued = false, usage = "color to be replaced with transparency")
	public Color transColor = null;

	@Option(name = "-transSmooth", required = false, multiValued = false, usage = "when replacing with transparent this flag will cause the background color to be subtracted from the whole image")
	public boolean transSmooth = false;

	@Option(name = "-noUI", required = false, multiValued = false, usage = "don't display the progress user interface")
	public boolean noUI = false;

	@Option(name = "-revTime", required = false, multiValued = false, usage = "the reference timestamp of the revision, in ISO 8601 extended format (YYYY-MM-DDThh:mm:ss.sss); ")
	public TimeSpec revTime = null;
	
	@Option(name = "-interpolate", required = false, multiValued = false, usage = "use bilinear interpolation when scaling images (default false)")
	public boolean interpolate = false;

	@Option(name = "-originalVersion", required = false, multiValued = false, usage = "the reference timestamp of the revision, in ISO 8601 extended format (YYYY-MM-DDThh:mm:ss.sss); ")
	public String originalVersion = null;
	
	@Option(name = "-originalDeltaVersion", required = false, multiValued = false, usage = "if set, the process will create additional combined version of the current sources' difference and the specified difference of the previous version")
	public String originalDeltaVersion = null;
	
	
	public UpdateDOF updater = new UpdateDOF();
	protected RasterDatasetVersionInfo versionToUpdate;
	protected RasterDatasetVersionInfo newVersion;
	protected RasterDatasetVersionInfo deltaVersion;
	protected RasterDatasetVersionInfo deltaCombinedVersion;
	
	public boolean commit = true;
	
	public void go() throws Exception {
		prepare();
		updater.go();
		commit();
	}

	public void prepare() throws IOException {
		VersionedRasterDataset vrd = null;
		if (baseDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		}).length > 0) {
			try {
				vrd = VersionedTilesProvider.load(baseDir);
			} catch (TransformerException t) {
				throw new IOException(t);
			}
		}

		updater.bgColor = bgColor;
		updater.crsSpec = crsSpec;
		updater.dirsForTypes = true;
		updater.doTransparent = doTransparent;
		updater.interpolate = interpolate;
		updater.noUI = noUI;
		updater.outputTypesString = outputTypesString;
		updater.recursive = true;
		updater.skipEmpty = true;
		updater.sourcesFile = sourcesFile;
		updater.transColor = transColor;
		updater.transSmooth = transSmooth;
		updater.userScale = userScale;
		
		if (vrd == null) {
			updater.resolveInputFiles();
			updater.resolveOutputTypes();
			try {
				vrd = VersionedTilesProvider.load(baseDir, null, null);
			} catch(Exception e) {
				vrd = new VersionedRasterDataset(baseDir.toURI().toURL(), updater.resolveCRS(), updater.outputTypes, new RasterDatasetVersionInfo[0]);
			}
		}
		updater.resolvedCRS = vrd.cs;
		updater.outputTypes = vrd.types;

		String newVersionName = PyramidUtil.timeSpecToVersionName(revTime);
		newVersion = new RasterDatasetVersionInfo(newVersionName, revTime, null);

		if (!ArrayUtil.isNullOrEmpty(vrd.versionNames)) {
			if (originalVersion == null) {
				versionToUpdate = vrd.getVersionInfo(vrd.getActiveVersionName());
			} else {
				versionToUpdate = vrd.getVersionInfo(originalVersion);
			}
			updater.originalDir = new File(versionToUpdate.getPath(baseDir.getAbsolutePath()));
			newVersion.delegateVersionName = versionToUpdate.versionName;

//			File originalIndex = PyramidIndexIO.getIndexFile(updater.originalDir);
//			if (!originalIndex.exists()) {
//				//save original index of target to its version dir
//				TilesIndex lastIdx = PyramidIndexIO.buildFromDirs(updater.resolvedCRS, updater.originalDir, updater.outputTypes[0]);
//				PyramidIndexIO.saveIndexFile(((QuadIdxBuilder)lastIdx).createPacked(), originalIndex);
//			}
		}
		
		VersionedTilesProvider.save(baseDir, newVersion);

		// BASIC DELTA		
		String deltaVersionName = newVersionName + PyramidUtil.REV_SUFFIX_DELTA;
		deltaVersion = new RasterDatasetVersionInfo(deltaVersionName, revTime, newVersionName);
		VersionedTilesProvider.save(baseDir, deltaVersion);
		
		// COMBINED DELTA		
		if(originalDeltaVersion!=null) {
			String deltaCombinedVersionName = getCombinedDeltaName(newVersionName);
			deltaCombinedVersion = new RasterDatasetVersionInfo(deltaCombinedVersionName, revTime, newVersionName);
			VersionedTilesProvider.save(baseDir, deltaCombinedVersion);
		}
		

		updater.outputDir = new File(newVersion.getPath(baseDir.getAbsolutePath()));
		updater.deltaDir = new File(deltaVersion.getPath(baseDir.getAbsolutePath()));
		
		
		if(deltaCombinedVersion!=null) {
			updater.deltaCombinedDir = new File(deltaCombinedVersion.getPath(baseDir.getAbsolutePath()));
			updater.deltaBase = new File(baseDir.getAbsolutePath() + "/" + originalDeltaVersion + "/");
		}
	}

	protected String getCombinedDeltaName(String newVersionName) {
		String res = originalDeltaVersion;
		// remove suffix
		int index = res.lastIndexOf(PyramidUtil.REV_SUFFIX_DELTA);
		if (index > 1) res = res.substring(0, index);
		res = newVersionName + "_" + res + PyramidUtil.REV_SUFFIX_DELTA;
		return res;
	}
	
	public void commit() throws IOException {
		if (!commit) {
			return;
		}
		try {
			commit(baseDir, versionToUpdate, newVersion);
		} catch(TransformerException e) {
			throw new IOException(e);
		}
	}
	public static void commit(File baseDir, RasterDatasetVersionInfo versionToUpdate, RasterDatasetVersionInfo newVersion) throws IOException, TransformerException {
		TiledCRS cs = PyramidUtil.findCRS(baseDir);
		File newVersionDir = new File(newVersion.getPath(baseDir.getAbsolutePath()));
		String[] fTypes = null;
		if (versionToUpdate != null) {
			File originalDir = new File(versionToUpdate.getPath(baseDir.getAbsolutePath()));
			fTypes = PyramidUtil.findFileTypes(originalDir, cs.getTilePrefixChar(), cs.getMinLevelId());
			
			moveTilesToNewVersion(originalDir, newVersionDir);

			versionToUpdate.delegateVersionName = newVersion.versionName;
			VersionedTilesProvider.save(baseDir, versionToUpdate);
		} else {
			fTypes = PyramidUtil.findFileTypes(newVersionDir, cs.getTilePrefixChar(), cs.getMinLevelId());
		}
		newVersion.delegateVersionName = null;
		VersionedTilesProvider.save(baseDir, newVersion);
		PyramidIndexIO.buildAndSave(cs, newVersionDir, fTypes[0]);
	}

	private static void moveTilesToNewVersion(File originalDir, File outputDir) {
		try {
			RasterProcessor.moveTilesNoBkp(originalDir, outputDir, false);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
//		File tmpDir = new File(updater.outputDir.getParentFile(), "temp");
//		updater.outputDir.renameTo(tmpDir);
//		updater.originalDir.renameTo(updater.outputDir);
//		updater.originalDir.mkdir();
//		RasterProcessor.moveTiles(tmpDir, updater.outputDir, updater.originalDir, true);
	}

	public static void reverseDelegation(File baseDir, String newMaterializedVersion) throws IOException, TransformerException, URISyntaxException {
		VersionedTilesProvider vtp = new VersionedTilesProvider(baseDir);
		VersionedRasterDataset vrd = vtp.getInfo();
		RasterDatasetVersionInfo targetInfo = vrd.getVersionInfo(newMaterializedVersion);
		if (targetInfo.delegateVersionName == null) {
			return;
		}
		RasterDatasetVersionInfo srcInfo = vrd.getVersionInfo(targetInfo.delegateVersionName);
		if (srcInfo.delegateVersionName != null) {
			throw new IllegalArgumentException("Cannot reverse delegation if source version delegates (to "+srcInfo.delegateVersionName+")");
		}
		
		File srcDir = new File(vrd.getAbsoluteURL(srcInfo.versionName).toURI());
		File targetDir = new File(vrd.getAbsoluteURL(targetInfo.versionName).toURI());
		TilesIndex tgtIndex = GPTilesProvider.WithDelegate.loadIndex(targetDir);
		copyTiles(srcDir, targetDir, vrd.cs, tgtIndex);
		
		targetInfo.delegateVersionName = null;
		srcInfo.delegateVersionName = targetInfo.versionName;
		
		PyramidUtil.saveVersionInfo(targetInfo, targetDir);
		PyramidUtil.saveVersionInfo(srcInfo, srcDir);
	}

	private static void copyTiles(File srcF, File tgtF, TiledCRS cs, TilesIndex index) throws IOException {
		if (srcF.isDirectory()) {
			System.out.println("Going into "+srcF.getAbsolutePath());
			for (File f : srcF.listFiles()) {
				copyTiles(f, new File(tgtF, f.getName()), cs, index);
			}
		}
		PointI tPos = new PointI();
		if (srcF.getName().matches("[A-Z][0-9A-Z][0-9A-F]+\\.(jpg|png|gif|tif)")) {
			String tName = FileUtil.getNameNoSuffix(srcF.getName());
			int level = TileUtilGWT.parseTileSpec(cs, tName, tPos);
			if (index.hasTile(level, tPos.y, tPos.x) && !(tgtF.exists())) {
				FileUtilJava.forceMkDirs(tgtF.getParentFile());
				FileUtilJava.forceRename(srcF, tgtF);
			}
		}
	}
}
