package com.sinergise.geopedia.config;

import java.io.Serializable;

import com.sinergise.geopedia.core.config.Configuration;

public class InstanceConfiguration implements Serializable{
	public ServerConfiguration serverCfg;
	public Configuration commonCfg;
	public int instanceId;
	public String instanceDomainName;
			
	
	public static class All {
		public All(InstanceConfiguration[] configs) {
			this.configurations = configs;
		}
		public All() {
			// TODO Auto-generated constructor stub
		}
		public InstanceConfiguration [] configurations;
	}
}
