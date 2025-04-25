package com.sinergise.geopedia.core.config;

import java.io.Serializable;
import java.util.Arrays;

import com.sinergise.common.util.string.StringUtil;

public class ClusteredURLProvider implements IsURLProvider, Serializable {
	private static final long serialVersionUID = -77548352000864484L;

	private String hosts[];
	private String basePath;
	
	private int offset=0;
	/* serialization only*/
	@Deprecated
	public ClusteredURLProvider() {
		
	}
	
	public ClusteredURLProvider(String hosts[], String basePath) {
		this.hosts=hosts;
		if (StringUtil.isNullOrEmpty(basePath))
			basePath="/";
		else {
			if (basePath.startsWith("/"))
				this.basePath=basePath;
			else
				this.basePath="/"+basePath;
			if (!this.basePath.endsWith("/"))
				this.basePath+="/";
		}
	}
	
	
	@Override
	public String getBaseURL() {
		return getHost()+basePath;
	}

	@Override
	public String getHost() {
		offset++;
		if (offset>=hosts.length)
			offset=0;
		return "http://"+hosts[offset];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((basePath == null) ? 0 : basePath.hashCode());
		result = prime * result + Arrays.hashCode(hosts);
		result = prime * result + offset;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClusteredURLProvider other = (ClusteredURLProvider) obj;
		if (basePath == null) {
			if (other.basePath != null)
				return false;
		} else if (!basePath.equals(other.basePath))
			return false;
		if (!Arrays.equals(hosts, other.hosts))
			return false;
		if (offset != other.offset)
			return false;
		return true;
	}
	
	
}
