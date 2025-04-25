package com.sinergise.generics.gwt.core;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GenericsClientSession implements IsSerializable {
	private static GenericsClientSession INSTANCE;
	
	private GenericsClientSession() {		
	}
	public static GenericsClientSession getInstance() {
		if (INSTANCE==null) {
			INSTANCE=new GenericsClientSession();
		}
		return INSTANCE;
	}
	public String locale;	
}
