package com.sinergise.geopedia.core.entities;

import java.io.Serializable;
import java.util.HashMap;

public class WebLink implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final String SYSTEM_GROUP="system";
	public static final String LITE_GROUP="lightLinks";
	
	public String name;
	public String displayName;
	public String URL;
	public String description;
	
	public static class Group extends HashMap<String, WebLink> {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2953853870366177197L;
	}
	
	public static class LinksCollection extends  HashMap<String, Group> implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6124976594786476005L;

		public Group getSystemGroup() {
			return get(SYSTEM_GROUP);
		}
	}
	
}


