package com.sinergise.generics.core.upload.entities;

import java.io.Serializable;

public class GenericFile implements Serializable {

	private static final long serialVersionUID = 4301164973824377392L;

	public static enum Status {
		STORED, NEW, DELETED
	}

	public interface FILE {
		public static final String TABLE_NAME = "GENERIC_FILES";

		public static final String FILE_ID = "FILE_ID";
		public static final String MIME_TYPE = "MIME_TYPE";
		public static final String DATA = "DATA";
		public static final String FILE_NAME = "FILE_NAME";
		public static final String DOCUMENT_ID = "DOCUMENT_ID";
		public static final String FILE_SIZE = "FILE_SIZE";
		public static final String DATE = "X_DAT_CRE";
	}

	public Integer fileId;
	public String mimeType;
	public String fileName;
	public int documentId;
	public int fileSize;

	// save params
	public String token;
	public Status status;

	public byte[] data;

}
