package com.sinergise.java.geometry.crs;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.CrsRepository;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.crs.CrsIdentifier.CrsAuthority;
import com.sinergise.common.util.settings.NamedTypedObject;
import com.sinergise.common.util.settings.ResolvedType;
import com.sinergise.java.util.settings.MapTransformer;

public class CrsLegacyTransformer extends MapTransformer.TrGeneric<CRS> {
	
	private static final String LEGACY_FIELD_SRID = "srid";
	private static final String LEGACY_FIELD_ID = "id";

	@Override
	@SuppressWarnings("rawtypes")
	public boolean canProcess(Class type) {
		return CRS.class.isAssignableFrom(type);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <C> ResolvedType<C> childType(ResolvedType<CRS> parentType, String name, boolean complex) {
		if (LEGACY_FIELD_ID.equals(name) || LEGACY_FIELD_SRID.equals(name)) {
			return (ResolvedType<C>)ResolvedType.create(String.class);
		}
		return super.childType(parentType, name, complex);
	}
	
	@Override
	protected void setFieldValue(CRS parent, ResolvedType<CRS> parentType, NamedTypedObject<?> value) {
		
		if (value.name.equals(LEGACY_FIELD_ID)) {
			CrsRepository.INSTANCE.add(new CrsIdentifier(String.valueOf(value.value)), parent);
			
		} else if (value.name.equals(LEGACY_FIELD_SRID)) {
			CrsRepository.INSTANCE.add(new CrsIdentifier(CrsAuthority.EPSG, String.valueOf(value.value)), parent);
			
		} else {
			super.setFieldValue(parent, parentType, value);
		}
	}
}
