package com.sinergise.geopedia.core.entities;

import java.io.Serializable;
import java.util.HashMap;

import com.sinergise.geopedia.core.entities.Permissions.GeopediaEntity;

public class UserPermissions implements Serializable {
	private static final long serialVersionUID = 7752321810969401004L;
	
	
	
	private HashMap<Integer, HashMap<Integer,Integer>> permissionsHolder = new HashMap<Integer, HashMap<Integer,Integer>>();

	
	public HashMap<Integer,Integer> getPermissionsFor(int entityId) {
		HashMap<Integer,Integer> entityPerms = permissionsHolder.get(entityId);
		if (entityPerms == null) {
			entityPerms = new HashMap<Integer,Integer>();
			permissionsHolder.put(entityId, entityPerms);
		}
		return entityPerms;
	}
	public HashMap<Integer,Integer> getPermissionsFor(GeopediaEntity entity) {
		return getPermissionsFor(entity.getId());
	}
	
	public Integer getPermission(GeopediaEntity entity, Integer entityId) {
		return getPermissionsFor(entity).get(entityId);
	}
	public boolean hasPermission(GeopediaEntity entity, int entityId, int requiredPermission) {
		Integer permission = getPermission(entity, entityId);
		if (permission==null) return false;
		if (permission>=requiredPermission)
			return true;		
		return false;
	}
	
}
