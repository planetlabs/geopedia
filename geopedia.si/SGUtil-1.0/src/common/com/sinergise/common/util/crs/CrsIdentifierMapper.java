package com.sinergise.common.util.crs;

import com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority;


public interface CrsIdentifierMapper {

	public CrsIdentifier mapForAuthority(CrsIdentifier sourceId, CrsAuthority targetAuth);
	
}
