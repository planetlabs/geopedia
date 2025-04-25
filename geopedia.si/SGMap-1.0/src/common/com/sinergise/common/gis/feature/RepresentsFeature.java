package com.sinergise.common.gis.feature;

import com.sinergise.common.util.naming.Identifiable;


public interface RepresentsFeature extends Identifiable {
	@Override
	public CFeatureIdentifier getQualifiedID();
}
