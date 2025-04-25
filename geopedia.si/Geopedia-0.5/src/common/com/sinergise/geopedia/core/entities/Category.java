package com.sinergise.geopedia.core.entities;

import java.io.Serializable;

import com.sinergise.common.util.string.StringUtil;

public class Category implements Serializable{
	private static final long serialVersionUID = -1278983818931510523L;

	public static final int ID_NO_PARENT = 0;
	
	public static Category PERSONAL = new Category(-5, ID_NO_PARENT, "*Osebno*", "*Osebno*");	
	public static Category FAVOURITE = new Category(-6, ID_NO_PARENT, "*Priljubljeno*", "*Priljubljeno*");
	

	
	@Deprecated
	protected Category() {}
	
	private Integer id;
	private Integer parentId;
	private String name;
	private String description;
	
	
	
	public Category (Integer id, Integer parentId, String name, String description) {
		this.name=name;
		this.description=description;
		this.id=id;
		this.parentId=parentId;
	}
	
	
	
	
	public Integer getId() {
		return id;
	}
	
	public Integer getParentId() {
		return parentId;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean hasId() {
		return id!=null;
	}
	
	public boolean hasParentId() {
		return parentId!=null;
	}
	
	public boolean hasName() {
		return !StringUtil.isNullOrEmpty(name);
	}

	public boolean hasDescription() {
		return !StringUtil.isNullOrEmpty(description);
	}
	
	public String toString() {
		return id+":"+parentId+":"+name;
	}




	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Category other = (Category) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
	
}
