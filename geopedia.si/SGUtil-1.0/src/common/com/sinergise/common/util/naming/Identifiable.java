package com.sinergise.common.util.naming;

import com.sinergise.common.util.lang.Function;

public interface Identifiable {
	public static final Function<Identifiable, Identifier> FUNC_QUALIFIED_ID_GETTER = new Function<Identifiable, Identifier>() {
		@Override
		public Identifier execute(Identifiable param) {
			return param.getQualifiedID();
		}
	};

	public static final Function<Identifiable, String> FUNC_LOCAL_ID_GETTER = new Function<Identifiable, String>() {
		@Override
		public String execute(Identifiable param) {
			return param.getLocalID();
		}
	};

	String getLocalID();
	
	Identifier getQualifiedID();
}
