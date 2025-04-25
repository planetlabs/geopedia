package com.sinergise.java.geometry.io.shp;

import java.io.File;
import java.io.IOException;

import com.sinergise.common.util.io.FileUtil;
import com.sinergise.java.util.io.FileUtilJava;

public class ShpUtil {
	
	public static void copyShapefile(File srcShp, File tgtShp) throws IOException {
		String prefix = FileUtil.getNameNoSuffix(srcShp.getName());
		File srcDir = srcShp.getParentFile();
		File tgtDir = tgtShp.getParentFile();
		for (File f : srcDir.listFiles()) {
			if (prefix.equals(FileUtil.getNameNoSuffix(f.getName()))) {
				FileUtilJava.copyFile(f, new File(tgtDir, f.getName()));
			}
		}
	}
	
	public static void renameShapefile(File srcShp, File tgtShp) throws IOException {
		String prefix = FileUtil.getNameNoSuffix(srcShp.getName());
		File srcDir = srcShp.getParentFile();
		File tgtDir = tgtShp.getParentFile();
		for (File f : srcDir.listFiles()) {
			if (prefix.equals(FileUtil.getNameNoSuffix(f.getName()))) {
				FileUtilJava.forceRename(f, new File(tgtDir, f.getName()));
			}
		}
	}

	public static void deleteShapefile(File shp) throws IOException {
		String prefix = FileUtil.getNameNoSuffix(shp.getName());
		File dir = shp.getParentFile();
		for (File f : dir.listFiles()) {
			if (prefix.equals(FileUtil.getNameNoSuffix(f.getName()))) {
				FileUtilJava.forceDelete(f);
			}
		}
	}
}
