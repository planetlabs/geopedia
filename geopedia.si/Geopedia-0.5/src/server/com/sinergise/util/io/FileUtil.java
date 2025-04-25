package com.sinergise.util.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtil {
	
	protected static String TEMP_PREFIX = "pedia";
	private static final String TEMP_POSTFIX = ".tmp";
	
	public static final int BUFFER = 16384;
	
	protected static void cleanup() {
		final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
		if (sysTempDir.isDirectory()) {
			for (File innerFile : sysTempDir.listFiles()) {
				String fileName = innerFile.getName();
				if (fileName.startsWith(TEMP_PREFIX) && fileName.endsWith(TEMP_POSTFIX)) {
					deleteFile(innerFile);
				}
			}
		}
	}
	
    public static File createTempFile() throws IOException {
        return createTempFile(TEMP_POSTFIX);
    }

    public static File createTempFile(String posfix) throws IOException {
        return File.createTempFile(TEMP_PREFIX + Long.toString(System.nanoTime()), posfix);
    }

    public static File createTempDirectory() throws IOException {
        File temp = createTempFile();

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }
        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }
        return (temp);
    }
	
	public static void deleteFile(File file) {
		if (file!=null && file.exists() && file.isFile()) {
			file.delete();
		}
	}

	public static File copyFile(File inputFile) throws FileNotFoundException {
		FileInputStream fileInputStream = new FileInputStream(inputFile);
		
		File outputFile = null;
		try {
			byte[] buf = new byte[BUFFER];

			if (fileInputStream != null) {

				FileOutputStream fileOutputStream = null;
				try {
					outputFile = createTempFile();
					fileOutputStream = new FileOutputStream(outputFile);

					int len;
					while ((len = fileInputStream.read(buf, 0, BUFFER)) > -1) {
						fileOutputStream.write(buf, 0, len);
					}
					
				} finally {
					if (fileOutputStream != null) {
						try {
							fileOutputStream.close();
						} catch (IOException e) {
							// ..
							e.printStackTrace();
						}
					}
				}
			}// if

			fileInputStream.close();

			return outputFile;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static File extractFile(File inputFile) throws IOException {
		byte[] bytes = get_bytes_from_file(inputFile);
		ZipInputStream zipinputstream = new ZipInputStream(new ByteArrayInputStream(bytes));
		File outputFile = get_from_zip(zipinputstream);
		return outputFile;
	}
	
	public static byte[] get_bytes_from_file(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}
	
	/**
	 * extract a single file from zip
	 */
	private static File get_from_zip(ZipInputStream zipInputStream) {
		File file = null;
		try {
			byte[] buf = new byte[BUFFER];
			ZipEntry zipEntry;

			zipEntry = zipInputStream.getNextEntry();
			if (zipEntry != null) {
				String entryName = zipEntry.getName();

				FileOutputStream fileOutputStream = null;
				try {
					file = createTempFile();
					fileOutputStream = new FileOutputStream(file);

					int len;
					while ((len = zipInputStream.read(buf, 0, BUFFER)) > -1) {
						fileOutputStream.write(buf, 0, len);
					}
					
				} finally {
					if (fileOutputStream != null) {
						try {
							fileOutputStream.close();
						} catch (IOException e) {
							// ..
							e.printStackTrace();
						}
					}
				}

				zipInputStream.closeEntry();

			}// if

			zipInputStream.close();

			return file;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void write_to_stream(File file, OutputStream outputStream) {
		try {
			byte[] bytes = get_bytes_from_file(file);
			outputStream.write(bytes);
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static File zipFile(File inputFile, String fileName) {
		HashMap<String, File> map = new HashMap<String, File>();
		map.put(fileName, inputFile);
		return zipFiles(map);
	}
	
	public static File zipFiles(HashMap<String, File> map) {
		File zipFile = null;
		try {
			zipFile = createTempFile();
			FileOutputStream dest = new FileOutputStream(zipFile);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
			
			for (String mapKey : map.keySet()) {
				File inputFile = map.get(mapKey);
				addZipEntry(inputFile, mapKey, out);
			}

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return zipFile;
	}
	
	private static void addZipEntry(File inputFile, String fileName, ZipOutputStream out) {
		BufferedInputStream origin = null;
		
		byte data[] = new byte[BUFFER];
		
		try {
			System.out.println("Adding: " + fileName);
			FileInputStream fi = new FileInputStream(inputFile);
			origin = new BufferedInputStream(fi, BUFFER);
			ZipEntry entry = new ZipEntry(fileName);
			out.putNextEntry(entry);
			int count;
			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			origin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean contains(int num, int[] values) {
		if (values==null) {
			return false;
		}
		boolean match = false;
		for (int value : values) {
			if (value==num) {
				match = true;
			}
		}
		return match;
	}
	
	
	public static String getResourceFileAsString(String resourcefilename) {
        final String newline = System.getProperty("line.separator");
        String line = null;
        BufferedReader b = null;
        StringBuilder sb = new StringBuilder();

        try {
//            logger.info("Reading "+resourcefilename);
            
            // open buffered reader
            b = new BufferedReader(
                    new InputStreamReader(
                            FileUtil.class.getResourceAsStream(resourcefilename)));
            try {
                while ((line = b.readLine()) != null) {
                    sb.append(line);
                    sb.append(newline);
                }
            } finally {
                b.close();
            }
            
        } catch (Exception ex) {
//            logger.error("Error reading "+resourcefilename, ex);
            ex.printStackTrace();
        }

        return (sb.toString());
    }
	
	public static void writeStringToFile(File file, final String string) {
	    try {
	        FileWriter writer = new FileWriter(file);
	        writer.write(string);
	        writer.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
