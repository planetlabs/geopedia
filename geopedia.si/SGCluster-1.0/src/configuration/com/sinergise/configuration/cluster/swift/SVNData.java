package com.sinergise.configuration.cluster.swift;

import com.sinergise.common.cluster.swift.SwiftAccount;
import com.sinergise.common.cluster.swift.SwiftClusterSinergise;
import com.sinergise.common.cluster.swift.SwiftTileURLProvider;
import com.sinergise.common.geometry.tiles.TiledDatasetProperties;

public class SVNData {
	
	public static final SwiftAccount ACCOUNT_SVN_RASTERS = new SwiftAccount(
			SwiftClusterSinergise.SWIFT_CLUSTER, "svn_rasters");
	
	public static final SwiftAccount ACCOUNT_SVN_GEOPEDIA = new SwiftAccount(
			SwiftClusterSinergise.SWIFT_CLUSTER, "svn_geopedia");  

	public static final SwiftTileURLProvider GI_SIGI_2011 = 
			new SwiftTileURLProvider(ACCOUNT_SVN_RASTERS, new TiledDatasetProperties("gi.sigi.2011.epsg:3912")
			.setMaxScale(18));

	public static final SwiftTileURLProvider GI_SIGI_2014 = 
			new SwiftTileURLProvider(ACCOUNT_SVN_RASTERS, new TiledDatasetProperties("gi.sigi.2014.epsg:3912")
	.setMaxScale(18));
	
	public static final SwiftTileURLProvider GI_SIGI_SHADED_2011 = 
			new SwiftTileURLProvider(ACCOUNT_SVN_RASTERS, new TiledDatasetProperties("gi.sigi.shaded.2011.epsg:3912")
			.setMaxScale(18));

	public static final SwiftTileURLProvider GI_SIGI_LIGHT_2011 = 
			new SwiftTileURLProvider(ACCOUNT_SVN_RASTERS, new TiledDatasetProperties("gi.sigi.light.2011.epsg:3912")
			.setMaxScale(18));

	
	public static final SwiftTileURLProvider GURS_DOF050_2011 =
			new SwiftTileURLProvider(ACCOUNT_SVN_RASTERS, new TiledDatasetProperties("gurs.dof.50cm.2011.epsg:3912")
			.setMaxScale(18));

	public static final SwiftTileURLProvider GURS_DOF050_2006 =
			new SwiftTileURLProvider(ACCOUNT_SVN_RASTERS, new TiledDatasetProperties("gurs.dof.50cm.2006.epsg:3912")
			.setMaxScale(18));

	
	public static final SwiftTileURLProvider GI_SIGI_2007 =
			new SwiftTileURLProvider(ACCOUNT_SVN_RASTERS, new TiledDatasetProperties("gi.sigi.2007.epsg:3912")
			.setMaxScale(18));

	
	public static final SwiftTileURLProvider GURS_DMV125_COLOR_2005 =
			new SwiftTileURLProvider(ACCOUNT_SVN_GEOPEDIA, new TiledDatasetProperties("gurs_dmv125-color_2005")
			.setMaxScale(18));
		
	public static final SwiftTileURLProvider GURS_DMV125_SHADED_2005 =
			new SwiftTileURLProvider(ACCOUNT_SVN_GEOPEDIA, new TiledDatasetProperties("gurs_dmv125-shaded_2005")
			.setMaxScale(18));
	
	
	public static final SwiftTileURLProvider GLOBALVISION_QUO_TK_2007 =
			new SwiftTileURLProvider(ACCOUNT_SVN_GEOPEDIA, new TiledDatasetProperties("globalvision_quo-tk_2007")
			.setMaxScale(16));

	
	public static final SwiftTileURLProvider GURS_DOF050_DMV125_2005 =
			new SwiftTileURLProvider(ACCOUNT_SVN_GEOPEDIA, new TiledDatasetProperties("gurs_dof050-dmv125_2005")
			.setMaxScale(18));
	public static final SwiftTileURLProvider GURS_DMV125_COLOR_SHADED_2005 =
			new SwiftTileURLProvider(ACCOUNT_SVN_GEOPEDIA, new TiledDatasetProperties("gurs_dmv125-color-shaded_2005")
			.setMaxScale(18));

	public static final SwiftTileURLProvider POLICIJA_VFR_2014 =
			new SwiftTileURLProvider(ACCOUNT_SVN_RASTERS, new TiledDatasetProperties("policija.vfr.2014.epsg:3912")
			.setMaxScale(18));


}
