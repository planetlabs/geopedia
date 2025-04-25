package com.sinergise.java.raster.pyramid;

import static com.sinergise.common.geometry.tiles.TileUtilGWT.MAX_POSSIBLE_LEVEL_ID;
import static com.sinergise.common.geometry.tiles.TileUtilGWT.tileLevelCharFromZoomLevel;
import static com.sinergise.common.geometry.tiles.TileUtilGWT.zoomLevelIntFromChar;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.tiles.TileUtilGWT;
import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.geometry.tiles.TiledCRSMapping;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.common.raster.core.TilesIndex;
import com.sinergise.common.raster.core.VersionedRasterDataset;
import com.sinergise.common.raster.core.VersionedRasterDataset.RasterDatasetVersionInfo;
import com.sinergise.common.util.ImageUtil;
import com.sinergise.common.util.Util;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.common.util.io.IOUtil;
import com.sinergise.common.util.lang.TimeSpec;
import com.sinergise.common.util.lang.TimeSpec.Resolution;
import com.sinergise.common.util.settings.ResolvedType;
import com.sinergise.java.geometry.tiles.TileUtilJava;
import com.sinergise.java.raster.core.RasterIO;
import com.sinergise.java.raster.io.PyramidIndexIO;
import com.sinergise.java.util.io.FileUtilJava;
import com.sinergise.java.util.settings.ObjectStorage;


public class PyramidUtil {
	public static final String SRC_SHP_FILENAME = "source_images.shp";
	public static final String VERSION_INFO_FILE_NAME = "SGRasterVersionInfo.xml";
	public static final String EXTERNAL_VERSIONS_FILE_NAME = "ExternalVersions.xml";
	public static final String REV_SYSTEM_ACTIVE = "active";
	public static final String REV_SYSTEM_PROVISIONAL = "provisional";
	public static final String REV_SYSTEM_TEMP = "temp";
	public static final String REV_SUFFIX_DELTA = "_delta";
	public static final Pattern IGNORED_DIRS_PATTERN = Pattern.compile("^[_.].*");
	
	public static final class PyramidDirsInfo {
		public char tilePrefix;
		public int minLevel;
		public int maxLevel;
		public String[] imageTypes;
		public Boolean interlacedName = null;
	}

	public static final PyramidDirsInfo getDirInfo(File baseDir) {
		File[] levelDirs = baseDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return isVersionDir(pathname) && pathname.getName().matches("[A-Z][1-9A-Z]");
			}
		});
		if (levelDirs == null || levelDirs.length<1) return null;
		HashMap<Character, List<File>> freqMap = new HashMap<Character, List<File>>();
		List<File> maxList = null;
		for (File dir : levelDirs) {
			Character curCh = Character.valueOf(dir.getName().charAt(0));
			List<File> flsList = freqMap.get(curCh);
			if (flsList == null) freqMap.put(curCh, flsList = new ArrayList<File>());
			flsList.add(dir);
			if (maxList == null || maxList.size() < flsList.size()) maxList = flsList;
		}
		if (maxList == null) throw new IllegalArgumentException("");
		Collections.sort(maxList);
		
		PyramidDirsInfo ret = new PyramidDirsInfo();
		ret.minLevel = zoomLevelIntFromChar(maxList.get(0).getName().charAt(1));
		ret.maxLevel = zoomLevelIntFromChar(maxList.get(maxList.size()-1).getName().charAt(1));
		ret.tilePrefix = maxList.get(0).getName().charAt(0);
		ret.imageTypes = findFileTypes(baseDir, ret.tilePrefix, ret.minLevel);
		ret.interlacedName = checkNameInterlaced(baseDir, ret.tilePrefix, ret.minLevel, ret.maxLevel);
		return ret;
	}
	
	private static Boolean checkNameInterlaced(File baseDir, char tilePrefix, int minLevel, int maxLevel) {
		int[] testLevels = new int[]{minLevel+5, minLevel+6, minLevel+7, minLevel+9, minLevel+10, minLevel+11, minLevel+13, minLevel+14, minLevel+15};
		for (int i = 0; i < testLevels.length; i++) {
			int curLevel = testLevels[i];
			if (curLevel > maxLevel) break;
			Boolean ret = checkNamesInterlaced(
					new File(baseDir, tilePrefix + "" + tileLevelCharFromZoomLevel(curLevel))
					,""
					,TileUtilGWT.numCharsPerOrdinateForLevel(curLevel, minLevel)
					,1<<(curLevel-minLevel)-1
					,1<<(curLevel-minLevel)-1);
			if (ret != null) return ret;
		}
		return null;
	}

	private static Boolean checkNamesInterlaced(File dir, String prefix, int numChars, int maxTileW, int maxTileH) {
		String[] fls = dir.list();
		if (fls == null) return null;
		if (prefix.length() == 2*numChars-2) {
			//Plain tile files
			PointI pos = new PointI();
			for (String f : fls) {
				String tStr = f.substring(2,2+2*numChars);
				pos = TileUtilGWT.parseIndex2D(tStr, false, pos);
				if (pos.x > maxTileW || pos.y > maxTileH) return Boolean.TRUE;
				pos = TileUtilGWT.parseIndex2D(tStr, true, pos);
				if (pos.x > maxTileW || pos.y > maxTileH) return Boolean.FALSE;
			}
		} else {
			for (String fl : fls) {
				Boolean ret = checkNamesInterlaced(new File(dir, fl), prefix+fl, numChars, maxTileW, maxTileH);
				if (ret != null) return ret;
			}
		}
		return null;
	}

	public static final File fileFor(TiledCRS space, File baseDir, int level, int col, int row, String ext) {
		String tileId = TileUtilGWT.tileNameForColRow(space, level, col, row);
		File dir = getOutDir(baseDir, tileId);
		return new File(dir, tileId + ext);
	}

	public static final String fileForRelative(TiledCRS space, int level, int col, int row, String ext) {
		String tileId = TileUtilGWT.tileNameForColRow(space, level, col, row);
		return TileUtilGWT.tileDir(tileId) + File.separatorChar + tileId + ext;
	}

	public static final File getOutDir(File base, String tileId) {
		return new File(base, TileUtilGWT.tileDir(tileId));
	}

	public static String[] findFileTypes(File baseDir, char tileChar, int minLevel) {
		File levelDir = null;
		String levelStr = null;
		int curLevel = minLevel;
		do {
			levelStr = tileChar + "" + tileLevelCharFromZoomLevel(curLevel);
			levelDir = new File(baseDir, levelStr);
			curLevel++;
			if (curLevel > MAX_POSSIBLE_LEVEL_ID) break;
		} while (!levelDir.exists());
		if (levelDir == null || !levelDir.exists()) return findFileTypeDirs(baseDir);
		
		return findImageSuffixes(levelDir, levelStr);
	}

	private static String[] findImageSuffixes(File levelDir, String levelStr) {
		final String tStartStr = levelStr;
		String[] fls = levelDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(tStartStr);
			}
		});
		HashSet<String> suffixes = new HashSet<String>();
		for (String fl : fls) {
			suffixes.add(fl.substring(fl.lastIndexOf('.')+1));
		}
		if (!suffixes.isEmpty()) {
			return suffixes.toArray(new String[suffixes.size()]);
		}
		
		File[] dirs = levelDir.listFiles();
		for (File dir : dirs) {
			if (isVersionDir(dir) && dir.getName().length() == 2) {
				return findImageSuffixes(dir, levelStr);
			}
		}
		
		return new String[0];
	}

	private static String[] findFileTypeDirs(File baseDir) {
		if (!baseDir.exists()) return null;
		File[] subDirs = baseDir.listFiles();
		HashSet<String> suffixes = new HashSet<String>();
		for (File fl : subDirs) {
			if (!isVersionDir(fl)) continue;
			if (ImageUtil.isImageSuffix(fl.getName())) {
				suffixes.add(fl.getName());
			}
		}
		return suffixes.toArray(new String[suffixes.size()]);
	}

	public static String typesStringFromSubdirs(File pyramidBaseDir) {
		File[] subdirs = pyramidBaseDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return isVersionDir(f);
			}
		});
		if (subdirs == null || subdirs.length==0) return null;
		StringBuilder sb = new StringBuilder();
		sb.append(subdirs[0].getName());
		for (int i = 1; i < subdirs.length; i++) {
			sb.append(',').append(subdirs[i].getName());
		}
		return sb.toString();
	}
	
	public static void saveVersionInfo(RasterDatasetVersionInfo datasetInfo, OutputStream out) {
		if (Util.safeEquals(datasetInfo.versionName, datasetInfo.path)) {
			datasetInfo.path = null;
		}
		ObjectStorage.store("RasterDataset", datasetInfo, new ResolvedType<RasterDatasetVersionInfo>(RasterDatasetVersionInfo.class), out, false);
	}

	public static void saveVersionInfo(RasterDatasetVersionInfo datasetInfo, File versionBaseDir) throws IOException {
		FileOutputStream os = new FileOutputStream(new File(versionBaseDir, VERSION_INFO_FILE_NAME));
		try {
			saveVersionInfo(datasetInfo, os);
		} finally {
			os.close();
		}
	}
	
	public static RasterDatasetVersionInfo loadVersionInfo(InputStream in) throws TransformerException{
		return ObjectStorage.load(in, new RasterDatasetVersionInfo());
	}

	public static RasterDatasetVersionInfo loadVersionInfo(URL versionBaseURL) throws TransformerException, IOException, URISyntaxException {
		String urlStr = versionBaseURL.toExternalForm();
		String fullStr = urlStr.endsWith("/") ? urlStr + VERSION_INFO_FILE_NAME : urlStr + "/" + VERSION_INFO_FILE_NAME;
		InputStream is = null;
		try {
			if (fullStr.startsWith("file:/")) {
				File f = new File(new URL(fullStr).toURI());
				is = new FileInputStream(f);
			} else {
				is = new URL(fullStr).openStream();
			}
			return loadVersionInfo(is);
		} finally {
			IOUtil.closeSilent(is);
		}
	}
	

	public static RasterDatasetVersionInfo[] loadVersions(File baseDir) throws TransformerException, IOException {
		File[] dirs = baseDir.listFiles();
		HashMap<String, RasterDatasetVersionInfo> versions = new HashMap<String, RasterDatasetVersionInfo>();
		for (File f : dirs) {
			if (isVersionDir(f)) {
				try {
					RasterDatasetVersionInfo v = loadVersionInfo(f.toURI().toURL());
					if (versions.containsKey(v.versionName)) throw new IllegalStateException("Two version files with same versionName "+v.versionName);
					if (f.getName() != v.versionName) {
						v.path = f.getName();
					}
					versions.put(v.versionName, v);
				} catch (FileNotFoundException fnf) {
					//ignore
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		RasterDatasetVersionInfo[] extraVersions = loadExternalVersions(baseDir);
		if (extraVersions != null) {
			for (RasterDatasetVersionInfo v : extraVersions) {
				if (versions.containsKey(v.versionName)) throw new IllegalStateException("Two version files with same versionName "+v.versionName);
				versions.put(v.versionName, v);
			}
		}
		
		RasterDatasetVersionInfo[] ret = versions.values().toArray(new RasterDatasetVersionInfo[versions.size()]);
		Arrays.sort(ret, new VersionedRasterDataset.TimeComparator());
		return ret;
	}

	public static boolean isVersionDir(File f) {
		return f.isDirectory() && !IGNORED_DIRS_PATTERN.matcher(f.getName()).matches();
	}
	
	/**
	 * 
	 * @param fileOrDir
	 * @return
	 * @throws IOException
	 * @throws TransformerException
	 */
	public static TiledCRS findCRS(final File fileOrDir) throws IOException, TransformerException {
		if (isVersionDir(fileOrDir)) {
			{
				File csFile = new File(fileOrDir,TileUtilGWT.FILENAME_TILEDCRS);
				if (!csFile.exists()) csFile = new File(fileOrDir.getParentFile(), TileUtilGWT.FILENAME_TILEDCRS);
				if (csFile.exists()) return TileUtilJava.load(csFile);
			}
			String[] csCandidates = fileOrDir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith("tiles.xml") || name.toLowerCase().endsWith("tiledcrs.xml");
				}
			});
			if (csCandidates != null && csCandidates.length>0) {
				return TileUtilJava.load(new File(fileOrDir, csCandidates[0]));
			}
			String[] zoomDirs = fileOrDir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.matches("[A-Z][0-9A-Z]");
				}
			});
			if (zoomDirs != null && zoomDirs.length > 0) {
				PyramidUtil.PyramidDirsInfo dirsInfo = PyramidUtil.getDirInfo(fileOrDir);
				TiledCRS cs = TiledCRSMapping.INSTANCE.getByPrefix(dirsInfo.tilePrefix);
				if (cs != null) return cs;
				
				String[] fileType = findFileTypes(fileOrDir, dirsInfo.tilePrefix, dirsInfo.minLevel);
				DimI tileSize = findTileSize(fileOrDir, dirsInfo.tilePrefix, dirsInfo.minLevel, fileType[0]);
				
				WithBounds retCS = WithBounds.createDefault(
						CRS.NONAME_WORLD_CRS, "Default TiledCRS for "+fileOrDir.getAbsolutePath(),
						dirsInfo.minLevel, dirsInfo.maxLevel, tileSize);
				if (dirsInfo.interlacedName != null) {
					retCS.setInterlacedName(dirsInfo.interlacedName.booleanValue());
				}
				retCS.setTilePrefixChar(dirsInfo.tilePrefix);
				return retCS;
			}
			
			throw new IllegalArgumentException("Could not determine TiledCRS for the provided directory");
		}
		return TileUtilJava.load(fileOrDir);
	}
	

	private static DimI findTileSize(File baseDir, char tileChar, int minLevel, String fileType) throws IOException {
		String levelStr = tileChar + "" + tileLevelCharFromZoomLevel(minLevel);
		baseDir = new File(baseDir, levelStr);
		File f = new File(baseDir, levelStr+"00."+fileType.toLowerCase());
		if (!f.exists()) {
			f = new File(baseDir, levelStr+"00."+fileType.toUpperCase());
		}
		return RasterIO.readImageSize(f.toURI().toURL());
	}
	
	public static final RasterDatasetVersionInfo[] loadExternalVersions(File baseDir) throws IOException, TransformerException {
		File vFile = new File(baseDir, EXTERNAL_VERSIONS_FILE_NAME);
		if (!vFile.exists()) return null;
		FileInputStream fis = new FileInputStream(vFile);
		try {
			return loadExternalVersions(fis);
		} finally {
			fis.close();
		}
	}
	
	public static RasterDatasetVersionInfo[] loadExternalVersions(InputStream is) throws TransformerException {
		return ObjectStorage.load(is, new ResolvedType<RasterDatasetVersionInfo[]>(RasterDatasetVersionInfo[].class));
	}

	public static void main(String[] args) {
		RasterDatasetVersionInfo info = new RasterDatasetVersionInfo("2007-10", new TimeSpec("2007-10"), null);
		try {
			saveVersionInfo(info, new File("D:/Data/GeoData/slo/dof/out/last"));
			info = loadVersionInfo(new URL("file:///D:/Data/GeoData/slo/dof/out/last"));
			System.out.println(info);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static RasterDatasetVersionInfo loadVersionInfo(URL datasetBaseURL, String versionName) throws IOException, TransformerException, URISyntaxException {
		String urlStr = datasetBaseURL.toExternalForm();
		String fullStr = urlStr.endsWith("/") ? urlStr + versionName : urlStr + "/" + versionName;
		
		RasterDatasetVersionInfo origInfo = loadVersionInfo(new URL(fullStr));
		if (origInfo == null) {
			fullStr = urlStr.endsWith("/") ? urlStr + REV_SYSTEM_ACTIVE : urlStr + "/" + REV_SYSTEM_ACTIVE;
			URL newURL = new URL(fullStr);
			RasterDatasetVersionInfo newInfo = loadVersionInfo(newURL);
			if (newInfo != null && versionName.equals(newInfo.versionName)) {
				newInfo.path = REV_SYSTEM_ACTIVE;
				return newInfo;
			}
			return null;
		}
		return origInfo;
	}
	
	public static String timeSpecToVersionName(TimeSpec spc) {
		if (spc.getResolution().ordinal() < Resolution.HOUR.ordinal()) return spc.toISOString();
		String isoStr = spc.toISOString();
		return isoStr.replace(':', '_').replace('.', '_');
	}
	
	public static List<String> mergePyramids(File srcDir, File tgtDir, File histDir) throws Exception {
		FileUtilJava.forceMkDir(histDir);
		
		RasterDatasetVersionInfo tgtInfo = loadVersionInfo(tgtDir.toURI().toURL());
		RasterDatasetVersionInfo newInfo = loadVersionInfo(srcDir.toURI().toURL());

		File tgtIndex = PyramidIndexIO.getIndexFile(tgtDir);
		if (!tgtIndex.exists()) {
			TiledCRS cs = findCRS(tgtDir);
			String[] typs = findFileTypes(tgtDir, cs.getTilePrefixChar(), cs.getMinLevelId());
			PyramidIndexIO.buildAndSave(cs, tgtDir, typs[0]);
		}
		FileUtilJava.forceRename(tgtIndex, PyramidIndexIO.getIndexFile(histDir));
		newInfo.versionName = tgtInfo.versionName;
		newInfo.delegateVersionName = tgtInfo.delegateVersionName;
		tgtInfo.versionName = histDir.getName();
		tgtInfo.delegateVersionName = newInfo.versionName;
		saveVersionInfo(tgtInfo, histDir);
		saveVersionInfo(newInfo, tgtDir);

		List<String> ret = RasterProcessor.moveTiles(srcDir, tgtDir, histDir, false);
		FileUtilJava.deleteIfEmpty(srcDir, false, true);
		return ret;
	}

	public static void renameVersion(File base, String oldVersionName, String newVersionName) throws Exception {
		VersionedRasterDataset vd = VersionedTilesProvider.load(base);
		File oldBaseDir = new File(vd.getAbsoluteURL(oldVersionName).toURI());
		RasterDatasetVersionInfo info = vd.getVersionInfo(oldVersionName);
		info.versionName = newVersionName;
		info.path = null;
		//TODO: change name where other versions delegate to this
		File newBaseDir = new File(base, newVersionName);
		if (newBaseDir.exists()) throw new IllegalArgumentException("Directory already exists ("+newBaseDir+")");
		if (FileUtilJava.renameWithRetry(oldBaseDir, newBaseDir, 1000)) {
			PyramidUtil.saveVersionInfo(info, newBaseDir);
		} else {
			throw new IOException("Failed to rename directory "+oldBaseDir+" to "+newBaseDir); 
		}
	}

	public static boolean isVersionSystem(String versionName) {
		if (versionName.equals(REV_SYSTEM_ACTIVE)) {
			return true;
		}
		if (versionName.equals(REV_SYSTEM_PROVISIONAL)) {
			return true;
		}
		if (versionName.equals(REV_SYSTEM_TEMP)) {
			return true;
		}		
		return false;
	}

	public static boolean isVersionDelta(String versionName) {
		return versionName.endsWith(REV_SUFFIX_DELTA);
	}

	public static void materializeDelegated(File base, String version, File out, String[] outTypes, String crsSpec){
		VersionedTilesProvider vtp;
		try {
			
			// first copy metadata
			RasterDatasetVersionInfo tgtVerInfo = loadVersionInfo(base.toURI().toURL(), version);
			if(!out.exists()) out.mkdir();
			saveVersionInfo(tgtVerInfo, out);
			
			// get existing index
			File indexFile = new File(base.getAbsoluteFile()+"/"+ version +"/index.zip");
			TilesIndex index = PyramidIndexIO.loadIndexFile(indexFile);

			// COPY
			vtp = new VersionedTilesProvider(base);
			File outDir;
			GPTilesProvider tp;
			File writeFile;
			File writeDir;
			File tile;
			
			List<File> temp = Arrays.asList(out);
			TiledCRS cs = TileUtilJava.resolveCRS(temp, "S");
			
			// for each type
			for (String type : outTypes) {
				outDir = new File(out, type);
				if(!outDir.exists()) outDir.mkdir();
				tp = vtp.getForVersion(version, type);
				
				
				

				// copy whole delegated pyramid
				for(int level = index.getMinIndexedLevel(); level<=index.getMaxIndexedLevel(); level++) {
					System.out.println("\n\nMaterializing level: " + level);
					for(int row = 0; row <= index.maxTileRow(level); row++) {
						for(int col = 0; col <= index.maxTileColumn(level); col++) {							
							if(col%99==0) System.out.print("\rLine: " + (row+1) + " Column: " + (col+1));
							
							if(index.hasTile(level, row, col)) {
								// copy tile files
								tile = tp.getTileFile(level, col, row);
								if(tile==null) {
									System.out.println("\nNo tile: lvl:" + level +", x:" + col + " y:" + row);
									throw new RuntimeException();
								}
								writeFile = fileFor(cs, outDir, level, col, row, "." + type);
								writeDir = writeFile.getParentFile(); 
								if(!writeDir.exists()) { 
									writeDir.mkdirs();	
								}
								FileUtilJava.copyFile(tile, writeFile);								
							}							
						}
					}					
				}
				
				System.out.println("\n\nMaterializing done!");
			}
			
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	


	
}
