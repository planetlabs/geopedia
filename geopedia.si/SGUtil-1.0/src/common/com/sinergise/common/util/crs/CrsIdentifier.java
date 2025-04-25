package com.sinergise.common.util.crs;

import java.io.Serializable;

public class CrsIdentifier implements Serializable {
	
	public enum CrsAuthority {
		CRS_AUTH("CRS"), EPSG("EPSG"), AUTO2("AUTO2"),
		@Deprecated
		MYSQL("MYSQL"),
		@Deprecated
		ORACLE("ORACLE"), 
		@Deprecated
		POSTGIS("POSTGIS"),
		SINERGISE("SG");
		
		private String name;
		private CrsAuthority(String name) {
			this.name = name;
		}
		
		public String getAuthorityName() {
			return name;
		}
		
		public static CrsAuthority findByName(String name) {
			for (CrsAuthority auth : values()) {
				if (name.equals(auth.getAuthorityName())) {
					return auth;
				}
			}
			return null;
		}
	}
	
	
	private static final long serialVersionUID = -1864954973198235713L;

	private static final String AUTH_SEPARATOR = ":";

	private String code;
	
	@Deprecated /** Serialization only */
	protected CrsIdentifier() { }
	
	public CrsIdentifier(CrsAuthority authority, int id) {
		this(authority, String.valueOf(id));
	}
	
	public CrsIdentifier(CrsAuthority authority, String id) {
		this(authority.getAuthorityName() + AUTH_SEPARATOR + id);
	}
	
	public CrsIdentifier(String code) {
		assert code != null : "code is null";
		this.code = code;
	}
	
	public CrsAuthority getAuthority() {
		if (code.indexOf(AUTH_SEPARATOR) > 0) {
			String authName = code.substring(0, code.indexOf(AUTH_SEPARATOR));
			return CrsAuthority.findByName(authName);
		}
		return null;
	}
	
	public int getSrid() {
		String sridStr = code;
		
		int sepIndex = code.indexOf(AUTH_SEPARATOR);
		if (sepIndex > 0) {
			sridStr = code.substring(sepIndex+1);
		}
		
		try {
			return Integer.parseInt(sridStr);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
	public String getCode() {
		return code;
	}
	
	@Override
	public String toString() {
		return code;
	}


	@Override
	public int hashCode() {
		return code.hashCode();
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CrsIdentifier))
			return false;
		CrsIdentifier other = (CrsIdentifier)obj;
		if (!code.equals(other.code)) {
			return false;
		}
		return true;
	}
	
	public static int getSrid(CrsIdentifier id) {
		if (id == null) {
			return 0;
		}
		return id.getSrid();
	}
	
	
}
