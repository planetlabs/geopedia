package com.sinergise.generics.core;

import static com.sinergise.common.util.lang.TypeUtil.boxI;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sinergise.generics.core.util.EntityUtils;

public class MasterDetailsHolder implements ValueHolder, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7499353943063132797L;
	
	private EntityObject master = null;
	private Map<Integer, Integer> attributeMap = new HashMap<Integer, Integer>();
	private ValueHolder details = null;
	private String masterDatasourceId;
	private String detailsDatasourceId;
	
	protected MasterDetailsHolder(){	
	}

	public MasterDetailsHolder (EntityObject master, String masterDatasourceID) {
		this.master = master;
		this.setMasterDatasourceId(masterDatasourceID);
	}
	
	public MasterDetailsHolder(MasterDetailsHolder vh) {
		this.master = (EntityObject)EntityUtils.deepCopy(vh.getMaster());
		this.setMasterDatasourceId(vh.getMasterDatasourceId());
		setDetails(EntityUtils.deepCopy(vh.getDetails()), vh.getDetailsDatasourceId());
		this.attributeMap = vh.getAttributeMapping();
	}

	public void addMapping(TypeAttribute masterAttribute, TypeAttribute detailAttribute) {
		attributeMap.put(boxI(detailAttribute.getId()), boxI(masterAttribute.getId()));
	}
	
	public void setDetails(ValueHolder details, String detailsDatasourceID) {
		this.details = details;
		this.setDetailsDatasourceId(detailsDatasourceID);
	}
	
	@Override
	public boolean isNull() {
	
		return false;
	}
	/**
	 * Returns hash for attribute mapping. Hash keys are detail attributes, values are master attributes
	 * @return map for attribute mappings;
	 */
	public Map<Integer,Integer> getAttributeMapping() {
		return attributeMap;
	}
	
	public ValueHolder getDetails() {
		return details;
	}

	public EntityObject getMaster() {
		return master;
	}
	public void setMaster(EntityObject master) {
		this.master=master;
	}


	private void setMasterDatasourceId(String masterDatasourceId) {
		this.masterDatasourceId = masterDatasourceId;
	}

	public String getMasterDatasourceId() {
		return masterDatasourceId;
	}

	private void setDetailsDatasourceId(String detailsDatasourceId) {
		this.detailsDatasourceId = detailsDatasourceId;
	}

	public String getDetailsDatasourceId() {
		return detailsDatasourceId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributeMap == null) ? 0 : attributeMap.hashCode());
		result = prime * result + ((details == null) ? 0 : details.hashCode());
		result = prime * result + ((detailsDatasourceId == null) ? 0 : detailsDatasourceId.hashCode());
		result = prime * result + ((master == null) ? 0 : master.hashCode());
		result = prime * result + ((masterDatasourceId == null) ? 0 : masterDatasourceId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MasterDetailsHolder other = (MasterDetailsHolder)obj;
		if (attributeMap == null) {
			if (other.attributeMap != null) return false;
		} else if (!attributeMap.equals(other.attributeMap)) return false;
		if (details == null) {
			if (other.details != null) return false;
		} else if (!details.equals(other.details)) return false;
		if (detailsDatasourceId == null) {
			if (other.detailsDatasourceId != null) return false;
		} else if (!detailsDatasourceId.equals(other.detailsDatasourceId)) return false;
		if (master == null) {
			if (other.master != null) return false;
		} else if (!master.equals(other.master)) return false;
		if (masterDatasourceId == null) {
			if (other.masterDatasourceId != null) return false;
		} else if (!masterDatasourceId.equals(other.masterDatasourceId)) return false;
		return true;
	}

	
	
	
}
