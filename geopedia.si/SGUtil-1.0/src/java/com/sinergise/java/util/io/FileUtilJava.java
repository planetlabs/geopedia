package com.sinergise.java.util.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.io.FileUtil;
import com.sinergise.common.util.io.IOUtil;
import com.sinergise.common.util.lang.SGCallable;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.java.util.ExecutionUtilJava;
import com.sinergise.java.util.UtilJava;

public class FileUtilJava extends FileUtil {
	static {
		UtilJava.initStaticUtils();
	}

	public static boolean deleteIfEmpty(final File dir, final boolean moveUp, final boolean moveDown)
		throws IOException {
		if (!dir.exists()) {
			return true;
		}
		if (dir.isFile()) {
			return false;
		}
		final String[] contents = dir.list();
		if (!ArrayUtil.isNullOrEmpty(contents)) {
			if (!moveDown) {
				return false;
			}
			for (String sub : contents) {
				if (!deleteIfEmpty(new File(dir, sub), false, true)) {
					return false;
				}
			}
		}
		forceDelete(dir);
		if (moveUp) {
			deleteIfEmpty(dir.getParentFile(), true, false);
		}
		return true;
	}

	public static void deleteDir(final File directory, final boolean contentsOnly) throws IOException {
		if (directory == null) {
			return;
		}

		if (directory.isDirectory()) {
			for (final File f : directory.listFiles()) {
				if (f.isDirectory()) {
					deleteDir(f);
				} else {
					forceDelete(f);
				}
			}
		}
		if (!contentsOnly) {
			forceDelete(directory);
		}
	}

	public static void deleteDir(final File directory) throws IOException {
		deleteDir(directory, false);
	}

	public static void moveFilesInDirs(final File srcDir, final File tgtDir, final FileFilter filter,
		boolean overwrite, List<String> outErrors) throws IOException {
		moveFilesInDirs(srcDir, tgtDir, filter, overwrite, true, outErrors);
	}
	public static void moveFilesInDirs(final File srcDir, final File tgtDir, final FileFilter filter,
			boolean overwrite, boolean allowMoveWholeDir, List<String> outErrors) throws IOException {		
		if (!tgtDir.exists()) {
			forceMkDirs(tgtDir.getParentFile());
			try {
				if (allowMoveWholeDir) {
					forceRename(srcDir, tgtDir);
					return;
				}
			} catch(Exception e) {
				//Ignore; fallback to one-by-one
			}
			forceMkDir(tgtDir);
		}
		for (final File oldF : srcDir.listFiles(filter)) {
			try {
				final File newF = new File(tgtDir, oldF.getName());
				if (oldF.isDirectory()) {
//					deleteIfEmpty(newF, false, true);
					moveFilesInDirs(oldF, newF, filter, overwrite, allowMoveWholeDir, outErrors);
					deleteIfEmpty(oldF, false, false);
					continue;
				}
				if (newF.exists()) {
					if (!overwrite) {
						continue;
					}
					forceDelete(newF);
				}
				forceRename(oldF, newF);
			} catch(Throwable t) {
				outErrors.add("Failed <" + oldF.getAbsolutePath() + ">:" + t.getMessage());
			}
		}
	}

	public static void moveFilesBackupExisting(final File srcDir, final File tgtDir, final File bkpDir,
		final FileFilter filter, final boolean overwrite, List<String> errorMessages) throws IOException {
		if (bkpDir == null) {
			moveFilesInDirs(srcDir, tgtDir, filter, overwrite, errorMessages);
			return;
		}
		if (!tgtDir.exists()) {
			forceMkDirs(tgtDir.getParentFile());
			forceRename(srcDir, tgtDir);
			return;
		}
		for (final File file : srcDir.listFiles(filter)) {
			try {
				final File tgtF = new File(tgtDir, file.getName());
				final File bkpF = new File(bkpDir, file.getName());
				if (tgtF.isDirectory()) {
					deleteIfEmpty(tgtF, false, false);
				}
				if (bkpF.isDirectory()) {
					deleteIfEmpty(bkpF, false, false);
				}
				if (file.isDirectory()) {
					deleteIfEmpty(tgtF, false, true);
					moveFilesBackupExisting(file, tgtF, bkpF, filter, overwrite, errorMessages);
					deleteIfEmpty(file, false, false);
					continue;
				}
				if (tgtF.exists()) {
					if (bkpF.exists()) {
						if (overwrite) {
							forceDelete(bkpF);
							forceRename(tgtF, bkpF);
						} else {
							forceDelete(tgtF);
						}
					} else {
						forceMkDirs(bkpF.getParentFile());
						forceRename(tgtF, bkpF);
					}
				} else {
					forceMkDirs(tgtF.getParentFile());
				}
				forceRename(file, tgtF);
			} catch(Throwable e) {
				errorMessages.add("Failed <" + file.getAbsolutePath() + ">:" + e.getMessage());
			}
		}
	}

	public static void forceMkDirs(File dir) throws IOException {
		if (!dir.mkdirs()) {
			if (dir.isDirectory()) {
				return;
			}
			throw new IOException("Failed to mkdirs for " + dir);
		}
	}

	/**
	 * Copies all files from one directory to another.
	 * 
	 * @param strPath Source directory.
	 * @param dstPath Target directory.
	 * @throws IOException
	 */
	public static void copyFiles(final String strPath, final String dstPath) throws IOException {
		copyFiles(strPath, dstPath, dstPath);
	}

	/**
	 * Copies all files from one directory to another.
	 * 
	 * @param strPath Source directory.
	 * @param dstPath Target directory.
	 * @param initDest Initial destination directory to prevent loop copying when destination is inside source
	 *            directory.
	 * @throws IOException
	 */
	public static void copyFiles(final String strPath, final String dstPath, final String initDest) throws IOException {
		final File src = new File(strPath);
		final File dest = new File(dstPath);

		if (src.isDirectory()) {
			dest.mkdirs();
			final String list[] = src.list();
			for (final String element : list) {
				final String dest1 = dest.getAbsolutePath() + File.separator + element;
				final String src1 = src.getAbsolutePath() + File.separator + element;
				if (!initDest.equals(src.getAbsolutePath().substring(0, src.getAbsolutePath().lastIndexOf('\\') + 1))) {
					copyFiles(src1, dest1, initDest);
				}
			}
		} else {
			copyFile(src, dest);
		}
	}

	/**
	 * Copies one file to another
	 * 
	 * @param source Source filename.
	 * @param destination Destination filename.
	 * @throws IOException
	 */
	public static void copyFile(final String source, final String destination) throws IOException {
		copyFile(new File(source), new File(destination));
	}

	public static void writeStreamToFile(final InputStream source, final String destination) throws IOException {
		writeStreamToFile(source, new File(destination));
	}

	public static void writeStreamToFile(final InputStream source, final File destination) throws IOException {
		FileOutputStream outS = new FileOutputStream(destination);
		FileChannel out = outS.getChannel();
		try {
			out.transferFrom(Channels.newChannel(source), 0, Integer.MAX_VALUE);
		} finally {
			outS.close();
		}
	}

	/**
	 * Copies one file to another
	 * 
	 * @param source Source file.
	 * @param destination Destination file.
	 * @throws IOException
	 */
	public static void copyFile(final File source, final File destination) throws IOException {
		destination.getParentFile().mkdirs();

		FileInputStream fin = null;
		FileOutputStream fout = null;
		FileChannel chIn = null;
		FileChannel chOut = null;
		try {
			fin = new FileInputStream(source);
			fout = new FileOutputStream(destination);
			chIn = fin.getChannel();
			chOut = fout.getChannel();
			chIn.transferTo(0, chIn.size(), chOut);
		} finally {
			IOUtil.close(chIn, chOut, fin, fout);
		}
	}

	public static void moveFilesWithSuffix(final File srcDir, final File tgtDir, String suffix, final boolean ignoreCase)
		throws IOException {
		suffix = suffix.charAt(0) == '.' ? suffix : "." + suffix;
		if (ignoreCase) {
			suffix = suffix.toUpperCase();
		}
		final String dotSuf = suffix;
		moveFilesInDirs(srcDir, tgtDir, new FileFilter() {
			@Override
			public boolean accept(final File pathname) {
				if (pathname.isDirectory()) {
					return true;
				}
				if (ignoreCase) {
					return pathname.getName().toUpperCase().endsWith(dotSuf);
				}
				return pathname.getName().endsWith(dotSuf);
			}
		}, false, new ArrayList<String>());
	}

	public static void replaceInFiles(File srcFileOrDir, File tgtFileOrDir, FileFilter filter, String match,
		String substitution) throws IOException {
		if (!filterAccepts(filter, srcFileOrDir)) {
			return;
		}
		if (srcFileOrDir.isDirectory()) {
			tgtFileOrDir.mkdirs();
			final File list[] = srcFileOrDir.listFiles(filter);
			for (final File element : list) {
				replaceInFiles(element, new File(tgtFileOrDir, element.getName()), filter, match, substitution);
			}
		} else {
			final Pattern pt = Pattern.compile(match, Pattern.MULTILINE);
			final CharSequence str = readCharSequenceFromFile(srcFileOrDir);
			final Matcher mchr = pt.matcher(str);
			final String ret = mchr.replaceAll(substitution);
			if (ret.contentEquals(str)) {
				System.out.println("Not replaced " + srcFileOrDir);
			}
			writeCharSequenceToFile(tgtFileOrDir, ret);
		}
	}

	private static boolean filterAccepts(FileFilter filter, File f) {
		return filter == null || filter.accept(f);
	}

	private static void writeCharSequenceToFile(final File tgtFileOrDir, final String contents) throws IOException {
		final FileWriter wr = new FileWriter(tgtFileOrDir);
		try {
			wr.write(contents.toCharArray());
		} finally {
			wr.close();
		}
	}

	private static CharSequence readCharSequenceFromFile(final File srcFileOrDir) throws IOException {
		final FileReader rdr = new FileReader(srcFileOrDir);
		try {
			final StringBuilder sb = new StringBuilder();
			final char[] cBuf = new char[1024];
			int len = 0;
			do {
				len = rdr.read(cBuf);
				if (len == -1) {
					break;
				}
				sb.append(cBuf, 0, len);
			} while (len >= 0);
			return sb;
		} finally {
			rdr.close();
		}
	}

	public static byte[] md5Checksum(final File file) throws IOException, NoSuchAlgorithmException {
		FileInputStream is = new FileInputStream(file);
		try {
			return md5Checksum(is);
		} finally {
			IOUtil.close(is);
		}
	}

	public static byte[] md5Checksum(final InputStream is) throws IOException, NoSuchAlgorithmException {
		final byte[] buffer = new byte[1024];
		try {
			final MessageDigest complete = MessageDigest.getInstance("MD5");
			int numRead;
			do {
				numRead = is.read(buffer);
				if (numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while (numRead != -1);
			return complete.digest();
		} finally {
			is.close();
		}
	}

	public static byte[] toByteArray(final File file) throws IOException {
		final long length = file.length();
		if (length > Integer.MAX_VALUE) {
			throw new IOException("File to large: " + length + " bytes.");
		}

		final byte[] bytes = new byte[(int)length];
		final InputStream is = new FileInputStream(file);

		try {
			int offset = 0;
			int chunk = 0;
			while (offset < bytes.length && (chunk = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += chunk;
			}

			if (offset < bytes.length) {
				throw new IOException("Could not completely read file " + file.getName());
			}
		} finally {
			is.close();
		}

		return bytes;
	}

	public static Set<File> extractParentDirs(File[] inFiles) {
		if (inFiles == null) {
			return null;
		}
		if (inFiles.length == 0) {
			return null;
		}
		HashSet<File> retDirs = new HashSet<File>();
		for (File f : inFiles) {
			if (f == null) {
				continue;
			}
			if (f.isDirectory()) {
				retDirs.add(f);
			}
			retDirs.add(f.getParentFile());
		}
		return retDirs;
	}

	public static File findExistingFile(String fName, Collection<File> lst) {
		File fl = new File(fName);
		if (fl.isAbsolute()) {
			return fl;
		}
		for (File f : lst) {
			File tmp = new File(f, fName);
			if (tmp.exists()) {
				return tmp;
			}
		}
		return null;
	}

	public static void copyFile(File f, OutputStream out) throws IOException {
		copyFile(f, Channels.newChannel(out));
	}

	public static void copyFile(File source, WritableByteChannel chOut) throws IOException {
		FileInputStream fin = null;
		FileChannel chIn = null;
		try {
			fin = new FileInputStream(source);
			chIn = fin.getChannel();
			chIn.transferTo(0, chIn.size(), chOut);
		} finally {
			IOUtil.close(chIn, fin);
		}
	}

	/***
	 * Thread safe create temporary directory
	 * 
	 * @return temporary directory
	 */
	public static File createTempDirectory() {
		return createTempDirectory(null);
	}
	
	public static File createTempDirectory(File baseDir) {
		if (baseDir == null) {
			baseDir = new File(System.getProperty("java.io.tmpdir"));
		}
		String baseName = "tmp" + System.currentTimeMillis() + "_";

		for (int counter = 0; counter < 10000; counter++) {
			File tempDir = new File(baseDir, baseName + String.valueOf(counter));
			if (!tempDir.exists() && tempDir.mkdir()) {
				return tempDir;
			}
		}
		throw new IllegalStateException("Failed to create temporary directory");
	}

	public static boolean renameWithRetry(final File oldFile, final File newFile, int millis) {
		return ExecutionUtilJava.executeWithRetry(new SGCallable<Boolean>() {
			@Override
			public Boolean call() {
				if (oldFile.renameTo(newFile)) {
					return Boolean.TRUE;
				}
				if (!oldFile.exists()) {
					throw new RuntimeException("Source doesn't exist: " + oldFile.getAbsolutePath());
				}
				if (newFile.exists()) {
					throw new RuntimeException("Target already exists: " + newFile.getAbsolutePath());
				}
				if (!newFile.getParentFile().isDirectory()) {
					throw new RuntimeException("Target parent doesn't exist or is not a directory: "
						+ newFile.getParentFile().getAbsolutePath());
				}
				return Boolean.FALSE;
			}
		}, 2, millis);
	}

	public static void forceRename(File oldFile, File newFile) throws IOException {
		if (!renameWithRetry(oldFile, newFile, 10)) {
			throw new IOException("Failed to rename " + oldFile + " to " + newFile);
		}
	}

	public static boolean deleteWithRetry(final File f, int delay) throws SecurityException {
		return ExecutionUtilJava.executeWithRetry(new SGCallable<Boolean>() {
			@Override
			public Boolean call() {
				if (f.delete()) {
					return Boolean.TRUE;
				}
				return Boolean.valueOf(!f.exists());
			}
		}, 2, delay);
	}

	public static void forceDelete(File f) throws IOException {
		if (!deleteWithRetry(f, 10)) {
			throw new IOException("Failed to delete " + f);
		}
	}

	public static void writeBytesToFile(byte[] data, int off, int len, File outFile) throws IOException {
		FileOutputStream fos = new FileOutputStream(outFile);
		try {
			fos.write(data, off, len);
			fos.flush();
		} finally {
			fos.close();
		}
	}

	public static boolean isEmpty(File directory) {
		return ArrayUtil.isNullOrEmpty(directory.list());
	}

	public static boolean isSuffixIgnoreCase(File file, String suffix) {
		return isSuffixIgnoreCase(file.getName(), suffix);
	}

	public static void unzipFile(File zipFile, File destination, boolean flatten) throws IOException {
		FileInputStream src = new FileInputStream(zipFile);
		try {
			unzipStream(src, destination, flatten);
		} finally {
			IOUtil.close(src);
		}
	}
	
	public static void unzipStream(InputStream src, File destination, boolean flatten) throws IOException {
		ZipInputStream zipStream = null;
		try {
			zipStream = new ZipInputStream(src);
			ZipEntry zEntry = null;
			while ((zEntry = zipStream.getNextEntry()) != null) {
				File zipEntryFile = new File(zEntry.getName());
				File targetFile = null;
				if (flatten) {
					targetFile = new File(destination, zipEntryFile.getName());
				} else {
					targetFile = new File(destination, zEntry.getName());
					String parentDir = zipEntryFile.getParent();
					if (!StringUtil.isNullOrEmpty(parentDir)) {
						File targetDir = new File(destination, parentDir);
						targetDir.mkdirs();
					}
				}
				FileOutputStream fo = new FileOutputStream(targetFile);
				try {
					IOUtilJava.copyStream(zipStream, fo, 16384);
				} finally {
					IOUtil.close(fo);
				}
			}
		} finally {
			IOUtil.close(zipStream);
		}
	}
	
	public static void zipFiles(File zipFile, File... toZip) throws IOException {
		zipFiles(zipFile, Arrays.asList(toZip));
	}
	
	public static void zipFiles(File zipFile, List<File> toZip) throws IOException {
		Map<String, File> fileMap = new HashMap<String, File>(toZip.size());
		for (File f : toZip) {
			fileMap.put(f.getName(), f);
		}
		zipFiles(fileMap, zipFile);
	}

	public static void zipFiles(Map<String, File> map, File zipFile) throws FileNotFoundException, IOException {
		FileOutputStream dest = new FileOutputStream(zipFile);
		ZipOutputStream out = null;
		try {
			out = new ZipOutputStream(new BufferedOutputStream(dest));
			for (Entry<String, File> e : map.entrySet()) {
				addZipEntry(e.getValue(), e.getKey(), out);
			}
		} finally {
			IOUtil.close(out, dest);
		}
	}

	private static void addZipEntry(File inputFile, String fileName, ZipOutputStream out) throws IOException {
		FileInputStream fi = new FileInputStream(inputFile);
		try {
			ZipEntry entry = new ZipEntry(fileName);
			out.putNextEntry(entry);
			IOUtilJava.copyStream(fi, out, 16384);
		} finally {
			fi.close();
		}
	}

	public static byte[] copyFileToMem(File f) throws IOException {
		byte[] ret = new byte[(int)f.length()];
		FileInputStream fis = new FileInputStream(f);
		try {
			fis.read(ret);
		} finally {
			fis.close();
		}
		return ret;
	}

	public static void createZip(Iterable<File> srcFiles, File targetZip) throws IOException {
		FileOutputStream fos = new FileOutputStream(targetZip);
		try {
			ZipOutputStream zos = new ZipOutputStream(fos);
			try {
				for (File f : srcFiles) {
					ZipEntry entry = new ZipEntry(f.getName());
					entry.setSize(f.length());
					zos.putNextEntry(entry);
					copyFile(f, zos);
					zos.flush();
					zos.closeEntry();
				}
			} finally {
				zos.close();
			}
		} finally {
			fos.close();
		}
	}

	public static File setSuffix(File original, String newSuffix) {
		if (!newSuffix.startsWith(".")) {
			newSuffix = "." + newSuffix;
		}
		return new File(getNameNoSuffix(original.getPath()) + newSuffix);
	}

	public static String ensureSeparatorAtEnd(String path) {
		if (path.endsWith(File.separator)) {
			return path;
		}
		return path + File.separatorChar;
	}

	public static void forceMkDir(File dir) throws IOException {
		if (!dir.mkdir()) {
			if (dir.isDirectory()) {
				return;
			}
			throw new IOException("Failed to mkdir for " + dir);
		}
	}

	public static void forceCreateNewFile(File f) throws IOException {
		if (!f.createNewFile()) {
			if (f.isFile() && f.length() == 0) {
				return;
			}
			throw new IOException("Failed to createNewFile for " + f);
		}
	}
	
	public static File replaceSuffix(final File file, final String newSuffix) {
		return new File(file.getParentFile(), replaceSuffix(file.getName(), newSuffix));
	}
}
