package com.sinergise.generics.core.upload.entities;

import java.util.ArrayList;

public class GenericFileSearchParams {

	public ArrayList<Integer> docIds;
	public String fileName = null;
	public Integer documentId = null;

	public boolean loadData = false;

	public GenericFileSearchParams() {
		fileName = null;
		docIds = new ArrayList<Integer>();
	}

}
