package com.sinergise.generics.core.upload.entities;

import java.io.Serializable;
import java.util.ArrayList;

public class GenericDocument implements Serializable {

	private static final long serialVersionUID = 8463445894532097535L;

	public interface DOCUMENT {
		public static final String TABLE_NAME = "GENERIC_DOCUMENTS";

		public static final String DOCUMENT_ID = "DOCUMENT_ID";
		public static final String DOCUMENT_TYPE = "DOC_TYPE";
		public static final String DATE = "X_DAT_CRE";
	}

	public static enum Status {
		STORED, NEW, DELETED
	}

	public Integer documentId = null;
	public String docType = " ";
	public ArrayList<GenericFile> fileList;
	public Status status;
	
	
	public GenericDocument() {
		fileList = new ArrayList<GenericFile>();
	}

}
