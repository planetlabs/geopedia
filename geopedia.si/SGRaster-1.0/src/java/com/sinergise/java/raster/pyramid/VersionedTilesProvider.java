package com.sinergise.java.raster.pyramid;

import static com.sinergise.common.util.ArrayUtil.isNullOrEmpty;
import static com.sinergise.java.raster.pyramid.PyramidUtil.REV_SYSTEM_ACTIVE;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.transform.TransformerException;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.raster.core.VersionedRasterDataset;
import com.sinergise.common.raster.core.VersionedRasterDataset.RasterDatasetVersionInfo;
import com.sinergise.common.util.lang.TimeSpec;
import com.sinergise.java.util.io.FileUtilJava;

public class VersionedTilesProvider {
	
	public static void save(File baseDir, RasterDatasetVersionInfo version) throws IOException {
		File f = new File(version.getPath(baseDir.getAbsolutePath()));
		FileUtilJava.forceMkDir(f);
		PyramidUtil.saveVersionInfo(version, f);
	}
	
	public static final VersionedRasterDataset load(File baseDir) throws IOException, TransformerException {
		return load(baseDir, null, null);
	}
	public static final VersionedRasterDataset load(File baseDir, TiledCRS externalCS, String[] defaultFileTypes) throws IOException, TransformerException {
		try {
			URL url = baseDir.toURI().toURL();
			TiledCRS cs = resolveCS(baseDir, externalCS);
			RasterDatasetVersionInfo[] versions = PyramidUtil.loadVersions(baseDir);
			
			if (versions.length == 0) { //Try to make a non-versioned dataset
				versions = new RasterDatasetVersionInfo[]{new RasterDatasetVersionInfo(null, TimeSpec.createForCurrentDate(), null, ".")};
			}
			
			String[] fileTypes = new String[0];
			for (int i = 0; i < versions.length; i++) {
				URL verURL = VersionedRasterDataset.getAbsoluteURL(url, versions[i]);
				fileTypes = PyramidUtil.findFileTypes(new File(verURL.toURI()), cs.getTilePrefixChar(), cs.getMinLevelId());
				if (!isNullOrEmpty(fileTypes)) {
					break;
				}
			}
			if (isNullOrEmpty(fileTypes)) {
				if (isNullOrEmpty(defaultFileTypes)) {
					throw new NullPointerException("Null or empty defaultFileTypes");
				}
				fileTypes = defaultFileTypes;
			}
			return new VersionedRasterDataset(url, cs, fileTypes, versions);
			
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}
	public static TiledCRS resolveCS(File baseDir, TiledCRS externalCS) {
		TiledCRS cs = externalCS;
		try {
			TiledCRS intCS = PyramidUtil.findCRS(baseDir);
			if (intCS != null) {
				cs = intCS;
			}
		} catch (Exception e) {
			if (cs == null) {
				throw new IllegalArgumentException("CRS not found in '"+baseDir+"'. Should be provided externally.", e);
			}
		}
		return cs;
	}
	
	protected VersionedRasterDataset info;
	protected final File baseDir;
	protected final HashMap<String, GPTilesProvider> providers = new HashMap<String, GPTilesProvider>();
	
	public VersionedTilesProvider(File baseDir) throws IOException, TransformerException {
		this.baseDir = baseDir;
		reload();
	}
	
	public boolean hasVersion(String versionName) {
		return info.getVersionInfo(versionName) != null;
	}
	
	public GPTilesProvider getForVersion(String versionName, String fileType) throws IOException {
		GPTilesProvider ret = providers.get(versionName+"/"+fileType);
		if (ret != null) {
			return ret;
		}
		if (!info.hasType(fileType)) {
			throw new IllegalArgumentException("Type not available: "+fileType);
		}
		try {
			RasterDatasetVersionInfo verInfo = info.getVersionInfo(versionName);
			if (verInfo == null) {
				throw new IllegalArgumentException("Version not found ("+versionName+")");
			}
			
			File dir = new File(info.getAbsoluteURL(versionName).toURI());
			if (verInfo.delegateVersionName == null) {
				ret = new GPTilesProvider(dir, info.cs, fileType);
			} else {
				ret = new GPTilesProvider.WithDelegate(dir, info.cs, fileType, getForVersion(verInfo.delegateVersionName, fileType));
			}
			// Use original name to add...
			providers.put(verInfo.versionName+"/"+ret.getFileType(), ret);
			if (info.isActive(versionName)) {
				providers.put(REV_SYSTEM_ACTIVE+"/"+ret.getFileType(), ret);
			}
			return ret;
		} catch(URISyntaxException e) {
			throw (IOException)(new IOException("Failed to read version info").initCause(e));
		}
	}

	public VersionedRasterDataset getInfo() {
		return info;
	}

	public void scanForVersionChange() throws TransformerException, IOException {
		String[] oldVers = info.getVersionNames();
		boolean changed = false;
		for (String ver : oldVers) {
			RasterDatasetVersionInfo verInfo = info.getVersionInfo(ver);
			try {
				URL verBaseURL = info.getAbsoluteURL(verInfo.versionName);
				File verBaseDir = new File(verBaseURL.toURI());
				if (!verBaseDir.isDirectory()) {
					changed = true;
					break;
				}
			} catch(URISyntaxException e) {
				e.printStackTrace();
			}
		}
		if (!changed) {
			File[] dirs = baseDir.listFiles();
			for (File f : dirs) {
				if (f.isDirectory()) {
					File verInfoXML = new File(f, PyramidUtil.VERSION_INFO_FILE_NAME);
					if (verInfoXML.exists()) {
						if (info.getVersionInfo(f.getName()) == null) {
							changed = true;
							break;
						}
					}
				}
			}
		}
		if (changed) reload();
	}

	public void reload() throws TransformerException, IOException {
		providers.clear();
		String[] defTypes = info == null ? null : info.types;
		TiledCRS defCRS = info == null ? null : info.cs;
		info = load(baseDir, defCRS, defTypes);
	}
}
