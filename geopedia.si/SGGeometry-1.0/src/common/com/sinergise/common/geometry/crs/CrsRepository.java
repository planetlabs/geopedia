/*
 *
 */
package com.sinergise.common.geometry.crs;

import java.util.HashMap;
import java.util.Map;

import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.crs.CrsIdentifierMapper;
import com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority;

public class CrsRepository {
    public static final CrsRepository INSTANCE=new CrsRepository();
    
    private Map<CrsIdentifier, CRS> crsRegistry = new HashMap<CrsIdentifier, CRS>();
    private Map<CrsAuthority, CrsIdentifierMapper> authMappers = new HashMap<CrsAuthority, CrsIdentifierMapper>();
    
    private CrsRepository() {
        add(CRS.MAP_PIXEL_CRS);
        add(CRS.MAP_CRS);
        add(CRS.SI_D48);
        add(CRS.D48_GK);
        add(CRS.D96_TM);
        add(CRS.WGS84);
        add(CRS.WGS84_GLOBAL_PLATTE_CARRE);
        add(CRS.WGS84_GLOBAL_PSEUDO_PLATTE_CARRE);
        add(CRS.BNG);
        add(CRS.NONAME_WORLD_CRS);
    }
    
    public boolean add(CRS crs) {
    	boolean added = false;
        for (CrsIdentifier crsId : crs.getIdentifiers()) {
        	added |= add(crsId, crs);
        }
        return added;
    }
    
    public boolean add(CrsIdentifier id, CRS crs) {
    	crs.registerIdentifier(id);
        return crsRegistry.put(id, crs) == null;
    }
    
    public boolean remove(CRS crs) {
    	boolean removed = false;
    	for (CrsIdentifier crsId : crs.getIdentifiers()) {
    		removed |= remove(crsId);
    	}
        return removed; 
    }
    
    public boolean remove(CrsIdentifier crsId) {
        return crsRegistry.remove(crsId) != null;
    }
    
    public CRS get(CrsIdentifier crsId) {
        CRS crs = crsRegistry.get(crsId);
        
        //check if mapping for EPSG exists 
        if (crs == null) {
        	CrsIdentifierMapper mapper = authMappers.get(crsId.getAuthority());
        	if (mapper != null) {
        		crs = get(mapper.mapForAuthority(crsId, CrsAuthority.EPSG));
        	}
        }
        
        return crs;
    }
    
    public CRS get(String code) {
        return get(new CrsIdentifier(code));
    }
    
    public CrsIdentifier getIdentifierForAuthority(CrsIdentifier crsId, CrsAuthority auth) {
    	if (crsId == null) {
    		return null;
    	}
    	
    	if (auth.equals(crsId.getAuthority())) {
    		return crsId;
    	}
    	
    	CRS crs = get(crsId);
    	if (crs != null) {
    		return getIdentifierForAuthority(crs, auth);
    	}
    	return null;
    }
    
    public CrsIdentifier getDefaultIdentifier(CrsIdentifier crsId) {
    	CRS crs = get(crsId);
    	if (crs != null) {
    		return crs.getDefaultIdentifier();
    	}
    	return crsId;
    }
    
    public CrsIdentifier getIdentifierForAuthority(CRS crs, CrsAuthority auth) {
    	if (crs == null) {
    		return null;
    	}
    	
    	//check already registered identifiers
    	for (CrsIdentifier crsId : crs.getIdentifiers()) {
    		if (auth.equals(crsId.getAuthority())) {
    			return crsId;
    		}
    	}
    	
    	//check if mapping exists
    	for (CrsIdentifier crsId : crs.getIdentifiers()) {
    		CrsIdentifierMapper mapper = authMappers.get(auth);
    		if (mapper != null) {
    			CrsIdentifier mappedId = mapper.mapForAuthority(crsId, auth);
    			if (mappedId != null) {
    				//register new mapping with repository
    				add(mappedId, crs);
    				return mappedId;
    			}
    		}
    	}
    	
    	return null;
    }
    
    public boolean registerIdentifierMapper(CrsAuthority authority, CrsIdentifierMapper mapper) {
    	if (authMappers.containsKey(authority)) {
    		return false;
    	}
    	
    	authMappers.put(authority, mapper);
    	return true;
    }
    
    public boolean equals(CrsIdentifier id1, CrsIdentifier id2) {
    	if (id1 == null && id2 == null) {
    		return true;
    	}
    	if (id1 == null || id2 == null) {
    		return false;
    	}
    	
    	if (id1.equals(id2)) {
    		return true;
    	}
    	
    	CRS crs1 = get(id1);
    	if (crs1 != null && crs1.equals(get(id2))) {
    		return true;
    	}
    	
    	return false;
    }
    
}
