/**
 * 
 */
package com.sinergise.java.util;

import java.io.File;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Initializes classes in provided package. Based on javolution ClassInitializer.
 * 
 * @author tcerovski
 */
public class ClassInitializer {

	public static void initClasses(final String packageName) {

		final String classPath = System.getProperty("java.class.path");
		final String pathSeparator = System.getProperty("path.separator");
		if ((classPath == null) || (pathSeparator == null)) {
			System.err.print("Cannot initialize classpath through system properties");
			return;
		}
		initialize(classPath, pathSeparator, packageName);
	}

	private static void initialize(String classPath, final String pathSeparator, final String packageName) {
		while (classPath.length() > 0) {
			String name;
			final int index = classPath.indexOf(pathSeparator);
			if (index < 0) {
				name = classPath;
				classPath = "";
			} else {
				name = classPath.substring(0, index);
				classPath = classPath.substring(index + pathSeparator.length());
			}
			if (name.endsWith(".jar") || name.endsWith(".zip")) {
				initializeFromJar(name, packageName);
			} else {
				initializeFromDir(name, packageName);
			}
		}
	}

	private static void initClass(final String name) {
		String className = null;
		try {
			className = name.substring(0, name.length() - 6);
			className = className.replace('/', '.');
			System.out.println("Initialize " + className);
			Class.forName(className);
		} catch(final Throwable ex) {
			System.err.println("Could not initialize class: " + className);
			ex.printStackTrace();
		}
	}

	public static void initializeFromJar(final String jarName, String pkgToInit) {
		try {
			final ZipFile jarFile = new ZipFile(jarName);
			pkgToInit = pkgToInit.replaceAll("\\.", "/");

			final Enumeration<? extends ZipEntry> e = jarFile.entries();
			while (e.hasMoreElements()) {
				final ZipEntry entry = e.nextElement();
				final String entryName = entry.getName();
				if (!entryName.startsWith(pkgToInit)) {
					continue;
				}
				if (entryName.endsWith(".class")) {
					initClass(entryName);
				}
			}
		} catch(final Exception e) {
			e.printStackTrace();
		}
	}

	public static void initializeFromDir(final String dirName, final String pkgToInit) {
		final File file = new File(dirName);

		if (file.isDirectory()) {
			final File[] files = file.listFiles();
			for (final File file2 : files) {
				initialize("", file2, pkgToInit);
			}
		}
	}

	private static void initialize(final String pkg, final File file, String pkgToInit) {
		if (!pkgToInit.startsWith(pkg)) { return; }

		final String name = file.getName();
		if (file.isDirectory()) {
			final File[] files = file.listFiles();
			final String newPkg = (pkg.length() == 0) ? name : pkg + "." + name;
			if (pkg.equals(pkgToInit)) {
				pkgToInit = newPkg;
			}
			for (final File file2 : files) {
				initialize(newPkg, file2, pkgToInit);
			}
		} else if (name.endsWith(".class")) {
			initClass(pkg + "." + name);
		}
	}

}
