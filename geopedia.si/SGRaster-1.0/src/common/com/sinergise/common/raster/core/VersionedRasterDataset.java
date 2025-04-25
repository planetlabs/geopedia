package com.sinergise.common.raster.core;

import static com.sinergise.java.raster.pyramid.PyramidUtil.REV_SUFFIX_DELTA;
import static com.sinergise.java.raster.pyramid.PyramidUtil.REV_SYSTEM_ACTIVE;
import static com.sinergise.java.raster.pyramid.PyramidUtil.REV_SYSTEM_PROVISIONAL;
import static com.sinergise.java.raster.pyramid.PyramidUtil.REV_SYSTEM_TEMP;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.util.lang.TimeSpec;
import com.sinergise.common.util.settings.Settings;
import com.sinergise.java.raster.pyramid.PyramidUtil;

public class VersionedRasterDataset implements Settings {
	public static class TimeComparator implements Comparator<RasterDatasetVersionInfo> {
		@Override
		public int compare(RasterDatasetVersionInfo o1, RasterDatasetVersionInfo o2) {
			return o1.versionTime.compareTo(o2.versionTime);
		}
	}
	
	public static class RasterDatasetVersionInfo implements Settings {
		public String versionName;
		public TimeSpec versionTime;
		public String delegateVersionName;
		public String path;
		
		public RasterDatasetVersionInfo() {
		}
		
		public RasterDatasetVersionInfo(String name, TimeSpec time, String delegateName) {
			this(name, time, delegateName, null);
		}

		public RasterDatasetVersionInfo(String name, TimeSpec time, String delegateName, String path) {
			this.versionName = name;
			this.versionTime = time;
			this.delegateVersionName = delegateName;
			this.path = path;
		}

		
		@Override
		public String toString() {
			return versionName + " ("+versionTime+") --> "+delegateVersionName;
		}

		public String getLastVersionPathComponent() {
			if (path == null) return versionName;
			if (path.endsWith("/")) return path.substring(path.lastIndexOf('/', path.length()-2)+1,path.length()-1);
			return path.substring(path.lastIndexOf('/')+1);
		}
		
		public boolean isSystem() {
			return PyramidUtil.isVersionSystem(versionName);
		}
		
		public boolean isDelta() {
			return PyramidUtil.isVersionDelta(versionName);
		}

		public String getPath(String basePath) {
			if (path != null) {
				try {
					return getAbsoluteURL(new File(basePath).toURI().toURL(), this).getPath();
				} catch(MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
			return basePath + "/" + getLastVersionPathComponent();
		}
	}
	public final URL baseURL;
	public final TiledCRS cs;
	public final String[] types;
	transient private String activeVersionName = null;
	private final HashMap<String, RasterDatasetVersionInfo> versionNamesMap = new HashMap<String, RasterDatasetVersionInfo>();
	public final String[] versionNames;
	
	public VersionedRasterDataset(URL baseURL, TiledCRS cs, String[] fileTypes, RasterDatasetVersionInfo[] versions) {
		this.cs = cs;
		this.baseURL = baseURL;
		this.types = fileTypes;
		if (fileTypes == null) throw new NullPointerException("Null fileTypes for VersionedRasterDataset");
		if (baseURL == null) throw new NullPointerException("Null baseURL for VersionedRasterDataset");
		
		versionNames = new String[versions.length];
		int i=0;
		for (RasterDatasetVersionInfo info : versions) {
			versionNamesMap.put(info.versionName, info);
			versionNames[i++] = info.versionName;
		}
	}

	public RasterDatasetVersionInfo getVersionInfo(String versionName) {
		checkTransient();
		if (REV_SYSTEM_ACTIVE.equals(versionName)) {
			return versionNamesMap.get(activeVersionName);
		}
		return versionNamesMap.get(versionName);
	}
	
	public RasterDatasetVersionInfo getVersionInfoForTime(String timeString) {
		checkTransient();
		for (RasterDatasetVersionInfo ver : versionNamesMap.values()) {
			if (ver.versionTime.toISOString().equals(timeString)) return ver;
		}
		return null;
	}

	public boolean isActive(String versionName) {
		if (REV_SYSTEM_ACTIVE.equals(versionName)) return true;
		checkTransient();
		if (activeVersionName == null) return false;
		return activeVersionName.equals(versionName);
	}

	protected void checkTransient() {
		if (activeVersionName != null) {
			return;
		}
		if (versionNames == null || versionNames.length == 0) {
			return;
		}
		
		//Set the last non-provisional and non-temp value as active; return 'active' if found
		for (String str : versionNames) {
			if (!REV_SYSTEM_PROVISIONAL.equals(str) && 
					!REV_SYSTEM_TEMP.equals(str) &&
					!(str != null && str.endsWith(REV_SUFFIX_DELTA))) {
				activeVersionName = str;
			}
			if (REV_SYSTEM_ACTIVE.equals(activeVersionName)) {
				return;
			}
		}
	}

	public boolean hasType(String fileType) {
		for (String t : types) {
			if (t.equals(fileType)) return true;
		}
		return false;
	}

	public String[] getVersionNames() {
		return versionNames;
	}

	public URL getAbsoluteURL(String versionName) throws MalformedURLException {
		URL ret = getAbsoluteURL(baseURL, getVersionInfo(versionName));
		if (ret != null) {
			return ret;
		}
		return new URL(baseURL, versionName);
	}
	public static URL getAbsoluteURL(URL baseURL, RasterDatasetVersionInfo vInfo) throws MalformedURLException {
		if (vInfo == null) return null;
		if (vInfo.path == null) return new URL(baseURL, vInfo.versionName);
		return new URL(baseURL, vInfo.path);
	}

	public String getActiveVersionName() {
		checkTransient();
		return activeVersionName;
	}
	
	public String latestVersionName() {
		return versionNames[versionNames.length-1];
	}
}
