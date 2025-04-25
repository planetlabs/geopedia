package com.sinergise.common.cluster.swift;

import com.sinergise.common.util.naming.Identifier;
import com.sinergise.common.util.server.ServersCluster;

public class SwiftClusterSinergise {
	
	public static final String URI_SCHEME = "sgswift";
	
	public static final SwiftServer SWIFT_SERVER_ARNES1 = new SwiftServer(new Identifier(Identifier.ROOT,"sgsArnes1"), "http://gpcl01.geopedia.si");
	public static final SwiftServer SWIFT_SERVER_ARNES2 = new SwiftServer(new Identifier(Identifier.ROOT,"sgsArnes2"), "http://gpcl02.geopedia.si");
	public static final  SwiftServer SWIFT_SERVER_ARNES3 = new SwiftServer(new Identifier(Identifier.ROOT,"sgsArnes3"), "http://gpcl03.geopedia.si");
	public static final SwiftServer SWIFT_SERVER_ARNES4 = new SwiftServer(new Identifier(Identifier.ROOT,"sgsArnes4"), "http://gpcl04.geopedia.si");
	public static final SwiftServer SWIFT_SERVER_ARNES5 = new SwiftServer(new Identifier(Identifier.ROOT,"sgsArnes5"), "http://gpcl05.geopedia.si");
	
	public static final SwiftServer SWIFT_SERVER_AMIS1 = new SwiftServer(new Identifier(Identifier.ROOT,"sgsAmis1"), "http://gpcl06.geopedia.si");
	public static final SwiftServer SWIFT_SERVER_AMIS2 = new SwiftServer(new Identifier(Identifier.ROOT,"sgsAmis2"), "http://gpcl07.geopedia.si");
	public static final SwiftServer SWIFT_SERVER_AMIS3 = new SwiftServer(new Identifier(Identifier.ROOT,"sgsAmis3"), "http://gpcl08.geopedia.si");
	public static final SwiftServer SWIFT_SERVER_AMIS4 = new SwiftServer(new Identifier(Identifier.ROOT,"sgsAmis4"), "http://gpcl09.geopedia.si");
	public static final SwiftServer SWIFT_SERVER_AMIS5 = new SwiftServer(new Identifier(Identifier.ROOT,"sgsAmis5"), "http://gpcl10.geopedia.si");
	
	public static final ServersCluster SWIFT_CLUSTER = new ServersCluster(new Identifier(Identifier.ROOT,
			"SGSSwift1"), 
			SWIFT_SERVER_ARNES1, SWIFT_SERVER_ARNES2, SWIFT_SERVER_ARNES3, SWIFT_SERVER_ARNES4, SWIFT_SERVER_ARNES5,
			SWIFT_SERVER_AMIS1, SWIFT_SERVER_AMIS2, SWIFT_SERVER_AMIS3, SWIFT_SERVER_AMIS4, SWIFT_SERVER_AMIS5);
	
}
